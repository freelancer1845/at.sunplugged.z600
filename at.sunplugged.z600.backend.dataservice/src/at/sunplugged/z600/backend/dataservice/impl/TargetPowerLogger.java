package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.core.machinestate.api.PowerSource.State;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

public class TargetPowerLogger implements MachineEventHandler {

    private static TargetPowerLogger instance = null;

    private Map<PowerSourceId, String> powerSourceTargetMap = new HashMap<>();

    private Map<PowerSourceId, ScheduledFuture<?>> powerSourceUpdaterMap = new HashMap<>();

    private Map<PowerSourceId, TimePowerDataPoint> timePowerData = new HashMap<>();

    private StandardThreadPoolService threadPool;

    private CloseableHttpClient client;

    private TargetPowerLogger(CloseableHttpClient client) {
        this.threadPool = DataServiceImpl.getStandardThreadPoolService();
        DataServiceImpl.getMachineStateService().registerMachineEventHandler(this);
        this.client = client;

    }

    /**
     * This maps the given target to the powersource. If targetId is empty, null
     * or does not exist no logging is done.
     * 
     * @param targetId
     *            of the material. Get the TargetId from the dataService via
     *            getTargetMaterials()
     * @param powerSourceId
     *            the material will be mapped to.
     */
    public void mapTargetToPowersource(String targetId, PowerSourceId powerSourceId) {
        if (targetId == null || targetId.isEmpty() == true) {
            doStateOff(powerSourceId);
        }

        powerSourceTargetMap.put(powerSourceId, targetId);

        // This is the case if the mapping is done after the power source was
        // started
        if (DataServiceImpl.getMachineStateService().getPowerSourceRegistry().getPowerSource(powerSourceId)
                .getState() != State.OFF && powerSourceUpdaterMap.containsKey(powerSourceId) == false) {
            handlePowerSourceStateEvent(powerSourceId, State.STARTING);
        }
    }

    public static TargetPowerLogger getInstance(CloseableHttpClient client) {
        if (instance == null) {
            instance = new TargetPowerLogger(client);
        }
        instance.client = client;
        return instance;
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType().equals(MachineStateEvent.Type.POWER_SOURCE_STATE_CHANGED)) {
            handlePowerSourceStateEvent((PowerSourceId) event.getOrigin(), (State) event.getValue());
        }
    }

    private void handlePowerSourceStateEvent(PowerSourceId id, State newState) {

        if (newState == State.STARTING) {
            String targetId = powerSourceTargetMap.get(id);
            if (targetId == null) {
                return;
            } else if (targetId.isEmpty() == true) {
                return;
            }
            if (powerSourceUpdaterMap.containsKey(id)) {
                powerSourceUpdaterMap.get(id).cancel(false);
            }
            powerSourceUpdaterMap.put(id,
                    threadPool.timedPeriodicExecute(new PowerSourceMaterialLoggerRunnable(id), 0, 1, TimeUnit.SECONDS));
        } else if (newState == State.OFF) {
            doStateOff(id);
        }

    }

    private void doStateOff(PowerSourceId id) {
        if (powerSourceUpdaterMap.containsKey(id)) {
            powerSourceUpdaterMap.get(id).cancel(false);
            powerSourceUpdaterMap.remove(id);
        }
    }

    private final class PowerSourceMaterialLoggerRunnable implements Runnable {

        private final PowerSourceId id;

        public PowerSourceMaterialLoggerRunnable(PowerSourceId id) {
            this.id = id;
        }

        @Override
        public void run() {
            if (timePowerData.containsKey(id) == false) {
                timePowerData.put(id, new TimePowerDataPoint(System.currentTimeMillis(), 0));
            }
            String targetId = powerSourceTargetMap.get(id);
            if (targetId == null) {
                doStateOff(id);
                return;
            }

            TimePowerDataPoint lastPoint = timePowerData.get(id);

            long currentTime = System.currentTimeMillis();
            double currentPower = DataServiceImpl.getMachineStateService().getPowerSourceRegistry().getPowerSource(id)
                    .getPower();
            double timePassedInMms = currentTime - lastPoint.getTimePoint();

            double workDone = (currentPower + lastPoint.getPowerPoint()) / 2 * timePassedInMms / 1000 / 3600;

            timePowerData.put(id, new TimePowerDataPoint(currentTime, currentPower));

            if (HttpHelper.checkIfHttpServerIsRunning(client) == true) {
                try {
                    addWorkDone(targetId, workDone);
                } catch (DataServiceException | SQLException e) {
                    DataServiceImpl.getLogService().log(LogService.LOG_ERROR,
                            "Failed to add workDone data to target consumption table.", e);
                    doStateOff(id);
                }
            }

        }

    }

    private final class TimePowerDataPoint {
        private final long timePoint;

        private final double powerPoint;

        public TimePowerDataPoint(long timePoint, double powerPoint) {
            this.timePoint = timePoint;
            this.powerPoint = powerPoint;
        }

        public double getPowerPoint() {
            return powerPoint;
        }

        public long getTimePoint() {
            return timePoint;
        }

    }

    private void addWorkDone(String targetId, double workDone) throws DataServiceException, SQLException {
        WriteDataTableUtils.writeTargetConsumptionData(client, targetId, workDone);
    }

}

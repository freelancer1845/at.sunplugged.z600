package at.sunplugged.z600.core.machinestate.impl.powersource;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.GasFlowControl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.WaterControl.WaterOutlet;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PowerSourceEvent;
import at.sunplugged.z600.core.machinestate.api.exceptions.InvalidPowerSourceStateException;
import at.sunplugged.z600.core.machinestate.impl.MachineStateServiceImpl;
import at.sunplugged.z600.mbt.api.MbtService;

public abstract class AbstractPowerSource implements PowerSource {

    protected final StandardThreadPoolService threadPool;

    protected final MbtService mbtService;

    protected final LogService logService;

    protected final SettingsService settings;

    protected final MachineStateService machineStateService;

    protected final PowerSourceId id;

    protected State state = State.OFF;

    protected double setPoint = 0;

    protected double currentControlValue = 0;

    private Future<?> startFuture;

    private ScheduledFuture<?> controlFuture;

    public AbstractPowerSource(MachineStateService machineStateService, PowerSourceId id) {
        threadPool = MachineStateServiceImpl.getStandardThreadPoolService();
        mbtService = MachineStateServiceImpl.getMbtService();
        logService = MachineStateServiceImpl.getLogService();
        settings = MachineStateServiceImpl.getSettingsService();
        this.machineStateService = machineStateService;
        this.id = id;
    }

    @Override
    public void on() {
        if (state == State.ON || state == State.ON_ADJUSTING || state == State.STARTING) {
            logService.log(LogService.LOG_DEBUG, "Powersource is already on: \"" + id.name() + "\"");
            return;
        }
        changeState(State.STARTING);
        startFuture = threadPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    machineStateService.getWaterControl().setOutletState(WaterOutlet.KATH_ONE, true);
                    machineStateService.getWaterControl().setOutletState(WaterOutlet.KATH_TWO, true);
                    machineStateService.getWaterControl().setOutletState(WaterOutlet.KATH_THREE, true);
                    machineStateService.getWaterControl().setOutletState(WaterOutlet.SHIELD, true);
                    if (state != State.STARTING) {
                        logService.log(LogService.LOG_DEBUG,
                                "Power source (" + id.name() + ") start interrupted during start phase.");
                        return;
                    }
                    powerSourceSpecificOn();
                    changeState(State.ON_ADJUSTING);
                    startControl();
                } catch (InterruptedException et) {
                    logService.log(LogService.LOG_DEBUG, "Starting power source: \"" + id.name() + "\" interrupted.");
                    off();
                } catch (Exception e) {
                    logService.log(LogService.LOG_ERROR,
                            "Unexpected Exception when starting power source: \"" + id.name() + "\"", e);
                    off();
                }
            }

        });

    }

    protected abstract void powerSourceSpecificOn() throws Exception;

    @Override
    public void off() {
        if (state == State.OFF) {
            logService.log(LogService.LOG_DEBUG, "Powersource is already off: \"" + id.name() + "\"");
        }
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    if (startFuture.isDone() == false) {
                        startFuture.cancel(true);
                    } else {
                        powerSourceSpecificOff();
                        changeState(State.OFF);
                        stopControl();
                    }
                } catch (Exception e) {
                    logService.log(LogService.LOG_ERROR,
                            "Unexpected exception when stopping power source: \"" + id.name() + "\"", e);
                }
            }

        });

    }

    protected abstract void powerSourceSpecificOff() throws IOException;

    @Override
    public void setPower(double power) {
        if (power > settings.getPropertAsDouble(ParameterIds.MAX_POWER)) {
            logService.log(LogService.LOG_WARNING,
                    "Setting setpoint power to \"" + power + "\" is not allowed (too high)! Ignoring value...");
            return;
        }
        this.setPoint = power;
    }

    @Override
    public double getSetPointpower() {
        return setPoint;
    }

    @Override
    public State getState() {
        return state;
    }

    protected void changeState(State newState) {
        if (newState == this.state) {
            return;
        }
        this.state = newState;
        machineStateService.fireMachineStateEvent(new PowerSourceEvent(id, newState));
    }

    protected void writeControlValue(double value) throws IOException {
        writeSourceSpecificControlValue(value);
        currentControlValue = value;
    }

    protected abstract void writeSourceSpecificControlValue(double value) throws IOException;

    protected abstract void powerSourceSpecificControlTick() throws Exception;

    private void checkPowerSourceRunConditions() throws InvalidPowerSourceStateException {

        if (getPower() < settings.getPropertAsDouble(ParameterIds.LOWER_SAFETY_LIMIT_POWER_AT_POWER_SORUCE)) {
            throw new InvalidPowerSourceStateException("Lower Limit for power reached! ("
                    + settings.getPropertAsDouble(ParameterIds.LOWER_SAFETY_LIMIT_POWER_AT_POWER_SORUCE)
                    + "). Shutting down \"" + id.name() + "\"");
        }
        if (machineStateService.getGasFlowControl().getState() != GasFlowControl.State.RUNNING) {
            throw new InvalidPowerSourceStateException("GasflowControl not running!");
        }
        checkPowerSourceRunConditionsSpecific();
    }

    protected void checkPowerSourceRunConditionsSpecific() {
        // default nothing
        // override if needed
    }

    private void startControl() {
        if (controlFuture != null) {
            logService.log(LogService.LOG_WARNING, "Control was already running, cancelling it and restarting!");
            controlFuture.cancel(false);
        }
        controlFuture = threadPool.timedPeriodicExecute(new Runnable() {

            @Override
            public void run() {
                try {
                    checkPowerSourceRunConditions();

                    powerSourceSpecificControlTick();

                    if (Math.abs(getPower() - setPoint) < 0.05) {
                        changeState(State.ON);
                    } else {
                        changeState(State.ON_ADJUSTING);
                    }

                } catch (Exception e) {
                    logService.log(LogService.LOG_ERROR, "PowerControl failed unexpected for powersource: \""
                            + id.name() + "\". Cause: \"" + e.getMessage() + "\"", e);
                    off();
                }

            }
        }, PowerSource.INITIAL_DELAY, PowerSource.STEPSPEED, PowerSource.TIMEUNIT);

    }

    private void stopControl() {
        if (controlFuture == null) {
            logService.log(LogService.LOG_DEBUG, "Control wasn't running. Doing nothing.");
        } else {
            controlFuture.cancel(false);
        }
        controlFuture = null;
    }

}

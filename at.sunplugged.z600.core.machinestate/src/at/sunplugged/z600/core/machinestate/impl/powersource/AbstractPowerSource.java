package at.sunplugged.z600.core.machinestate.impl.powersource;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PowerSourceEvent;
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
        try {
            powerSourceSpecificOn();
            changeState(State.ON_ADJUSTING);
            startControl();
        } catch (Exception e) {
            logService.log(LogService.LOG_ERROR,
                    "Unexpected Exception when starting power source: \"" + id.name() + "\"", e);
            changeState(State.OFF);
        }

    }

    protected abstract void powerSourceSpecificOn() throws IOException;

    @Override
    public void off() {
        try {

            powerSourceSpecificOff();
            changeState(State.OFF);
            stopControl();
        } catch (Exception e) {
            logService.log(LogService.LOG_ERROR,
                    "Unexpected exception when stopping power source: \"" + id.name() + "\"", e);
        }
    }

    protected abstract void powerSourceSpecificOff() throws IOException;

    @Override
    public void setPower(double power) {
        this.setPoint = power;
    }

    @Override
    public State getState() {
        return state;
    }

    protected void changeState(State newState) {
        this.state = newState;
        machineStateService.fireMachineStateEvent(new PowerSourceEvent(id, newState));
    }

    protected abstract void powerSourceSpecificControlTick();

    private void startControl() {
        if (controlFuture != null) {
            logService.log(LogService.LOG_WARNING, "Control was already running, cancelling it and restarting!");
            controlFuture.cancel(false);
        }
        threadPool.timedPeriodicExecute(new Runnable() {

            @Override
            public void run() {
                powerSourceSpecificControlTick();
            }
        }, 0, PowerSource.STEPSPEED, PowerSource.TIMEUNIT);

    }

    private void stopControl() {
        if (controlFuture == null) {
            logService.log(LogService.LOG_DEBUG, "Control wasn't running. Doing nothing.");
        } else {
            controlFuture.cancel(false);
        }
    }

}

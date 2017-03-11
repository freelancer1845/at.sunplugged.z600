package at.sunplugged.z600.core.machinestate.impl.powersource;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.impl.MachineStateServiceImpl;
import at.sunplugged.z600.mbt.api.MbtService;

public abstract class AbstractPowerSource implements PowerSource, MachineEventHandler {

    protected final StandardThreadPoolService threadPool;

    protected final MbtService mbtService;

    protected final LogService logService;

    protected final SettingsService settings;

    protected State state = State.OFF;

    protected double setPoint = 0;

    public AbstractPowerSource() {
        threadPool = MachineStateServiceImpl.getStandardThreadPoolService();
        mbtService = MachineStateServiceImpl.getMbtService();
        logService = MachineStateServiceImpl.getLogService();
        settings = MachineStateServiceImpl.getSettingsService();
    }

    @Override
    public void setPower(double power) {
        this.setPoint = power;
    }

    @Override
    public State getState() {
        return state;
    }

    protected void changeState(State newState) {

    }

}

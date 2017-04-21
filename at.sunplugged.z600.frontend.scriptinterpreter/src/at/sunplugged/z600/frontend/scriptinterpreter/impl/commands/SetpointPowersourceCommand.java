package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class SetpointPowersourceCommand extends AbstractCommand {

    private final PowerSourceId id;

    private final double setpoint;

    public SetpointPowersourceCommand(PowerSourceId id, double setpoint) {
        this.id = id;
        this.setpoint = setpoint;
    }

    @Override
    public String name() {
        return Commands.SETPOINT_POWERSOURCE + "(" + id + ", " + setpoint + ")";
    }

    @Override
    protected void executeCommandSpecific() throws InterruptedException, ScriptExecutionException {

        PowerSource powerSource = ScriptInterpreterServiceImpl.getMachineStateService().getPowerSourceRegistry()
                .getPowerSource(id);

        if (setpoint <= 0) {
            powerSource.off();
            return;
        }
        if (powerSource.getState() == PowerSource.State.OFF) {
            powerSource.on();
            Thread.sleep(1000);
        }
        if (powerSource.getState() == PowerSource.State.OFF) {
            setState(State.FAILED);
            throw new ScriptExecutionException("Failed to start power source: \"" + id.name() + "\"");
        }
        powerSource.setPower(setpoint);
        while (powerSource.getState() != PowerSource.State.ON) {
            Thread.sleep(100);
        }
    }

    @Override
    public long getEstimededTimeNeeded(TimeUnit unit) {
        return 0;
    }

    public PowerSourceId getId() {
        return id;
    }

    public double getSetpoint() {
        return setpoint;
    }

}

package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

import at.sunplugged.z600.core.machinestate.api.GasFlowControl;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class SetPressureCommand extends AbstractCommand {

    private final double pressure;

    public SetPressureCommand(double pressure) {
        this.pressure = pressure;
    }

    @Override
    protected void executeCommandSpecific() throws InterruptedException, ScriptExecutionException {
        GasFlowControl gasflowControl = ScriptInterpreterServiceImpl.getMachineStateService().getGasFlowControl();
        if (pressure == 0) {
            gasflowControl.stopGasFlowControl();
            return;
        }
        if (gasflowControl.getState() == GasFlowControl.State.STOP
                || gasflowControl.getState() == GasFlowControl.State.STOPPING) {
            gasflowControl.startGasFlowControl();
            Thread.sleep(500);
        } else if (gasflowControl.getState() == GasFlowControl.State.STARTING) {
            Thread.sleep(5000);
        }
        if (!(gasflowControl.getState() == GasFlowControl.State.ADJUSTING
                || gasflowControl.getState() == GasFlowControl.State.RUNNING_STABLE)) {
            setState(State.FAILED);
            throw new ScriptExecutionException(
                    "Failed to execute setPressure command: Not able to start gasflow control.");
        }
        gasflowControl.setGasflowDesiredPressure(pressure);
        long timer = 0;
        while (true) {
            if (timer > TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES)) {
                setState(State.FAILED);
                throw new ScriptExecutionException(String.format(
                        "Failed to execute command \"%s\". Desired pressure not reached after 5 minutes", toString()));
            }
            if (Math.abs(ScriptInterpreterServiceImpl.getMachineStateService().getPressureMeasurmentControl()
                    .getCurrentValue(PressureMeasurementSite.CHAMBER) - pressure) < 1e-4) {
                break;
            } else {
                timer += 500;
                Thread.sleep(500);

            }
        }
    }

    @Override
    public String name() {
        return Commands.SET_PRESSURE + "(" + pressure + ")";
    }

    @Override
    public long getEstimededTimeNeeded(TimeUnit unit) {
        return unit.convert(60, TimeUnit.SECONDS);
    }

}

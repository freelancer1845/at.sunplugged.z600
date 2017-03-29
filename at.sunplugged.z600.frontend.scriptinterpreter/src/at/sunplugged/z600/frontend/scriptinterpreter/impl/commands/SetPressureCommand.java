package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

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
        if (gasflowControl.getState() == GasFlowControl.State.STOPPED) {
            gasflowControl.startGasFlowControl();
            Thread.sleep(500);
        } else if (gasflowControl.getState() == GasFlowControl.State.STARTING) {
            Thread.sleep(1000);
        }
        if (gasflowControl.getState() != GasFlowControl.State.RUNNING) {
            throw new ScriptExecutionException(
                    "Failed to execute setPressure command: Not able to start gasflow control.");
        }
        gasflowControl.setGasflowDesiredPressure(pressure);
        while (true) {
            if (Math.abs(ScriptInterpreterServiceImpl.getMachineStateService().getPressureMeasurmentControl()
                    .getCurrentValue(PressureMeasurementSite.CHAMBER) - pressure) < 1e-4) {
                break;
            } else {
                Thread.sleep(500);
            }
        }
    }

    @Override
    public String name() {
        return Commands.SET_PRESSURE + "(" + pressure + ")";
    }

}

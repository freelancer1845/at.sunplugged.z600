package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class WaitForConveyorCommand extends AbstractCommand {

    @Override
    public String name() {
        return Commands.WAIT_FOR_CONVEYOR + "()";
    }

    @Override
    protected void executeCommandSpecific() throws InterruptedException, ScriptExecutionException {
        ConveyorControlService service = ScriptInterpreterServiceImpl.getConveyorControlService();
        while (service.getActiveMode() != Mode.STOP) {
            Thread.sleep(100);
        }
    }

}

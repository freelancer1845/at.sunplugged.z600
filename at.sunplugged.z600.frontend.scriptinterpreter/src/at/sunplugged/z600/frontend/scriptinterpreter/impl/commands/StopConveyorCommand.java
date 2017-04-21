package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class StopConveyorCommand extends AbstractCommand {

    @Override
    public String name() {
        return Commands.STOP_CONVEYOR + "()";
    }

    @Override
    protected void executeCommandSpecific() throws InterruptedException, ScriptExecutionException {
        ScriptInterpreterServiceImpl.getConveyorControlService().stop();
    }

    @Override
    public long getEstimededTimeNeeded(TimeUnit unit) {
        return 0;
    }

}

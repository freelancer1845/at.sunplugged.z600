package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class StartConveyorTimeCommand extends AbstractCommand {

    private final Mode mode;

    private final double speed;

    private final long timeInMs;

    public StartConveyorTimeCommand(Mode mode, double speed, long timeInMs) {
        this.mode = mode;
        this.speed = speed;
        this.timeInMs = timeInMs;

    }

    @Override
    public String name() {
        return Commands.START_CONVEYOR_TIME + "(" + mode + ", " + speed + ", " + timeInMs + ")";
    }

    @Override
    protected void executeCommandSpecific() throws InterruptedException, ScriptExecutionException {
        ScriptInterpreterServiceImpl.getConveyorControlService().start(speed, mode, timeInMs, TimeUnit.MILLISECONDS);
    }

}

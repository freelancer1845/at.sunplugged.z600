package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class StartConveyorSimpleCommand extends AbstractCommand {

    public enum Command {
        JUST_START, DISTANCE, TIME;
    }

    private final Mode mode;

    private final double speed;

    public StartConveyorSimpleCommand(Mode mode, double speed) {
        this.mode = mode;
        this.speed = speed;
    }

    @Override
    public String name() {
        return Commands.START_CONVEYOR_SIMPLE + "(" + mode.name() + ", " + speed + ")";
    }

    @Override
    protected void executeCommandSpecific() throws InterruptedException, ScriptExecutionException {
        ScriptInterpreterServiceImpl.getConveyorControlService().start(speed, mode);
    }

    @Override
    public long getEstimededTimeNeeded(TimeUnit unit) {
        return 0;
    }

}

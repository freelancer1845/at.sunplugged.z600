package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class StartConveyorTimeCommand extends AbstractCommand {

    private final Mode mode;

    private final double speed;

    private final long timeInSeconds;

    public StartConveyorTimeCommand(Mode mode, double speed, long timeInSeconds) {
        this.mode = mode;
        this.speed = speed;
        this.timeInSeconds = timeInSeconds;

    }

    @Override
    public String name() {
        return Commands.START_CONVEYOR_TIME + "(" + mode + ", " + speed + ", " + timeInSeconds + ")";
    }

    @Override
    protected void executeCommandSpecific() throws InterruptedException, ScriptExecutionException {
        ScriptInterpreterServiceImpl.getConveyorControlService().start(speed, mode, timeInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public long getEstimededTimeNeeded(TimeUnit unit) {
        return unit.convert(timeInSeconds, TimeUnit.SECONDS);
    }

}

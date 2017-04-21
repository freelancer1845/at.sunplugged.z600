package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class StartConveyorDistanceCommand extends AbstractCommand {

    private final Mode mode;

    private final double speed;

    private final double distance;

    public StartConveyorDistanceCommand(Mode mode, double speed, double distance) {
        this.mode = mode;
        this.speed = speed;
        this.distance = distance;

    }

    @Override
    public String name() {
        return Commands.START_CONVEYOR_DISTANCE + "(" + mode + ", " + speed + ", " + distance + ")";
    }

    @Override
    protected void executeCommandSpecific() throws InterruptedException, ScriptExecutionException {
        ScriptInterpreterServiceImpl.getConveyorControlService().start(speed, mode, distance);
    }

    @Override
    public long getEstimededTimeNeeded(TimeUnit unit) {
        return unit.convert((long) (distance * 10 / speed), TimeUnit.SECONDS);
    }

}

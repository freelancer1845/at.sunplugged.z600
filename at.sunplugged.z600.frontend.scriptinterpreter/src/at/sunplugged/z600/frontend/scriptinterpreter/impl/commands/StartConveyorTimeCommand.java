package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Reference;

import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorMonitor;
import at.sunplugged.z600.conveyor.api.ConveyorMonitor.StopMode;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class StartConveyorTimeCommand extends AbstractCommand {

    private final Mode mode;

    private final double speed;

    private final long timeInSeconds;

    @Reference
    private ConveyorMonitor conveyorMonitor;

    @Reference
    private ConveyorControlService conveyorService;

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
        conveyorMonitor.setStopTime(LocalDateTime.now().plusSeconds(timeInSeconds));
        conveyorMonitor.setStopMode(StopMode.TIME_REACHED);
        ScriptInterpreterServiceImpl.getConveyorControlService().start(speed, mode);
    }

    @Override
    public long getEstimededTimeNeeded(TimeUnit unit) {
        return unit.convert(timeInSeconds, TimeUnit.SECONDS);
    }

}

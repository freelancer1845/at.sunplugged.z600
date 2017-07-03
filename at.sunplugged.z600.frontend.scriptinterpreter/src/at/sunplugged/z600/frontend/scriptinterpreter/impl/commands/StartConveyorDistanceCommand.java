package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Reference;

import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorMonitor;
import at.sunplugged.z600.conveyor.api.ConveyorMonitor.StopMode;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class StartConveyorDistanceCommand extends AbstractCommand {

    private final Mode mode;

    private final double speed;

    private final double distance;

    @Reference
    private ConveyorMonitor conveyorMonitor;

    @Reference
    private ConveyorControlService conveyorService;

    @Reference
    private SettingsService settingsService;

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
        double stopPosition;
        if (mode == Mode.LEFT_TO_RIGHT) {
            stopPosition = conveyorService.getPosition() + distance / 10;
        } else {
            stopPosition = conveyorService.getPosition() - distance / 10;
        }
        conveyorMonitor.setStopPosition(stopPosition);
        conveyorMonitor.setStopMode(StopMode.DISTANCE_REACHED);
        ScriptInterpreterServiceImpl.getConveyorControlService().start(speed, mode);
    }

    @Override
    public long getEstimededTimeNeeded(TimeUnit unit) {
        return unit.convert((long) (distance * 10 / speed), TimeUnit.SECONDS);
    }

}

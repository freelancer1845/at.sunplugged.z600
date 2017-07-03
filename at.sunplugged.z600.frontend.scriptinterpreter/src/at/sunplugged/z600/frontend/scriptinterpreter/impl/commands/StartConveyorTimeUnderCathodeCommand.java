package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Reference;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.conveyor.api.ConveyorMonitor;
import at.sunplugged.z600.conveyor.api.ConveyorMonitor.StopMode;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class StartConveyorTimeUnderCathodeCommand extends AbstractCommand {

    private final Mode mode;

    private final double distanceInCm;

    private final long timeUnderCathodeInSeconds;

    @Reference
    private ConveyorMonitor conveyorMonitor;

    @Reference
    private ConveyorControlService conveyorService;

    @Reference
    private SettingsService settingsService;

    public StartConveyorTimeUnderCathodeCommand(Mode mode, double distanceInCm, long timeUnderCathodeInSeconds) {
        this.mode = mode;
        this.distanceInCm = distanceInCm;
        this.timeUnderCathodeInSeconds = timeUnderCathodeInSeconds;
    }

    @Override
    public long getEstimededTimeNeeded(TimeUnit unit) {
        double lengthOfCathode = ScriptInterpreterServiceImpl.getSettingsService()
                .getPropertAsDouble(ParameterIds.CATHODE_LENGTH_MM);
        double speed = lengthOfCathode / timeUnderCathodeInSeconds;
        long timeNeededInSeconds = (long) (distanceInCm / (speed / 10));
        return unit.convert(timeNeededInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String name() {
        return Commands.START_CONVEYOR_TIME_UNDER_CATHODE + "(" + mode + ", " + distanceInCm + ", "
                + timeUnderCathodeInSeconds + ")";
    }

    @Override
    protected void executeCommandSpecific() throws InterruptedException, ScriptExecutionException {
        double stopPosition;
        if (mode == Mode.LEFT_TO_RIGHT) {
            stopPosition = conveyorService.getPosition() + distanceInCm / 10;
        } else {
            stopPosition = conveyorService.getPosition() - distanceInCm / 10;
        }

        double lengthOfCathode = settingsService.getPropertAsDouble(ParameterIds.CATHODE_LENGTH_MM);
        double speed = lengthOfCathode / timeUnderCathodeInSeconds;
        conveyorMonitor.setStopPosition(stopPosition);
        conveyorMonitor.setStopMode(StopMode.DISTANCE_REACHED);

        ScriptInterpreterServiceImpl.getConveyorControlService().start(speed, mode);

    }

}

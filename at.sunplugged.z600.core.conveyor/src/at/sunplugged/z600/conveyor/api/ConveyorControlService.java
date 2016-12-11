package at.sunplugged.z600.conveyor.api;

public interface ConveyorControlService {

    public enum Mode {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT;
    }

    public void start(double speed, Mode direction);

    public void stop();

    public double getCurrentSpeed();

    public double getSetpointSpeed();

    public Mode getActiveMode();

    public Engine getEngineOne();

    public Engine getEngineTwo();

}

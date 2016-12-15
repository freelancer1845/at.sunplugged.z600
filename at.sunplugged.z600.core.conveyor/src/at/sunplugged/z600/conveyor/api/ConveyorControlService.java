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

    /**
     * This accesses the left Engine.
     * 
     * @return {@linkplain Engine}
     */
    public Engine getEngineOne();

    /**
     * This accesses the right Engine.
     * 
     * @return {@linkplain Engine}
     */
    public Engine getEngineTwo();

    /**
     * This accesses the SpeedLogger.
     * 
     * @return {@linkplain SpeedLogger}
     */
    public SpeedLogger getSpeedLogger();

}

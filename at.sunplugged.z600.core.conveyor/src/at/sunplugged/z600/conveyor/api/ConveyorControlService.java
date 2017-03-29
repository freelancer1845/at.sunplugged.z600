package at.sunplugged.z600.conveyor.api;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface ConveyorControlService {

    public static final String CALIBRATION_TABLE_NAME = "speed_logger_calibration_sql_table123g90";

    public enum Mode {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT, STOP;
    }

    /**
     * 
     * @param speedInMms in mm/s
     * @param direction
     */
    public void start(double speedInMms, Mode direction);

    /**
     * 
     * @param speedInMmS in mm/s
     * @param direction
     * @param distanceInCm in cm
     */
    public void start(double speedInMmS, Mode direction, double distanceInCm);

    /**
     * 
     * @param speedInMmS
     * @param direction
     * @param time in given time unit
     * @param unit
     */
    public void start(double speedInMmS, Mode direction, long time, TimeUnit unit);

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

    /**
     * This returns the relative position of the conveyor belt in "m".
     * 
     * @return
     */
    public double getPosition();

    /**
     * This sets the relative position of the conveyor belt in "m".
     * 
     * @param value
     */
    public void setPosition(double value);

    /**
     * This starts a calibration routine. The data obtained is saved in an
     * bundle specific file with name "speed_calibration.cfg".
     * 
     * @return {@linkplain Future<?>} with which the process may be canceled.
     */
    public Future<?> calibrate();

}

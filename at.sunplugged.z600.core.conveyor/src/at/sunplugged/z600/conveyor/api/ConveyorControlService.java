package at.sunplugged.z600.conveyor.api;

import java.util.concurrent.Future;

public interface ConveyorControlService {

    public static final String CALIBRATION_TABLE_NAME = "speed_logger_calibration_sql_table123g90";

    public enum Mode {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT, STOP;
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

    /**
     * This starts a calibration routine. The data obtained is saved in an
     * bundle specific file with name "speed_calibration.cfg".
     * 
     * @return {@linkplain Future<?>} with which the process may be canceled.
     */
    public Future<?> calibrate();

}

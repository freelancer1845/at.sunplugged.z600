package at.sunplugged.z600.conveyor.api;

public interface SpeedLogger {

    public static final int LEFT_DIGITAL_IN_ADDRESS = 39;

    public static final int RIGHT_DIGITAL_IN_ADDRESS = 40;

    public static final double LEFT_DRUM_RADIUS = 0.0775;

    public static final int LEFT_NUMBER_OF_HOLES = 30;

    public static final double LEFT_DISTANCE_PER_HOLE = 2 * Math.PI * LEFT_DRUM_RADIUS / LEFT_NUMBER_OF_HOLES;

    public static final double RIGHT_DRUM_RADIUS = 0.0775;

    public static final int RIGHT_NUMBER_OF_HOLES = 30;

    public static final double RIGHT_DISTANCE_PER_HOLE = 2 * Math.PI * RIGHT_DRUM_RADIUS / RIGHT_NUMBER_OF_HOLES;

    /**
     * 
     * @return the mean value of left and right speed in m/s measured.
     */
    public double getCurrentSpeed();

    /**
     * 
     * @return value of right side speed in m/s as measured.
     */
    public double getRightSpeed();

    /**
     * 
     * @return value of left side speed in m/s measured.
     */
    public double getLeftSpeed();

}

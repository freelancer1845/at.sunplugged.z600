package at.sunplugged.z600.conveyor.api;

public interface SpeedLogger {

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

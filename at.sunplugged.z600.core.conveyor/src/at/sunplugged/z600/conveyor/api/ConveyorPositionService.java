package at.sunplugged.z600.conveyor.api;

public interface ConveyorPositionService {

    public void start();

    public void togglePositionControl(boolean state);

    public void stop();

    /**
     * Gets current estimated band position in [mm] on left side.
     * 
     * @return
     */
    public double getLeftPosition();

    /**
     * Gets current estimated band position in [mm] on right side.
     * 
     * @return
     */
    public double getRightPosition();

}

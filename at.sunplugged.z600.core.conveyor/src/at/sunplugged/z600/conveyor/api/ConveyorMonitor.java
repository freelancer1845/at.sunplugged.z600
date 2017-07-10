package at.sunplugged.z600.conveyor.api;

import java.time.LocalDateTime;

public interface ConveyorMonitor {

    /**
     * Stops the conveyor if the position is reached.
     * 
     * @param position
     *            to be reached
     */
    public void setStopPosition(double position);

    public double getCurrentStopPosition();

    /**
     * Stops the ocnveyor at the given LocalDateTime.
     * 
     * 
     * @param stopTime
     */
    public void setStopTime(LocalDateTime stopTime);

    public LocalDateTime getCurrentStopTime();

    public enum StopMode {
        DISTANCE_REACHED, TIME_REACHED, OFF;
    }

    /**
     * Sets the current mode. Set it to OFF to deactivate automatic stopping.
     * 
     * @param mode
     */
    public void setStopMode(StopMode mode);

    public StopMode getStopMode();

    public String getFormattedMessage();

    public long getETCinMs();

    public String getFormattedETCMessage();

}

package at.sunplugged.z600.core.machinestate.api;

import java.io.IOException;

public interface PumpControl {

    public enum PumpState {
        ON, OFF, STARTING, STOPPING, FAILED;
    }

    /**
     * Updates the state.
     */
    public void update() throws IOException;

    public void startPumpOne();

    public void stopPumpOne();

    public void startPumpTwo();

    public void stopPumpTwo();

    public void startTurboPump();

    public void stopTurboPump();

}

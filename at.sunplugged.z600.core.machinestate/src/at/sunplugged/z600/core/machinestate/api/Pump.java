package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.eventhandling.FutureEvent;

/**
 * Interface every pump implements. To get a pump use the PumpControl.
 * 
 * @author Jascha Riedel
 *
 */
public interface Pump {

    public enum PumpState {
        ON, OFF, STARTING, STOPPING, FAILED;
    }

    public FutureEvent startPump();

    public FutureEvent stopPump();

    public PumpState getState();

}

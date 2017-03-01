package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.eventhandling.GasFlowEvent;

/**
 * This Interfaces allows the control of argon gas flow in the chamber.
 * 
 * @author Jascha Riedel
 *
 */
public interface GasFlowControl {

    public enum State {
        STARTING, RUNNING, STOPPED;
    }

    /**
     * Only sets the desired pressure point. Use {@linkplain startGasFlow()} to
     * actually start gas flow control.
     * 
     * @param desiredPresure for the gas flow control.
     */
    public void setGasflowDesiredPressure(double desiredPresure);

    public double getCurrentGasFlowValue();

    /**
     * Tries to open Outlet 9 and starts the gas flow control. To stop call
     * {@linkplain stopGasFlowControl()}. This is executed asynchronously. A
     * {@linkplain GasFlowEvent} is fired when done.
     */
    public void startGasFlowControl();

    /**
     * Closes Outlet 9 and stops gas flow control.
     */
    public void stopGasFlowControl();

    /**
     * Indicates the state of the gasflow control.
     */
    public State getState();

}

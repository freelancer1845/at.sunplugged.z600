package at.sunplugged.z600.core.machinestate.api;

/**
 * Interface every power source should implement.
 * 
 * 
 * @author Jascha Riedel
 *
 */
public interface PowerSource {

    public enum State {
        ON_STARTING, ON, OFF;
    }

    /**
     * Starts the source (i. e. power on). The power is gradually increased
     * until the setpoint is reached. It may shut down because of safety
     * reasons.
     */
    public void on();

    /**
     * Stops the power source immediately.
     */
    public void off();

    /**
     * Used to set the setpoint power.
     * 
     * @param power in kW.
     */
    public void setPower(double power);

    /**
     * Returns the state of the power source.
     * 
     * @return
     */
    public State getState();
}

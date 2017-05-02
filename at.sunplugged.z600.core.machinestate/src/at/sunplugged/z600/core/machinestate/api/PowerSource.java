package at.sunplugged.z600.core.machinestate.api;

import java.util.concurrent.TimeUnit;

import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;

/**
 * Interface every power source should implement.
 * 
 * 
 * @author Jascha Riedel
 *
 */
public interface PowerSource {

    public static final long STEPSPEED = 100;

    public static final long INITIAL_DELAY = 100;

    public static final TimeUnit TIMEUNIT = TimeUnit.MILLISECONDS;

    public enum State {
        STARTING, ON_ADJUSTING, ON, OFF;
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
     * @param power
     *            in kW.
     */
    public void setPower(double power);

    /**
     * Get setpoint power.
     */
    public double getSetPointpower();

    /**
     * Returns the currently measured power at the power source.
     * 
     * @return double value of power in kW.
     */
    public double getPower();

    public double getCurrent();

    public double getVoltage();

    /**
     * Returns the state of the power source.
     * 
     * @return
     */
    public State getState();

    public PowerSourceId getId();
}

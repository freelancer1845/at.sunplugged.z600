package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.PowerControl.PowerUnit;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.KathodeStateEvent;
import at.sunplugged.z600.core.machinestate.api.exceptions.InvalidKathodeStateException;

public interface KathodeControl {

    /**
     * Sets the setPoint of the {@linkplain Kathode} to the desired value
     * (Ampere).
     * 
     * @param kathode
     * @param value
     */
    public void setSetPoint(Kathode kathode, double value);

    /**
     * Initializes the start method of the {@linkplain Kathode}. When finished a
     * {@linkplain KathodeStateEvent} is fired.
     * 
     * @param kathode to start.
     * @throws InvalidKathodeStateException
     */
    public void startKathode(Kathode kathode) throws InvalidKathodeStateException;

    /**
     * Stops the desired kathode. When finished a {@linkplain KathodeStateEvent}
     * is fired.
     * 
     * @param kathode to stop.
     */
    public void stopKathode(Kathode kathode);

    public enum Kathode {
        KATHODE_ONE(AnalogOutput.KATHODE_ONE_SETPOINT, PowerUnit.PINNACLE),
        KATHODE_TWO(AnalogOutput.KATHODE_TWO_SETPOINT, PowerUnit.SSV_ONE),
        KATHODE_THREE(AnalogOutput.KATHODE_THREE_SETPOINT, PowerUnit.SSV_TWO);

        private final AnalogOutput analogOutput;

        private final PowerUnit powerUnit;

        private Kathode(AnalogOutput analogOutput, PowerUnit powerUnit) {
            this.analogOutput = analogOutput;
            this.powerUnit = powerUnit;
        }

        public AnalogOutput getAnalogOutput() {
            return analogOutput;
        }

        public PowerUnit getPowerUnit() {
            return powerUnit;
        }

    }

}

package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.PowerControl.PowerUnit;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.KathodeStateEvent;
import at.sunplugged.z600.core.machinestate.api.exceptions.InvalidKathodeStateException;

public interface KathodeControl {

    /**
     * Sets the setPoint of the {@linkplain Kathode} to the desired value (kW).
     * 
     * @param kathode
     * @param value
     */
    public void setPowerSetpoint(Kathode kathode, double power);

    /**
     * Returns the setPoint of the {@linkplain Kathode} in [kw].
     * 
     * @param kathode
     * @return value in [kw]
     */
    public double getPowerSetpoint(Kathode kathode);

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

    /**
     * Gets the currently measured voltage at the kathode in V.
     * 
     * @param kathode
     * @return
     */
    public double getVoltageAtKathode(Kathode kathode);

    /**
     * Gets the currently measured current at the kathode in A.
     * 
     * @param kathode
     * @return
     */
    public double getCurrentAtKathode(Kathode kathode);

    /**
     * Gets the current power calculated form voltage and current at the kathode
     * in kW.
     * 
     * @param kathode
     * @return
     */
    public double getPowerAtKathode(Kathode kathode);

    public enum Kathode {
        KATHODE_ONE(
                AnalogOutput.KATHODE_ONE_SETPOINT,
                PowerUnit.PINNACLE,
                AnalogInput.VOLTAGE_KATHODE_ONE,
                AnalogInput.POWER_KATHODE_ONE),
        KATHODE_TWO(
                AnalogOutput.KATHODE_TWO_SETPOINT,
                PowerUnit.SSV_ONE,
                AnalogInput.VOLTAGE_KATHODE_TWO,
                AnalogInput.CURRENT_KATHODE_TWO),
        KATHODE_THREE(
                AnalogOutput.KATHODE_THREE_SETPOINT,
                PowerUnit.SSV_TWO,
                AnalogInput.VOLTAGE_KATHODE_THREE,
                AnalogInput.CURRENT_KATHODE_THREE);

        private final AnalogOutput analogOutput;

        private final PowerUnit powerUnit;

        private final AnalogInput voltageInput;

        private final AnalogInput currentInput;

        private Kathode(AnalogOutput analogOutput, PowerUnit powerUnit, AnalogInput voltageInput,
                AnalogInput currentInput) {
            this.analogOutput = analogOutput;
            this.powerUnit = powerUnit;
            this.voltageInput = voltageInput;
            this.currentInput = currentInput;
        }

        public AnalogOutput getAnalogOutput() {
            return analogOutput;
        }

        public PowerUnit getPowerUnit() {
            return powerUnit;
        }

        public AnalogInput getVoltageInput() {
            return voltageInput;
        }

        public AnalogInput getCurrentInput() {
            return currentInput;
        }

    }

}

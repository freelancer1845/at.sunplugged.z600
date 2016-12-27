package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.PowerControl.PowerUnit;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;

public interface KathodeControl {

    /**
     * Sets the setPoint of the {@linkplain Kathode} to the desired value
     * (Ampere).
     * 
     * @param kathode
     * @param value
     */
    public void setSetPoint(Kathode kathode, double value);

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

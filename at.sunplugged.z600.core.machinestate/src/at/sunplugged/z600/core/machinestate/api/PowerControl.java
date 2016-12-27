package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public interface PowerControl {

    /**
     * Starts the desired unit.
     * 
     * @param {@linkplain PowerUnit} powerUnit
     */
    public void start(PowerUnit powerUnit);

    /**
     * Stops the desired unit.
     * 
     * @param {@linkplain PowerUnit} powerUnit
     */
    public void stop(PowerUnit powerUnit);

    /**
     * Sets the interlock of the unit to the provided value if the powerunit has
     * an interlock.
     * 
     * @param {@linkplain PowerUnit} powerUnit
     * @param interlock
     */
    public void setInterlock(PowerUnit powerUnit, boolean interlock);

    public boolean getState(PowerUnit powerUnit);

    public enum PowerUnit {
        SSV_ONE(DigitalInput.SSV_ONE_ERROR, DigitalOutput.SSV_ONE_START, null, null),
        SSV_TWO(DigitalInput.SSV_TWO_ERROR, DigitalOutput.SSV_TWO_START, null, null),
        PINNACLE(
                DigitalInput.PINNACLE_OUT,
                DigitalOutput.PINNACLE_START,
                DigitalOutput.PINNACLE_INTERLOCK,
                DigitalOutput.PINNACLE_OFF);

        private final DigitalInput digitalInput;

        private final DigitalOutput startOutput;

        private final DigitalOutput interlockOutput;

        private final DigitalOutput offOutput;

        private PowerUnit(DigitalInput digitalInput, DigitalOutput startOutput, DigitalOutput interlockOutput,
                DigitalOutput offOutput) {
            this.digitalInput = digitalInput;
            this.startOutput = startOutput;
            this.interlockOutput = interlockOutput;
            this.offOutput = offOutput;
        }

        public DigitalInput getDigitalInput() {
            return digitalInput;
        }

        public DigitalOutput getStartOutput() {
            return startOutput;
        }

        public DigitalOutput getInterlockOutput() {
            return interlockOutput;
        }

        public DigitalOutput getOffOutput() {
            return offOutput;
        }

    }

}

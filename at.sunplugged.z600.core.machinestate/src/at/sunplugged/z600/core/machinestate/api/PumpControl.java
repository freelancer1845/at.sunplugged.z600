package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public interface PumpControl {

    public enum Pumps {
        PRE_PUMP_ONE(DigitalOutput.PRE_PUMP_ONE, DigitalInput.PRE_PUMP_ONE_OK),
        PRE_PUMP_ROOTS(DigitalOutput.PRE_PUMP_ROOTS, DigitalInput.PRE_PUMP_ROOTS_OK),
        PRE_PUMP_TWO(DigitalOutput.PRE_PUMP_TWO, DigitalInput.PRE_PUMP_TWO_OK),
        TURBO_PUMP(DigitalOutput.TUROBO_OUMP, DigitalInput.TURBO_PUMP_OK);

        private final DigitalOutput digitalOutput;

        private final DigitalInput digitalInput;

        private Pumps(DigitalOutput digitalOutput, DigitalInput digitalInput) {
            this.digitalOutput = digitalOutput;
            this.digitalInput = digitalInput;
        }

        public DigitalOutput getDigitalOutput() {
            return digitalOutput;
        }

        public DigitalInput getDigitalInput() {
            return digitalInput;
        }

    }

    public enum PumpState {
        ON, OFF, STARTING, STOPPING, FAILED;
    }

    public void startPump(Pumps pump);

    public void stopPump(Pumps pump);

    public PumpState getState(Pumps pump);

}

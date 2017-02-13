package at.sunplugged.z600.core.machinestate.api;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public interface WaterControl {

    public enum WaterOutlet {
        TURBO_PUMP(DigitalOutput.WATER_TURBO_PUMP),
        KATH_ONE(DigitalOutput.WATER_KATH_ONE),
        KATH_TWO(DigitalOutput.WATER_KATH_TWO),
        KATH_THREE(DigitalOutput.WATER_KATH_THREE),
        SHIELD(DigitalOutput.WATER_SHIELD);

        private final DigitalOutput digitalOutput;

        private WaterOutlet(DigitalOutput digitalOutput) {
            this.digitalOutput = digitalOutput;
        }

        public DigitalOutput getDigitalOutput() {
            return digitalOutput;
        }
    }

    public enum FlowCheckPoint {
        KATH_ONE(DigitalInput.WATER_KATH_ONE_ON),
        KATH_TWO(DigitalInput.WATER_KATH_TWO_ON),
        KATH_THREE(DigitalInput.WATER_KATH_THREE_ON),
        KATH_FOUR(DigitalInput.WATER_KATH_FOUR_ON);

        private final DigitalInput digitalInput;

        private FlowCheckPoint(DigitalInput digitalInput) {
            this.digitalInput = digitalInput;
        }

        public DigitalInput getDigitalInput() {
            return digitalInput;
        }

    }

    public boolean getOutletState(WaterOutlet outlet);

    public void setOutletState(WaterOutlet outlet, boolean state) throws IOException;

    public boolean getFlowCheckPointState(FlowCheckPoint checkPoint);

}

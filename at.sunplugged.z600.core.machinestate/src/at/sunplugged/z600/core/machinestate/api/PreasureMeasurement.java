package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;

public interface PreasureMeasurement {

    public enum PreasureMeasurementSite {
        TURBO_PUMP(AnalogInput.PREASURE_TURBO_PUMP),
        CRYO_PUMP_ONE(AnalogInput.PREASURE_CRYO_ONE),
        CRYO_PUMP_TWO(AnalogInput.PREASURE_CRYO_TWO),
        CHAMBER(AnalogInput.PREASURE_CHAMBER);

        private final AnalogInput analogInput;

        private PreasureMeasurementSite(AnalogInput analogInput) {
            this.analogInput = analogInput;
        }

        public AnalogInput getAnalogInput() {
            return analogInput;
        }
    }

    public double getCurrentValue(PreasureMeasurementSite site);

}

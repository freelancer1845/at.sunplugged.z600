package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;

public interface PressureMeasurement {

    public enum PressureMeasurementSite {
        TURBO_PUMP(AnalogInput.PREASURE_TURBO_PUMP),
        CRYO_PUMP_ONE(AnalogInput.PREASURE_CRYO_ONE),
        CRYO_PUMP_TWO(AnalogInput.PREASURE_CRYO_TWO),
        CHAMBER(AnalogInput.PREASURE_CHAMBER);

        private final AnalogInput analogInput;

        private PressureMeasurementSite(AnalogInput analogInput) {
            this.analogInput = analogInput;
        }

        public AnalogInput getAnalogInput() {
            return analogInput;
        }
    }

    /**
     * Returns the current value at that site in mbar.
     * 
     * @param site
     * @return
     */
    public double getCurrentValue(PressureMeasurementSite site);

}

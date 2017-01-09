package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;

public class PressureChangedEvent extends MachineStateEvent {

    private final PressureMeasurementSite site;

    public PressureChangedEvent(PressureMeasurementSite site, double value) {
        super(Type.PRESSURE_CHANGED, value);
        this.site = site;
    }

    public PressureMeasurementSite getSite() {
        return site;
    }

}

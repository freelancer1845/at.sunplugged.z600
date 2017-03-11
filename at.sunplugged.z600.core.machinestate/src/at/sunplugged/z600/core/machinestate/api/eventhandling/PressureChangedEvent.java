package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;

public class PressureChangedEvent extends MachineStateEvent {

    public PressureChangedEvent(PressureMeasurementSite site, double value) {
        super(Type.PRESSURE_CHANGED, site, value);
    }

    @Override
    public PressureMeasurementSite getOrigin() {
        return (PressureMeasurementSite) super.getOrigin();
    }

}

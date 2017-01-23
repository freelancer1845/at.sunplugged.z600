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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((site == null) ? 0 : site.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PressureChangedEvent other = (PressureChangedEvent) obj;
        if (site != other.site)
            return false;
        return true;
    }

}

package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;

public class OutletChangedEvent extends MachineStateEvent {

    private Integer position = null;

    public OutletChangedEvent(Outlet outlet, boolean newState) {
        super(Type.OUTLET_CHANGED, outlet, newState);
    }

    public OutletChangedEvent(Outlet outlet, boolean newState, int position) {
        super(Type.OUTLET_CHANGED, outlet, newState);
        this.position = position;
    }

    public Outlet getOutlet() {
        return (Outlet) super.getOrigin();
    }

    public int getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((position == null) ? 0 : position.hashCode());
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
        OutletChangedEvent other = (OutletChangedEvent) obj;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        return true;
    }

}

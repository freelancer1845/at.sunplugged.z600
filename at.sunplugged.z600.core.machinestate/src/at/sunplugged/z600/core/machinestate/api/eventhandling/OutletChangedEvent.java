package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;

public class OutletChangedEvent extends MachineStateEvent {

    private final Outlet outlet;

    private Integer position = null;

    public OutletChangedEvent(Outlet outlet, boolean newState) {
        super(Type.OUTLET_CHANGED, newState);
        this.outlet = outlet;
    }

    public OutletChangedEvent(Outlet outlet, boolean newState, int position) {
        super(Type.OUTLET_CHANGED, newState);
        this.outlet = outlet;
        this.position = position;
    }

    public Outlet getOutlet() {
        return outlet;
    }

    public int getPosition() {
        return position;
    }

}

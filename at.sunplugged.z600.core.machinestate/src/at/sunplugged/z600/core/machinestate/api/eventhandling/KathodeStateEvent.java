package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.KathodeControl;
import at.sunplugged.z600.core.machinestate.api.KathodeControl.Kathode;

public class KathodeStateEvent extends MachineStateEvent {

    private final Kathode kathode;

    private final boolean state;

    public KathodeStateEvent(Kathode kathode, boolean state) {
        super(Type.KATHODE_STATUS_CHANGED);
        this.kathode = kathode;
        this.state = state;
    }

    public boolean getState() {
        return state;
    }

}

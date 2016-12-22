package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.PumpControl.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpControl.Pumps;

public class PumpStateEvent extends MachineStateEvent {

    private final Pumps pump;

    private final PumpState state;

    public PumpStateEvent(Pumps pump, PumpState state) {
        super(Type.PUMP_STATUS_CHANGED);
        this.pump = pump;
        this.state = state;
    }

    public Pumps getPump() {
        return pump;
    }

    public PumpState getState() {
        return state;
    }

}

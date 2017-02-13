package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.Pump.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;

public class PumpStateEvent extends MachineStateEvent {

    private final PumpIds pump;

    private final PumpState state;

    public PumpStateEvent(PumpIds pump, PumpState state) {
        super(Type.PUMP_STATUS_CHANGED);
        this.pump = pump;
        this.state = state;
    }

    public PumpIds getPump() {
        return pump;
    }

    public PumpState getState() {
        return state;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pump == null) ? 0 : pump.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PumpStateEvent other = (PumpStateEvent) obj;
        if (pump != other.pump)
            return false;
        if (state != other.state)
            return false;
        return true;
    }

}

package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.PowerSource.State;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;

public class PowerSourceEvent extends MachineStateEvent {

    public PowerSourceEvent(PowerSourceId id, State state) {
        super(Type.POWER_SOURCE_STATE_CHANGED, id, state);
    }

    @Override
    public PowerSourceId getOrigin() {
        return (PowerSourceId) super.getOrigin();
    }

    @Override
    public State getValue() {
        // TODO Auto-generated method stub
        return (State) super.getValue();
    }

}

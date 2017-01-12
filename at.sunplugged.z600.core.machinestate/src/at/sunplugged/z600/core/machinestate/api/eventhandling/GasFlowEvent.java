package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.GasFlowControl.State;

public class GasFlowEvent extends MachineStateEvent {

    public GasFlowEvent(State state) {
        super(Type.GAS_FLOW_STATE_CHANGED, state);
    }
}

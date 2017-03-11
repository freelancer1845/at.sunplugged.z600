package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.Pump.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;

public class PumpStateEvent extends MachineStateEvent {

    public PumpStateEvent(PumpIds pump, PumpState state) {
        super(Type.PUMP_STATUS_CHANGED, pump, state);
    }

    @Override
    public PumpIds getOrigin() {
        return (PumpIds) super.getOrigin();
    }

    @Override
    public PumpState getValue() {
        return (PumpState) super.getValue();
    }

}

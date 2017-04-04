package at.sunplugged.z600.conveyor.api;

import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

public class ConveyorMachineEvent extends MachineStateEvent {

    public enum Type {
        LEFT_SPEED_CHANGED,
        RIGHT_SPEED_CHANGED,
        LEFT_ENGINE_MAXIMUM_SPEED_CHANGED,
        RIGHT_ENGINE_MAXIMUM_SPEED_CHANGED,
        NEW_DISTANCE,
        MODE_CHANGED;
    }

    private final Type type;

    public ConveyorMachineEvent(Type type, Object value) {
        super(at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type.CONVEYOR_EVENT, value);
        this.type = type;
    }

    public Type getConveyorEventType() {
        return type;
    }

}

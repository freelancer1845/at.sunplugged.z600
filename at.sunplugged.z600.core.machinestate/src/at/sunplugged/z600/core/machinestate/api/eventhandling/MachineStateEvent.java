package at.sunplugged.z600.core.machinestate.api.eventhandling;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MachineStateEvent {

    public enum Type {
        DIGITAL_OUTPUT_CHANGED, ANALOG_OUTPUT_CHANGED, DIGITAL_INPUT_CHANGED, ANALOG_INPUT_CHANGED, PUMP_STATUS_CHANGED, PRESSURE_CHANGED, POWER_SOURCE_STATE_CHANGED, GAS_FLOW_STATE_CHANGED, CONVEYOR_EVENT, OUTLET_CHANGED;
    }

    private final Type type;

    private final Object value;

    private final Object origin;

    private final LocalDateTime dateOfCreation;

    public MachineStateEvent(Type type) {
        this.type = type;
        this.origin = null;
        this.value = null;
        this.dateOfCreation = LocalDateTime.now();
    }

    public MachineStateEvent(Type type, Object value) {
        this.type = type;
        this.origin = null;
        this.value = value;
        this.dateOfCreation = LocalDateTime.now();
    }

    public MachineStateEvent(Type type, Object origin, Object value) {
        this.type = type;
        this.origin = origin;
        this.value = value;
        this.dateOfCreation = LocalDateTime.now();
    }

    public Type getType() {
        return type;
    }

    public Object getOrigin() {
        return origin;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        MachineStateEvent other = (MachineStateEvent) obj;
        if (origin == null) {
            if (other.origin != null)
                return false;
        } else if (!origin.equals(other.origin))
            return false;
        if (type != other.type)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public String getSigniture() {
        return String.format("Event Created: \"%s\" Event Origin: \"%s\" Event Value: \"%s\"",
                dateOfCreation.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), origin.toString(), value.toString());
    }
}

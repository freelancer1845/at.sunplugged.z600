package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public class MachineStateEvent {

    public enum Type {
        DIGITAL_OUTPUT_CHANGED,
        ANALOG_OUTPUT_CHANGED,
        DIGITAL_INPUT_CHANGED,
        ANALOG_INPUT_CHANGED,
        PUMP_STATUS_CHANGED,
        KATHODE_STATUS_CHANGED,
        PRESSURE_CHANGED,
        POWER_UNIT_CHANGED,
        GAS_FLOW_STATE_CHANGED,
        CONVEYOR_EVENT,
        OUTLET_CHANGED;
    }

    private final Type type;

    private final DigitalInput digitalInput;

    private final DigitalOutput digitalOutput;

    private final AnalogInput analogInput;

    private final AnalogOutput analogOutput;

    private final Object value;

    public MachineStateEvent(Type type) {
        this.type = type;
        this.digitalInput = null;
        this.digitalOutput = null;
        this.analogInput = null;
        this.analogOutput = null;
        this.value = null;
    }

    public MachineStateEvent(Type type, Object value) {
        this.type = type;
        this.digitalInput = null;
        this.digitalOutput = null;
        this.analogInput = null;
        this.analogOutput = null;
        this.value = value;
    }

    public MachineStateEvent(Type type, DigitalInput digitalInput, boolean value) {
        this.type = type;
        this.digitalInput = digitalInput;
        this.digitalOutput = null;
        this.analogInput = null;
        this.analogOutput = null;
        this.value = value;
    }

    public MachineStateEvent(Type type, AnalogInput analogInput, int value) {
        this.type = type;
        this.digitalInput = null;
        this.digitalOutput = null;
        this.analogInput = analogInput;
        this.analogOutput = null;
        this.value = value;
    }

    public MachineStateEvent(Type type, DigitalOutput digitalOutput, boolean value) {
        this.type = type;
        this.digitalInput = null;
        this.digitalOutput = digitalOutput;
        this.analogInput = null;
        this.analogOutput = null;
        this.value = value;
    }

    public MachineStateEvent(Type type, AnalogOutput analogOutput, int value) {
        this.type = type;
        this.digitalInput = null;
        this.digitalOutput = null;
        this.analogInput = null;
        this.analogOutput = analogOutput;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public DigitalInput getDigitalInput() {
        return digitalInput;
    }

    public DigitalOutput getDigitalOutput() {
        return digitalOutput;
    }

    public AnalogInput getAnalogInput() {
        return analogInput;
    }

    public AnalogOutput getAnalogOutput() {
        return analogOutput;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((analogInput == null) ? 0 : analogInput.hashCode());
        result = prime * result + ((analogOutput == null) ? 0 : analogOutput.hashCode());
        result = prime * result + ((digitalInput == null) ? 0 : digitalInput.hashCode());
        result = prime * result + ((digitalOutput == null) ? 0 : digitalOutput.hashCode());
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
        if (analogInput != other.analogInput)
            return false;
        if (analogOutput != other.analogOutput)
            return false;
        if (digitalInput != other.digitalInput)
            return false;
        if (digitalOutput != other.digitalOutput)
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

}

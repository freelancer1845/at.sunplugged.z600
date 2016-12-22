package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public class MachineStateEvent {

    public enum Type {
        DIGITAL_OUTPUT_CHANGED, ANALOG_OUTPUT_CHANGED, DIGITAL_INPUT_CHANGED, ANALOG_INPUT_CHANGED, PUMP_STATUS_CHANGED;
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

}

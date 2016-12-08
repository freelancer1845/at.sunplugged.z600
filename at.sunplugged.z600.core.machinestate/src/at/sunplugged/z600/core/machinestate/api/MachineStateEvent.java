package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public class MachineStateEvent {

    public enum Type {
        DIGITAL_OUTPUT_CHANGED, ANALOG_OUTPUT_CHANGED, DIGITAL_INPUT_CHANGED, ANALOG_INPUT_CHANGED;
    }

    private final Type type;

    private final DigitalInput digitalInput;

    private final DigitalOutput digitalOutput;

    private final AnalogInput analogInput;

    private final AnalogOutput analogOutput;

    public MachineStateEvent(Type type) {
        this.type = type;
        this.digitalInput = null;
        this.digitalOutput = null;
        this.analogInput = null;
        this.analogOutput = null;
    }

    public MachineStateEvent(Type type, DigitalInput digitalInput) {
        this.type = type;
        this.digitalInput = digitalInput;
        this.digitalOutput = null;
        this.analogInput = null;
        this.analogOutput = null;
    }

    public MachineStateEvent(Type type, AnalogInput analogInput) {
        this.type = type;
        this.digitalInput = null;
        this.digitalOutput = null;
        this.analogInput = analogInput;
        this.analogOutput = null;
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

}

package at.sunplugged.z600.core.machinestate.impl.eventhandling;

import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.impl.MachineStateServiceImpl;

public class MachineStateEventHandler implements MachineEventHandler {

    private final MachineStateServiceImpl machineStateServiceImpl;

    public MachineStateEventHandler(MachineStateServiceImpl machineStateServiceImpl) {
        this.machineStateServiceImpl = machineStateServiceImpl;
    }

    public void handleEvent(MachineStateEvent event) {
        switch (event.getType()) {
        case ANALOG_OUTPUT_CHANGED:
            handleAnalogOutputChanged();
            break;
        case DIGITAL_OUTPUT_CHANGED:
            handleDigitalOutputChanged();
            break;
        default:
            break;
        }
    }

    private void handleAnalogOutputChanged() {
        machineStateServiceImpl.updateAnalogOutputState();
    }

    private void handleDigitalOutputChanged() {
        machineStateServiceImpl.updateDigitalOutputState();
    }

}

package at.sunplugged.z600.core.machinestate.impl.pumps;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.exceptions.IllegalPumpConditionsException;

public class PrePumpTwo extends AbstractPrePump {

    public PrePumpTwo(MachineStateService machineStateService) {
        super(machineStateService, PumpIds.PRE_PUMP_TWO, DigitalInput.PRE_PUMP_TWO_OK, DigitalOutput.PRE_PUMP_TWO);
    }

    @Override
    protected void startPumpChecks() throws IllegalPumpConditionsException {
        // None needed
    }

    @Override
    protected void stopPumpChecks() throws IllegalPumpConditionsException {
        // None needed
    }

}

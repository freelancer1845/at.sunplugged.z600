package at.sunplugged.z600.core.machinestate.impl.pumps;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.exceptions.IllegalPumpConditionsException;

public class PrePumpOne extends AbstractPrePump {

    public PrePumpOne(MachineStateService machineStateService) {
        super(machineStateService, PumpIds.PRE_PUMP_ONE, DigitalInput.PRE_PUMP_ONE_OK, DigitalOutput.PRE_PUMP_ONE);
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

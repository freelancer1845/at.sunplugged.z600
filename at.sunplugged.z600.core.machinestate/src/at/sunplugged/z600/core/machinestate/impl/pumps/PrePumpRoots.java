package at.sunplugged.z600.core.machinestate.impl.pumps;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.exceptions.IllegalPumpConditionsException;

public class PrePumpRoots extends AbstractPrePump {

    public PrePumpRoots(MachineStateService machineStateService) {
        super(machineStateService, PumpIds.PRE_PUMP_ROOTS, DigitalInput.PRE_PUMP_ROOTS_OK,
                DigitalOutput.PRE_PUMP_ROOTS);
    }

    @Override
    protected void startPumpChecks() throws IllegalPumpConditionsException {
        boolean p120State = machineStateService.getDigitalInputState(DigitalInput.P_120_MBAR);
        if (p120State == false) {
            throw new IllegalPumpConditionsException("Roots pump couldn't start. P_120_MBAR trigger not reached.");
        }
    }

    @Override
    protected void stopPumpChecks() throws IllegalPumpConditionsException {
        // None needed
    }

}

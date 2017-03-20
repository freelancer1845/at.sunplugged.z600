package at.sunplugged.z600.core.machinestate.impl.powersource;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public class SsvOne extends AbstractSSVPowerSource {

    public SsvOne(MachineStateService machineStateService) {
        super(machineStateService, PowerSourceId.SSV1, DigitalOutput.SSV_ONE_START, AnalogOutput.SSV_ONE_SETPOINT,
                AnalogInput.CURRENT_SVV_ONE, AnalogInput.VOLTAGE_SVV_ONE, DigitalInput.SSV_ONE_ERROR);
    }

}

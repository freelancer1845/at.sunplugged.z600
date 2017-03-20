package at.sunplugged.z600.core.machinestate.impl.powersource;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public class SsvTwo extends AbstractSSVPowerSource {

    public SsvTwo(MachineStateService machineStateService) {
        super(machineStateService, PowerSourceId.SSV2, DigitalOutput.SSV_TWO_START, AnalogOutput.SSV_TWO_SETPOINT,
                AnalogInput.CURRENT_SSV_TWO, AnalogInput.VOLTAGE_SSV_TWO, DigitalInput.SSV_TWO_ERROR);
    }

}

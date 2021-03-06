package at.sunplugged.z600.core.machinestate.impl.pumps;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public class CryoPumpOne extends AbstractCryoPump {

    public CryoPumpOne(MachineStateService machineStateService) {
        super(PumpIds.CRYO_ONE, machineStateService, DigitalOutput.COMPRESSOR_ONE, DigitalInput.COMPRESSOR_ONE_OK,
                DigitalInput.CRYO_ONE_LOW, PressureMeasurementSite.CRYO_PUMP_ONE, Outlet.OUTLET_FIVE,
                Outlet.OUTLET_SEVEN);
    }

}

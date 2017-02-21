package at.sunplugged.z600.core.machinestate.impl.pumps;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public class CryoPumpTwo extends AbstractCryoPump {

    public CryoPumpTwo(MachineStateService machineStateService) {
        super(PumpIds.CRYO_TWO, machineStateService, DigitalOutput.COMPRESSOR_TWO, DigitalInput.COMPRESSOR_TWO_OK,
                DigitalInput.CRYO_TWO_LOW, PressureMeasurementSite.CRYO_PUMP_TWO, Outlet.OUTLET_SIX,
                Outlet.OUTLET_EIGHT);
    }

}

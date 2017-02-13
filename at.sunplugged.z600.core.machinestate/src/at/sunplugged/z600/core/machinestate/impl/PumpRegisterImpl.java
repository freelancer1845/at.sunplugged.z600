package at.sunplugged.z600.core.machinestate.impl;

import java.util.HashMap;
import java.util.Map;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.Pump;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.impl.pumps.AbstractPrePump;
import at.sunplugged.z600.core.machinestate.impl.pumps.PrePumpOne;
import at.sunplugged.z600.core.machinestate.impl.pumps.PrePumpRoots;
import at.sunplugged.z600.core.machinestate.impl.pumps.PrePumpTwo;
import at.sunplugged.z600.core.machinestate.impl.pumps.TurboPump;
import at.sunplugged.z600.core.machinestate.impl.pumps.WaterPump;

public class PumpRegisterImpl implements PumpRegistry {

    private Map<PumpIds, Pump> pumpMap = new HashMap<PumpIds, Pump>();

    public PumpRegisterImpl(MachineStateService machineStateService) {
        pumpMap.put(PumpIds.PRE_PUMP_ONE, new PrePumpOne(machineStateService));
        pumpMap.put(PumpIds.PRE_PUMP_TWO, new PrePumpTwo(machineStateService));
        pumpMap.put(PumpIds.PRE_PUMP_ROOTS, new PrePumpRoots(machineStateService));
        pumpMap.put(PumpIds.TURBO_PUMP, new TurboPump(machineStateService));
        pumpMap.put(PumpIds.WATER_PUMP, new WaterPump(machineStateService));
    }

    @Override
    public Pump getPump(PumpIds pumpId) {
        return pumpMap.get(pumpId);
    }

}

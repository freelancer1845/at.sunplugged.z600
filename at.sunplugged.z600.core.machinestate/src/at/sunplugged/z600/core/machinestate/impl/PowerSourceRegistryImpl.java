package at.sunplugged.z600.core.machinestate.impl;

import java.util.HashMap;
import java.util.Map;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry;
import at.sunplugged.z600.core.machinestate.impl.powersource.Pinnacle;
import at.sunplugged.z600.core.machinestate.impl.powersource.SsvOne;
import at.sunplugged.z600.core.machinestate.impl.powersource.SsvTwo;

public class PowerSourceRegistryImpl implements PowerSourceRegistry {

    private final Map<PowerSourceId, PowerSource> registry = new HashMap<>();

    public PowerSourceRegistryImpl(MachineStateService machineStateService) {
        registry.put(PowerSourceId.PINNACLE, new Pinnacle(machineStateService));
        registry.put(PowerSourceId.SSV1, new SsvOne(machineStateService));
        registry.put(PowerSourceId.SSV2, new SsvTwo(machineStateService));
    }

    @Override
    public PowerSource getPowerSource(PowerSourceId id) {
        return registry.get(id);
    }

}

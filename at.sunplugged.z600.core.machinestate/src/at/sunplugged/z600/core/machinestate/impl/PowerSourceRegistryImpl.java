package at.sunplugged.z600.core.machinestate.impl;

import java.util.HashMap;
import java.util.Map;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry;

public class PowerSourceRegistryImpl implements PowerSourceRegistry {

    private final MachineStateService machineStateService;

    private final Map<PowerSourceId, PowerSource> registry = new HashMap<>();

    public PowerSourceRegistryImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;

    }

    @Override
    public PowerSource getPowerSource(PowerSourceId id) {
        return registry.get(id);
    }

}

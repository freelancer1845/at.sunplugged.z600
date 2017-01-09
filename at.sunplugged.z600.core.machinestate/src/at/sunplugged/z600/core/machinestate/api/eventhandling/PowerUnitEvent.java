package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.PowerControl.PowerUnit;

public class PowerUnitEvent extends MachineStateEvent {

    private final PowerUnit unit;

    public PowerUnitEvent(PowerUnit unit, boolean state) {
        super(Type.POWER_UNIT_CHANGED, state);
        this.unit = unit;
    }

    public PowerUnit getUnit() {
        return unit;
    }

}

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PowerUnitEvent other = (PowerUnitEvent) obj;
        if (unit != other.unit)
            return false;
        return true;
    }

}

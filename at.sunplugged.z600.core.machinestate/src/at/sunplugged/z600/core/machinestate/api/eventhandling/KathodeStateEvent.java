package at.sunplugged.z600.core.machinestate.api.eventhandling;

import at.sunplugged.z600.core.machinestate.api.KathodeControl.Kathode;

public class KathodeStateEvent extends MachineStateEvent {

    private final Kathode kathode;

    private final boolean state;

    public KathodeStateEvent(Kathode kathode, boolean state) {
        super(Type.KATHODE_STATUS_CHANGED);
        this.kathode = kathode;
        this.state = state;
    }

    public Kathode getKathode() {
        return kathode;
    }

    public boolean getState() {
        return state;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((kathode == null) ? 0 : kathode.hashCode());
        result = prime * result + (state ? 1231 : 1237);
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
        KathodeStateEvent other = (KathodeStateEvent) obj;
        if (kathode != other.kathode)
            return false;
        if (state != other.state)
            return false;
        return true;
    }

}

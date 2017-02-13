package at.sunplugged.z600.core.machinestate.api;

public interface PumpRegistry {

    public enum PumpIds {
        PRE_PUMP_ONE, PRE_PUMP_ROOTS, PRE_PUMP_TWO, TURBO_PUMP, WATER_PUMP;

    }

    public Pump getPump(PumpIds pump);

}

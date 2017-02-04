package at.sunplugged.z600.common.settings.impl.defaultproperties;

import java.util.Properties;

import at.sunplugged.z600.common.settings.api.ParameterIds;

public class ParameterDefaultProperties extends Properties {

    /**
     * 
     */
    private static final long serialVersionUID = 873663602850539518L;

    public ParameterDefaultProperties() {
        writeDefaultProperties();
    }

    private void writeDefaultProperties() {
        this.put(ParameterIds.TURBO_PUMP_ON_TRIGGER, "0,025");
        this.put(ParameterIds.SAFETY_PROTOCOLS_OUTLETS, "true");
        this.put(ParameterIds.INITIAL_DESIRED_PRESSURE_GAS_FLOW, "0.003");
        this.put(ParameterIds.INITIAL_GAS_FLOW_PARAMETER, "0");
        this.put(ParameterIds.GAS_FLOW_CONTROL_PARAMETER, "1");
        this.put(ParameterIds.GAS_FLOW_HYSTERESIS_CONTROL_PARAMETER, "0.0001");
        this.put(ParameterIds.INITIAL_POWER_KATHODE_ONE, "0.1");
        this.put(ParameterIds.INITIAL_CURRENT_KATHODE_TWO, "0.4");
        this.put(ParameterIds.INITIAL_CURRENT_KATHODE_THREE, "0.4");
        this.put(ParameterIds.DELTA_CURRENT_KATHODE_ONE, "0.05");
        this.put(ParameterIds.DELTA_CURRENT_KATHODE_TWO, "0.02");
        this.put(ParameterIds.DELTA_CURRENT_KATHODE_THREE, "0.02");
        this.put(ParameterIds.VACUUM_LOWER_LIMIT_MBAR, "0.001");
        this.put(ParameterIds.VACUUM_UPPER_LIMIT_MBAR, "0.007");
        this.put(ParameterIds.START_TRIGGER_TURBO_PUMP, "0.2");
    }

}

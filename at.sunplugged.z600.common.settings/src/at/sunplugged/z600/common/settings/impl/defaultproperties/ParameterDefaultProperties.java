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
        this.put(ParameterIds.SAFETY_PROTOCOLS_OUTLETS, "true");
        this.put(ParameterIds.INITIAL_DESIRED_PRESSURE_GAS_FLOW, "0.003");
        this.put(ParameterIds.INITIAL_GAS_FLOW_PARAMETER, "0");
        this.put(ParameterIds.GAS_FLOW_CONTROL_PARAMETER, "1");
        this.put(ParameterIds.GAS_FLOW_HYSTERESIS_CONTROL_PARAMETER, "0.00001");
        this.put(ParameterIds.PRESSURE_CONTROL_LOWER_LIMIT, "5E-04");
        this.put(ParameterIds.VACUUM_LOWER_LIMIT_MBAR, "0.001");
        this.put(ParameterIds.VACUUM_UPPER_LIMIT_MBAR, "0.007");
        this.put(ParameterIds.START_TRIGGER_TURBO_PUMP, "0.025");
        this.put(ParameterIds.CRYO_PUMP_PRESSURE_TRIGGER, "0.03");
        this.put(ParameterIds.ENGINE_MAXIMUM_SPEED, "6000");
        this.put(ParameterIds.LOWER_SAFETY_LIMIT_POWER_AT_POWER_SORUCE, "0.03");
        this.put(ParameterIds.INITIAL_POWER_PINNACLE, "0.5");
        this.put(ParameterIds.MAX_POWER_PINNACLE, "1.6");
        this.put(ParameterIds.CURRENT_CHANGE_PINNACLE, "0.002");
    }

}

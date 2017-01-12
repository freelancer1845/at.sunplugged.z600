package at.sunplugged.z600.common.settings.impl;

import java.util.Properties;

import at.sunplugged.z600.common.settings.api.SettingsIds;

/**
 * Save Default Settings here.
 * 
 * 
 * @author Jascha Riedel
 *
 */
public class DefaultProperties extends Properties {

    /**
     * 
     */
    private static final long serialVersionUID = 6871087323100946174L;

    public DefaultProperties() {
        this.put(SettingsIds.TURBO_PUMP_ON_TRIGGER, "0,025");
        this.put(SettingsIds.MBT_CONTROLLER_IP, "localhost");
        this.put(SettingsIds.LEFT_ENGINE_COM_PORT, "COM6");
        this.put(SettingsIds.RIGHT_ENGINE_COMP_PORT, "COM5");
        this.put(SettingsIds.SAFETY_PROTOCOLS_OUTLETS, "true");
        this.put(SettingsIds.INTIAL_DESIRED_PRESSURE_GAS_FLOW, "0.003");
        this.put(SettingsIds.INTIAL_GAS_FLOW_PARAMETER, "0");
        this.put(SettingsIds.GAS_FLOW_CONTROL_PARAMETER, "1");
        this.put(SettingsIds.GAS_FLOW_HYSTERESIS_CONTROL_PARAMETER, "0.0001");
    }

}

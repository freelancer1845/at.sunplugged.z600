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
    }

}

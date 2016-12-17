package at.sunplugged.z600.common.settings.api;

/**
 * This service gives access to settings like trigger values, initial values
 * etc.
 * 
 * 
 * 
 * @author Jascha Riedel
 *
 */
public interface SettingsService {

    /**
     * Returns the value of the current setting if there is one registered for
     * the id.
     * 
     * @param id
     *            from {@linkplain SettingsIds}
     * @return the property value as String.
     */
    public String getProperty(String id);

    public void loadSettings();

    public void saveSettings();

}

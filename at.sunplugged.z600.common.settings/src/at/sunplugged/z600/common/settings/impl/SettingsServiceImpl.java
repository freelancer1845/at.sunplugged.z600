package at.sunplugged.z600.common.settings.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.SettingsException;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.common.settings.impl.defaultproperties.NetworkComDefaultProperties;
import at.sunplugged.z600.common.settings.impl.defaultproperties.ParameterDefaultProperties;

@Component(immediate = true)
public class SettingsServiceImpl implements SettingsService {

    private static LogService logService;

    private SettingsFile networkPropertiesFile = new SettingsFile("network", new NetworkComDefaultProperties());

    private SettingsFile parameterPropertiesFile = new SettingsFile("parameter", new ParameterDefaultProperties());

    @Activate
    protected void activate() {
        loadSettings();
    }

    @Deactivate
    protected void deactivate() {
        saveSettings();
    }

    @Override
    public String getProperty(String id) {
        if (parameterPropertiesFile.containsProperty(id)) {
            return parameterPropertiesFile.getProperty(id);
        } else if (networkPropertiesFile.containsProperty(id)) {
            return networkPropertiesFile.getProperty(id);
        } else {
            throw new SettingsException("No setting saved with id: \"" + id + "\"");
        }
    }

    @Reference(unbind = "unbindLogService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindLogService(LogService logService) {
        SettingsServiceImpl.logService = logService;
    }

    public synchronized void unbindLogService(LogService logService) {
        if (SettingsServiceImpl.logService == logService) {
            SettingsServiceImpl.logService = null;
        }
    }

    public static LogService getLogService() {
        return logService;
    }

    @Override
    public void loadSettings() {
        networkPropertiesFile.load();
        parameterPropertiesFile.load();
    }

    @Override
    public void saveSettings() {
        networkPropertiesFile.save();
        parameterPropertiesFile.save();
    }

    @Override
    public double getPropertAsDouble(String id) {
        String stringValue = getProperty(id);
        return Double.valueOf(stringValue);
    }
}

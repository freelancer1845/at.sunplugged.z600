package at.sunplugged.z600.common.settings.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.SettingsService;

@Component(immediate = true)
public class SettingsServiceImpl implements SettingsService {

    private static final String SETTINGS_FILE_NAME = "settings.cfg";

    private static final String SETTINGS_BACKUP_FILE_NAME = "settings.cfg.bak";

    private LogService logService;

    private Properties userProperties;

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
        if (!userProperties.containsKey(id)) {
            logService.log(LogService.LOG_WARNING, "Properties did not contain key: " + id);
        }
        return userProperties.getProperty(id);
    }

    @Reference(unbind = "unbindLogService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindLogService(LogService logService) {
        this.logService = logService;
    }

    public synchronized void unbindLogService(LogService logService) {
        if (this.logService == logService) {
            this.logService = null;
        }
    }

    @Override
    public void loadSettings() {
        File settingsFile = new File(SETTINGS_FILE_NAME);
        if (!settingsFile.exists()) {
            logService.log(LogService.LOG_WARNING,
                    "No settings file found at: " + settingsFile.getAbsolutePath() + ". Loading default settings.");
            userProperties = new DefaultProperties();
        } else {
            InputStream inputStream;
            userProperties = new Properties();
            try {
                inputStream = new FileInputStream(settingsFile);
                userProperties.load(inputStream);
            } catch (FileNotFoundException e) {
                logService.log(LogService.LOG_ERROR, "Couldn't open file stream to: " + settingsFile.getAbsolutePath(),
                        e);
                return;
            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR,
                        "Failed to load propertes from file: " + settingsFile.getAbsolutePath(), e);
                return;
            }
        }
        logService.log(LogService.LOG_DEBUG, "LoadedSettings: " + userProperties.toString());

    }

    @Override
    public void saveSettings() {
        File settingsFile = new File(SETTINGS_FILE_NAME);
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(settingsFile);
                userProperties.store(outputStream, "Settings File For Z600");
                return;
            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR,
                        "Failed to create settings file at: " + settingsFile.getAbsolutePath(), e);
                return;
            }
        }
        File backupSettingsFile = new File(SETTINGS_BACKUP_FILE_NAME);
        if (backupSettingsFile.exists()) {
            backupSettingsFile.delete();
        }
        try {
            settingsFile.renameTo(backupSettingsFile);
            File newSettingsFile = new File(SETTINGS_FILE_NAME);
            FileOutputStream outputStream = new FileOutputStream(newSettingsFile);
            userProperties.store(outputStream, "Settings File for Z600");
            outputStream.close();
            settingsFile.delete();
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Failed to save settings.", e);
            if (settingsFile.exists()) {
                settingsFile.delete();
            }
            backupSettingsFile.renameTo(settingsFile);
        }
    }
}

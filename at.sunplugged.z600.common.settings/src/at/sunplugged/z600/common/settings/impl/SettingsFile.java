package at.sunplugged.z600.common.settings.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.osgi.service.log.LogService;

public class SettingsFile {

    private static final String BACKUP_SUFFIX = ".bak";

    private static final String FILE_ENDING = ".cfg";

    private final String settingsFileName;

    private LogService logService;

    private Properties properties;

    private final Properties defaultProperties;

    private LogService getLogService() {
        if (logService == null) {
            logService = SettingsServiceImpl.getLogService();
        }
        return logService;
    }

    public SettingsFile(String settingsFileName, Properties defaultProeprties) {
        this.settingsFileName = settingsFileName;
        this.defaultProperties = defaultProeprties;
    }

    public void save() {
        File settingsFile = new File(settingsFileName + FILE_ENDING);
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(settingsFile);
                properties.store(outputStream, "Settings File For Z600 - \"" + settingsFileName + FILE_ENDING + "\"");
                return;
            } catch (IOException e) {
                getLogService().log(LogService.LOG_ERROR,
                        "Failed to create settings file at: " + settingsFile.getAbsolutePath(), e);
                return;
            }
        }
        File backupSettingsFile = new File(settingsFileName + FILE_ENDING + BACKUP_SUFFIX);
        if (backupSettingsFile.exists()) {
            backupSettingsFile.delete();
        }
        try {
            settingsFile.renameTo(backupSettingsFile);
            File newSettingsFile = new File(settingsFileName + FILE_ENDING);
            FileOutputStream outputStream = new FileOutputStream(newSettingsFile);
            properties.store(outputStream, "Settings File For Z600 - \"" + settingsFileName + FILE_ENDING + "\"");
            outputStream.close();
            backupSettingsFile.delete();
        } catch (IOException e) {
            getLogService().log(LogService.LOG_ERROR, "Failed to save settings.", e);
            if (settingsFile.exists()) {
                settingsFile.delete();
            }
            backupSettingsFile.renameTo(settingsFile);
        }
    }

    public void load() {
        File settingsFile = new File(settingsFileName + FILE_ENDING);
        if (!settingsFile.exists()) {
            getLogService().log(LogService.LOG_WARNING,
                    "No settings file found at: " + settingsFile.getAbsolutePath() + ". Loading default settings.");
            properties = defaultProperties;
        } else {
            InputStream inputStream;
            properties = new Properties();
            try {
                inputStream = new FileInputStream(settingsFile);
                properties.load(inputStream);
                for (String property : defaultProperties.stringPropertyNames()) {
                    if (properties.containsKey(property) == false) {
                        properties.put(property, defaultProperties.getProperty(property));
                    }
                }
            } catch (FileNotFoundException e) {
                logService.log(LogService.LOG_ERROR, "Couldn't open file stream to: " + settingsFile.getAbsolutePath(),
                        e);
                return;
            } catch (IOException e) {
                getLogService().log(LogService.LOG_ERROR,
                        "Failed to load propertes from file: " + settingsFile.getAbsolutePath(), e);
                return;
            }
        }
        getLogService().log(LogService.LOG_DEBUG, "LoadedSettings: " + properties.toString());
    }

    public String getProperty(String id) {
        return properties.getProperty(id);
    }

    public boolean containsProperty(String id) {
        return properties.containsKey(id);
    }

}

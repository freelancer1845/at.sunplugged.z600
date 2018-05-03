package at.sunplugged.z600.backend.dataservice.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.backend.dataservice.impl.model.Z600Setting;
import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.NetworkComIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.common.utils.Events;
import at.sunplugged.z600.common.utils.LogServiceNOOP;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorPositionCorrectionService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

/**
 * Standard implementation of the DataService interface. Connection String
 * Working: "10.0.0.1\SQLEXPRESS; Integrated Security=false;Initial
 * Catalog=Z600_Datenerfassung"
 * 
 * @author Jascha Riedel
 *
 */

@Component()
public class DataServiceImpl implements DataService {

    private static LogService logService;

    private static StandardThreadPoolService threadPool;

    private static SettingsService settings;

    private static EventAdmin eventAdmin;

    private static MachineStateService machineStateService;

    private static ConveyorControlService conveyorService;

    private static ConveyorPositionCorrectionService conveyorPositionCorrectionService;

    private static SrmCommunicator srmCommunicatorService;

    private static SqlConnection sqlConnection = null;

    private HttpClient client;

    public static SqlConnection getSqlConnection() {
        return sqlConnection;
    }

    @Activate
    protected synchronized void activate(BundleContext context) {
        client = HttpClientBuilder.create().build();
        if (threadPool == null) {
            return;
        }
        threadPool.execute(new Runnable() {

            @Override
            public void run() {

                if (HttpHelper.checkIfHttpServerIsRunning(client)) {
                    postConnectEvent(true, null);
                    injectSettings();
                } else {
                    postConnectEvent(false, new IOException("Http Server not running."));
                }

            }

        });
    }

    @Deactivate
    protected synchronized void deactivate() {

        try {
            saveSettings(client);
            stopUpdate();
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Failed to save SQL Settings.");
        } finally {
            try {
                ((CloseableHttpClient) client).close();
            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR, "Failed to close http client.");
            }
        }
    }

    @Override
    public void startUpdate() throws DataServiceException {
        DataSavingThread.startInstance();

    }

    @Override
    public void stopUpdate() {
        DataSavingThread.stopInstance();
    }

    @Override
    public String[] getTargetMaterials() {
        try {
            return HttpHelper.getTargetMaterials(client);
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Failed to get target materials.", e);
            return new String[0];
        }
    }

    @Override
    public void mapTargetToPowersource(PowerSourceId id, String targetName) {
        TargetPowerLogger.getInstance(client).mapTargetToPowersource(targetName, id);
    }

    private void injectSettings() {
        List<Z600Setting> settingsTable;
        try {
            if (HttpHelper.checkIfHttpServerIsRunning(client) == false) {
                logService.log(LogService.LOG_ERROR, "Http Server not running, can't inject settings.");
            } else {
                settingsTable = getSettings(client);
                for (Z600Setting setting : settingsTable) {
                    logService.log(LogService.LOG_DEBUG,
                            String.format("Setting Loaded from SQL --- Name: \"%s\" Value: \"%s\"", setting.getKey(),
                                    setting.getValue()));
                }

                for (Z600Setting setting : settingsTable) {
                    if (setting.getKey().equals(SettingIds.BELT_CORRECTION_RUNTIME_LEFT)) {
                        conveyorPositionCorrectionService.setRuntimeLeft(Long.valueOf(setting.getValue()));
                    } else if (setting.getKey().equals(SettingIds.BELT_CORRECTION_RUNTIME_RIGHT)) {
                        conveyorPositionCorrectionService.setRuntimeRight(Long.valueOf(setting.getValue()));
                    } else if (setting.getKey().equals(SettingIds.BELT_POSITION)) {
                        conveyorService.setPosition(Double.valueOf(setting.getValue()));
                    }
                }
            }

        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Failed to Inject settings from Database.");
        }
    }

    private void saveSettings(HttpClient client) throws IOException {

        logService.log(LogService.LOG_DEBUG, "Saving persistence settings...");
        List<Z600Setting> settings = new ArrayList<>();
        settings.add(new Z600Setting(SettingIds.BELT_POSITION, String.valueOf(conveyorService.getPosition())));
        settings.add(new Z600Setting(SettingIds.BELT_CORRECTION_RUNTIME_LEFT,
                String.valueOf(conveyorPositionCorrectionService.getRuntimeLeft())));
        settings.add(new Z600Setting(SettingIds.BELT_CORRECTION_RUNTIME_RIGHT,
                String.valueOf(conveyorPositionCorrectionService.getRuntimeRight())));
        HttpHelper.saveSettings(client, settings);
        logService.log(LogService.LOG_DEBUG, "Saving done.");
    }

    private List<Z600Setting> getSettings(HttpClient client) throws IOException {

        List<Z600Setting> settings = HttpHelper.getAllSettings(client);

        return settings;

    }

    private void postConnectEvent(boolean successful, Throwable e) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("IP", settings.getProperty(NetworkComIds.HTTP_BASE_SERVER_URL));
        properties.put("success", successful);
        if (!successful) {
            properties.put("Error", e);
        }
        eventAdmin.postEvent(new Event(Events.SQL_CONNECT_EVENT, properties));
    }

    public static LogService getLogService() {
        if (logService == null) {
            return new LogServiceNOOP();
        }
        return DataServiceImpl.logService;
    }

    @Reference(unbind = "unsetLogService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void setLogService(LogService logService) {
        DataServiceImpl.logService = logService;
    }

    public synchronized void unsetLogService(LogService logService) {
        if (DataServiceImpl.logService == logService) {
            DataServiceImpl.logService = null;
        }
    }

    public static StandardThreadPoolService getStandardThreadPoolService() {
        return DataServiceImpl.threadPool;
    }

    @Reference(unbind = "unbindStandardThreadPoolService", cardinality = ReferenceCardinality.MANDATORY)
    public synchronized void bindStandardThreadPoolService(StandardThreadPoolService service) {
        threadPool = service;
    }

    public synchronized void unbindStandardThreadPoolService(StandardThreadPoolService service) {
        if (threadPool.equals(service)) {
            threadPool = null;
        }
    }

    public static SettingsService getSettingsServce() {
        return settings;
    }

    @Reference(unbind = "unbindSettingsService")
    public synchronized void bindSettingsService(SettingsService service) {
        settings = service;
    }

    public synchronized void unbindSettingsService(SettingsService service) {
        if (settings.equals(service)) {
            settings = null;
        }
    }

    public static EventAdmin getEventAdmin() {
        return eventAdmin;
    }

    @Reference(unbind = "unbindEventAdmin")
    public synchronized void bindEventAdmin(EventAdmin service) {
        eventAdmin = service;
    }

    public synchronized void unbindEventAdmin(EventAdmin service) {
        if (eventAdmin.equals(service)) {
            eventAdmin = null;
        }
    }

    @Reference(unbind = "unbindMachineStateService", cardinality = ReferenceCardinality.OPTIONAL)
    public synchronized void bindMachineStateService(MachineStateService machineStateService) {
        DataServiceImpl.machineStateService = machineStateService;
    }

    public synchronized void unbindMachineStateService(MachineStateService machineStateService) {
        if (DataServiceImpl.machineStateService == machineStateService) {
            DataServiceImpl.machineStateService = null;
        }
    }

    public static MachineStateService getMachineStateService() {
        return machineStateService;
    }

    @Reference(unbind = "unbindConveyorControlService")
    public synchronized void bindConveyorControlService(ConveyorControlService conveyorControlService) {
        DataServiceImpl.conveyorService = conveyorControlService;
    }

    public synchronized void unbindConveyorControlService(ConveyorControlService conveyorControlService) {
        if (DataServiceImpl.conveyorService == conveyorControlService) {
            DataServiceImpl.conveyorService = null;
        }
    }

    public static ConveyorControlService getConveyorControlService() {
        return conveyorService;
    }

    @Reference(unbind = "unbindConveyorPositionService")
    public synchronized void bindConveyorPositionService(ConveyorPositionCorrectionService conveyorPositionService) {
        DataServiceImpl.conveyorPositionCorrectionService = conveyorPositionService;
    }

    public synchronized void unbindConveyorPositionService(ConveyorPositionCorrectionService conveyorPositionService) {
        if (DataServiceImpl.conveyorPositionCorrectionService == conveyorPositionService) {
            DataServiceImpl.conveyorPositionCorrectionService = null;
        }
    }

    @Reference(unbind = "unbindSrmCommunicatorService")
    public synchronized void bindSrmCommunicatorService(SrmCommunicator srmCommunicator) {
        DataServiceImpl.srmCommunicatorService = srmCommunicator;
    }

    public synchronized void unbindSrmCommunicatorService(SrmCommunicator srmCommunicator) {
        if (DataServiceImpl.srmCommunicatorService == srmCommunicator) {
            DataServiceImpl.srmCommunicatorService = null;
        }
    }

    public static SrmCommunicator getSrmCommunicatorService() {
        return srmCommunicatorService;
    }

    public static ConveyorPositionCorrectionService getConveyorPositionService() {
        return DataServiceImpl.conveyorPositionCorrectionService;
    }

}

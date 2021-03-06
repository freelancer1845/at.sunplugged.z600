package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.NetworkComIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.common.utils.Events;
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

    public static SqlConnection getSqlConnection() {
        return sqlConnection;
    }

    @Activate
    protected synchronized void activate(BundleContext context) {
        if (threadPool == null) {
            return;
        }
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    connectToSqlServer(settings.getProperty(NetworkComIds.SQL_CONNECTION_STRING),
                            settings.getProperty(NetworkComIds.SQL_USERNAME),
                            settings.getProperty(NetworkComIds.SQL_PASSWORD));
                    postConnectEvent(true, null);
                    injectSettings();
                } catch (DataServiceException e) {
                    logService.log(LogService.LOG_ERROR, "Failed to connect to sql server specified in settings file.",
                            e);
                    postConnectEvent(false, e);
                }
            }

        });
    }

    @Deactivate
    protected synchronized void deactivate() {
        if (sqlConnection.isOpen() == false) {
            return;
        }
        try {
            saveSettings();
        } catch (SQLException e) {
            logService.log(LogService.LOG_ERROR, "Failed to save settings to sql database.", e);
        }
    }

    @Override
    public void startUpdate() throws DataServiceException {
        DataSavingThread.startInstance(sqlConnection);

    }

    @Override
    public void stopUpdate() {
        DataSavingThread.stopInstance();
    }

    @Override
    public String[] getTargetMaterials() {
        try {
            if (SqlUtils.checkIfTableExists(sqlConnection, TableNames.TARGET_IDS) == false) {
                logService.log(LogService.LOG_ERROR,
                        "TargetID's table not found in sql database. Can't map powersources to targets...");
                return new String[0];
            } else {
                Statement stm = sqlConnection.getStatement();
                stm.executeQuery("SELECT Name FROM " + TableNames.TARGET_IDS);
                ResultSet resultSet = stm.getResultSet();
                List<String> arrayList = new ArrayList<>();
                while (resultSet.next() == true) {
                    arrayList.add(resultSet.getString(1));
                }
                stm.close();
                return arrayList.toArray(new String[0]);
            }
        } catch (SQLException e) {
            logService.log(LogService.LOG_ERROR, "Failed to read TargetID's from sql database...", e);
            return new String[0];
        }
    }

    @Override
    public void mapTargetToPowersource(PowerSourceId id, String targetName) {
        TargetPowerLogger.getInstance().mapTargetToPowersource(targetName, id);
    }

    private void connectToSqlServer(String address, String username, String password) throws DataServiceException {
        sqlConnection = new SqlConnection("jdbc:sqlserver://" + address, username, password);
        sqlConnection.open();
    }

    private void injectSettings() {
        try {
            Map<String, Object> settingsTable = getSettingsTable();
            for (String name : settingsTable.keySet()) {
                logService.log(LogService.LOG_DEBUG, String.format(
                        "Setting Loaded from SQL --- Name: \"%s\" Value: \"%s\"", name, settingsTable.get(name)));
            }
            if (settingsTable.containsKey(SettingIds.BELT_CORRECTION_RUNTIME_LEFT)) {
                conveyorPositionCorrectionService
                        .setRuntimeLeft((long) settingsTable.get(SettingIds.BELT_CORRECTION_RUNTIME_LEFT));
            }
            if (settingsTable.containsKey(SettingIds.BELT_CORRECTION_RUNTIME_RIGHT)) {
                conveyorPositionCorrectionService
                        .setRuntimeRight((long) settingsTable.get(SettingIds.BELT_CORRECTION_RUNTIME_RIGHT));
            }
            if (settingsTable.containsKey(SettingIds.BELT_POSITION)) {
                conveyorService.setPosition((double) settingsTable.get(SettingIds.BELT_POSITION));
            }
        } catch (SQLException e) {
            logService.log(LogService.LOG_ERROR, "Failed to inject settings from sql database.", e);
        }
    }

    private void saveSettings() throws SQLException {
        saveSingleSetting(SettingIds.BELT_POSITION, "Double", String.valueOf(conveyorService.getPosition()));
        saveSingleSetting(SettingIds.BELT_CORRECTION_RUNTIME_LEFT, "Long",
                String.valueOf(conveyorPositionCorrectionService.getRuntimeLeft()));
        saveSingleSetting(SettingIds.BELT_CORRECTION_RUNTIME_RIGHT, "Long",
                String.valueOf(conveyorPositionCorrectionService.getRuntimeRight()));

    }

    private void saveSingleSetting(String id, String type, String value) {
        try (Statement stm = sqlConnection.getStatement()) {
            String sql2 = "IF NOT EXISTS (select * from " + TableNames.SETTINGS_TABLE + " where id = '" + id
                    + "')\n BEGIN INSERT INTO " + TableNames.SETTINGS_TABLE + " (id, type , value) VALUES ('" + id
                    + "','" + type + "'," + value + ") END;  UPDATE " + TableNames.SETTINGS_TABLE + " SET value = '"
                    + value + "' WHERE id ='" + id + "'";
            stm.execute(sql2);
            stm.close();
        } catch (SQLException e) {
            logService.log(LogService.LOG_ERROR, "Failed to save setting: \"" + id + "\" Value: \"" + value + "\".", e);
        }
    }

    private Map<String, Object> getSettingsTable() throws SQLException {
        if (SqlUtils.checkIfTableExists(sqlConnection, TableNames.SETTINGS_TABLE) == false) {
            Map<String, Object> emptySettingsTable = createSettingsTable();
            logService.log(LogService.LOG_DEBUG, "No Settings Table found in Database!");
            return emptySettingsTable;
        }
        Statement stm = sqlConnection.getStatement();
        stm.executeQuery("SELECT * FROM " + TableNames.SETTINGS_TABLE);
        ResultSet resultSet = stm.getResultSet();
        Map<String, Object> returnMap = new HashMap<>();
        while (resultSet.next() == true) {
            String id = resultSet.getString("id");
            switch (resultSet.getString("id")) {
            case SettingIds.BELT_POSITION:
                returnMap.put(id, Double.valueOf(resultSet.getString("value")));
                break;
            case SettingIds.BELT_CORRECTION_RUNTIME_RIGHT:
                returnMap.put(id, Long.valueOf(resultSet.getString("value")));
                break;
            case SettingIds.BELT_CORRECTION_RUNTIME_LEFT:
                returnMap.put(id, Long.valueOf(resultSet.getString("value")));
                break;
            default:
                logService.log(LogService.LOG_DEBUG, "Unexpected settings id: \"" + id + "\" Type: \""
                        + resultSet.getString("type") + "\" Value: \"" + resultSet.getString("value") + "\".");
                break;
            }

        }
        stm.close();
        return returnMap;
    }

    private Map<String, Object> createSettingsTable() throws SQLException {

        try {
            Statement stm = sqlConnection.getStatement();
            String sql = "CREATE TABLE " + TableNames.SETTINGS_TABLE + " (id VARCHAR(255) not NULL PRIMARY KEY, "
                    + " type VARCHAR(255) not NULL," + " value VARCHAR(255))";
            stm.executeUpdate(sql);
            stm.close();
        } catch (SQLException e) {
            logService.log(LogService.LOG_DEBUG, "Failed to create settings table.");
            throw e;
        }

        return new HashMap<String, Object>();
    }

    public void issueStatement(String statement) {
        if (!sqlConnection.isOpen()) {
            return;
        }

        try {
            Statement statementObject = sqlConnection.getStatement();
            statementObject.execute(statement);
            ResultSet resultSet = statementObject.getResultSet();
            if (resultSet == null) {
                return;
            }
            ResultSetMetaData rsmd = resultSet.getMetaData();
            if (rsmd == null) {
                return;
            }
            int columnsNumber = rsmd.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1)
                        System.out.print(",  ");
                    String columnValue = resultSet.getString(i);
                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
                System.out.println("");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void postConnectEvent(boolean successful, Throwable e) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("IP", sqlConnection.getDbUrl());
        properties.put("success", successful);
        if (!successful) {
            properties.put("Error", e);
        }
        eventAdmin.postEvent(new Event(Events.SQL_CONNECT_EVENT, properties));
    }

    public static LogService getLogService() {
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

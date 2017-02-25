package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
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
import at.sunplugged.z600.core.machinestate.api.MachineStateService;

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

    private SqlConnection sqlConnection = null;

    private final Map<String, VariableSlot<?>> variableSlots = new HashMap<>();

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
                            NetworkComIds.SQL_USERNAME, NetworkComIds.SQL_PASSWORD);
                    postConnectEvent(true, null);
                } catch (DataServiceException e) {
                    logService.log(LogService.LOG_ERROR, "Failed to connect to sql server specified in settings file.",
                            e);
                    postConnectEvent(false, e);
                }
            }

        });
    }

    @Override
    public void connectToSqlServer(String address, String username, String password) throws DataServiceException {
        sqlConnection = new SqlConnection("jdbc:sqlserver://" + address, username, password);
        sqlConnection.open();
    }

    @Override
    public void startAutomaticSqlTableUpdating(int tickrate, String variableName, String... columns)
            throws DataServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopAutomaticSqlTableUpdating(String variableName) throws DataServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createDataBaseSnapshot(String filePath) throws DataServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveData(String variableName, Date date, Object data) throws DataServiceException {
        if (!variableSlots.containsKey(variableName)) {
            variableSlots.put(variableName, createVariableSlot(variableName, data.getClass()));
        }
        variableSlots.get(variableName).addData(date, data);
    }

    @Override
    public void clearDatabase() {
        variableSlots.clear();
    }

    private VariableSlot<?> createVariableSlot(String variableName, Class<?> type) throws DataServiceException {

        switch (type.getName()) {
        case "java.lang.Double":
            return new VariableSlot<Double>(variableName);
        default:
            throw new DataServiceException("The type of this data is not supported: \"" + type.getName() + "\"");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getData(String variableName, Class<T> type) throws DataServiceException {
        if (variableSlots.containsKey(variableName)) {
            switch (type.getName()) {
            case "java.lang.Double":
                return (List<T>) variableSlots.get(variableName).getData();
            default:
                throw new DataServiceException("The type of this data is not supported: \"" + type.getName() + "\"");
            }
        } else {
            throw new DataServiceException("Variable Not Registered: " + variableName);
        }
    }

    @Override
    public void startAddingSrmDataToTable() {

    }

    @Override
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

}

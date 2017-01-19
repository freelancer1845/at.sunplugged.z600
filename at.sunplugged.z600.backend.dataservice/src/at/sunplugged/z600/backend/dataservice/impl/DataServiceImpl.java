package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;

/**
 * Standard implementation of the DataService interface.
 * 
 * @author Jascha Riedel
 *
 */

@Component(immediate = true)
public class DataServiceImpl implements DataService {

    private static LogService logService;

    private SqlConnection sqlConnection = null;

    private final Map<String, VariableSlot<?>> variableSlots = new HashMap<>();

    public DataServiceImpl() {
        // sqlConnection = new SqlConnection(
        // "jdbc:sqlserver://10.0.0.1;integratedsecurity=false;Initialcatalog=Z600_Datenerfassung;",
        // "Z600",
        // "alwhrh29035uafpue9ru3AWU");

    }

    @Override
    public void connectToSqlServer(String address, String username, String password) throws DataServiceException {
        sqlConnection = new SqlConnection("jdbc:sqlserver://" + address, username, password);
        sqlConnection.open();
        if (sqlConnection.isOpen()) {
            System.out.println("SQL Connection OPEN");
        }
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

    public static LogService getLogService() {
        return DataServiceImpl.logService;
    }

    @Reference(unbind = "unsetLogService")
    public synchronized void setLogService(LogService logService) {
        DataServiceImpl.logService = logService;
    }

    public synchronized void unsetLogService(LogService logService) {
        if (DataServiceImpl.logService == logService) {
            DataServiceImpl.logService = null;
        }
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

}

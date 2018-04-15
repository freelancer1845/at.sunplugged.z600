package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.common.settings.api.NetworkComIds;

public class DataSavingThread extends Thread {

    private static DataSavingThread instance = null;

    public static void startInstance(SqlConnection sqlConnection) throws DataServiceException {
        if (instance == null) {
            instance = new DataSavingThread(sqlConnection);
            instance.start();
        } else {
            throw new DataServiceException("There is already a running instance of the DataSavingThread");
        }
    }

    public static void stopInstance() {
        if (instance != null) {
            instance.running = false;
        }
    }

    private final SqlConnection sqlConnection;

    private volatile boolean running = false;

    private DataSavingThread(SqlConnection sqlConnection) {
        this.setName("Data Saving Thread");
        this.sqlConnection = sqlConnection;
    }

    @Override
    public void run() {
        running = true;

        int exceptionCount = 0;
        int sessionId;
        int dataPoint = 0;

        try {
            if (SqlUtils.checkIfTableExists(sqlConnection, DataService.TABLE_NAME) == false) {
                createProcessDataTable();
                sessionId = 1;
            } else {
                sessionId = getSessionId();
            }

            while (isRunning()) {
                try {
                    Thread.sleep(Long.valueOf(
                            DataServiceImpl.getSettingsServce().getProperty(NetworkComIds.SQL_UPDATE_TIME_STEP)));
                    dataPoint++;
                    WriteDataTableUtils.writeDataTable(sqlConnection, sessionId, dataPoint);
                    exceptionCount = 0;
                } catch (InterruptedException it) {
                    // Proably program closed... finish.
                    setRunning(false);
                } catch (Exception e) {
                    if (DataServiceImpl.getLogService() == null) {
                        e.printStackTrace();
                    }
                    DataServiceImpl.getLogService().log(LogService.LOG_WARNING,
                            "Unexpected exception in DataSavingThread!", e);
                    exceptionCount++;
                    if (exceptionCount > 5) {
                        setRunning(false);
                        DataServiceImpl.getLogService().log(LogService.LOG_ERROR,
                                "Stopped Data Saving. Too many exceptions...");
                    }
                }
            }
        } catch (

        SQLException e1) {
            DataServiceImpl.getLogService().log(LogService.LOG_ERROR, "Failed to access Process Table...", e1);
            setRunning(false);
        }
        instance = null;

    }

    private void createProcessDataTable() throws SQLException {
        WriteDataTableUtils.createDataTable(sqlConnection);
    }

    private int getSessionId() throws SQLException {
        Statement stmt = sqlConnection.getStatement();

        ResultSet res = stmt.executeQuery("SELECT Session FROM " + DataService.TABLE_NAME);

        List<Integer> sessions = new ArrayList<>();
        while (res.next()) {
            sessions.add(res.getInt(1));
        }

        return sessions.stream().mapToInt(i -> i).max().getAsInt() + 1;
    }

    private void listDataInTable(String tableName) throws SQLException {
        Statement stmt = sqlConnection.getStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i < metaData.getColumnCount(); i++) {
            System.out.print(metaData.getColumnName(i) + "---");
        }
        System.out.println();

        while (rs.next()) {
            for (int i = 1; i < metaData.getColumnCount(); i++) {
                System.out.print(rs.getString(i) + "---");
            }
            System.out.println();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}

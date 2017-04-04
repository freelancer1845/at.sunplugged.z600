package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.common.settings.api.NetworkComIds;

public class DataSavingThread extends Thread {

    private static DataSavingThread instance = null;

    public static void startInstance(SqlConnection sqlConnection) throws DataServiceException {
        if (instance != null) {
            instance = new DataSavingThread(sqlConnection);
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
        String tableName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dMuHms"));
        while (isRunning()) {
            try {
                WriteDataTableUtils.writeDataTable(sqlConnection, tableName);
                listAvailableTables();
                Thread.sleep(Long
                        .valueOf(DataServiceImpl.getSettingsServce().getProperty(NetworkComIds.SQL_UPDATE_TIME_STEP)));
            } catch (Exception e) {
                DataServiceImpl.getLogService().log(LogService.LOG_ERROR, "Unexpected exception in DataSavingThread!",
                        e);
                setRunning(false);
            }
        }

        instance = null;

    }

    private void listAvailableTables() throws SQLException {
        DatabaseMetaData md = sqlConnection.getConnection().getMetaData();
        ResultSet rs = md.getTables(null, null, "%", null);
        System.out.println("Current Tables: ");
        while (rs.next()) {
            System.out.println(rs.getString(3));
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}

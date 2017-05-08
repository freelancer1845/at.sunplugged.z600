package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.osgi.service.log.LogService;

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
        String tableName = "Zyklus" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuuMMddHHmm"));
        while (isRunning()) {
            try {
                WriteDataTableUtils.writeDataTable(sqlConnection, tableName);
                Thread.sleep(Long
                        .valueOf(DataServiceImpl.getSettingsServce().getProperty(NetworkComIds.SQL_UPDATE_TIME_STEP)));
                exceptionCount = 0;
            } catch (Exception e) {
                DataServiceImpl.getLogService().log(LogService.LOG_WARNING, "Unexpected exception in DataSavingThread!",
                        e);
                exceptionCount++;
                if (exceptionCount > 5) {
                    setRunning(false);
                    DataServiceImpl.getLogService().log(LogService.LOG_ERROR,
                            "Stopped Data Saving. Too many exceptions...");
                }
            }
        }

        instance = null;

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

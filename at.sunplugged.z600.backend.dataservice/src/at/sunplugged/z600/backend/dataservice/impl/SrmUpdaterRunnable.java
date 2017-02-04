package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataServiceException;

public class SrmUpdaterRunnable implements Runnable {

    private final LogService logService;

    private final SqlConnection sqlConnection;

    public SrmUpdaterRunnable(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
        this.logService = DataServiceImpl.getLogService();
    }

    @Override
    public void run() {
        if (!sqlConnection.isOpen()) {
            try {
                sqlConnection.open();
            } catch (DataServiceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            stmt = sqlConnection.getStatement();
        } catch (SQLException e) {
            logService.log(LogService.LOG_ERROR,
                    "Error getting statement from connection in " + this.getClass().getName(), e);
        }
        if (stmt != null) {
            // TODO: Complete
            // stmt.executeQuery(sql)
        }

        String currentTableName = findCurrentTableName();

    }

    private String findCurrentTableName() {
        return null;
    }

}

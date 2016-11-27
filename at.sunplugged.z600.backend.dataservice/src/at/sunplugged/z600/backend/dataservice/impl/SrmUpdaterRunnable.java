package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.DataServiceActivator;

public class SrmUpdaterRunnable implements Runnable {

    private final LogService logService;

    private final SqlConnection sqlConnection;

    public SrmUpdaterRunnable(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
        this.logService = DataServiceActivator.getLogService();
    }

    @Override
    public void run() {
        if (!sqlConnection.isOpen()) {
            sqlConnection.open();
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

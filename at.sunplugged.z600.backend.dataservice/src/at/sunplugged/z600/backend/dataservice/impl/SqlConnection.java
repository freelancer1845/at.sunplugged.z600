package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.osgi.service.log.LogService;

public class SqlConnection {

    /**
     * Standard:
     * "jdbc:sqlserver://10.0.0.1;integratedsecurity=false;Initialcatalog=Z600_Datenerfassung;".
     */
    private final String dbUrl;

    private final String username;

    private final String password;

    private Connection conn = null;

    public SqlConnection(String dbUrl, String username, String password) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
    }

    public void open() throws DataServiceException {
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            DriverManager.setLoginTimeout(5);
            conn = DriverManager.getConnection(dbUrl, username, password);
        } catch (SQLException e) {
            DataServiceImpl.getLogService().log(LogService.LOG_ERROR, "Failed to open connection", e);
            throw new DataServiceException(e);
        }

    }

    public Statement getStatement() throws SQLException {
        if (conn.isValid(1)) {
            Statement stmt;
            stmt = conn.createStatement();
            return stmt;
        } else {
            throw new SQLException("Connection Not Valid.");
        }

    }

    public boolean isOpen() {
        if (conn == null) {
            return false;
        }
        try {
            if (!conn.isValid(5)) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public Connection getConnection() {
        return conn;
    }

}

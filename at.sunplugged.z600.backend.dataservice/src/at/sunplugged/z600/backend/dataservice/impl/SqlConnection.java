package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.DataServiceActivator;

public class SqlConnection {

    private final String dbUrl = "jdbc:sqlserver://10.0.0.1;integratedsecurity=false;Initialcatalog=Z600_Datenerfassung;";

    private final String username;

    private final String password;

    private Connection conn = null;

    public SqlConnection(String dbUrl, String username, String password) {
        // this.username = username;
        // this.password = password;
        // Test
        this.username = "Z600";
        this.password = "alwhrh29035uafpue9ru3AWU";
    }

    public void open() {
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            DriverManager.setLoginTimeout(5);
            conn = DriverManager.getConnection(dbUrl, username, password);
        } catch (SQLException e) {
            DataServiceActivator.getLogService().log(LogService.LOG_ERROR, "Failed to open connection", e);
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

}

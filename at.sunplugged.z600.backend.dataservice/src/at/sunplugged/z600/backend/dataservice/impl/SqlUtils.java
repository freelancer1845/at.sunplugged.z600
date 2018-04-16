package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlUtils {

    public static boolean checkIfTableExists(SqlConnection connection, String tableName) throws SQLException {
        ResultSet rs = connection.getStatement().executeQuery(
                "SELECT TABLE_NAME FROM Z600_Datenerfassung.INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'");

        while (rs.next()) {

            if (rs.getString(1).equals(tableName)) {
                return true;
            }
        }
        return false;
    }

    private SqlUtils() {

    }

}

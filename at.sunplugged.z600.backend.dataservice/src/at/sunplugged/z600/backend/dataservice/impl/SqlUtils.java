package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlUtils {

    public static boolean checkIfTableExists(SqlConnection connection, String tableName) throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getConnection().getMetaData();
        ResultSet res = databaseMetaData.getTables(null, null, tableName, new String[] { "TABLE" });
        boolean returnValue = false;
        while (res.next()) {
            if (res.getString("TABLE_NAME").equals(tableName)) {
                returnValue = true;
            }
        }
        res.close();
        return returnValue;
    }

    private SqlUtils() {

    }

}

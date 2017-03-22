package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;

public class WriteDataTableUtils {

    public static void writeDataTable(SqlConnection connection, String tableName)
            throws DataServiceException, SQLException {
        if (connection.isOpen() == false) {
            throw new DataServiceException("Failed to write DataTable." + "Connection not open!");
        }

        if (SqlUtils.checkIfTableExists(connection, tableName)) {
            createDataTable(connection, tableName);
        }
        Map<String, Object> dataMap = getDataSnapShot();
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder placeholders = new StringBuilder();

        sql.append("Time,");
        placeholders.append("?,");
        for (Iterator<String> iter = dataMap.keySet().iterator(); iter.hasNext();) {
            sql.append(iter.next());
            placeholders.append("?");
            if (iter.hasNext()) {
                sql.append(",");
                placeholders.append(",");
            }
        }

        sql.append(") VALUES (").append(placeholders).append(")");
        PreparedStatement statement = connection.getConnection().prepareStatement(sql.toString());
        int i = 1;
        java.sql.Time sqlDate = new java.sql.Time(new java.util.Date().getTime());
        statement.setObject(0, sqlDate);
        for (Object value : dataMap.values()) {
            statement.setObject(i++, value);
        }
        statement.executeUpdate();
        statement.close();

    }

    private static Map<String, Object> getDataSnapShot() {
        Map<String, Object> dataMap = new HashMap<>();
        MachineStateService machine = DataServiceImpl.getMachineStateService();
        if (machine != null) {
            dataMap.putAll(getPressureSnapShot(machine));
            dataMap.putAll(getCathodeSettingsSnapShot(machine));
        }
        ConveyorControlService conveyor = DataServiceImpl.getConveyorControlService();
        if (conveyor != null) {
            dataMap.putAll(getConveyorControlSnapShot(conveyor));
        }

        return dataMap;
    }

    private static void createDataTable(SqlConnection connection, String tableName) throws SQLException {
        Statement stm = connection.getStatement();
        String sql = "CREATE TABLE [" + tableName + "] (";
        sql += "Time TIME, ";
        sql += ColumnNames.PERSSURE_TMP + " FLOAT, ";
        sql += ColumnNames.PRESSURE_CHAMBER + " FLOAT, ";
        sql += ColumnNames.PRESSURE_CRYO_ONE + " FLOAT, ";
        sql += ColumnNames.PRESSURE_CRYO_TWO + " FLOAT, ";
        sql += ColumnNames.CONVEYOR_MODE + " VARCHAR(256), ";
        sql += ColumnNames.CONVEYOR_SPEED_SETPOINT + " FLOAT, ";
        sql += ColumnNames.CONVEYOR_SPEED_COMBINED + " FLOAT, ";
        sql += ColumnNames.CONVEYOR_SPEED_LEFT + " FLOAT, ";
        sql += ColumnNames.CONVEYOR_SPEED_RIGHT + " FLOAT, ";
        sql += ColumnNames.CONVEYOR_ENGINE_LEFT_MAXIMUM + " INTEGER, ";
        sql += ColumnNames.CONVEYOR_ENGINE_RIGHT_MAXIMUM + " INTEGER, ";
        sql += ColumnNames.CATHODE_ONE_SETPOINT + " FLOAT, ";
        sql += ColumnNames.CATHODE_TWO_SETPOINT + " FLOAT, ";
        sql += ColumnNames.CATHODE_THREE_SETPOINT + " FLOAT; ";
        stm.executeUpdate(sql);
        stm.close();

    }

    private static Map<String, Object> getPressureSnapShot(MachineStateService machine) {
        Map<String, Object> dataMap = new HashMap<>();
        PressureMeasurement pressureInterface = machine.getPressureMeasurmentControl();
        dataMap.put(ColumnNames.PERSSURE_TMP,
                pressureInterface.getCurrentValue(PressureMeasurementSite.TURBO_PUMP));
        dataMap.put(ColumnNames.PRESSURE_CHAMBER,
                pressureInterface.getCurrentValue(PressureMeasurementSite.CHAMBER));
        dataMap.put(ColumnNames.PRESSURE_CRYO_ONE,
                pressureInterface.getCurrentValue(PressureMeasurementSite.CRYO_PUMP_ONE));
        dataMap.put(ColumnNames.PRESSURE_CRYO_TWO,
                pressureInterface.getCurrentValue(PressureMeasurementSite.CRYO_PUMP_TWO));
        return dataMap;
    }

    private static Map<String, Object> getPowerSnapShot(MachineStateService machine) {
        Map<String, Object> dataMap = new HashMap<>();
        // TODO : Mapping of Cathodes to Power is unclear
        return dataMap;
    }

    private static Map<String, Object> getCathodeSettingsSnapShot(MachineStateService machine) {
        Map<String, Object> dataMap = new HashMap<>();

        return dataMap;
    }

    private static Map<String, Object> getConveyorControlSnapShot(ConveyorControlService conveyor) {
        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put(ColumnNames.CONVEYOR_MODE, conveyor.getActiveMode().name());
        dataMap.put(ColumnNames.CONVEYOR_SPEED_COMBINED, conveyor.getCurrentSpeed());
        dataMap.put(ColumnNames.CONVEYOR_SPEED_LEFT, conveyor.getSpeedLogger().getLeftSpeed());
        dataMap.put(ColumnNames.CONVEYOR_SPEED_RIGHT, conveyor.getSpeedLogger().getRightSpeed());
        dataMap.put(ColumnNames.CONVEYOR_SPEED_SETPOINT, conveyor.getSetpointSpeed());
        dataMap.put(ColumnNames.CONVEYOR_ENGINE_LEFT_MAXIMUM, conveyor.getEngineOne().getCurrentMaximumSpeed());
        dataMap.put(ColumnNames.CONVEYOR_ENGINE_RIGHT_MAXIMUM, conveyor.getEngineTwo().getCurrentMaximumSpeed());

        return dataMap;
    }

    private WriteDataTableUtils() {

    }
}

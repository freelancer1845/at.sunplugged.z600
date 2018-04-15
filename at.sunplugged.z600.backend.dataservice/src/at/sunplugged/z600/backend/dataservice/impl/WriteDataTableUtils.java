package at.sunplugged.z600.backend.dataservice.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

public class WriteDataTableUtils {

    public static void addWorkToTargetIdConsumptionTable(SqlConnection connection, String targetId, double workDone)
            throws DataServiceException, SQLException {
        if (connection.isOpen() == false) {
            throw new DataServiceException(
                    "Failed to write add work to TargetIdConsumptionTable, Connection not open!");
        }
        if (SqlUtils.checkIfTableExists(connection, TableNames.TARGET_CONSUMPTION_TABLE) == false) {
            createTargetConsumtionTable(connection);
        }
        Statement stm = connection.getStatement();
        String sql2 = "IF NOT EXISTS (select * from " + TableNames.TARGET_CONSUMPTION_TABLE + " where TargetId = '"
                + targetId + "')\n BEGIN INSERT INTO " + TableNames.TARGET_CONSUMPTION_TABLE + " (TargetId, "
                + ColumnNames.TARGET_WORK_DONE + ") VALUES ('" + targetId + "', " + 0 + ") END;  UPDATE "
                + TableNames.TARGET_CONSUMPTION_TABLE + " SET " + ColumnNames.TARGET_WORK_DONE + " = "
                + ColumnNames.TARGET_WORK_DONE + " + " + workDone + " WHERE TargetId ='" + targetId + "'";
        stm.execute(sql2);
        stm.close();

    }

    private static void createTargetConsumtionTable(SqlConnection connection) throws SQLException {
        Statement stm = connection.getStatement();
        String sql = "CREATE TABLE [" + TableNames.TARGET_CONSUMPTION_TABLE + "] (";
        sql += "TargetId VARCHAR(256), ";
        sql += ColumnNames.TARGET_WORK_DONE + " double)";
        stm.executeUpdate(sql);
        stm.close();
    }

    public static void writeDataTable(SqlConnection connection, int sessionId, int dataPoint)
            throws DataServiceException, SQLException {
        if (connection.isOpen() == false) {
            throw new DataServiceException("Failed to write DataTable." + "Connection not open!");
        }

        Map<String, Object> dataMap = getDataSnapShot();
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(DataService.TABLE_NAME).append(" (");
        StringBuilder placeholders = new StringBuilder();

        sql.append("Session,");
        sql.append("DataPoint,");
        sql.append("`Date`,");
        placeholders.append("?,");
        placeholders.append("?,");
        placeholders.append("?,");
        for (Iterator<String> iter = dataMap.keySet().iterator(); iter.hasNext();) {
            sql.append(iter.next());
            placeholders.append("?");
            if (iter.hasNext()) {
                sql.append(",");
                placeholders.append(",");
            }
        }

        if (placeholders.toString().endsWith(",")) {
            placeholders.deleteCharAt(placeholders.length() - 1);
        }
        sql.append(") VALUES (").append(placeholders).append(")");
        PreparedStatement statement = connection.getConnection().prepareStatement(sql.toString());
        int i = 1;
        java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
        statement.setObject(i++, sessionId);
        statement.setObject(i++, dataPoint);
        statement.setObject(i++, date);
        for (Object value : dataMap.values()) {
            statement.setObject(i++, value);
        }
        statement.executeUpdate();
        statement.close();

    }

    private static Map<String, Object> getDataSnapShot() {
        Map<String, Object> dataMap = new LinkedHashMap<>();
        MachineStateService machine = DataServiceImpl.getMachineStateService();
        if (machine != null) {
            dataMap.putAll(getPressureSnapShot(machine));
            dataMap.putAll(getPowerSnapShot(machine));
        }
        ConveyorControlService conveyor = DataServiceImpl.getConveyorControlService();
        if (conveyor != null) {
            dataMap.putAll(getConveyorControlSnapShot(conveyor));
        }

        SrmCommunicator srmCommunicator = DataServiceImpl.getSrmCommunicatorService();
        if (srmCommunicator != null) {
            dataMap.putAll(getSrmSnapShot(srmCommunicator));
        }
        return dataMap;
    }

    public static void createDataTable(SqlConnection connection) throws SQLException {
        Statement stm = connection.getStatement();
        String sql = "CREATE TABLE `PROCESS_DATA` (";
        sql += "`Session` int(11) NOT NULL, ";
        sql += "`DataPoint` int(11) NOT NULL, ";
        sql += "`Date` datetime NOT NULL, ";
        sql += ColumnNames.PERSSURE_TMP + " double, ";
        sql += ColumnNames.PRESSURE_CHAMBER + " double, ";
        sql += ColumnNames.PRESSURE_CRYO_ONE + " double, ";
        sql += ColumnNames.PRESSURE_CRYO_TWO + " double, ";
        sql += ColumnNames.CURRENT_GAS_FLOW_SCCM + " double, ";
        sql += ColumnNames.CONVEYOR_MODE + " VARCHAR(256), ";
        sql += ColumnNames.CONVEYOR_SPEED_SETPOINT + " double, ";
        sql += ColumnNames.CONVEYOR_SPEED_COMBINED + " double, ";
        sql += ColumnNames.CONVEYOR_SPEED_LEFT + " double, ";
        sql += ColumnNames.CONVEYOR_SPEED_RIGHT + " double, ";
        sql += ColumnNames.CONVEYOR_ENGINE_LEFT_MAXIMUM + " int(11), ";
        sql += ColumnNames.CONVEYOR_ENGINE_RIGHT_MAXIMUM + " int(11), ";
        sql += ColumnNames.CONVEYOR_POSITION_COMBINED + " double, ";
        sql += ColumnNames.CONVEYOR_POSITION_LEFT + " double, ";
        sql += ColumnNames.CONVEYOR_POSITION_RIGHT + " double, ";
        sql += ColumnNames.PINNACLE_POWER + " double, ";
        sql += ColumnNames.PINNACLE_POWER_SETPOINT + " double, ";
        sql += ColumnNames.PINNACLE_VOLTAGE + " double, ";
        sql += ColumnNames.PINNACLE_CURRENT + " double, ";
        sql += ColumnNames.SSV_ONE_POWER + " double, ";
        sql += ColumnNames.SSV_ONE_POWER_SETPOINT + " double, ";
        sql += ColumnNames.SSV_ONE_VOLTAGE + " double, ";
        sql += ColumnNames.SSV_ONE_CURRENT + " double, ";
        sql += ColumnNames.SSV_TWO_POWER + " double, ";
        sql += ColumnNames.SSV_TWO_POWER_SETPOINT + " double, ";
        sql += ColumnNames.SSV_TWO_VOLTAGE + " double, ";
        sql += ColumnNames.SSV_TWO_CURRENT + " double,";
        sql += ColumnNames.SRM_CHANNEL_2_LEFT + " double,";
        sql += ColumnNames.SRM_CHANNEL_3_RIGHT + " double,";
        sql += "PRIMARY KEY (`Session`, `DataPoint`) )";
        stm.executeUpdate(sql);
        stm.close();

    }

    private static Map<String, Object> getPressureSnapShot(MachineStateService machine) {
        Map<String, Object> dataMap = new LinkedHashMap<>();
        PressureMeasurement pressureInterface = machine.getPressureMeasurmentControl();
        dataMap.put(ColumnNames.PERSSURE_TMP, pressureInterface.getCurrentValue(PressureMeasurementSite.TURBO_PUMP));
        dataMap.put(ColumnNames.PRESSURE_CHAMBER, pressureInterface.getCurrentValue(PressureMeasurementSite.CHAMBER));
        dataMap.put(ColumnNames.PRESSURE_CRYO_ONE,
                pressureInterface.getCurrentValue(PressureMeasurementSite.CRYO_PUMP_ONE));
        dataMap.put(ColumnNames.PRESSURE_CRYO_TWO,
                pressureInterface.getCurrentValue(PressureMeasurementSite.CRYO_PUMP_TWO));
        dataMap.put(ColumnNames.CURRENT_GAS_FLOW_SCCM, machine.getGasFlowControl().getCurrentGasFlowInSccm());
        return dataMap;
    }

    private static Map<String, Object> getPowerSnapShot(MachineStateService machine) {
        Map<String, Object> dataMap = new LinkedHashMap<>();
        PowerSourceRegistry powerSourceRegistry = machine.getPowerSourceRegistry();
        PowerSource currentSource;

        // Pinnalce
        currentSource = powerSourceRegistry.getPowerSource(PowerSourceId.PINNACLE);
        dataMap.put(ColumnNames.PINNACLE_POWER, currentSource.getPower());
        dataMap.put(ColumnNames.PINNACLE_POWER_SETPOINT, currentSource.getSetPointpower());
        dataMap.put(ColumnNames.PINNACLE_VOLTAGE, currentSource.getVoltage());
        dataMap.put(ColumnNames.PINNACLE_CURRENT, currentSource.getCurrent());

        // SSV ONE
        currentSource = powerSourceRegistry.getPowerSource(PowerSourceId.SSV1);
        dataMap.put(ColumnNames.SSV_ONE_POWER, currentSource.getPower());
        dataMap.put(ColumnNames.SSV_ONE_POWER_SETPOINT, currentSource.getSetPointpower());
        dataMap.put(ColumnNames.SSV_ONE_VOLTAGE, currentSource.getVoltage());
        dataMap.put(ColumnNames.SSV_ONE_CURRENT, currentSource.getCurrent());

        // SSV TWO
        currentSource = powerSourceRegistry.getPowerSource(PowerSourceId.SSV2);
        dataMap.put(ColumnNames.SSV_TWO_POWER, currentSource.getPower());
        dataMap.put(ColumnNames.SSV_TWO_POWER_SETPOINT, currentSource.getSetPointpower());
        dataMap.put(ColumnNames.SSV_TWO_VOLTAGE, currentSource.getVoltage());
        dataMap.put(ColumnNames.SSV_TWO_CURRENT, currentSource.getCurrent());

        return dataMap;
    }

    private static Map<String, Object> getConveyorControlSnapShot(ConveyorControlService conveyor) {
        Map<String, Object> dataMap = new LinkedHashMap<>();

        dataMap.put(ColumnNames.CONVEYOR_MODE, conveyor.getActiveMode().name());
        dataMap.put(ColumnNames.CONVEYOR_SPEED_COMBINED, conveyor.getCurrentSpeed());
        dataMap.put(ColumnNames.CONVEYOR_SPEED_LEFT, conveyor.getSpeedLogger().getLeftSpeed());
        dataMap.put(ColumnNames.CONVEYOR_SPEED_RIGHT, conveyor.getSpeedLogger().getRightSpeed());
        dataMap.put(ColumnNames.CONVEYOR_SPEED_SETPOINT, conveyor.getSetpointSpeed());
        dataMap.put(ColumnNames.CONVEYOR_ENGINE_LEFT_MAXIMUM, conveyor.getEngineOne().getCurrentMaximumSpeed());
        dataMap.put(ColumnNames.CONVEYOR_ENGINE_RIGHT_MAXIMUM, conveyor.getEngineTwo().getCurrentMaximumSpeed());
        dataMap.put(ColumnNames.CONVEYOR_POSITION_COMBINED, conveyor.getPosition());
        dataMap.put(ColumnNames.CONVEYOR_POSITION_LEFT, conveyor.getLeftPosition());
        dataMap.put(ColumnNames.CONVEYOR_POSITION_RIGHT, conveyor.getRightPosition());

        return dataMap;
    }

    private static Map<String, Object> getSrmSnapShot(SrmCommunicator srm) {
        Map<String, Object> dataMap = new LinkedHashMap<>();
        List<Double> list = null;
        list = srm.getData();
        if (list != null) {
            dataMap.put(ColumnNames.SRM_CHANNEL_2_LEFT, list.get(1));
            dataMap.put(ColumnNames.SRM_CHANNEL_3_RIGHT, list.get(2));
        } else {
            dataMap.put(ColumnNames.SRM_CHANNEL_2_LEFT, null);
            dataMap.put(ColumnNames.SRM_CHANNEL_3_RIGHT, null);
        }

        return dataMap;

    }

    private WriteDataTableUtils() {

    }
}

package at.sunplugged.z600.backend.dataservice.impl;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.osgi.service.log.LogService;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.sunplugged.z600.backend.dataservice.impl.model.DataPoint;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

public class WriteDataTableUtils {

    private WriteDataTableUtils() {

    }

    public static boolean writeTargetConsumptionData(CloseableHttpClient client, String targetName, Double workToAdd) {
        try {
            HttpHelper.addWorkToTarget(client, targetName, workToAdd);
        } catch (IOException e) {
            DataServiceImpl.getLogService().log(LogService.LOG_ERROR, "Failed to add work done data.", e);
            return false;
        }
        return true;
    }

    public static boolean writeHttpDataTable(CloseableHttpClient client, int sessionId, int dataPoint) {

        LogService log = DataServiceImpl.getLogService();

        DataPoint point = new DataPoint();
        point.setDataPoint((long) dataPoint);
        point.setTime(LocalDateTime.now());

        fillDataPoint(point);

        HttpPut dataPut = new HttpPut(DataSavingThread.API_POST_DATAPOINT + "/" + sessionId);

        ObjectMapper mapper = new ObjectMapper();
        try {

            String jsonWrite = mapper.writeValueAsString(point);
            dataPut.setEntity(new StringEntity(jsonWrite, ContentType.APPLICATION_JSON));
            try {
                CloseableHttpResponse response = client.execute(dataPut);
                response.close();
            } catch (IOException e) {
                log.log(LogService.LOG_ERROR, "IO Exception while sending datapoint.", e);
                return false;
            }
        } catch (UnsupportedCharsetException e1) {
            log.log(LogService.LOG_ERROR, "Json Mapping Failed.", e1);
            return false;
        } catch (JsonGenerationException e1) {
            log.log(LogService.LOG_ERROR, "Json Mapping Failed.", e1);
            return false;
        } catch (JsonMappingException e1) {
            log.log(LogService.LOG_ERROR, "Json Mapping Failed.", e1);
            return false;
        } catch (IOException e1) {
            log.log(LogService.LOG_ERROR, "Json Mapping Failed.", e1);
            return false;
        }
        return true;

    }

    private static void fillDataPoint(DataPoint point) {
        MachineStateService machine = DataServiceImpl.getMachineStateService();
        if (machine != null) {
            fillPressureData(machine, point);
            fillPowerData(machine, point);
        }
        ConveyorControlService conveyor = DataServiceImpl.getConveyorControlService();
        if (conveyor != null) {
            fillConveyorData(conveyor, point);
        }

        SrmCommunicator srmCommunicator = DataServiceImpl.getSrmCommunicatorService();
        if (srmCommunicator != null) {
            fillSrmData(srmCommunicator, point);
        }
    }

    private static void fillSrmData(SrmCommunicator srmCommunicator, DataPoint point) {
        List<Double> list = null;
        list = srmCommunicator.getData();
        if (list != null) {
            point.setSrmChannelTwoLeft(list.get(1));
            point.setSrmChannelThreeRight(list.get(2));
        } else {
            point.setSrmChannelThreeRight(null);
            point.setSrmChannelTwoLeft(null);
        }

    }

    private static void fillConveyorData(ConveyorControlService conveyor, DataPoint point) {
        point.setConveyorMode(conveyor.getActiveMode().name());
        point.setConveyorSpeedCombined(conveyor.getCurrentSpeed());
        point.setConveyorSpeedLeft(conveyor.getSpeedLogger().getLeftSpeed());
        point.setConveyorSpeedRight(conveyor.getSpeedLogger().getRightSpeed());
        point.setConveyorSpeedSetpoint(conveyor.getSetpointSpeed());
        point.setConveyorEngineLeftMaximum((double) conveyor.getEngineOne().getCurrentMaximumSpeed());
        point.setConveyorEngineRightMaximum((double) conveyor.getEngineTwo().getCurrentMaximumSpeed());
        point.setConveyorPositionCombined(conveyor.getPosition());
        point.setConveyorPositionLeft(conveyor.getLeftPosition());
        point.setConveyorPositionRight(conveyor.getRightPosition());
    }

    private static void fillPowerData(MachineStateService machine, DataPoint point) {
        PowerSourceRegistry powerSourceRegistry = machine.getPowerSourceRegistry();
        PowerSource currentSource;

        // Pinnalce
        currentSource = powerSourceRegistry.getPowerSource(PowerSourceId.PINNACLE);
        point.setPinnaclePower(currentSource.getPower());
        point.setPinnaclePowerSetpoint(currentSource.getSetPointpower());
        point.setPinnacleVoltage(currentSource.getVoltage());
        point.setPinnacleCurrent(currentSource.getCurrent());

        // SSV ONE
        currentSource = powerSourceRegistry.getPowerSource(PowerSourceId.SSV1);
        point.setSsvOnePower(currentSource.getPower());
        point.setSsvOnePowerSetpoint(currentSource.getSetPointpower());
        point.setSsvOneVoltage(currentSource.getVoltage());
        point.setSsvOneCurrent(currentSource.getCurrent());

        // SSV TWO
        currentSource = powerSourceRegistry.getPowerSource(PowerSourceId.SSV2);
        point.setSsvTwoPower(currentSource.getPower());
        point.setSsvTwoPowerSetpoint(currentSource.getSetPointpower());
        point.setSsvTwoVoltage(currentSource.getVoltage());
        point.setSsvTwoCurrent(currentSource.getCurrent());
    }

    private static void fillPressureData(MachineStateService machine, DataPoint point) {
        PressureMeasurement pressureInterface = machine.getPressureMeasurmentControl();

        point.setPressureTurboPump(pressureInterface.getCurrentValue(PressureMeasurementSite.TURBO_PUMP));
        point.setPressureChamber(pressureInterface.getCurrentValue(PressureMeasurementSite.CHAMBER));
        point.setPressureCryoOne(pressureInterface.getCurrentValue(PressureMeasurementSite.CRYO_PUMP_ONE));
        point.setPressureCryoTwo(pressureInterface.getCurrentValue(PressureMeasurementSite.CRYO_PUMP_TWO));
        point.setCurrentGasFlowSccm(machine.getGasFlowControl().getCurrentGasFlowInSccm());
    }

}

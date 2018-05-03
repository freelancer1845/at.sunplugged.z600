package at.sunplugged.z600.backend.dataservice.impl.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DataPoint {

    private SessionPK sessionPK;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime time;

    private Double pressureCryoOne;

    private Double pressureCryoTwo;

    private Double pressureChamber;

    private Double pressureTurboPump;

    private Double currentGasFlowSccm;

    private Double pinnaclePower;

    private Double pinnaclePowerSetpoint;

    private Double pinnacleCurrent;

    private Double pinnacleVoltage;

    private Double ssvOnePower;

    private Double ssvOnePowerSetpoint;

    private Double ssvOneCurrent;

    private Double ssvOneVoltage;

    private Double ssvTwoPower;

    private Double ssvTwoPowerSetpoint;

    private Double ssvTwoCurrent;

    private Double ssvTwoVoltage;

    private Double conveyorSpeedCombined;

    private Double conveyorSpeedLeft;

    private Double conveyorSpeedRight;

    private Double conveyorSpeedSetpoint;

    private Double conveyorEngineLeftMaximum;

    private Double conveyorEngineRightMaximum;

    private Double conveyorPositionLeft;

    private Double conveyorPositionRight;

    private Double conveyorPositionCombined;

    private String conveyorMode;

    private Double srmChannelTwoLeft;

    private Double srmChannelThreeRight;

    public static class SessionPK implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -8491720672704257250L;

        protected Long sessionId;

        protected Long dataPoint;

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }

        public Long getDataPoint() {
            return dataPoint;
        }

        public void setDataPoint(Long dataPoint) {
            this.dataPoint = dataPoint;
        }

    }

    public SessionPK getSessionPK() {
        return sessionPK;
    }

    public void setSessionPK(SessionPK sessionPK) {
        this.sessionPK = sessionPK;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Double getPressureCryoOne() {
        return pressureCryoOne;
    }

    public void setPressureCryoOne(Double pressureCryoOne) {
        this.pressureCryoOne = pressureCryoOne;
    }

    public Double getPressureCryoTwo() {
        return pressureCryoTwo;
    }

    public void setPressureCryoTwo(Double pressureCryoTwo) {
        this.pressureCryoTwo = pressureCryoTwo;
    }

    public Double getPressureChamber() {
        return pressureChamber;
    }

    public void setPressureChamber(Double pressureChamber) {
        this.pressureChamber = pressureChamber;
    }

    public Double getPressureTurboPump() {
        return pressureTurboPump;
    }

    public void setPressureTurboPump(Double pressureTurboPump) {
        this.pressureTurboPump = pressureTurboPump;
    }

    public Double getCurrentGasFlowSccm() {
        return currentGasFlowSccm;
    }

    public void setCurrentGasFlowSccm(Double currentGasFlowSccm) {
        this.currentGasFlowSccm = currentGasFlowSccm;
    }

    public Double getPinnaclePower() {
        return pinnaclePower;
    }

    public void setPinnaclePower(Double pinnaclePower) {
        this.pinnaclePower = pinnaclePower;
    }

    public Double getPinnaclePowerSetpoint() {
        return pinnaclePowerSetpoint;
    }

    public void setPinnaclePowerSetpoint(Double pinnaclePowerSetpoint) {
        this.pinnaclePowerSetpoint = pinnaclePowerSetpoint;
    }

    public Double getPinnacleCurrent() {
        return pinnacleCurrent;
    }

    public void setPinnacleCurrent(Double pinnacleCurrent) {
        this.pinnacleCurrent = pinnacleCurrent;
    }

    public Double getPinnacleVoltage() {
        return pinnacleVoltage;
    }

    public void setPinnacleVoltage(Double pinnacleVoltage) {
        this.pinnacleVoltage = pinnacleVoltage;
    }

    public Double getSsvOnePower() {
        return ssvOnePower;
    }

    public void setSsvOnePower(Double ssvOnePower) {
        this.ssvOnePower = ssvOnePower;
    }

    public Double getSsvOnePowerSetpoint() {
        return ssvOnePowerSetpoint;
    }

    public void setSsvOnePowerSetpoint(Double ssvOnePowerSetpoint) {
        this.ssvOnePowerSetpoint = ssvOnePowerSetpoint;
    }

    public Double getSsvOneCurrent() {
        return ssvOneCurrent;
    }

    public void setSsvOneCurrent(Double ssvOneCurrent) {
        this.ssvOneCurrent = ssvOneCurrent;
    }

    public Double getSsvOneVoltage() {
        return ssvOneVoltage;
    }

    public void setSsvOneVoltage(Double ssvOneVoltage) {
        this.ssvOneVoltage = ssvOneVoltage;
    }

    public Double getSsvTwoPower() {
        return ssvTwoPower;
    }

    public void setSsvTwoPower(Double ssvTwoPower) {
        this.ssvTwoPower = ssvTwoPower;
    }

    public Double getSsvTwoPowerSetpoint() {
        return ssvTwoPowerSetpoint;
    }

    public void setSsvTwoPowerSetpoint(Double ssvTwoPowerSetpoint) {
        this.ssvTwoPowerSetpoint = ssvTwoPowerSetpoint;
    }

    public Double getSsvTwoCurrent() {
        return ssvTwoCurrent;
    }

    public void setSsvTwoCurrent(Double ssvTwoCurrent) {
        this.ssvTwoCurrent = ssvTwoCurrent;
    }

    public Double getSsvTwoVoltage() {
        return ssvTwoVoltage;
    }

    public void setSsvTwoVoltage(Double ssvTwoVoltage) {
        this.ssvTwoVoltage = ssvTwoVoltage;
    }

    public Double getConveyorSpeedCombined() {
        return conveyorSpeedCombined;
    }

    public void setConveyorSpeedCombined(Double conveyorSpeedCombined) {
        this.conveyorSpeedCombined = conveyorSpeedCombined;
    }

    public Double getConveyorSpeedLeft() {
        return conveyorSpeedLeft;
    }

    public void setConveyorSpeedLeft(Double conveyorSpeedLeft) {
        this.conveyorSpeedLeft = conveyorSpeedLeft;
    }

    public Double getConveyorSpeedRight() {
        return conveyorSpeedRight;
    }

    public void setConveyorSpeedRight(Double conveyorSpeedRight) {
        this.conveyorSpeedRight = conveyorSpeedRight;
    }

    public Double getConveyorSpeedSetpoint() {
        return conveyorSpeedSetpoint;
    }

    public void setConveyorSpeedSetpoint(Double conveyorSpeedSetpoint) {
        this.conveyorSpeedSetpoint = conveyorSpeedSetpoint;
    }

    public Double getConveyorEngineLeftMaximum() {
        return conveyorEngineLeftMaximum;
    }

    public void setConveyorEngineLeftMaximum(Double conveyorEngineLeftMaximum) {
        this.conveyorEngineLeftMaximum = conveyorEngineLeftMaximum;
    }

    public Double getConveyorEngineRightMaximum() {
        return conveyorEngineRightMaximum;
    }

    public void setConveyorEngineRightMaximum(Double conveyorEngineRightMaximum) {
        this.conveyorEngineRightMaximum = conveyorEngineRightMaximum;
    }

    public Double getConveyorPositionLeft() {
        return conveyorPositionLeft;
    }

    public void setConveyorPositionLeft(Double conveyorPositionLeft) {
        this.conveyorPositionLeft = conveyorPositionLeft;
    }

    public Double getConveyorPositionRight() {
        return conveyorPositionRight;
    }

    public void setConveyorPositionRight(Double conveyorPositionRight) {
        this.conveyorPositionRight = conveyorPositionRight;
    }

    public Double getConveyorPositionCombined() {
        return conveyorPositionCombined;
    }

    public void setConveyorPositionCombined(Double conveyorPositionCombined) {
        this.conveyorPositionCombined = conveyorPositionCombined;
    }

    public String getConveyorMode() {
        return conveyorMode;
    }

    public void setConveyorMode(String conveyorMode) {
        this.conveyorMode = conveyorMode;
    }

    public Double getSrmChannelTwoLeft() {
        return srmChannelTwoLeft;
    }

    public void setSrmChannelTwoLeft(Double srmChannelTwoLeft) {
        this.srmChannelTwoLeft = srmChannelTwoLeft;
    }

    public Double getSrmChannelThreeRight() {
        return srmChannelThreeRight;
    }

    public void setSrmChannelThreeRight(Double srmChannelThreeRight) {
        this.srmChannelThreeRight = srmChannelThreeRight;
    }

}

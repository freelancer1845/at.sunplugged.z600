package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.SettingsIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.KathodeControl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerControl.PowerUnit;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FutureEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.exceptions.InvalidKathodeStateException;
import at.sunplugged.z600.mbt.api.MbtService;

/**
 * Implementing class of the {@linkplain KathodeControl} Interface.
 * 
 * @author Jascha Riedel
 *
 */
public class KathodeControlImpl implements KathodeControl, MachineEventHandler {

    private MachineStateService machineStateService;

    private MbtService mbtService;

    private LogService logService;

    private SettingsService settings;

    private PowerControlThread powerControlThread;

    private double kathodeOneSetpoint = 0;

    private double kathodeOnePowerSetpoint = 0;

    private boolean kathodeOneRunning = false;

    private double kathodeTwoSetpoint = 0;

    private double kathodeTwoPowerSetpoint = 0;

    private boolean kathodeTwoRunning = false;

    private double kathodeThreeSetpoint = 0;

    private double kathodeThreePowerSetpoint = 0;

    private boolean kathodeThreeRunning = false;

    public KathodeControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        this.mbtService = MachineStateServiceImpl.getMbtService();
        this.logService = MachineStateServiceImpl.getLogService();
        this.settings = MachineStateServiceImpl.getSettingsService();
        this.powerControlThread = new PowerControlThread();

        powerControlThread.start();
    }

    @Override
    public void setPowerSetpoint(Kathode kathode, double power) {
        switch (kathode) {
        case KATHODE_ONE:
            kathodeOnePowerSetpoint = power;
            break;
        case KATHODE_TWO:
            kathodeTwoPowerSetpoint = power;
            break;
        case KATHODE_THREE:
            kathodeThreePowerSetpoint = power;
            break;
        default:
            logService.log(LogService.LOG_DEBUG, "No Setpoint Variable for kathode: " + kathode.name());
        }
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType().equals(Type.ANALOG_INPUT_CHANGED)) {
            safetyChecks(event);
        }
    }

    @Override
    public void startKathode(Kathode kathode) throws InvalidKathodeStateException {
        if (powerControlThread.isRunning() == false) {
            throw new InvalidKathodeStateException("Power Control Thread is not running. Try restarting the program.");
        }
        switch (kathode) {
        case KATHODE_ONE:
            startKathodeOne();
            kathodeOneRunning = true;
            break;
        }
    }

    @Override
    public void stopKathode(Kathode kathode) {
        switch (kathode) {
        case KATHODE_ONE:
            stopKathodeOne();
            kathodeOneRunning = false;
            break;
        }

    }

    private void startKathodeOne() throws InvalidKathodeStateException {
        logService.log(LogService.LOG_INFO, "Starting kathode one...");

        if (!checkVacuum(Kathode.KATHODE_ONE)) {
            throw new InvalidKathodeStateException("Vaccum not ready to start kathode.");
        }
        if (machineStateService.getGasFlowControl().getCurrentGasFlowValue() <= 1) {
            throw new InvalidKathodeStateException(
                    "There is no gas flowing through the chamber. Won't start kathode one.");
        }

        checkWaterKathodeOne(); // throws InvalidKathodeStateException if
                                // failed.

        logService.log(LogService.LOG_INFO, "Vacuum and Water are ok. Starting PowerSupply Pinnacle.");

        setPowerSetpoint(Kathode.KATHODE_ONE,
                Double.valueOf(settings.getProperty(SettingsIds.INITIAL_POWER_KATHODE_ONE)));
        try {
            mbtService.writeDigOut(DigitalOutput.PINNACLE_REG_ONE.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.PINNACLE_REG_TWO.getAddress(), true);
        } catch (IOException e) {
            stopKathode(Kathode.KATHODE_ONE);
            throw new InvalidKathodeStateException("Failed to set pinnacle reg.", e);
        }

        if (machineStateService.getPowerControl().getState(PowerUnit.PINNACLE)) {
            logService.log(LogService.LOG_DEBUG, "Pinnacle already started");
        } else {
            machineStateService.getPowerControl().setInterlock(PowerUnit.PINNACLE, true);
            machineStateService.getPowerControl().start(PowerUnit.PINNACLE);
        }

    }

    private void stopKathodeOne() {

    }

    private void checkWaterKathodeOne() throws InvalidKathodeStateException {
        if (!machineStateService.getDigitalInputState(DigitalInput.WATER_KATH_ONE_ON)) {
            logService.log(LogService.LOG_DEBUG, "Water for kathode one was not on.");
            try {
                mbtService.writeDigOut(DigitalOutput.WATER_KATH_ONE.getAddress(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FutureEvent waterKathodeOneEvent = new FutureEvent(machineStateService,
                    new MachineStateEvent(Type.DIGITAL_INPUT_CHANGED, DigitalInput.WATER_KATH_ONE_ON, true));
            try {
                waterKathodeOneEvent.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new InvalidKathodeStateException("Failed to start water for kathode one", e);
            }
        }
    }

    private boolean checkVacuum(Kathode kathode) {
        double currentPressure = machineStateService.getPressureMeasurmentControl()
                .getCurrentValue(PressureMeasurementSite.CHAMBER);
        if (currentPressure < Double.valueOf(settings.getProperty(SettingsIds.VACUUM_LOWER_LIMIT_MBAR))) {
            logService.log(LogService.LOG_ERROR,
                    "Won't start kathode: \"" + kathode.name() + "\". Pressure is too low.");
            return false;
        } else if (currentPressure > Double.valueOf(settings.getProperty(SettingsIds.VACUUM_UPPER_LIMIT_MBAR))) {
            logService.log(LogService.LOG_ERROR,
                    "Won't start kathode: \"" + kathode.name() + "\". Pressure is too high.");
            return false;
        }
        return true;
    }

    @Override
    public double getVoltageAtKathode(Kathode kathode) {
        int analogVoltageValue = machineStateService.getAnalogInputState(kathode.getVoltageInput());

        return convertVoltageKathodeSpecific(kathode, analogVoltageValue);
    }

    @Override
    public double getCurrentAtKathode(Kathode kathode) {
        switch (kathode) {
        case KATHODE_ONE:
            return getPowerAtKathode(kathode) * 1000 / getVoltageAtKathode(kathode);
        case KATHODE_TWO:
        case KATHODE_THREE:
            int analogCurrentValue = machineStateService.getAnalogInputState(kathode.getCurrentInput());
            return convertAnalogCurrent(analogCurrentValue);
        }
        return 0;
    }

    @Override
    public double getPowerAtKathode(Kathode kathode) {
        switch (kathode) {
        case KATHODE_ONE:
            int analogPowerValue = machineStateService.getAnalogInputState(kathode.getCurrentInput());
            return analogPowerValue / 4095.0 * 6;
        case KATHODE_TWO:
        case KATHODE_THREE:
            return getCurrentAtKathode(kathode) * getVoltageAtKathode(kathode) / 1000;
        }
        return 0;
    }

    private double convertVoltageKathodeSpecific(Kathode kathode, int analogVoltageValue) {
        switch (kathode) {
        case KATHODE_ONE:
            return analogVoltageValue / 4095.0 * 1500;
        case KATHODE_TWO:
            return analogVoltageValue / 4095.0 * 1000;
        case KATHODE_THREE:
            return analogVoltageValue / 4095.0 * 1000;
        default:
            return -1;
        }
    }

    private double convertAnalogCurrent(int analogCurrentValue) {
        return analogCurrentValue / 4095.0 * 35;
    }

    private void safetyChecks(MachineStateEvent event) {

        // TODO :
        // switch (event.getAnalogInput()) {
        // case VOLTAGE_KATHODE_ONE:
        // case CURRENT_KATHODE_ONE:
        // double currentPower = getPowerAtKathode(Kathode.KATHODE_ONE);
        //
        // }
    }

    private class PowerControlThread extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            this.setName("Kathode-Power-Control-Thread");

            running = true;
            while (running) {
                try {
                    if (kathodeOneRunning == true) {
                        updateKathodeOne();
                    }
                } catch (IOException e) {
                    running = false;
                    logService.log(LogService.LOG_ERROR, "Kathode Control Thread failed!", e);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logService.log(LogService.LOG_DEBUG, "Kathode Control Thread wait was interrupted");
                    e.printStackTrace();
                }
            }

        }

        public void stopSoft() {
            running = false;
        }

        public boolean isRunning() {
            return running;
        }

        private void updateKathodeOne() throws IOException {
            double currentPower = getPowerAtKathode(Kathode.KATHODE_ONE);

            if (kathodeOnePowerSetpoint > currentPower + 0.01) {
                kathodeOneSetpoint += Double.valueOf(settings.getProperty(SettingsIds.DELTA_POWER_KATHODE_ONE));
            } else if (kathodeOnePowerSetpoint < currentPower - 0.01) {
                kathodeOneSetpoint -= Double.valueOf(settings.getProperty(SettingsIds.DELTA_POWER_KATHODE_ONE));
            }
            mbtService.writeOutputRegister(Kathode.KATHODE_ONE.getAnalogOutput().getAddress(),
                    (int) (kathodeOneSetpoint * 4095.0 / 5));
        }

    }

}

package at.sunplugged.z600.core.machinestate.impl.kathode;

import java.io.IOException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.SettingsIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.KathodeControl.Kathode;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerControl.PowerUnit;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.exceptions.InvalidKathodeStateException;
import at.sunplugged.z600.mbt.api.MbtService;

public class KathodeOne extends AbstractKathode {

    public KathodeOne(LogService logService, MachineStateService machineStateService, SettingsService settings,
            MbtService mbtService) {
        super(logService, machineStateService, settings, mbtService);
    }

    @Override
    public void startKathode() throws InvalidKathodeStateException {
        logService.log(LogService.LOG_INFO, "Starting kathode one...");

        if (!checkVacuum(Kathode.KATHODE_ONE)) {
            throw new InvalidKathodeStateException("Vaccum not ready to start kathode.");
        }
        if (machineStateService.getGasFlowControl().getCurrentGasFlowValue() <= 1) {
            throw new InvalidKathodeStateException(
                    "There is no gas flowing through the chamber. Won't start kathode one.");
        }

        checkWater(DigitalInput.WATER_KATH_ONE_ON, DigitalOutput.WATER_KATH_ONE); // throws
                                                                                  // InvalidKathodeStateException
                                                                                  // if
        // failed.

        logService.log(LogService.LOG_INFO, "Vacuum and Water are ok. Starting PowerSupply Pinnacle.");

        setPowerSetpoint(Double.valueOf(settings.getProperty(SettingsIds.INITIAL_POWER_KATHODE_ONE)));
        try {
            mbtService.writeDigOut(DigitalOutput.PINNACLE_REG_ONE.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.PINNACLE_REG_TWO.getAddress(), true);
        } catch (IOException e) {
            stopKathode();
            throw new InvalidKathodeStateException("Failed to set pinnacle reg.", e);
        }

        if (machineStateService.getPowerControl().getState(PowerUnit.PINNACLE)) {
            logService.log(LogService.LOG_DEBUG, "Pinnacle already started");
        } else {
            machineStateService.getPowerControl().setInterlock(PowerUnit.PINNACLE, true);
            machineStateService.getPowerControl().start(PowerUnit.PINNACLE);
        }
    }

    @Override
    public void stopKathode() {
        logService.log(LogService.LOG_INFO, "Stopping kathode one...");

        setPowerSetpoint(0.0);

        machineStateService.getPowerControl().stop(PowerUnit.PINNACLE);
    }

    @Override
    public double getPowerAtKathode() {
        int analogPowerValue = machineStateService.getAnalogInputState(Kathode.KATHODE_ONE.getCurrentInput());
        return analogPowerValue / 4095.0 * 6;
    }

    @Override
    public double getCurrentAtKathode() {
        return getPowerAtKathode() * 1000 / getVoltageAtKathode();
    }

    @Override
    public double getVoltageAtKathode() {
        int analogVoltageValue = machineStateService.getAnalogInputState(Kathode.KATHODE_ONE.getVoltageInput());
        return analogVoltageValue / 4095.0 * 1500;
    }

    @Override
    public void tickPowerControl() throws IOException {
        try {
            checkWater(DigitalInput.WATER_KATH_ONE_ON, DigitalOutput.WATER_KATH_ONE);
        } catch (InvalidKathodeStateException e) {
            stopKathode();
        }

        double currentPower = getPowerAtKathode();

        if (getPowerSetpoint() > currentPower + 0.01) {
            setCurrentSetpoint(
                    getCurrentSetpoint() + Double.valueOf(settings.getProperty(SettingsIds.DELTA_CURRENT_KATHODE_ONE)));
        } else if (getPowerSetpoint() < currentPower - 0.01) {
            setCurrentSetpoint(
                    getCurrentSetpoint() - Double.valueOf(settings.getProperty(SettingsIds.DELTA_CURRENT_KATHODE_ONE)));
        }
        mbtService.writeOutputRegister(Kathode.KATHODE_ONE.getAnalogOutput().getAddress(),
                (int) (getCurrentSetpoint() * 4095.0 / 5));
    }

}

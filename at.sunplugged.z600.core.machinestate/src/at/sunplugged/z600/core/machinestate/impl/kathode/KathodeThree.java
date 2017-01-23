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

public class KathodeThree extends AbstractKathode {

    public KathodeThree(LogService logService, MachineStateService machineStateService, SettingsService settings,
            MbtService mbtService) {
        super(logService, machineStateService, settings, mbtService);
    }

    @Override
    public void startKathode() throws InvalidKathodeStateException {
        logService.log(LogService.LOG_INFO, "Starting kathode three...");

        if (!checkVacuum(Kathode.KATHODE_THREE)) {
            throw new InvalidKathodeStateException("Vaccum not ready to start kathode.");
        }
        if (machineStateService.getGasFlowControl().getCurrentGasFlowValue() <= 1) {
            throw new InvalidKathodeStateException(
                    "There is no gas flowing through the chamber. Won't start kathode three.");
        }

        checkWater(DigitalInput.WATER_KATH_THREE_ON, DigitalOutput.WATER_KATH_THREE); // throws
        // InvalidKathodeStateException
        // if
        // failed.

        logService.log(LogService.LOG_INFO, "Vacuum and Water are ok. Starting PowerSupply SVV2.");

        setCurrentSetpoint(Double.valueOf(settings.getProperty(SettingsIds.INITIAL_CURRENT_KATHODE_THREE)));
        machineStateService.getPowerControl().start(PowerUnit.SSV_TWO);
    }

    @Override
    public void stopKathode() {
        logService.log(LogService.LOG_INFO, "Stopping kathode three...");

        setPowerSetpoint(0.0);

        machineStateService.getPowerControl().stop(PowerUnit.SSV_TWO);
    }

    @Override
    public double getPowerAtKathode() {
        return getCurrentAtKathode() * getVoltageAtKathode() / 1000;
    }

    @Override
    public double getCurrentAtKathode() {
        int analogCurrentValue = machineStateService.getAnalogInputState(Kathode.KATHODE_THREE.getCurrentInput());
        return analogCurrentValue / 4095.0 * 35;
    }

    @Override
    public double getVoltageAtKathode() {
        int analogVoltageValue = machineStateService.getAnalogInputState(Kathode.KATHODE_THREE.getVoltageInput());
        return analogVoltageValue / 4095.0 * 1000;
    }

    @Override
    public void tickPowerControl() throws IOException {
        try {
            checkWater(DigitalInput.WATER_KATH_THREE_ON, DigitalOutput.WATER_KATH_THREE);
        } catch (InvalidKathodeStateException e) {
            stopKathode();
        }
        if (getPowerSetpoint() > (getPowerAtKathode() + 0.05)) {
            setCurrentSetpoint(getCurrentSetpoint()
                    + Double.valueOf(settings.getProperty(SettingsIds.DELTA_CURRENT_KATHODE_THREE)));
        } else if (getPowerSetpoint() < (getPowerAtKathode() - 0.05)) {
            setCurrentSetpoint(getCurrentSetpoint()
                    + Double.valueOf(settings.getProperty(SettingsIds.DELTA_CURRENT_KATHODE_THREE)));
        }
        int outputValue = 0;
        if (getCurrentSetpoint() > 30) {
            outputValue = 30;
        } else if (getCurrentSetpoint() < 0) {
            outputValue = 0;
        } else {
            outputValue = (int) (getCurrentSetpoint() * 4095.0 / 30);
        }

        mbtService.writeOutputRegister(Kathode.KATHODE_THREE.getAnalogOutput().getAddress(), outputValue);
    }

}

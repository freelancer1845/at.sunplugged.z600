package at.sunplugged.z600.core.machinestate.impl.kathode;

import java.io.IOException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.KathodeControl.Kathode;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerControl.PowerUnit;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.exceptions.InvalidKathodeStateException;
import at.sunplugged.z600.mbt.api.MbtService;

public class KathodeTwo extends AbstractKathode {

    public KathodeTwo(LogService logService, MachineStateService machineStateService, SettingsService settings,
            MbtService mbtService) {
        super(logService, machineStateService, settings, mbtService);
    }

    @Override
    public void startKathode() throws InvalidKathodeStateException {
        logService.log(LogService.LOG_INFO, "Starting kathode two...");

        if (!checkVacuum(Kathode.KATHODE_TWO)) {
            throw new InvalidKathodeStateException("Vaccum not ready to start kathode.");
        }
        if (machineStateService.getGasFlowControl().getCurrentGasFlowValue() <= 1) {
            throw new InvalidKathodeStateException(
                    "There is no gas flowing through the chamber. Won't start kathode one.");
        }

        checkWater(DigitalInput.WATER_KATH_TWO_ON, DigitalOutput.WATER_KATH_TWO); // throws
                                                                                  // InvalidKathodeStateException
                                                                                  // if
        // failed.

        logService.log(LogService.LOG_INFO, "Vacuum and Water are ok. Starting PowerSupply Pinnacle.");

        setCurrentSetpoint(Double.valueOf(settings.getProperty(ParameterIds.INITIAL_CURRENT_KATHODE_TWO)));
        machineStateService.getPowerControl().start(PowerUnit.SSV_ONE);

    }

    @Override
    public void stopKathode() {
        logService.log(LogService.LOG_INFO, "Stopping kathode two...");

        setPowerSetpoint(0.0);

        machineStateService.getPowerControl().stop(PowerUnit.SSV_ONE);
    }

    @Override
    public double getPowerAtKathode() {
        return getCurrentAtKathode() * getVoltageAtKathode() / 1000;
    }

    @Override
    public double getCurrentAtKathode() {
        int analogCurrentValue = machineStateService.getAnalogInputState(Kathode.KATHODE_TWO.getCurrentInput());
        return analogCurrentValue / 4095.0 * 35;
    }

    @Override
    public double getVoltageAtKathode() {
        int analogVoltageValue = machineStateService.getAnalogInputState(Kathode.KATHODE_TWO.getVoltageInput());
        return analogVoltageValue / 4095.0 * 1000;
    }

    @Override
    public void tickPowerControl() throws IOException {
        try {
            checkWater(DigitalInput.WATER_KATH_TWO_ON, DigitalOutput.WATER_KATH_TWO);
        } catch (InvalidKathodeStateException e) {
            stopKathode();
        }
        if (getPowerSetpoint() > (getPowerAtKathode() + 0.05)) {
            setCurrentSetpoint(getCurrentSetpoint()
                    + Double.valueOf(settings.getProperty(ParameterIds.DELTA_CURRENT_KATHODE_TWO)));
        } else if (getPowerSetpoint() < (getPowerAtKathode() - 0.05)) {
            setCurrentSetpoint(getCurrentSetpoint()
                    + Double.valueOf(settings.getProperty(ParameterIds.DELTA_CURRENT_KATHODE_TWO)));
        }
        int outputValue = 0;
        if (getCurrentSetpoint() > 30) {
            outputValue = 30;
        } else if (getCurrentSetpoint() < 0) {
            outputValue = 0;
        } else {
            outputValue = (int) (getCurrentSetpoint() * 4095.0 / 30);
        }

        mbtService.writeOutputRegister(Kathode.KATHODE_TWO.getAnalogOutput().getAddress(), outputValue);
    }

}

package at.sunplugged.z600.core.machinestate.impl.kathode;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.KathodeControl.Kathode;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FutureEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.exceptions.InvalidKathodeStateException;
import at.sunplugged.z600.mbt.api.MbtService;

public abstract class AbstractKathode implements KathodeInterface {

    protected LogService logService;

    protected MachineStateService machineStateService;

    protected SettingsService settings;

    protected MbtService mbtService;

    private double powerSetpoint = 0;

    private double currentSetpoint = 0;

    public AbstractKathode(LogService logService, MachineStateService machineStateService, SettingsService settings,
            MbtService mbtService) {
        this.logService = logService;
        this.machineStateService = machineStateService;
        this.settings = settings;
        this.mbtService = mbtService;
    }

    @Override
    public void setPowerSetpoint(Double power) {
        this.powerSetpoint = power;
    }

    @Override
    public double getPowerSetpoint() {
        return powerSetpoint;
    }

    protected double getCurrentSetpoint() {
        return currentSetpoint;
    }

    protected void setCurrentSetpoint(double currentSetpoint) {
        this.currentSetpoint = currentSetpoint;
    }

    protected boolean checkVacuum(Kathode kathode) {
        double currentPressure = machineStateService.getPressureMeasurmentControl()
                .getCurrentValue(PressureMeasurementSite.CHAMBER);
        if (currentPressure < Double.valueOf(settings.getProperty(ParameterIds.VACUUM_LOWER_LIMIT_MBAR))) {
            logService.log(LogService.LOG_ERROR,
                    "Won't start kathode: \"" + kathode.name() + "\". Pressure is too low.");
            return false;
        } else if (currentPressure > Double.valueOf(settings.getProperty(ParameterIds.VACUUM_UPPER_LIMIT_MBAR))) {
            logService.log(LogService.LOG_ERROR,
                    "Won't start kathode: \"" + kathode.name() + "\". Pressure is too high.");
            return false;
        }
        return true;
    }

    protected void checkWater(DigitalInput waterInput, DigitalOutput waterOutput) throws InvalidKathodeStateException {
        if (!machineStateService.getDigitalInputState(waterInput)) {
            logService.log(LogService.LOG_DEBUG, "Water for kathode one was not on.");
            try {
                mbtService.writeDigOut(waterOutput.getAddress(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FutureEvent waterKathodeOneEvent = new FutureEvent(machineStateService,
                    new MachineStateEvent(Type.DIGITAL_INPUT_CHANGED, waterInput, true));
            try {
                waterKathodeOneEvent.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException e) {
                throw new InvalidKathodeStateException("Failed to start " + waterOutput.name(), e);
            }
        }
    }

}

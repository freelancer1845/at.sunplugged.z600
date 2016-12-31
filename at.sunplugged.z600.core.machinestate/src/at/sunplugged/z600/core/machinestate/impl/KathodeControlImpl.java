package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.core.machinestate.api.KathodeControl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerControl.PowerUnit;
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

    private static final double KATHODE_ONE_MAX = 10.0;

    private static final double KATHODE_ONE_INITIAL_CURRENT = 1.5;

    private MachineStateService machineStateService;

    private MbtService mbtService;

    private LogService logService;

    private double kathodeOneSetpoint = 0;

    private double kathodeTwoSetpoint = 0;

    private double kathodeThreeSetpoint = 0;

    public KathodeControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        this.mbtService = MachineStateServiceImpl.getMbtService();
        this.logService = MachineStateServiceImpl.getLogService();
    }

    @Override
    public void setSetPoint(Kathode kathode, double value) {
        switch (kathode) {
        case KATHODE_ONE:
            kathodeOneSetpoint = value;
            break;
        case KATHODE_TWO:
            kathodeTwoSetpoint = value;
            break;
        case KATHODE_THREE:
            kathodeThreeSetpoint = value;
            break;
        default:
            logService.log(LogService.LOG_DEBUG, "No Setpoint Variable for kathode: " + kathode.name());
        }
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    public void startKathode(Kathode kathode) throws InvalidKathodeStateException {
        switch (kathode) {
        case KATHODE_ONE:
            startKathodeOne();
            break;
        }
    }

    @Override
    public void stopKathode(Kathode kathode) {
        // TODO Auto-generated method stub

    }

    private void startKathodeOne() throws InvalidKathodeStateException {
        logService.log(LogService.LOG_DEBUG, "Starting kathode one.");

        if (!checkVaccuum()) {
            throw new InvalidKathodeStateException("Vaccum not ready to start kathode.");
        }

        checkWaterKathodeOne(); // throws InvalidKathodeStateException if
                                // failed.
        logService.log(LogService.LOG_DEBUG, "Vaccuum and Water are ok. Starting PowerSupply Pinnacle.");
        if (machineStateService.getPowerControl().getState(PowerUnit.PINNACLE)) {
            logService.log(LogService.LOG_DEBUG, "Pinnacle already started");
        } else {
            machineStateService.getPowerControl().setInterlock(PowerUnit.PINNACLE, true);
            machineStateService.getPowerControl().start(PowerUnit.PINNACLE);
        }

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

    private void stopKathodeOne() {

    }

    private boolean checkVaccuum() {
        // TODO Needs to be implemented. Gasflow not implemented yet.
        return false;
    }

    private int kathodeOneConverter(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > KATHODE_ONE_MAX) {
            return kathodeOneConverter(KATHODE_ONE_MAX);
        }
        return (int) (value * 4095 / KATHODE_ONE_MAX);
    }

}

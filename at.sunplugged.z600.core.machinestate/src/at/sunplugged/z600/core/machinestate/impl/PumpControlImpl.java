package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PumpControl;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PumpStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.mbt.api.MbtService;

public class PumpControlImpl implements PumpControl, MachineEventHandler {

    private final MachineStateService machineStateService;

    private final MbtService mbtService;

    private final LogService logService;

    private PumpState pumpOneState = PumpState.OFF;

    private PumpState pumpRootState = PumpState.OFF;

    private PumpState pumpTwoState = PumpState.OFF;

    private PumpState turboPumpState = PumpState.OFF;

    public PumpControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        machineStateService.registerMachineEventHandler(this);
        this.mbtService = MachineStateServiceImpl.getMbtService();
        this.logService = MachineStateServiceImpl.getLogService();
    }

    @Override
    public void startPump(Pumps pump) {
        try {
            mbtService.writeDigOut(pump.getDigitalOutput().getAddress(), true);
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Failed to start Pump: " + pump.toString(), e);
        }
    }

    @Override
    public void stopPump(Pumps pump) {
        try {
            mbtService.writeDigOut(pump.getDigitalOutput().getAddress(), false);
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Failed to stop Pump: " + pump.toString(), e);
        }
    }

    @Override
    public PumpState getState(Pumps pump) {
        switch (pump) {
        case PRE_PUMP_ONE:
            return pumpOneState;
        case PRE_PUMP_TWO:
            return pumpTwoState;
        case TURBO_PUMP:
            return turboPumpState;
        case PRE_PUMP_ROOTS:
            return pumpRootState;
        default:
            logService.log(LogService.LOG_DEBUG, "State of unkown pump requested: " + pump.name());
            return null;
        }
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType().equals(MachineStateEvent.Type.DIGITAL_INPUT_CHANGED)) {
            DigitalInput digitalInput = event.getDigitalInput();
            if (digitalInput.equals(Pumps.PRE_PUMP_ONE.getDigitalInput())) {
                handleNormalPumpChange(Pumps.PRE_PUMP_ONE, (boolean) event.getValue(), pumpOneState);
            } else if (digitalInput.equals(Pumps.PRE_PUMP_TWO.getDigitalInput())) {
                handleNormalPumpChange(Pumps.PRE_PUMP_TWO, (boolean) event.getValue(), pumpTwoState);
            } else if (digitalInput.equals(Pumps.PRE_PUMP_ROOTS.getDigitalInput())) {
                handleNormalPumpChange(Pumps.PRE_PUMP_ROOTS, (boolean) event.getValue(), pumpRootState);
            } else if (digitalInput.equals(Pumps.TURBO_PUMP.getDigitalInput())) {
                if (machineStateService.getDigitalInputState(digitalInput)) {
                    turboPumpState = PumpState.STARTING;
                } else {
                    turboPumpState = PumpState.OFF;
                }
            } else if (digitalInput.equals(DigitalInput.TURBO_PUMP_HIGH_SPEED)) {
                if (machineStateService.getDigitalInputState(digitalInput)) {
                    turboPumpState = PumpState.ON;
                } else {
                    if (turboPumpState == PumpState.ON) {
                        turboPumpState = PumpState.STOPPING;
                    }
                }

            }
        }
        logService.log(LogService.LOG_DEBUG, "Event catched by pump Control: " + event.getType().name());
    }

    private void handleNormalPumpChange(Pumps pump, boolean state, PumpState stateContainer) {
        if (state == true) {
            stateContainer = PumpState.ON;
        } else {
            stateContainer = PumpState.OFF;
        }
        machineStateService.fireMachineStateEvent(new PumpStateEvent(pump, stateContainer));
    }
}

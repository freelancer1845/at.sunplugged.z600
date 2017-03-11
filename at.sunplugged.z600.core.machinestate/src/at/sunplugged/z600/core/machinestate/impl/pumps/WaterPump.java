package at.sunplugged.z600.core.machinestate.impl.pumps;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.Pump;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FutureEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PumpStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.exceptions.IllegalPumpConditionsException;
import at.sunplugged.z600.core.machinestate.impl.MachineStateServiceImpl;
import at.sunplugged.z600.mbt.api.MbtService;

public class WaterPump implements Pump, MachineEventHandler {

    private static DigitalInput INPUT_OK = DigitalInput.COOLING_PUMP_OK;

    private static DigitalOutput OUTPUT_START = DigitalOutput.WATERPUMP;

    private static PumpIds ID = PumpIds.WATER_PUMP;

    private MachineStateService machineStateService;

    private MbtService mbtService;

    public WaterPump(MachineStateService machineStateServce) {
        this.machineStateService = machineStateServce;
        this.mbtService = MachineStateServiceImpl.getMbtService();
        machineStateServce.registerMachineEventHandler(this);
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType().equals(Type.DIGITAL_INPUT_CHANGED)) {
            if (event.getOrigin().equals(INPUT_OK)) {
                if ((boolean) event.getValue() == true) {
                    machineStateService.fireMachineStateEvent(new PumpStateEvent(ID, PumpState.ON));
                } else {
                    machineStateService.fireMachineStateEvent(new PumpStateEvent(ID, PumpState.OFF));
                }
            }
        }
    }

    @Override
    public FutureEvent startPump() {
        FutureEvent returnEvent = new FutureEvent(machineStateService, new PumpStateEvent(ID, PumpState.ON));
        try {
            mbtService.writeDigOut(OUTPUT_START.getAddress(), true);
        } catch (IOException e) {
            throw new IllegalPumpConditionsException(e);
        }
        return returnEvent;
    }

    @Override
    public FutureEvent stopPump() {
        FutureEvent stopEvent = new FutureEvent(machineStateService, new PumpStateEvent(ID, PumpState.OFF));
        try {
            mbtService.writeDigOut(OUTPUT_START.getAddress(), false);
        } catch (IOException e) {
            throw new IllegalPumpConditionsException(e);
        }
        return stopEvent;
    }

    @Override
    public PumpState getState() {
        if (machineStateService.getDigitalInputState(INPUT_OK) == true) {
            return PumpState.ON;
        } else {
            return PumpState.OFF;
        }
    }

}

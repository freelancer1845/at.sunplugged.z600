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

/**
 * Class for the pre pumps. I. e. 1M1, Roots, 2M1
 * 
 * @author Jascha Riedel
 *
 */
public abstract class AbstractPrePump implements Pump, MachineEventHandler {

    private final DigitalInput controlInput;

    private final DigitalOutput startOutput;

    private final PumpIds pump;

    protected MachineStateService machineStateService;

    protected MbtService mbtService;

    public AbstractPrePump(MachineStateService machineStateService, PumpIds pump, DigitalInput controlInput,
            DigitalOutput startOutput) {
        this.pump = pump;
        this.controlInput = controlInput;
        this.startOutput = startOutput;
        this.machineStateService = machineStateService;
        mbtService = MachineStateServiceImpl.getMbtService();
        machineStateService.registerMachineEventHandler(this);
    }

    protected abstract void startPumpChecks() throws IllegalPumpConditionsException;

    @Override
    public FutureEvent startPump() {
        FutureEvent returnEvent = new FutureEvent(machineStateService, new PumpStateEvent(pump, PumpState.ON));

        startPumpChecks();
        try {
            mbtService.writeDigOut(startOutput.getAddress(), true);
        } catch (IOException e) {
            throw new IllegalPumpConditionsException(e);
        }

        return returnEvent;

    }

    protected abstract void stopPumpChecks() throws IllegalPumpConditionsException;

    @Override
    public FutureEvent stopPump() {
        FutureEvent returnEvent = new FutureEvent(machineStateService, new PumpStateEvent(pump, PumpState.OFF));
        stopPumpChecks();
        try {
            mbtService.writeDigOut(startOutput.getAddress(), false);
        } catch (IOException e) {
            throw new IllegalPumpConditionsException(e);
        }

        return returnEvent;
    }

    @Override
    public PumpState getState() {
        boolean controlInputState = machineStateService.getDigitalInputState(controlInput);
        if (controlInputState == false) {
            return PumpState.OFF;
        } else {
            return PumpState.ON;
        }

    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType().equals(Type.DIGITAL_INPUT_CHANGED)) {
            if (event.getDigitalInput() == controlInput) {
                if ((boolean) event.getValue() == true) {
                    machineStateService.fireMachineStateEvent(new PumpStateEvent(pump, PumpState.ON));
                } else if ((boolean) event.getValue() == false) {
                    machineStateService.fireMachineStateEvent(new PumpStateEvent(pump, PumpState.OFF));
                }

            }
        }
    }

}

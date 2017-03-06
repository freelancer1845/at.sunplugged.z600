package at.sunplugged.z600.core.machinestate.impl.pumps;

import java.io.IOException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.GasFlowControl.State;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.Pump;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.WaterControl.WaterOutlet;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FutureEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PumpStateEvent;
import at.sunplugged.z600.core.machinestate.api.exceptions.IllegalPumpConditionsException;
import at.sunplugged.z600.core.machinestate.impl.MachineStateServiceImpl;
import at.sunplugged.z600.mbt.api.MbtService;

public class TurboPump implements Pump, MachineEventHandler {

    private static PumpIds PUMP_ID = PumpIds.TURBO_PUMP;

    private static DigitalInput OK_INPUT = DigitalInput.TURBO_PUMP_OK;

    private static DigitalInput HIGH_SPEED_INPUT = DigitalInput.TURBO_PUMP_HIGH_SPEED;

    private static DigitalOutput START_OUTPUT = DigitalOutput.TUROBO_PUMP;

    private SettingsService settings;

    private MachineStateService machineStateService;

    private MbtService mbtService;

    private PumpState state;

    public TurboPump(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        this.settings = MachineStateServiceImpl.getSettingsService();
        this.mbtService = MachineStateServiceImpl.getMbtService();
        machineStateService.registerMachineEventHandler(this);
        state = PumpState.OFF;

    }

    @Override
    public FutureEvent startPump() {
        FutureEvent startEvent = new FutureEvent(machineStateService, new PumpStateEvent(PUMP_ID, PumpState.ON));

        if (machineStateService.getWaterControl().getOutletState(WaterOutlet.TURBO_PUMP) == false) {
            try {
                machineStateService.getWaterControl().setOutletState(WaterOutlet.TURBO_PUMP, true);
            } catch (IOException e) {
                throw new IllegalPumpConditionsException("Couldn't open water outlet for turbo pump!", e);
            }
        }
        double pressure = machineStateService.getPressureMeasurmentControl()
                .getCurrentValue(PressureMeasurementSite.TURBO_PUMP);
        if (pressure > Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP))) {
            throw new IllegalPumpConditionsException("Pressure at Turbo Pump not low enough to start!");
        }
        OutletControl outletControl = machineStateService.getOutletControl();
        if (outletControl.isOutletOpen(Outlet.OUTLET_ONE) == true) {
            throw new IllegalPumpConditionsException("Outlet One needs to be closed to start turbo pump!");
        }
        if (outletControl.isOutletOpen(Outlet.OUTLET_TWO) == false) {
            throw new IllegalPumpConditionsException("Outlet Two needs to be open to start turbo pump!");
        }
        try {
            machineStateService.getWaterControl().setOutletState(WaterOutlet.TURBO_PUMP, true);
        } catch (IOException e1) {
            throw new IllegalPumpConditionsException("Failed to open Water Outlet for turbo pump");
        }
        try {
            mbtService.writeDigOut(START_OUTPUT.getAddress(), true);
            changeState(PumpState.STARTING);
        } catch (IOException e) {
            throw new IllegalPumpConditionsException(e);
        }

        return startEvent;
    }

    @Override
    public FutureEvent stopPump() {
        FutureEvent stopEvent = new FutureEvent(machineStateService, new PumpStateEvent(PUMP_ID, PumpState.OFF));
        OutletControl outletControl = machineStateService.getOutletControl();
        if (outletControl.isOutletOpen(Outlet.OUTLET_ONE)) {
            throw new IllegalPumpConditionsException("Can't stop TurboPump when outlet one is open!");
        }
        try {
            mbtService.writeDigOut(START_OUTPUT.getAddress(), false);
            changeState(PumpState.STOPPING);
            MachineStateServiceImpl.getStandardThreadPoolService().execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(4500);
                    } catch (InterruptedException e) {

                    }

                    if (state == PumpState.STOPPING) {
                        if (machineStateService.getDigitalInputState(DigitalInput.TURBO_PUMP_HIGH_SPEED) == false
                                && machineStateService.getDigitalInputState(DigitalInput.TURBO_PUMP_OK) == false) {
                            changeState(PumpState.OFF);
                        }
                    }
                }

            });
        } catch (IOException e) {
            throw new IllegalPumpConditionsException(e);
        }

        return stopEvent;

    }

    @Override
    public PumpState getState() {
        return state;
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType().equals(Type.DIGITAL_INPUT_CHANGED)) {
            if (event.getDigitalInput().equals(OK_INPUT) || event.getDigitalInput().equals(HIGH_SPEED_INPUT)) {
                boolean okState = machineStateService.getDigitalInputState(OK_INPUT);
                boolean highSpeedState = machineStateService.getDigitalInputState(HIGH_SPEED_INPUT);
                if (okState == true && highSpeedState == false) {
                    changeState(PumpState.ON);
                } else if (okState == false && highSpeedState == true && state.equals(PumpState.ON)) {
                    changeState(PumpState.STOPPING);
                    MachineStateServiceImpl.getStandardThreadPoolService().execute(new Runnable() {

                        @Override
                        public void run() {
                            MachineStateServiceImpl.getLogService().log(LogService.LOG_DEBUG,
                                    "Turbo Pump stopping. Waiting 5 miniutes for high speed to drop!");
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                MachineStateServiceImpl.getLogService().log(LogService.LOG_ERROR,
                                        "Turbo Pump stopping interrupted!!! This is not recommended!", e);
                            }
                            changeState(PumpState.OFF);
                        }

                    });
                }
            }
        }
    }

    private void changeState(PumpState newState) {
        state = newState;
        machineStateService.fireMachineStateEvent(new PumpStateEvent(PUMP_ID, state));
    }

}

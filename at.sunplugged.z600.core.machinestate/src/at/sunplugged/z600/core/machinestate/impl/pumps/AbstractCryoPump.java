package at.sunplugged.z600.core.machinestate.impl.pumps;

import java.io.IOException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.Pump;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FutureEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PressureChangedEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PumpStateEvent;
import at.sunplugged.z600.core.machinestate.api.exceptions.IllegalPumpConditionsException;
import at.sunplugged.z600.core.machinestate.impl.MachineStateServiceImpl;
import at.sunplugged.z600.mbt.api.MbtService;

public class AbstractCryoPump implements Pump, MachineEventHandler {

    protected final PumpIds pumpId;

    protected final DigitalOutput compressorOutput;

    protected final DigitalInput compressorInput;

    protected final DigitalInput lowInput;

    protected final Outlet inOutlet;

    protected final Outlet outOutlet;

    protected final PressureMeasurementSite pressureSite;

    protected final MachineStateService machineStateService;

    protected final SettingsService settings;

    protected final LogService logService;

    protected final MbtService mbtService;

    protected PumpState state = PumpState.OFF;

    public AbstractCryoPump(PumpIds pumpId, MachineStateService machineStateService, DigitalOutput compressorOutput,
            DigitalInput compressorInput, DigitalInput lowInput, PressureMeasurementSite pressureSite, Outlet inOutlet,
            Outlet outOutlet) {
        this.pumpId = pumpId;
        this.machineStateService = machineStateService;
        this.compressorOutput = compressorOutput;
        this.compressorInput = compressorInput;
        this.lowInput = lowInput;
        this.settings = MachineStateServiceImpl.getSettingsService();
        this.logService = MachineStateServiceImpl.getLogService();
        this.mbtService = MachineStateServiceImpl.getMbtService();
        this.pressureSite = pressureSite;
        this.inOutlet = inOutlet;
        this.outOutlet = outOutlet;

        machineStateService.registerMachineEventHandler(this);
    }

    @Override
    public FutureEvent startPump() {
        FutureEvent startEvent = new FutureEvent(machineStateService, new PumpStateEvent(pumpId, PumpState.ON));
        double pressure = machineStateService.getPressureMeasurmentControl().getCurrentValue(pressureSite);
        if (pressure > Double.valueOf(settings.getProperty(ParameterIds.CRYO_PUMP_PRESSURE_TRIGGER))) {
            logService.log(LogService.LOG_ERROR,
                    "Can't start cryo pump: \"" + pumpId.name() + "\". Pressure Trigger not reached.");
            throw new IllegalPumpConditionsException("Pressure Trigger not reached for \"" + pumpId.name() + "\"!");
        }
        OutletControl outletControl = machineStateService.getOutletControl();
        if (outletControl.isOutletOpen(inOutlet) == true) {
            try {
                outletControl.closeOutlet(inOutlet);
            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR,
                        "Couldn't close \"" + inOutlet.name() + "\" for starting cryo pump.", e);
                changeState(PumpState.FAILED);
            }
        }
        if (outletControl.isOutletOpen(outOutlet)) {
            try {
                outletControl.closeOutlet(outOutlet);
            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR,
                        "Couldn't close \"" + outOutlet.name() + "\" for starting cryo pump.", e);
                throw new IllegalPumpConditionsException("Failed because outlet couldn't be closed.");
            }
        }
        try {
            mbtService.writeDigOut(compressorOutput.getAddress(), true);
            changeState(PumpState.STARTING);
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Failed to start compressor: \"" + compressorOutput.name() + "\".");
            throw new IllegalPumpConditionsException("Failed because compressor couldn't be started.");
        }
        if (machineStateService.getDigitalInputState(lowInput) == true) {
            changeState(PumpState.ON);
        }

        return startEvent;
    }

    @Override
    public FutureEvent stopPump() {
        FutureEvent stopEvent = new FutureEvent(machineStateService, new PumpStateEvent(pumpId, PumpState.OFF));
        try {
            mbtService.writeDigOut(compressorOutput.getAddress(), false);
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Failed to stop compressor: \"" + compressorOutput.name() + "\".", e);
            throw new IllegalPumpConditionsException("Failed because compressor couldn't be stopped.");
        }
        changeState(PumpState.STOPPING);
        return stopEvent;
    }

    @Override
    public PumpState getState() {
        return state;
    }

    private void changeState(PumpState newState) {
        state = newState;
        machineStateService.fireMachineStateEvent(new PumpStateEvent(pumpId, state));
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType().equals(Type.PRESSURE_CHANGED)) {
            PressureChangedEvent pressureEvent = (PressureChangedEvent) event;
            if (pressureEvent.getSite().equals(pressureSite)) {
                if ((double) pressureEvent.getValue() > Double
                        .valueOf(settings.getProperty(ParameterIds.CRYO_PUMP_PRESSURE_TRIGGER))) {
                    if (machineStateService.getDigitalInputState(lowInput) == true) {
                        changeState(PumpState.ON);
                    }
                }
            }
        } else if (event.getType().equals(Type.DIGITAL_INPUT_CHANGED)) {
            if (event.getDigitalInput().equals(lowInput)) {
                if ((boolean) event.getValue() == true) {
                    if (machineStateService.getPressureMeasurmentControl().getCurrentValue(pressureSite) < Double
                            .valueOf(settings.getProperty(ParameterIds.CRYO_PUMP_PRESSURE_TRIGGER))) {
                        changeState(PumpState.ON);
                    }
                } else if (state != PumpState.STARTING) {
                    changeState(PumpState.OFF);
                }
            }
            if (event.getDigitalInput().equals(compressorInput)) {
                if ((boolean) event.getValue() == false) {
                    changeState(PumpState.OFF);
                }
            }
        }
    }
}

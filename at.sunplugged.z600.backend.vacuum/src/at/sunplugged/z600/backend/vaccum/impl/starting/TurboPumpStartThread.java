package at.sunplugged.z600.backend.vaccum.impl.starting;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.vaccum.api.VacuumService.TurboPumpThreadState;
import at.sunplugged.z600.backend.vaccum.impl.VacuumServiceImpl;
import at.sunplugged.z600.backend.vaccum.impl.VacuumUtils;
import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.Pump;
import at.sunplugged.z600.core.machinestate.api.Pump.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WaterControl.WaterOutlet;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FutureEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FuturePressureReachedEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;

public class TurboPumpStartThread extends Thread {

    private TurboPumpThreadState state = TurboPumpThreadState.START_PRE_PUMPS;

    private MachineStateService machineStateService;

    private OutletControl outletControl;

    private PumpRegistry pumpRegistry;

    private LogService logService;

    private SettingsService settings;

    private volatile boolean cancel = false;

    public TurboPumpStartThread() {
        this.machineStateService = VacuumServiceImpl.getMachineStateService();
        this.logService = VacuumServiceImpl.getLogService();
        this.settings = VacuumServiceImpl.getSettingsService();

        this.outletControl = machineStateService.getOutletControl();
        this.pumpRegistry = machineStateService.getPumpRegistry();

        this.setName("TurboPumpThread");
    }

    private void cancel() {
        this.interrupt();
        if (state != TurboPumpThreadState.SHUTDOWN && state != TurboPumpThreadState.INIT_STATE) {
            state = TurboPumpThreadState.CANCELED;
        }
    }

    public void shutdown() {
        state = TurboPumpThreadState.SHUTDOWN;
        this.interrupt();
    }

    @Override
    public void run() {
        while (cancel == false) {
            try {

                logService.log(LogService.LOG_DEBUG, "New TurboPumpThread State: \"" + state.name() + "\"");
                VacuumServiceImpl.transmitState(state);
                switch (state) {
                case INIT_STATE:
                    state = TurboPumpThreadState.START_PRE_PUMPS;
                    Thread.interrupted();
                    break;
                case START_PRE_PUMPS:
                    if (isCanceled()) {
                        break;
                    }
                    startPumps();
                    stateSelector();
                    break;
                case EVACUATE_CHAMBER:
                    if (isCanceled()) {
                        break;
                    }
                    evacuateChamber();
                    stateSelector();
                    break;
                case EVACUATE_TURBO_PUMP:
                    if (isCanceled()) {
                        break;
                    }
                    evacuateTurboPump();
                    stateSelector();
                    break;
                case START_TURBO_PUMP:
                    if (isCanceled()) {
                        break;
                    }
                    startTurboPump();
                    stateSelector();
                    break;
                case TURBO_PUMP_RUNNING:
                    outletControl.openOutlet(Outlet.OUTLET_ONE);
                    while (true) {
                        Thread.sleep(100);
                        if (!state.equals(TurboPumpThreadState.TURBO_PUMP_RUNNING)) {
                            break;
                        }
                    }
                    break;
                case SHUTDOWN:
                    shutdownCase();
                    break;
                case CANCELED:
                    cancelProcess();
                    break;
                }
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                cancel();
            } catch (Exception e) {
                logService.log(LogService.LOG_ERROR, "Unhandled Exception in TurboPumpThread!", e);
                state = TurboPumpThreadState.CANCELED;
            }
        }
    }

    private void stateSelector() {
        if (isCanceled()) {
            return;
        }
        switch (state) {
        case START_PRE_PUMPS:
            if (VacuumUtils.hasChamberPressureReachedTurboPumpStartTrigger(0.8) == false) {
                state = TurboPumpThreadState.EVACUATE_CHAMBER;
            } else if (VacuumUtils.hasTurboPumpReachedTurboPumpStartTrigger(0.8) == false) {
                state = TurboPumpThreadState.EVACUATE_TURBO_PUMP;
            } else {
                state = TurboPumpThreadState.START_TURBO_PUMP;
            }
            break;
        case EVACUATE_CHAMBER:
            state = TurboPumpThreadState.EVACUATE_TURBO_PUMP;
            break;
        case EVACUATE_TURBO_PUMP:
            if (VacuumUtils.hasChamberPressureReachedTurboPumpStartTrigger() == false) {
                state = TurboPumpThreadState.EVACUATE_CHAMBER;
            } else {
                state = TurboPumpThreadState.START_TURBO_PUMP;
            }
            break;
        case START_TURBO_PUMP:
            if (pumpRegistry.getPump(PumpIds.TURBO_PUMP).getState().equals(PumpState.ON)) {
                state = TurboPumpThreadState.TURBO_PUMP_RUNNING;
            } else {
                state = TurboPumpThreadState.START_PRE_PUMPS;
            }
            break;
        default:
            logService.log(LogService.LOG_ERROR, "StateSelector of TurboPumpThread is in an unexpected state: \""
                    + state.name() + "\"! Canceling...");
            cancel = true;
            break;
        }
    }

    private void startPumps() throws IOException, InterruptedException, TimeoutException {
        VacuumUtils.closeAllOutlets(outletControl);
        startPrePumpOne();
        startPrePumpRoots();

    }

    private void startPrePumpOne() throws InterruptedException, TimeoutException {
        Pump prePumpOne = pumpRegistry.getPump(PumpIds.PRE_PUMP_ONE);
        if (prePumpOne.getState().equals(PumpState.ON)) {
            return;
        }
        prePumpOne.startPump().get(10, TimeUnit.SECONDS);
    }

    private void startPrePumpRoots() throws InterruptedException, TimeoutException {
        Pump prePumpRoots = pumpRegistry.getPump(PumpIds.PRE_PUMP_ROOTS);
        if (prePumpRoots.getState() == PumpState.ON) {
            return;
        }
        if (machineStateService.getDigitalInputState(DigitalInput.P_120_MBAR) == false) {
            FutureEvent p120TriggerEvent = new FutureEvent(machineStateService,
                    new MachineStateEvent(Type.DIGITAL_INPUT_CHANGED, DigitalInput.P_120_MBAR, true));
            p120TriggerEvent.get(10, TimeUnit.SECONDS);
        }
        prePumpRoots.startPump().get(10, TimeUnit.SECONDS);
    }

    private void evacuateChamber() throws IOException, InterruptedException, TimeoutException {
        outletControl.closeOutlet(Outlet.OUTLET_ONE);
        outletControl.closeOutlet(Outlet.OUTLET_TWO);
        outletControl.closeOutlet(Outlet.OUTLET_NINE);

        Thread.sleep(500);

        outletControl.openOutlet(Outlet.OUTLET_THREE);
        FuturePressureReachedEvent turboPumpPressureTrigger = new FuturePressureReachedEvent(machineStateService,
                PressureMeasurementSite.CHAMBER,
                Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP)) * 0.8);
        turboPumpPressureTrigger.get(2, TimeUnit.HOURS);
    }

    private void evacuateTurboPump() throws IOException, InterruptedException, TimeoutException {
        outletControl.closeOutlet(Outlet.OUTLET_ONE);
        outletControl.closeOutlet(Outlet.OUTLET_THREE);

        Thread.sleep(500);

        outletControl.openOutlet(Outlet.OUTLET_TWO);
        FuturePressureReachedEvent turboPumpChamberPressureTrigger = new FuturePressureReachedEvent(machineStateService,
                PressureMeasurementSite.TURBO_PUMP,
                Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP)) * 0.8);
        turboPumpChamberPressureTrigger.get(20, TimeUnit.MINUTES);

    }

    private void startTurboPump() throws InterruptedException, TimeoutException, IOException {
        outletControl.closeOutlet(Outlet.OUTLET_ONE);
        outletControl.openOutlet(Outlet.OUTLET_TWO);
        machineStateService.getWaterControl().setOutletState(WaterOutlet.TURBO_PUMP, true);
        Thread.sleep(500);
        Pump turboPump = pumpRegistry.getPump(PumpIds.TURBO_PUMP);
        if (turboPump.getState().equals(PumpState.OFF) || turboPump.getState().equals(PumpState.STOPPING)) {
            turboPump.startPump().get(5, TimeUnit.MINUTES);
        }
    }

    private boolean isCanceled() {
        if (this.isInterrupted() == true) {
            cancel = true;
        }
        if (cancel == true) {
            state = TurboPumpThreadState.CANCELED;
        }
        return cancel;
    }

    private void cancelProcess() throws InterruptedException {
        Thread.interrupted();
        try {
            outletControl.closeOutlet(Outlet.OUTLET_ONE);
            outletControl.closeOutlet(Outlet.OUTLET_THREE);
            Thread.sleep(1000);
            pumpRegistry.getPump(PumpIds.TURBO_PUMP).stopPump();
            Thread.sleep(1000);
            outletControl.closeOutlet(Outlet.OUTLET_TWO);
            pumpRegistry.getPump(PumpIds.PRE_PUMP_ROOTS).stopPump();
            Thread.sleep(500);
            pumpRegistry.getPump(PumpIds.PRE_PUMP_ONE).stopPump();

        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Error during cancel turboPumpThread!!!", e);
        } finally {
            cancel = true;
        }

    }

    private void shutdownCase() throws IOException, InterruptedException, TimeoutException {
        Thread.interrupted();
        outletControl.closeOutlet(Outlet.OUTLET_ONE);
        outletControl.closeOutlet(Outlet.OUTLET_THREE);
        Thread.sleep(5000);
        if (!pumpRegistry.getPump(PumpIds.TURBO_PUMP).getState().equals(PumpState.OFF)) {
            pumpRegistry.getPump(PumpIds.TURBO_PUMP).stopPump().get(15, TimeUnit.MINUTES);
        }

        outletControl.closeOutlet(Outlet.OUTLET_TWO);
        if (!pumpRegistry.getPump(PumpIds.PRE_PUMP_ROOTS).getState().equals(PumpState.OFF)) {
            pumpRegistry.getPump(PumpIds.PRE_PUMP_ROOTS).stopPump().get(30, TimeUnit.SECONDS);
        }
        if (!pumpRegistry.getPump(PumpIds.PRE_PUMP_ONE).getState().equals(PumpState.OFF)) {
            pumpRegistry.getPump(PumpIds.PRE_PUMP_ONE).stopPump().get(30, TimeUnit.SECONDS);
        }
        state = TurboPumpThreadState.INIT_STATE;
        VacuumServiceImpl.transmitState(state);
        cancel = true;
    }

    public void restart() {
        this.state = TurboPumpThreadState.INIT_STATE;
        this.interrupt();
    }

}

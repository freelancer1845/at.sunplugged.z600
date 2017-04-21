package at.sunplugged.z600.backend.vaccum.impl.starting;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.vaccum.api.VacuumService.CryoPumpsThreadState;
import at.sunplugged.z600.backend.vaccum.api.VacuumService.Interlocks;
import at.sunplugged.z600.backend.vaccum.impl.VacuumServiceImpl;
import at.sunplugged.z600.backend.vaccum.impl.VacuumUtils;
import at.sunplugged.z600.core.machinestate.api.GasFlowControl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.Pump;
import at.sunplugged.z600.core.machinestate.api.Pump.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FuturePressureReachedEvent;

public class CryoPumpsStartThread extends Thread {

    private CryoPumpsThreadState state = CryoPumpsThreadState.INIT_STATE;

    private MachineStateService machineStateService;

    private OutletControl outletControl;

    private PumpRegistry pumpRegistry;

    private LogService logService;

    private volatile boolean cancel = false;

    public CryoPumpsStartThread() {
        this.setName("CryoPumpsThread");

        this.machineStateService = VacuumServiceImpl.getMachineStateService();
        this.logService = VacuumServiceImpl.getLogService();

        this.outletControl = machineStateService.getOutletControl();
        this.pumpRegistry = machineStateService.getPumpRegistry();

    }

    public void cancel() {
        if (state != CryoPumpsThreadState.SHUTDOWN && state != CryoPumpsThreadState.INIT_STATE) {
            state = CryoPumpsThreadState.CANCELED;
        }
        this.interrupt();

    }

    public void shutdown() {
        state = CryoPumpsThreadState.SHUTDOWN;
        this.interrupt();

    }

    public void restart() {
        state = CryoPumpsThreadState.INIT_STATE;
        this.interrupt();
    }

    @Override
    public void run() {
        while (cancel == false) {
            try {
                logService.log(LogService.LOG_DEBUG, "CrypPumpThread new state: " + state.name());
                VacuumServiceImpl.transmitState(state);
                switch (state) {
                case INIT_STATE:
                    stateSelector();
                    break;
                case START_PRE_PUMP:
                    if (isCanceled()) {
                        break;
                    }
                    startPrePump();
                    stateSelector();
                    break;
                case EVACUATE_CRYO:
                    if (isCanceled()) {
                        break;
                    }
                    evacuateCryos();
                    stateSelector();
                    break;
                case START_COOLING:
                    if (isCanceled()) {
                        break;
                    }
                    startCooling();
                    stateSelector();
                    break;
                case EVACUATE_CHAMBER:
                    if (isCanceled()) {
                        break;
                    }
                    evacuateChamber();
                    stateSelector();
                    break;
                case WAIT_FOR_CRYO_COOL:
                    if (isCanceled()) {
                        break;
                    }
                    waitForCryoCool();
                    break;
                case CRYO_RUNNING:
                    if (isCanceled()) {
                        break;
                    }
                    try {
                        openCryoOutlets();
                        monitorCryos();
                    } finally {
                        outletControl.closeOutlet(Outlet.OUTLET_SEVEN);
                        outletControl.closeOutlet(Outlet.OUTLET_EIGHT);
                    }

                    break;
                case GAS_FLOW_RUNNING:
                    if (isCanceled()) {
                        break;
                    }
                    waitForGasflowToStop();
                    stateSelector();
                    break;
                case SHUTDOWN:
                    if (isCanceled()) {
                        break;
                    }
                    shutdownCase();
                    break;
                case CANCELED:
                    stopCryoPumpThread();
                    break;
                }
                Thread.sleep(500);

            } catch (IllegalStateException e1) {
                logService.log(LogService.LOG_ERROR, "Error in CryoPumpThread. Starting again.", e1);
                state = CryoPumpsThreadState.INIT_STATE;
            } catch (InterruptedException et) {
                cancel();
            } catch (Exception e) {
                logService.log(LogService.LOG_ERROR, "Unhandled Exception in CryoPumpsThread. Canceling...", e);
                cancel();
            }

        }

    }

    private void waitForGasflowToStop() throws InterruptedException {
        while (machineStateService.getGasFlowControl().getState() == GasFlowControl.State.RUNNING_STABLE) {
            if (isCanceled()) {
                return;
            }
            Thread.sleep(500);
        }
    }

    private void stopCryoPumpThread() {
        Thread.interrupted();
        try {
            outletControl.closeOutlet(Outlet.OUTLET_SEVEN);
            outletControl.closeOutlet(Outlet.OUTLET_EIGHT);
            outletControl.closeOutlet(Outlet.OUTLET_FOUR);
            outletControl.closeOutlet(Outlet.OUTLET_FIVE);
            outletControl.closeOutlet(Outlet.OUTLET_SIX);

            pumpRegistry.getPump(PumpIds.CRYO_ONE).stopPump();
            pumpRegistry.getPump(PumpIds.CRYO_TWO).stopPump();
            pumpRegistry.getPump(PumpIds.PRE_PUMP_TWO).stopPump();
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Error while stopping CryoPumpThread!!!", e);
        } finally {
            cancel = true;
        }

    }

    private void stateSelector() {
        if (isCanceled()) {
            return;
        }
        switch (state) {
        case INIT_STATE:
            state = CryoPumpsThreadState.START_PRE_PUMP;
            break;
        case START_PRE_PUMP:
            state = CryoPumpsThreadState.EVACUATE_CRYO;
            break;
        case EVACUATE_CRYO:
            state = CryoPumpsThreadState.START_COOLING;
            break;
        case START_COOLING:
            state = CryoPumpsThreadState.EVACUATE_CHAMBER;
            break;
        case EVACUATE_CHAMBER:
            state = CryoPumpsThreadState.WAIT_FOR_CRYO_COOL;
            break;
        case GAS_FLOW_RUNNING:
            state = CryoPumpsThreadState.WAIT_FOR_CRYO_COOL;
            break;
        default:
            state = CryoPumpsThreadState.INIT_STATE;
            break;
        }
    }

    private void startPrePump() throws InterruptedException, TimeoutException, IOException {
        Pump prePumpTwo = pumpRegistry.getPump(PumpIds.PRE_PUMP_TWO);
        if (prePumpTwo.getState().equals(PumpState.OFF)) {
            outletControl.closeOutlet(Outlet.OUTLET_FOUR);
            outletControl.closeOutlet(Outlet.OUTLET_FIVE);
            outletControl.closeOutlet(Outlet.OUTLET_SIX);
            prePumpTwo.startPump().get(10, TimeUnit.SECONDS);
        }
    }

    private void evacuateCryos() throws IOException, InterruptedException, TimeoutException {
        try {
            FuturePressureReachedEvent cryoOneEvacuated = null;
            FuturePressureReachedEvent cryoTwoEvacuated = null;
            if (VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_ONE) == true
                    && VacuumUtils.isCryoEvacuated(PumpIds.CRYO_ONE) == false) {
                outletControl.openOutlet(Outlet.OUTLET_FIVE);
                cryoOneEvacuated = new FuturePressureReachedEvent(machineStateService,
                        PressureMeasurementSite.CRYO_PUMP_ONE, VacuumUtils.getCryoPumpPressureTrigger() * 0.8);
            }
            if (VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_TWO) == true
                    && VacuumUtils.isCryoEvacuated(PumpIds.CRYO_TWO) == false) {
                outletControl.openOutlet(Outlet.OUTLET_SIX);
                cryoTwoEvacuated = new FuturePressureReachedEvent(machineStateService,
                        PressureMeasurementSite.CRYO_PUMP_TWO, VacuumUtils.getCryoPumpPressureTrigger() * 0.8);
            }
            if (cryoOneEvacuated != null) {
                cryoOneEvacuated.get(1, TimeUnit.HOURS);
            }
            if (cryoTwoEvacuated != null) {
                cryoTwoEvacuated.get(1, TimeUnit.HOURS);
            }
            outletControl.closeOutlet(Outlet.OUTLET_FIVE);
            outletControl.closeOutlet(Outlet.OUTLET_SIX);
        } finally {
            outletControl.closeOutlet(Outlet.OUTLET_FIVE);
            outletControl.closeOutlet(Outlet.OUTLET_SIX);
        }

    }

    private void startCooling() {
        Pump cryoOne = pumpRegistry.getPump(PumpIds.CRYO_ONE);
        Pump cryoTwo = pumpRegistry.getPump(PumpIds.CRYO_TWO);
        double triggerPressure = VacuumUtils.getCryoPumpPressureTrigger();
        if (VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_ONE) == true) {
            double pressureCryoOne = machineStateService.getPressureMeasurmentControl()
                    .getCurrentValue(PressureMeasurementSite.CRYO_PUMP_ONE);
            if (pressureCryoOne > triggerPressure) {
                throw new IllegalStateException("Presure at cryo One is too high. Pumping again.");
            }
            cryoOne.startPump();
        }
        if (VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_TWO) == true) {
            double pressureCryoTwo = machineStateService.getPressureMeasurmentControl()
                    .getCurrentValue(PressureMeasurementSite.CRYO_PUMP_TWO);
            if (pressureCryoTwo > triggerPressure) {
                throw new IllegalStateException("Pressure at cryo Two is too high. Pumping again.");
            }
            cryoTwo.startPump();
        }
    }

    private void evacuateChamber() throws IOException, InterruptedException, TimeoutException {
        if (VacuumUtils.hasChamberPressureReachedTurboPumpStartTrigger(0.8) == false) {
            try {
                outletControl.closeOutlet(Outlet.OUTLET_FIVE);
                outletControl.closeOutlet(Outlet.OUTLET_SIX);
                Thread.sleep(500);
                outletControl.openOutlet(Outlet.OUTLET_FOUR);
                while (!VacuumUtils.hasChamberPressureReachedTurboPumpStartTrigger(0.8)) {
                    Thread.sleep(500);
                    boolean cryoOneInterlock = VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_ONE);
                    boolean cryoTwoInterlock = VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_TWO);
                    if (cryoTwoInterlock && !VacuumUtils.isCryoEvacuated(PumpIds.CRYO_TWO)) {
                        break;
                    }
                    if (cryoOneInterlock && !VacuumUtils.isCryoEvacuated(PumpIds.CRYO_ONE)) {
                        break;
                    }
                    if (VacuumUtils.hasChamberPressureReachedTurboPumpStartTrigger(0.8)) {
                        break;
                    }
                }
            } finally {
                outletControl.closeOutlet(Outlet.OUTLET_FOUR);
            }

        }
    }

    private void waitForCryoCool() throws InterruptedException {
        double cryoOnePressure;
        double cryoTwoPressure;

        boolean cryoOneInterlock = VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_ONE);
        boolean cryoTwoInterlock = VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_TWO);

        double cryoPressureTrigger = VacuumUtils.getCryoPumpPressureTrigger();

        Pump cryoOne = pumpRegistry.getPump(PumpIds.CRYO_ONE);
        Pump cryoTwo = pumpRegistry.getPump(PumpIds.CRYO_TWO);

        while (true) {
            if (cryoOneInterlock == true) {
                cryoOnePressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CRYO_PUMP_ONE);
                if (cryoOnePressure > cryoPressureTrigger) {
                    state = CryoPumpsThreadState.EVACUATE_CRYO;
                    return;
                }

            }
            if (cryoTwoInterlock == true) {
                cryoTwoPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CRYO_PUMP_TWO);
                if (cryoTwoPressure > cryoPressureTrigger) {
                    state = CryoPumpsThreadState.EVACUATE_CRYO;
                    return;
                }
            }
            if (cryoTwoInterlock == true && cryoOneInterlock) {
                if (cryoOne.getState().equals(PumpState.ON) && cryoTwo.getState().equals(PumpState.ON)) {
                    state = CryoPumpsThreadState.CRYO_RUNNING;
                    return;
                }
            } else if (cryoTwoInterlock == true && cryoOneInterlock == false) {
                if (cryoTwo.getState().equals(PumpState.ON)) {
                    state = CryoPumpsThreadState.CRYO_RUNNING;
                    return;
                }
            } else if (cryoTwoInterlock == false && cryoOneInterlock == true) {
                if (cryoOne.getState().equals(PumpState.ON)) {
                    state = CryoPumpsThreadState.CRYO_RUNNING;
                    return;
                }
            }
            if (VacuumUtils.hasChamberPressureReachedTurboPumpStartTrigger(0.8) == false) {
                state = CryoPumpsThreadState.EVACUATE_CHAMBER;
                return;
            }
            Thread.sleep(500);
        }

    }

    private void openCryoOutlets() throws IOException {
        outletControl.closeOutlet(Outlet.OUTLET_FIVE);
        outletControl.closeOutlet(Outlet.OUTLET_SIX);
        if (VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_ONE)) {
            outletControl.openOutlet(Outlet.OUTLET_SEVEN);
        }
        if (VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_TWO)) {
            outletControl.openOutlet(Outlet.OUTLET_EIGHT);
        }
    }

    private void monitorCryos() throws InterruptedException {
        Pump cryoOne = pumpRegistry.getPump(PumpIds.CRYO_ONE);
        Pump cryoTwo = pumpRegistry.getPump(PumpIds.CRYO_TWO);

        boolean cryoOneInterlock = VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_ONE);
        boolean cryoTwoInterlock = VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_TWO);
        while (true) {
            if (cryoOneInterlock == true) {
                if (!cryoOne.getState().equals(PumpState.ON)) {
                    state = CryoPumpsThreadState.INIT_STATE;
                    return;
                }
            }
            if (cryoTwoInterlock == true) {
                if (!cryoTwo.getState().equals(PumpState.ON)) {
                    state = CryoPumpsThreadState.INIT_STATE;
                    return;
                }
            }
            if (machineStateService.getGasFlowControl().getState() != GasFlowControl.State.STOP
                    && machineStateService.getGasFlowControl().getState() != GasFlowControl.State.STOPPING) {
                state = CryoPumpsThreadState.GAS_FLOW_RUNNING;
                return;
            }

            Thread.sleep(500);
        }
    }

    private boolean isCanceled() {
        if (isInterrupted() == true) {
            if (state != CryoPumpsThreadState.SHUTDOWN) {
                state = CryoPumpsThreadState.CANCELED;
            }
        }
        if (cancel == true) {
            if (state != CryoPumpsThreadState.SHUTDOWN) {
                state = CryoPumpsThreadState.CANCELED;
            }
        }
        return state == CryoPumpsThreadState.CANCELED;
    }

    private void shutdownCase() throws IOException, InterruptedException {
        Thread.interrupted();
        outletControl.closeOutlet(Outlet.OUTLET_EIGHT);
        outletControl.closeOutlet(Outlet.OUTLET_SEVEN);
        outletControl.closeOutlet(Outlet.OUTLET_FIVE);
        outletControl.closeOutlet(Outlet.OUTLET_FOUR);
        Thread.sleep(500);
        machineStateService.getPumpRegistry().getPump(PumpIds.CRYO_ONE).stopPump();
        machineStateService.getPumpRegistry().getPump(PumpIds.CRYO_TWO).stopPump();

        Thread.sleep(1000);
        machineStateService.getPumpRegistry().getPump(PumpIds.PRE_PUMP_TWO).stopPump();
        cancel = true;
        state = CryoPumpsThreadState.INIT_STATE;
        VacuumServiceImpl.transmitState(state);
    }

}

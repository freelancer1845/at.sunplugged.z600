package at.sunplugged.z600.backend.vaccum.impl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.vaccum.api.VacuumService.Interlocks;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.Pump;
import at.sunplugged.z600.core.machinestate.api.Pump.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FuturePressureReachedEvent;

public class CryoPumpsThread extends Thread {

    private enum CryoPumpsThreadState {
        INIT_STATE,
        START_PRE_PUMP,
        EVACUATE_CRYO,
        EVACUATE_CHAMBER,
        START_COOLING,
        WAIT_FOR_CRYO_COOL,
        CRYO_RUNNING,
        CANCELED;
    }

    private CryoPumpsThreadState state = CryoPumpsThreadState.INIT_STATE;

    private MachineStateService machineStateService;

    private OutletControl outletControl;

    private PumpRegistry pumpRegistry;

    private LogService logService;

    private volatile boolean cancel = false;

    public CryoPumpsThread() {
        this.setName("CryoPumpsThread");

        this.machineStateService = VacuumServiceImpl.getMachineStateService();
        this.logService = VacuumServiceImpl.getLogService();

        this.outletControl = machineStateService.getOutletControl();
        this.pumpRegistry = machineStateService.getPumpRegistry();

    }

    public void cancel() {
        this.interrupt();
        cancel = true;
    }

    @Override
    public void run() {
        while (cancel == false) {
            try {
                logService.log(LogService.LOG_DEBUG, "CrypPumpThread new state: " + state.name());
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
                case CANCELED:
                    stopCryoPumpThread();
                    break;
                }

            } catch (IllegalStateException e1) {
                logService.log(LogService.LOG_ERROR, "Error in CryoPumpThread. Starting again.", e1);
                state = CryoPumpsThreadState.INIT_STATE;
            } catch (InterruptedException et) {
                cancel = true;
            } catch (Exception e) {
                logService.log(LogService.LOG_ERROR, "Unhandled Exception in CryoPumpsThread. Canceling...", e);
                cancel();
            }

        }

    }

    private void stopCryoPumpThread() throws IOException {
        outletControl.closeOutlet(Outlet.OUTLET_SEVEN);
        outletControl.closeOutlet(Outlet.OUTLET_EIGHT);
        outletControl.closeOutlet(Outlet.OUTLET_FOUR);
        outletControl.closeOutlet(Outlet.OUTLET_FIVE);
        outletControl.closeOutlet(Outlet.OUTLET_SIX);

        pumpRegistry.getPump(PumpIds.CRYO_ONE).stopPump();
        pumpRegistry.getPump(PumpIds.CRYO_TWO).stopPump();
        pumpRegistry.getPump(PumpIds.PRE_PUMP_TWO).stopPump();
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
        default:
            state = CryoPumpsThreadState.INIT_STATE;
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
                        PressureMeasurementSite.CRYO_PUMP_ONE, VacuumUtils.getCryoPumpPressureTrigger() * 0.6);
            }
            if (VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_TWO) == true
                    && VacuumUtils.isCryoEvacuated(PumpIds.CRYO_TWO) == false) {
                outletControl.openOutlet(Outlet.OUTLET_SIX);
                cryoTwoEvacuated = new FuturePressureReachedEvent(machineStateService,
                        PressureMeasurementSite.CRYO_PUMP_TWO, VacuumUtils.getCryoPumpPressureTrigger() * 0.6);
            }
            if (cryoOneEvacuated != null) {
                cryoOneEvacuated.get(10, TimeUnit.MINUTES);
            }
            if (cryoTwoEvacuated != null) {
                cryoTwoEvacuated.get(10, TimeUnit.MINUTES);
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
        if (VacuumUtils.hasChamberPressureReachedTurboPumpStartTrigger() == false) {
            try {
                outletControl.closeOutlet(Outlet.OUTLET_FIVE);
                outletControl.closeOutlet(Outlet.OUTLET_SIX);
                Thread.sleep(500);
                outletControl.openOutlet(Outlet.OUTLET_FOUR);

                FuturePressureReachedEvent turboPumpPressureReached = new FuturePressureReachedEvent(
                        machineStateService, PressureMeasurementSite.CHAMBER,
                        VacuumUtils.getTurboPumpStartTrigger() * 0.8);
                turboPumpPressureReached.get(10, TimeUnit.MINUTES);
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
            if (VacuumUtils.hasChamberPressureReachedTurboPumpStartTrigger() == false) {
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

            Thread.sleep(500);
        }
    }

    private boolean isCanceled() {
        if (cancel == true) {
            state = CryoPumpsThreadState.CANCELED;
        }
        return cancel;
    }

}

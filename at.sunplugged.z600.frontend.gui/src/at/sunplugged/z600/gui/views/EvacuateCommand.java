package at.sunplugged.z600.gui.views;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.PumpControl;
import at.sunplugged.z600.core.machinestate.api.PumpControl.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpControl.Pumps;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FutureEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FuturePressureReachedEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PumpStateEvent;

public class EvacuateCommand {

    private StandardThreadPoolService threadPool;

    private MachineStateService machineStateService;

    private OutletControl outletControl;

    private PressureMeasurement pressureMeasurement;

    private PumpControl pumpControl;

    private LogService logService;

    private SettingsService settings;

    private Future<?> primaryVacuumRouteFuture = null;

    private Future<?> secondaryVacuumRouteFuture = null;

    public EvacuateCommand(StandardThreadPoolService threadPool, MachineStateService machineStateService,
            LogService logService, SettingsService settings) {
        this.threadPool = threadPool;
        this.machineStateService = machineStateService;
        this.outletControl = machineStateService.getOutletControl();
        this.pressureMeasurement = machineStateService.getPressureMeasurmentControl();
        this.pumpControl = machineStateService.getPumpControl();
        this.logService = logService;
        this.settings = settings;
    }

    public void executeRouteOne() {
        if (primaryVacuumRouteFuture == null) {
            primaryVacuumRouteFuture = threadPool.submit(new PrimaryVacuumRoute());
        } else {
            if (primaryVacuumRouteFuture.isDone()) {
                primaryVacuumRouteFuture = threadPool.submit(new PrimaryVacuumRoute());
            } else {
                logService.log(LogService.LOG_ERROR, "First vacuum route is already being executed. Cancel it first!");
            }
        }

    }

    public void cancelRouteOne() {
        if (primaryVacuumRouteFuture == null || primaryVacuumRouteFuture.isDone()) {
            logService.log(LogService.LOG_ERROR, "Trying to cancel primary route failed. None running.");
        } else {
            primaryVacuumRouteFuture.cancel(true);
        }
    }

    public void executeRouteTwo() {
        if (secondaryVacuumRouteFuture == null) {
            secondaryVacuumRouteFuture = threadPool.submit(new SecondVaccumRoute());
        } else {
            if (secondaryVacuumRouteFuture.isDone()) {
                secondaryVacuumRouteFuture = threadPool.submit(new SecondVaccumRoute());
            } else {
                logService.log(LogService.LOG_ERROR,
                        "Secondary vacuum route is already being executed. Cancel it first!");
            }
        }
    }

    public void cancelRouteTwo() {
        if (secondaryVacuumRouteFuture == null || secondaryVacuumRouteFuture.isDone()) {
            logService.log(LogService.LOG_ERROR, "Trying to cancel secondary route failed. None running.");
        } else {
            secondaryVacuumRouteFuture.cancel(true);
        }
    }

    /**
     * Controls the primary route (i. e. where the turbo pump is on).
     * 
     * @author Jascha Riedel
     *
     */
    private class PrimaryVacuumRoute implements Runnable {

        @Override
        public void run() {
            try {
                try {
                    logService.log(LogService.LOG_INFO, "Executing primary vaccum route.");
                    logService.log(LogService.LOG_INFO, "Closing Outlets 1, 2, 3, 7, 8, 9");
                    try {
                        outletControl.closeOutlet(Outlet.OUTLET_ONE);
                        outletControl.closeOutlet(Outlet.OUTLET_TWO);
                        outletControl.closeOutlet(Outlet.OUTLET_THREE);
                        outletControl.closeOutlet(Outlet.OUTLET_SEVEN);
                        outletControl.closeOutlet(Outlet.OUTLET_EIGHT);
                        outletControl.closeOutlet(Outlet.OUTLET_NINE);
                    } catch (IOException e1) {
                        logService.log(LogService.LOG_ERROR,
                                "Failed to close outlets at beginning of primary vaccum route.", e1);
                        cancel();
                    }

                    PumpState state = pumpControl.getState(Pumps.PRE_PUMP_ONE);
                    System.out.println("State: " + state.name());
                    if (pumpControl.getState(Pumps.PRE_PUMP_ONE).equals(PumpState.OFF)) {
                        logService.log(LogService.LOG_INFO, "Starting pre pump one...");

                        FutureEvent pumpOneEvent = new FutureEvent(machineStateService,
                                new PumpStateEvent(Pumps.PRE_PUMP_ONE, PumpState.ON));
                        pumpControl.startPump(Pumps.PRE_PUMP_ONE);
                        try {
                            pumpOneEvent.get(5, TimeUnit.SECONDS);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            logService.log(LogService.LOG_ERROR,
                                    "Pre Pump One didn't start after 5 Seconds. Canceling Evacuation.");
                            cancel();
                        }
                    }

                    if (pumpControl.getState(Pumps.PRE_PUMP_ROOTS).equals(PumpState.OFF)) {
                        if (machineStateService.getDigitalInputState(DigitalInput.P_120_MBAR)) {
                            pumpControl.startPump(Pumps.PRE_PUMP_ROOTS);
                        } else {
                            FutureEvent p120TriggerEvent = new FutureEvent(machineStateService,
                                    new MachineStateEvent(Type.DIGITAL_INPUT_CHANGED, DigitalInput.P_120_MBAR, true));
                            try {
                                p120TriggerEvent.get(10, TimeUnit.SECONDS);
                            } catch (ExecutionException e) {
                            } catch (TimeoutException e) {
                                logService.log(LogService.LOG_ERROR,
                                        "Roots pump couldn't start. P_120_MBAR trigger not reached.");
                                cancel();
                            }
                            pumpControl.startPump(Pumps.PRE_PUMP_ROOTS);
                        }
                        logService.log(LogService.LOG_INFO, "Starting roots pump...");

                        FutureEvent rootsPumpEvent = new FutureEvent(machineStateService,
                                new PumpStateEvent(Pumps.PRE_PUMP_ROOTS, PumpState.ON));

                        try {
                            rootsPumpEvent.get(5, TimeUnit.SECONDS);
                        } catch (ExecutionException | TimeoutException e) {
                            logService.log(LogService.LOG_ERROR, "Roots Pump didnt start after 5 seconds.");
                            cancel();
                        }
                    }

                    if (pressureMeasurement.getCurrentValue(PressureMeasurementSite.TURBO_PUMP) >= 1) {
                        logService.log(LogService.LOG_INFO, "Preevacuating turbo pump chamber...");
                        try {
                            outletControl.openOutlet(Outlet.OUTLET_TWO);
                        } catch (IOException e) {
                            logService.log(LogService.LOG_ERROR, "Failed to open V2", e);
                            cancel();
                        }
                        FuturePressureReachedEvent preEvacuateTurboPumpEvent = new FuturePressureReachedEvent(
                                machineStateService, PressureMeasurementSite.TURBO_PUMP, 1);
                        try {
                            preEvacuateTurboPumpEvent.get(1, TimeUnit.MINUTES);
                        } catch (TimeoutException e) {
                            logService.log(LogService.LOG_ERROR,
                                    "Pre Vacuum in turbo pump chamber not reached in 1 minute.");
                        }
                    }

                    logService.log(LogService.LOG_INFO,
                            "Prevacuating turbo pump chamber finished. Closing Outlet 2...");
                    try {
                        outletControl.closeOutlet(Outlet.OUTLET_TWO);
                    } catch (IOException e) {
                        logService.log(LogService.LOG_ERROR, "Failed to close V2.", e);
                        cancel();
                    }

                    if (pressureMeasurement.getCurrentValue(PressureMeasurementSite.CHAMBER) > Double
                            .valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP)) * 0.9) {
                        logService.log(LogService.LOG_INFO,
                                "Opening Outlet 3 to evacuate chamber until the start trigger of the turbo pump is reached (90%) ...");
                        try {
                            outletControl.openOutlet(Outlet.OUTLET_THREE);
                        } catch (IOException e) {
                            logService.log(LogService.LOG_ERROR, "Failed to open V3.", e);
                            cancel();
                        }
                        FuturePressureReachedEvent turboPumpPressureTrigger = new FuturePressureReachedEvent(
                                machineStateService, PressureMeasurementSite.CHAMBER,
                                Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP)) * 0.9);
                        try {
                            turboPumpPressureTrigger.get(1, TimeUnit.HOURS);
                        } catch (TimeoutException e) {
                            logService.log(LogService.LOG_ERROR,
                                    "Turbopump trigger pressure not reached after one hour...");
                            cancel();
                        }

                        logService.log(LogService.LOG_INFO, "Chamber ready for turbo pump. Closing Outlet 3.");
                        try {
                            outletControl.closeOutlet(Outlet.OUTLET_THREE);
                        } catch (IOException e) {
                            logService.log(LogService.LOG_ERROR, "Failed to close Outlet 3.", e);
                            cancel();
                        }

                    }
                    Thread.sleep(100);

                    logService.log(LogService.LOG_INFO,
                            "Further evacuating turbo pump chamber until start trigger (90%) is reached.");
                    try {
                        outletControl.openOutlet(Outlet.OUTLET_TWO);
                    } catch (IOException e) {
                        logService.log(LogService.LOG_ERROR, "Failed to open Outlet 2.", e);
                        cancel();
                    }

                    FuturePressureReachedEvent turboPumpChamberPressureTrigger = new FuturePressureReachedEvent(
                            machineStateService, PressureMeasurementSite.TURBO_PUMP,
                            Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP)) * 0.9);

                    try {
                        turboPumpChamberPressureTrigger.get(10, TimeUnit.MINUTES);
                    } catch (TimeoutException e) {
                        logService.log(LogService.LOG_ERROR, "Turbo pump chamber was not evacuated after 10 minutes");
                        cancel();
                    }

                    try {
                        outletControl.closeOutlet(Outlet.OUTLET_TWO);
                    } catch (IOException e) {
                        logService.log(LogService.LOG_ERROR, "Failed to close Outlet 2", e);
                        cancel();
                    }

                    logService.log(LogService.LOG_INFO, "Ready to start turbo pump...");

                } catch (InterruptedException e1) {
                    logService.log(LogService.LOG_ERROR, "Primary route interrupted by user", e1);
                    cancel();
                }
            } catch (CancelException eC) {
                logService.log(LogService.LOG_ERROR, "Primary Route canceled", eC);
            } catch (RuntimeException runtimeException) {
                logService.log(LogService.LOG_ERROR, "Unhandled Runtime Exception in Primaray evacuation route.",
                        runtimeException);
            }
        }

        private void cancel() throws CancelException {
            try {
                outletControl.closeOutlet(Outlet.OUTLET_ONE);
                outletControl.closeOutlet(Outlet.OUTLET_TWO);
                outletControl.closeOutlet(Outlet.OUTLET_THREE);
            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR, "Failed to close outlets while canceling evacuation route one.",
                        e);
            }

            pumpControl.stopPump(Pumps.PRE_PUMP_ONE);
            pumpControl.stopPump(Pumps.PRE_PUMP_ROOTS);

            throw new CancelException("Evacuation Route One canceled by error.");
        }

    }

    private class SecondVaccumRoute implements Runnable {

        private void cancel() throws CancelException {
            try {
                outletControl.closeOutlet(Outlet.OUTLET_FOUR);
                outletControl.closeOutlet(Outlet.OUTLET_FIVE);
                outletControl.closeOutlet(Outlet.OUTLET_SIX);
            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR,
                        "Failed to close outlets while canceling secondary evacuation route.", e);
            }

            pumpControl.stopPump(Pumps.PRE_PUMP_TWO);
            throw new CancelException("Secondary Evacuation route canceled by error.");

        }

        @Override
        public void run() {
            try {
                try {
                    logService.log(LogService.LOG_INFO, "Executing Secondary evacuation route.");
                    logService.log(LogService.LOG_INFO, "Evacuating Cryo Pumps");
                    try {
                        outletControl.closeOutlet(Outlet.OUTLET_FOUR);
                        outletControl.openOutlet(Outlet.OUTLET_FIVE);
                        outletControl.openOutlet(Outlet.OUTLET_SIX);
                    } catch (IOException e1) {
                        logService.log(LogService.LOG_ERROR, "Failed to open Outlet Five and Six, secondary route!",
                                e1);
                        cancel();
                    }

                    if (pumpControl.getState(Pumps.PRE_PUMP_TWO).equals(PumpState.OFF)) {
                        logService.log(LogService.LOG_INFO, "Starting Pre Pump Two.");
                        pumpControl.startPump(Pumps.PRE_PUMP_TWO);
                        FutureEvent pumpStartEvent = new FutureEvent(machineStateService,
                                new PumpStateEvent(Pumps.PRE_PUMP_TWO, PumpState.ON));
                        try {
                            pumpStartEvent.get(10, TimeUnit.SECONDS);
                        } catch (ExecutionException | TimeoutException e) {
                            logService.log(LogService.LOG_ERROR,
                                    "Pre Pump Two didn't start after 10 seconds. Canceling...", e);
                            cancel();
                        }
                    } else {
                        logService.log(LogService.LOG_INFO, "Secondary: Pre Pump Two already running...");
                    }

                    FuturePressureReachedEvent cryoOneTriggerReached = new FuturePressureReachedEvent(
                            machineStateService, PressureMeasurementSite.CRYO_PUMP_ONE,
                            Double.valueOf(settings.getProperty(ParameterIds.CRYO_PUMP_PRESSURE_TRIGGER)) * 0.9);
                    FuturePressureReachedEvent cryoTwoTriggerReached = new FuturePressureReachedEvent(
                            machineStateService, PressureMeasurementSite.CRYO_PUMP_TWO,
                            Double.valueOf(settings.getProperty(ParameterIds.CRYO_PUMP_PRESSURE_TRIGGER)) * 0.9);

                    logService.log(LogService.LOG_INFO, "Waiting for Cryo Pumps to reach 90% trigger pressure");

                    try {
                        cryoOneTriggerReached.get(10, TimeUnit.MINUTES);
                        cryoTwoTriggerReached.get(10, TimeUnit.MINUTES);
                    } catch (TimeoutException e) {
                        logService.log(LogService.LOG_ERROR,
                                "Cryo Pumps trigger pressure not reached after 10 minutes... canceling secondary route!");
                        cancel();
                    }
                    try {
                        outletControl.closeOutlet(Outlet.OUTLET_FIVE);
                        outletControl.closeOutlet(Outlet.OUTLET_SIX);
                    } catch (IOException e2) {
                        logService.log(LogService.LOG_ERROR,
                                "Secondary: Failed to close outlets five and six after evacuating cryo pumps.", e2);
                        cancel();
                    }

                    double chamberPressure = machineStateService.getPressureMeasurmentControl()
                            .getCurrentValue(PressureMeasurementSite.CHAMBER);

                    if (chamberPressure > Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP))
                            * 0.9) {
                        logService.log(LogService.LOG_INFO,
                                "Switchting Secondary route to evacuate chamber for turbo pump");
                        try {
                            outletControl.openOutlet(Outlet.OUTLET_FOUR);
                        } catch (IOException e1) {
                            logService.log(LogService.LOG_ERROR, "Failed to close outlet four, secondary route!", e1);
                            cancel();
                        }
                        FuturePressureReachedEvent turboPumpChamberPressureTrigger = new FuturePressureReachedEvent(
                                machineStateService, PressureMeasurementSite.CHAMBER,
                                Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP)) * 0.9);

                        try {
                            turboPumpChamberPressureTrigger.get(10, TimeUnit.MINUTES);
                        } catch (TimeoutException e) {
                            logService.log(LogService.LOG_ERROR,
                                    "Turbo pump chamber was not evacuated after 10 minutes");
                            cancel();
                        }
                    }

                    logService.log(LogService.LOG_INFO, "Secondary route finished.");
                    try {
                        machineStateService.getOutletControl().closeOutlet(Outlet.OUTLET_FOUR);
                        machineStateService.getOutletControl().closeOutlet(Outlet.OUTLET_FIVE);
                        machineStateService.getOutletControl().closeOutlet(Outlet.OUTLET_SIX);
                    } catch (IOException e) {
                        logService.log(LogService.LOG_ERROR, "Failed to close Outlets to finish secondary route.", e);
                        cancel();
                    }

                } catch (InterruptedException e1) {
                    logService.log(LogService.LOG_ERROR, "Secondary route canceld", e1);
                    cancel();
                }
            } catch (CancelException eC) {
                logService.log(LogService.LOG_ERROR, "Secondary route canceled", eC);
            } catch (RuntimeException runtimeException) {
                logService.log(LogService.LOG_ERROR, "Unhandled Runtime Exception in Secondary evacuation route.",
                        runtimeException);
            }

        }

    }

    private class CancelException extends Exception {

        public CancelException() {
            super();
        }

        public CancelException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
            super(arg0, arg1, arg2, arg3);
        }

        public CancelException(String arg0, Throwable arg1) {
            super(arg0, arg1);
        }

        public CancelException(String arg0) {
            super(arg0);
        }

        public CancelException(Throwable arg0) {
            super(arg0);
        }

    }

}

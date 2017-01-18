package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.SettingsIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.GasFlowControl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.GasFlowEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.eventhandling.PressureChangedEvent;
import at.sunplugged.z600.mbt.api.MbtService;

public class GasFlowControlImpl implements GasFlowControl, MachineEventHandler {

    private static final int ANALOG_OUTPUT_MAX = 400;

    private static final int CONTROL_THREAD_TICKRATE = 2;

    private final MachineStateService machineStateService;

    private StandardThreadPoolService threadPoolService;

    private LogService logService;

    private SettingsService settingsService;

    private MbtService mbtService;

    private State state = State.STOPPED;

    private double desiredPressure = 0;

    private double gasFlowVariable = 0;

    private long lastPressureChangedReaction = 0;

    public GasFlowControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        this.logService = MachineStateServiceImpl.getLogService();
        this.settingsService = MachineStateServiceImpl.getSettingsService();
        this.threadPoolService = MachineStateServiceImpl.getStandardThreadPoolService();
        desiredPressure = Double.valueOf(settingsService.getProperty(SettingsIds.INITIAL_DESIRED_PRESSURE_GAS_FLOW));

        machineStateService.registerMachineEventHandler(this);
    }

    @Override
    public void setGasflowDesiredPressure(double desiredPressure) {
        this.desiredPressure = desiredPressure;
    }

    @Override
    public double getCurrentGasFlowValue() {
        int analogValue = machineStateService.getAnalogInputState(AnalogInput.GAS_FLOW);

        return analogValue / 4095 * 400;
    }

    @Override
    public void startGasFlowControl() {

        setState(State.STARTING);
        threadPoolService.execute(new StartRunnable());

    }

    @Override
    public void stopGasFlowControl() {
        setState(State.STOPPED);
        try {
            machineStateService.getOutletControl().closeOutlet(Outlet.OUTLET_NINE);
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Failed to close Outlet nine when stopping gas control.", e);
        }
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (state == State.RUNNING) {
            if (event.getType().equals(Type.PRESSURE_CHANGED)) {
                PressureChangedEvent pressureEvent = (PressureChangedEvent) event;
                if (pressureEvent.getSite().equals(PressureMeasurementSite.CHAMBER)) {
                    handleEventLocal((double) pressureEvent.getValue());
                }
            }
        }
    }

    private void handleEventLocal(double currentPressure) {
        if (lastPressureChangedReaction + TimeUnit.MILLISECONDS.toNanos(500) < System.nanoTime()) {
            int controlParameter = Integer.valueOf(settingsService.getProperty(SettingsIds.GAS_FLOW_CONTROL_PARAMETER));
            double controlParameterHysteresis = Double
                    .valueOf(settingsService.getProperty(SettingsIds.GAS_FLOW_HYSTERESIS_CONTROL_PARAMETER));

            if (currentPressure < desiredPressure * (1 + controlParameterHysteresis / 100.0)) {
                gasFlowVariable += controlParameter;
            } else if (currentPressure > desiredPressure * (1 - controlParameterHysteresis / 100.0)) {
                gasFlowVariable -= controlParameter;
            }
            try {
                writeGasFlowVariable();
            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR, "Gas Flow Control Failed. Stopping...", e);
                setState(State.STOPPED);
            }
        }
    }

    private int convertGasFlowParameterToAnalogOutput(double parameter) {
        int outputValue = 0;
        if (parameter > ANALOG_OUTPUT_MAX) {
            outputValue = ANALOG_OUTPUT_MAX;
        } else if (parameter < ANALOG_OUTPUT_MAX) {
            outputValue = 0;
        }
        return Math.round((outputValue * 4095 / ANALOG_OUTPUT_MAX));
    }

    private void setState(State state) {
        this.state = state;
        machineStateService.fireMachineStateEvent(new GasFlowEvent(state));
    }

    private void writeGasFlowVariable() throws IOException {
        mbtService.writeOutputRegister(WagoAddresses.AnalogOutput.GAS_FLOW_SETPOINT.getAddress(),
                convertGasFlowParameterToAnalogOutput(gasFlowVariable));
    }

    private class StartRunnable implements Runnable {

        @Override
        public void run() {

            try {
                setIntialGasFlow();
            } catch (IOException e1) {
                logService.log(LogService.LOG_ERROR, "Failed to start gas flow control. Couldn't set initial gas flow.",
                        e1);
                return;
            }
            try {
                openOutletNine();
            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR, "Starting gas flow control failed.", e);
                return;
            }

            startControlLoop();

        }

        private void setIntialGasFlow() throws IOException {
            gasFlowVariable = Double.valueOf(settingsService.getProperty(SettingsIds.INITIAL_GAS_FLOW_PARAMETER));
            mbtService.writeOutputRegister(WagoAddresses.AnalogOutput.GAS_FLOW_SETPOINT.getAddress(),
                    convertGasFlowParameterToAnalogOutput(desiredPressure));
        }

        private void openOutletNine() throws IOException {
            machineStateService.getOutletControl().openOutlet(Outlet.OUTLET_NINE);
        }

        private void startControlLoop() {
            int controlParameter = Integer.valueOf(settingsService.getProperty(SettingsIds.GAS_FLOW_CONTROL_PARAMETER));
            double controlParameterHysteresis = Double
                    .valueOf(settingsService.getProperty(SettingsIds.GAS_FLOW_HYSTERESIS_CONTROL_PARAMETER));

            long lastTick = System.nanoTime();
            while (true) {
                double chamberPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CHAMBER);
                if (state == State.STOPPED) {
                    logService.log(LogService.LOG_DEBUG, "Gas flow stopped during start phase");
                    break;
                }
                if (chamberPressure < desiredPressure * (1 + controlParameterHysteresis / 100.0)) {
                    gasFlowVariable += controlParameter;
                } else if (chamberPressure > desiredPressure * (1 - controlParameterHysteresis / 100.0)) {
                    gasFlowVariable -= controlParameter;
                } else {
                    setState(State.RUNNING);
                    break;
                }
                try {
                    writeGasFlowVariable();
                } catch (IOException e) {
                    logService.log(LogService.LOG_ERROR, "Failed to start Gas Flow Control!", e);
                    setState(State.STOPPED);
                    break;
                }

                // Thread speed control
                long waitTime = (long) (lastTick + (1 / CONTROL_THREAD_TICKRATE * 10e9) - System.nanoTime());
                if (waitTime > 0) {
                    try {
                        Thread.sleep((long) (waitTime * 1e6));
                    } catch (InterruptedException e) {
                        logService.log(LogService.LOG_ERROR,
                                "Starting of Gas Flow Control failed due to Interruption when waiting!");
                        setState(State.STOPPED);
                        break;
                    }
                }
                lastTick = System.nanoTime();
            }
        }
    }

}

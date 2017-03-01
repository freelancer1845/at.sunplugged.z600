package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.ParameterIds;
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

    public GasFlowControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        this.logService = MachineStateServiceImpl.getLogService();
        this.settingsService = MachineStateServiceImpl.getSettingsService();
        this.threadPoolService = MachineStateServiceImpl.getStandardThreadPoolService();
        this.mbtService = MachineStateServiceImpl.getMbtService();
        desiredPressure = Double.valueOf(settingsService.getProperty(ParameterIds.INITIAL_DESIRED_PRESSURE_GAS_FLOW));

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
        if (state == State.STARTING || state == State.RUNNING) {
            logService.log(LogService.LOG_DEBUG,
                    "Tried to start gasflow control, but state is: \"" + state.name() + "\"");
            return;
        }
        if (checkStartLimitPressureControl() == false) {
            logService.log(LogService.LOG_WARNING,
                    "Won't start gasflow control since pressure start limit is not reached...");
            return;
        }
        setState(State.STARTING);
    }

    @Override
    public void stopGasFlowControl() {
        if (state == State.STOPPED) {
            logService.log(LogService.LOG_DEBUG,
                    "Tried to stop gasflow control, but state is: \"" + state.name() + "\"");
            return;
        }
        setState(State.STOPPED);
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (state == State.RUNNING) {
            if (event.getType().equals(Type.PRESSURE_CHANGED)) {
                PressureChangedEvent pressureEvent = (PressureChangedEvent) event;
                if (pressureEvent.getSite().equals(PressureMeasurementSite.CHAMBER)) {
                    if ((double) pressureEvent.getValue() > 0.03) {
                        setState(State.STOPPED);
                    }
                }
            }
        }
    }

    private boolean checkStartLimitPressureControl() {
        double chamberPressure = machineStateService.getPressureMeasurmentControl()
                .getCurrentValue(PressureMeasurementSite.CHAMBER);
        double allowedPressure = Double.valueOf(settingsService.getProperty(ParameterIds.PRESSURE_CONTROL_LOWER_LIMIT));
        return (chamberPressure <= allowedPressure) == true;
    }

    private int convertGasFlowParameterToAnalogOutput(double parameter) {
        double outputValue = parameter;
        if (parameter > ANALOG_OUTPUT_MAX) {
            outputValue = ANALOG_OUTPUT_MAX;
        } else if (parameter < 0) {
            outputValue = 0;
        }
        return (int) Math.round((outputValue * 4095 / ANALOG_OUTPUT_MAX));
    }

    private void setState(State state) {
        this.state = state;
        if (state == State.STARTING) {
            threadPoolService.execute(new StartRunnable());
        } else if (state == State.STOPPED) {
            stopGasflowControl();
        }
        machineStateService.fireMachineStateEvent(new GasFlowEvent(state));
    }

    private void stopGasflowControl() {
        try {
            machineStateService.getOutletControl().closeOutlet(Outlet.OUTLET_NINE);
            gasFlowVariable = 0;
            writeGasFlowVariable();
        } catch (IOException e) {
            logService.log(LogService.LOG_ERROR, "Unexpected exception when stopping gasflow control!", e);
        }
    }

    private void writeGasFlowVariable() throws IOException {
        mbtService.writeOutputRegister(WagoAddresses.AnalogOutput.GAS_FLOW_SETPOINT.getAddress(),
                convertGasFlowParameterToAnalogOutput(gasFlowVariable));
    }

    // This starts the gasflow control and puts the pressure to the initial
    // definied value
    // When finished the State == State.RUNNING
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
            gasFlowVariable = Double.valueOf(settingsService.getProperty(ParameterIds.INITIAL_GAS_FLOW_PARAMETER));
            mbtService.writeOutputRegister(WagoAddresses.AnalogOutput.GAS_FLOW_SETPOINT.getAddress(),
                    convertGasFlowParameterToAnalogOutput(desiredPressure));
        }

        private void openOutletNine() throws IOException {
            machineStateService.getOutletControl().openOutlet(Outlet.OUTLET_NINE);
        }

        private void startControlLoop() {
            int controlParameter = Integer
                    .valueOf(settingsService.getProperty(ParameterIds.GAS_FLOW_CONTROL_PARAMETER));
            double controlParameterHysteresis = Double
                    .valueOf(settingsService.getProperty(ParameterIds.GAS_FLOW_HYSTERESIS_CONTROL_PARAMETER));

            long lastTick = System.nanoTime();
            while (true) {
                double chamberPressure = machineStateService.getPressureMeasurmentControl()
                        .getCurrentValue(PressureMeasurementSite.CHAMBER);
                if (state == State.STOPPED) {
                    logService.log(LogService.LOG_DEBUG, "Gas flow control stopped.");
                    break;
                }
                setState(State.RUNNING);
                if (desiredPressure - chamberPressure > controlParameterHysteresis) {
                    gasFlowVariable += controlParameter;
                } else if ((chamberPressure - desiredPressure) > controlParameterHysteresis) {
                    gasFlowVariable -= controlParameter;
                }
                try {
                    writeGasFlowVariable();
                } catch (IOException e) {
                    logService.log(LogService.LOG_ERROR, "Failed to start Gas Flow Control!", e);
                    setState(State.STOPPED);
                    break;
                }

                // Thread speed control
                long waitTime = (long) (lastTick + ((1.0 / CONTROL_THREAD_TICKRATE) * 10e9) - System.nanoTime());
                if (waitTime > 0) {
                    try {
                        Thread.sleep((long) (waitTime / 1e6));
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

    @Override
    public State getState() {
        return state;
    }

}

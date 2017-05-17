package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.common.utils.Conversion;
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

    private static final int ANALOG_INPUT_GAS_FLOW_MAX = 400;

    private static final int CONTROL_THREAD_TICKRATE = 5;

    private final MachineStateService machineStateService;

    private StandardThreadPoolService threadPoolService;

    private LogService logService;

    private SettingsService settingsService;

    private MbtService mbtService;

    private State state = State.STOP;

    private double desiredPressure = 0;

    private double gasFlowVariable = 0;

    private List<Double> stableControlList = new ArrayList<>();

    private Future<?> stopFuture;

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
        if (state == State.STARTING || state == State.RUNNING_STABLE || state == State.ADJUSTING) {
            logService.log(LogService.LOG_DEBUG,
                    "Tried to start gasflow control, but state is: \"" + state.name() + "\"");
            return;
        }
        if (checkStartLimitPressureControl() == false) {
            logService.log(LogService.LOG_WARNING,
                    "Won't start gasflow control since pressure start limit is not reached...");
            return;
        }
        if (stopFuture != null && stopFuture.isDone() == false) {
            stopFuture.cancel(true);
        }
        setState(State.STARTING);
    }

    @Override
    public void stopGasFlowControl() {
        if (state == State.STOP || state == State.STOPPING) {
            logService.log(LogService.LOG_DEBUG,
                    "Tried to stop gasflow control, but state is: \"" + state.name() + "\"");
            return;
        }
        setState(State.STOP);
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (state == State.RUNNING_STABLE) {
            if (event.getType().equals(Type.PRESSURE_CHANGED)) {
                PressureChangedEvent pressureEvent = (PressureChangedEvent) event;
                if (pressureEvent.getOrigin().equals(PressureMeasurementSite.CHAMBER)) {
                    if ((double) pressureEvent.getValue() > 0.03) {
                        setState(State.STOP);
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

        if (state == State.STARTING) {
            threadPoolService.execute(new StartRunnable());
            this.state = state;
        } else if (state == State.STOP) {
            logService.log(LogService.LOG_INFO, "Stopping gasflow control.");
            this.state = State.STOPPING;
            stopFuture = threadPoolService.submit(() -> {
                try {
                    stopGasflowControl();
                    this.state = state;
                } catch (IOException e) {
                    logService.log(LogService.LOG_ERROR, "Unexpected exception when stopping gasflow control!", e);
                } catch (InterruptedException e) {
                    logService.log(LogService.LOG_DEBUG, "Stopping of gasflow control interrupted.");
                }
            });
        } else {
            this.state = state;
        }

        machineStateService.fireMachineStateEvent(new GasFlowEvent(this.state));
    }

    private void stopGasflowControl() throws IOException, InterruptedException {

        gasFlowVariable = 0;
        writeGasFlowVariable();
        long waitTime = (long) settingsService.getPropertAsDouble(ParameterIds.GASFLOW_CONTROL_WAIT_TIME);
        logService.log(LogService.LOG_INFO, String.format("Waiting %d seconds until closing outlet nine.", waitTime));
        Thread.sleep(waitTime * 1000);
        machineStateService.getOutletControl().closeOutlet(Outlet.OUTLET_NINE);

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
                if (state == State.STOP || state == State.STOPPING) {
                    logService.log(LogService.LOG_DEBUG, "(CONTROL LOOP) Gas flow control stopped.");
                    break;
                }
                if (desiredPressure - chamberPressure > controlParameterHysteresis) {
                    gasFlowVariable += controlParameter;
                } else if ((chamberPressure - desiredPressure) > controlParameterHysteresis) {
                    gasFlowVariable -= controlParameter;
                }
                if (isStable(desiredPressure - chamberPressure)) {
                    setState(State.RUNNING_STABLE);
                } else {
                    setState(State.ADJUSTING);
                }
                try {
                    writeGasFlowVariable();
                } catch (IOException e) {
                    logService.log(LogService.LOG_ERROR, "Failed to start Gas Flow Control!", e);
                    setState(State.STOP);
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
                        setState(State.STOP);
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

    private boolean isStable(double differenceBetweenSetpointAndCurrent) {
        stableControlList.add(Math.abs(differenceBetweenSetpointAndCurrent));
        if (stableControlList.size() > 5) {
            stableControlList.remove(0);
        }
        double mean = stableControlList.stream().mapToDouble(Double::valueOf).average().orElse(0.1);
        if (mean < 0.0005) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public double getGasflowDesiredPressure() {
        return desiredPressure;
    }

    @Override
    public double getCurrentGasFlowInSccm() {
        return Conversion.clipConversionIn(machineStateService.getAnalogInputState(AnalogInput.GAS_FLOW), 0,
                ANALOG_INPUT_GAS_FLOW_MAX);
    }

}

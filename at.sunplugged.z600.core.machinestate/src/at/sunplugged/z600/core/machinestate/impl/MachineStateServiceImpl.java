package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.GasFlowControl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.WaterControl;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.impl.eventhandling.MachineStateEventHandler;
import at.sunplugged.z600.mbt.api.MbtService;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

@Component
public class MachineStateServiceImpl implements MachineStateService {

    /** Update tickrate. */
    private static final int INPUT_UPDATE_TICKRATE = 10;

    private static LogService logService;

    private static MbtService mbtService;

    private static SrmCommunicator srmCommunicator;

    private static StandardThreadPoolService standardThreadPoolService;

    private static SettingsService settingsService;

    private static EventAdmin eventAdmin;

    private OutletControl outletControl;

    private PumpRegistry pumpControl;

    private WaterControl waterControl;

    private PowerSourceRegistry powerSourceRegistry;

    private PressureMeasurement preasureMeasurement;

    private GasFlowControl gasFlowControl;

    private List<Boolean> digitalOutputState = new ArrayList<>();

    private List<Boolean> digitalInputState = new ArrayList<>();

    private List<Integer> analogOutputState = new ArrayList<>();

    private List<Integer> analogInputState = new ArrayList<>();

    private MachineStateEventHandler machineStateEventHandler;

    private InputUpdaterThread updaterThread;

    private Collection<MachineEventHandler> registeredEventHandler = new ConcurrentLinkedQueue<>();

    @Activate
    protected void activateMachineStateService(BundleContext context) {
        this.outletControl = new OutletControlImpl(this);
        this.pumpControl = new PumpRegisterImpl(this);
        this.waterControl = new WaterControlImpl(this);
        this.powerSourceRegistry = new PowerSourceRegistryImpl(this);
        this.preasureMeasurement = new PreasureMeasurementImpl(this);
        this.gasFlowControl = new GasFlowControlImpl(this);
        this.machineStateEventHandler = new MachineStateEventHandler(this);
        registerMachineEventHandler(machineStateEventHandler);
        registerMachineEventHandler((MachineEventHandler) outletControl);
        fillStateListsWithZeors();
    }

    @Deactivate
    protected void deactivateMachineStateService() {
        stop();
    }

    @Override
    public void start() {
        if (mbtService.isConnected() == false) {
            logService.log(LogService.LOG_ERROR, "Can't start machineStateService since mbt is not connected!");
            return;
        }
        if (this.updaterThread == null) {
            this.updaterThread = new InputUpdaterThread();
            this.updaterThread.start();
        } else if (updaterThread.isRunning()) {
            this.updaterThread = new InputUpdaterThread();
            this.updaterThread.start();
        } else {
            logService.log(LogService.LOG_DEBUG,
                    "Tried to start MachineStateService updater thread although it is already running.");
        }

    }

    @Override
    public void stop() {
        if (this.updaterThread != null) {
            if (this.updaterThread.isRunning()) {
                updaterThread.stop();
                updaterThread = null;
            }
        }

    }

    @Override
    public PumpRegistry getPumpRegistry() {
        return pumpControl;
    }

    @Override
    public OutletControl getOutletControl() {
        return outletControl;
    }

    @Override
    public WaterControl getWaterControl() {
        return waterControl;
    }

    @Override
    public PowerSourceRegistry getPowerSourceRegistry() {
        return powerSourceRegistry;
    }

    @Override
    public GasFlowControl getGasFlowControl() {
        return gasFlowControl;
    }

    @Override
    public PressureMeasurement getPressureMeasurmentControl() {
        return preasureMeasurement;
    }

    @Override
    public boolean getDigitalOutputState(DigitalOutput digitalOutput) {
        updateDigitalOutputState();
        return digitalOutputState.get(digitalOutput.getAddress());
    }

    @Override
    public boolean getDigitalInputState(DigitalInput digitalInput) {
        updateDigitalInputState();
        return digitalInputState.get(digitalInput.getAddress());
    }

    @Override
    public Integer getAnalogOutputState(AnalogOutput analogOutput) {
        updateAnalogOutputState();
        return analogOutputState.get(analogOutput.getAddress());
    }

    @Override
    public Integer getAnalogInputState(AnalogInput analogInput) {
        updateAnalogInputState();
        return analogInputState.get(analogInput.getAddress());
    }

    @Override
    public void fireMachineStateEvent(MachineStateEvent event) {
        standardThreadPoolService.execute(new Runnable() {

            @Override
            public void run() {
                for (MachineEventHandler handler : registeredEventHandler) {
                    handler.handleEvent(event);
                }
            }
        });
    }

    @Override
    public void registerMachineEventHandler(MachineEventHandler eventHandler) {
        if (!this.registeredEventHandler.contains(eventHandler)) {
            this.registeredEventHandler.add(eventHandler);
        }

    }

    @Override
    public void unregisterMachineEventHandler(MachineEventHandler eventHandler) {
        if (this.registeredEventHandler.contains(eventHandler)) {
            this.registeredEventHandler.remove(eventHandler);
        }
    }

    // #################################
    // Non Interface Functions
    // #################################

    public void updateDigitalOutputState() {
        try {
            List<Boolean> currentState = mbtService.readDigOuts(0, WagoAddresses.DIGITAL_OUTPUT_MAX_ADDRESS + 1);
            for (int i = 0; i < currentState.size() - 1; i++) {
                try {
                    boolean previousDigitalOutputState = digitalOutputState.get(i);
                    if (currentState.get(i) != previousDigitalOutputState) {
                        DigitalOutput digitalOutput = DigitalOutput.getByAddress(i);
                        if (digitalOutput != null) {
                            fireMachineStateEvent(new MachineStateEvent(Type.DIGITAL_OUTPUT_CHANGED, digitalOutput,
                                    currentState.get(i)));
                        } else {
                            logService.log(LogService.LOG_DEBUG, "Unkown Digital Output changed: " + i);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    DigitalOutput digitalOutput = DigitalOutput.getByAddress(i);
                    if (digitalOutput != null) {
                        fireMachineStateEvent(
                                new MachineStateEvent(Type.DIGITAL_OUTPUT_CHANGED, digitalOutput, currentState.get(i)));
                    }
                }

            }
            synchronized (digitalOutputState) {
                digitalOutputState = currentState;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateAnalogOutputState() {
        try {
            List<Integer> currentState = mbtService.readOutputRegister(0, WagoAddresses.ANALOG_OUTPUT_MAX_ADDRESS + 1);
            for (int i = 0; i < currentState.size() - 1; i++) {
                try {
                    int previousAnalogOutputState = analogOutputState.get(i);
                    if (currentState.get(i) != previousAnalogOutputState) {
                        AnalogOutput analogOutput = AnalogOutput.getByAddress(i);
                        if (analogOutput != null) {
                            fireMachineStateEvent(new MachineStateEvent(Type.ANALOG_OUTPUT_CHANGED, analogOutput,
                                    currentState.get(i)));
                        } else {
                            logService.log(LogService.LOG_DEBUG, "Unkown Analog Output changed: " + i);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    AnalogOutput analogOutput = AnalogOutput.getByAddress(i);
                    if (analogOutput != null) {
                        fireMachineStateEvent(
                                new MachineStateEvent(Type.ANALOG_OUTPUT_CHANGED, analogOutput, currentState.get(i)));
                    }
                }

            }
            synchronized (analogOutputState) {
                analogOutputState = currentState;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateDigitalInputState() {
        try {
            List<Boolean> currentState = mbtService.readDigIns(0, WagoAddresses.DIGITAL_INPUT_MAX_ADDRESS + 1);
            for (int i = 0; i < currentState.size() - 1; i++) {
                try {
                    boolean previousState = digitalInputState.get(i);
                    if (currentState.get(i) != previousState) {
                        DigitalInput digitalInput = DigitalInput.getByAddress(i);
                        if (digitalInput != null) {
                            fireMachineStateEvent(new MachineStateEvent(Type.DIGITAL_INPUT_CHANGED,
                                    DigitalInput.getByAddress(i), currentState.get(i)));
                        } else {
                            logService.log(LogService.LOG_DEBUG, "Unkown Digital Input changed: " + i);
                        }

                    }
                } catch (IndexOutOfBoundsException e) {
                    DigitalInput digitalInput = DigitalInput.getByAddress(i);
                    if (digitalInput != null) {
                        fireMachineStateEvent(new MachineStateEvent(Type.DIGITAL_INPUT_CHANGED,
                                DigitalInput.getByAddress(i), currentState.get(i)));
                    }
                }

            }
            synchronized (digitalInputState) {
                digitalInputState = currentState;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateAnalogInputState() {
        try {
            List<Integer> currentState = mbtService.readInputRegister(0, WagoAddresses.ANALOG_INPUT_MAX_ADDRESS + 1);
            for (int i = 0; i < currentState.size() - 1; i++) {
                try {
                    int previousAnalogInputState = analogInputState.get(i);
                    if (!currentState.get(i).equals(previousAnalogInputState)) {
                        AnalogInput analogInput = AnalogInput.getByAddress(i);
                        if (analogInput != null) {
                            fireMachineStateEvent(new MachineStateEvent(Type.ANALOG_INPUT_CHANGED,
                                    AnalogInput.getByAddress(i), currentState.get(i)));
                        } else {
                            logService.log(LogService.LOG_DEBUG, "Unkown Analog Input changed: " + i);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    AnalogInput analogInput = AnalogInput.getByAddress(i);
                    if (analogInput != null) {
                        fireMachineStateEvent(new MachineStateEvent(Type.ANALOG_INPUT_CHANGED,
                                AnalogInput.getByAddress(i), currentState.get(i)));
                    }
                }

            }
            synchronized (analogInputState) {
                analogInputState = currentState;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillStateListsWithZeors() {
        for (int i = 0; i < WagoAddresses.ANALOG_INPUT_MAX_ADDRESS; i++) {
            analogInputState.add(0);
        }
        for (int i = 0; i < WagoAddresses.ANALOG_OUTPUT_MAX_ADDRESS; i++) {
            analogOutputState.add(0);
        }
        for (int i = 0; i < WagoAddresses.DIGITAL_INPUT_MAX_ADDRESS; i++) {
            digitalInputState.add(false);
        }
        for (int i = 0; i < WagoAddresses.DIGITAL_OUTPUT_MAX_ADDRESS; i++) {
            digitalOutputState.add(false);
        }
    }

    // #################################
    // Declarative Service Specific Code
    // #################################

    @Reference(unbind = "unsetLogService")
    public synchronized void setLogService(LogService logService) {
        MachineStateServiceImpl.logService = logService;
    }

    public synchronized void unsetLogService(LogService logService) {
        if (MachineStateServiceImpl.logService == logService) {
            MachineStateServiceImpl.logService = null;
        }
    }

    @Reference(unbind = "unsetMbtService")
    public synchronized void setMbtService(MbtService mbtController) {
        MachineStateServiceImpl.mbtService = mbtController;
    }

    public synchronized void unsetMbtService(MbtService mbtController) {
        if (MachineStateServiceImpl.mbtService == mbtController) {
            MachineStateServiceImpl.mbtService = null;
        }
    }

    @Reference(unbind = "unsetSrmCommunicator")
    public synchronized void setSrmCommunicator(SrmCommunicator srmCommunicator) {
        MachineStateServiceImpl.srmCommunicator = srmCommunicator;
    }

    public synchronized void unsetSrmCommunicator(SrmCommunicator srmCommunicator) {
        if (MachineStateServiceImpl.srmCommunicator == srmCommunicator) {
            MachineStateServiceImpl.srmCommunicator = null;
        }
    }

    @Reference(unbind = "unsetStandardThreadPoolService")
    public synchronized void setStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        MachineStateServiceImpl.standardThreadPoolService = standardThreadPoolService;
    }

    public synchronized void unsetStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        if (MachineStateServiceImpl.standardThreadPoolService == standardThreadPoolService) {
            MachineStateServiceImpl.standardThreadPoolService = null;
        }
    }

    @Reference(unbind = "unbindSettingsService")
    public synchronized void bindSettingsService(SettingsService settingsService) {
        MachineStateServiceImpl.settingsService = settingsService;
    }

    public synchronized void unbindSettingsService(SettingsService settingsService) {
        if (MachineStateServiceImpl.settingsService == settingsService) {
            MachineStateServiceImpl.settingsService = null;
        }
    }

    private class InputUpdaterThread {

        private boolean running = false;

        private Thread thread;

        public InputUpdaterThread() {
            thread = new Thread() {
                @Override
                public void run() {
                    long lastTime = System.nanoTime();
                    try {

                        while (running) {
                            updateDigitalInputState();
                            updateAnalogInputState();
                            updateDigitalOutputState();
                            updateAnalogOutputState();
                            long now = System.nanoTime();
                            long delta = now - lastTime;
                            long timeToSleep = (long) (1.0 / INPUT_UPDATE_TICKRATE * 1000 - delta / 1000000.0);
                            if (timeToSleep > 0) {
                                try {
                                    Thread.sleep(timeToSleep);
                                } catch (InterruptedException e) {
                                    logService.log(LogService.LOG_DEBUG, "Updater Thread interrupted.", e);
                                }
                            }
                            lastTime = System.nanoTime();
                        }
                    } catch (Exception e) {
                        logService.log(LogService.LOG_DEBUG, "Unhandle Loopexception Machine State Updater thread", e);
                    }

                }
            };
            thread.setName("MachineStateUpdaterThread");
        }

        public void start() {
            running = true;
            thread.start();
        }

        public void stop() {
            running = false;
        }

        public boolean isRunning() {
            return running;
        }

    }

    public static LogService getLogService() {
        return logService;
    }

    public static MbtService getMbtService() {
        return mbtService;
    }

    public static SrmCommunicator getSrmCommunicator() {
        return srmCommunicator;
    }

    public static StandardThreadPoolService getStandardThreadPoolService() {
        return standardThreadPoolService;
    }

    public static SettingsService getSettingsService() {
        return settingsService;
    }

    @Reference(unbind = "unbindEventAdmin")
    public synchronized void bindEventAdmin(EventAdmin service) {
        eventAdmin = service;
    }

    public synchronized void unbindEventAdmin(EventAdmin service) {
        if (eventAdmin == service) {
            eventAdmin = null;
        }
    }

    public static EventAdmin getEventAdmin() {
        return eventAdmin;
    }

}

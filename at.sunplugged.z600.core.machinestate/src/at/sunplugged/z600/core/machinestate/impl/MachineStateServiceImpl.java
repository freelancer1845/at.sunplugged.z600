package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.core.machinestate.api.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.PumpControl;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.impl.eventhandling.MachineStateEventHandler;
import at.sunplugged.z600.mbt.api.MbtService;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

@Component
public class MachineStateServiceImpl implements MachineStateService {

    /** Value in ms. */
    private static final long UPDATE_INTERVAL_TIME = 250;

    /** Input update Tickrate. */
    private static final int INPUT_UPDATE_TICKRATE = 10;

    private DataService dataService;

    private LogService logService;

    private MbtService mbtController;

    private SrmCommunicator srmCommunicator;

    private StandardThreadPoolService standardThreadPoolService;

    private OutletControl outletControl;

    private PumpControl pumpControl;

    private List<Boolean> digitalOutputState = new ArrayList<>();

    private Long lastDigitalOutputUpdateTime = -UPDATE_INTERVAL_TIME;

    private List<Boolean> digitalInputState = new ArrayList<>();

    private Long lastDigitalInputUpdateTime = -UPDATE_INTERVAL_TIME;

    private List<Integer> analogOutputState = new ArrayList<>();

    private long lastAnalogOutputUpdateTime = -UPDATE_INTERVAL_TIME;

    private List<Integer> analogInputState = new ArrayList<>();

    private long lastAnalogInputUpdateTime = -UPDATE_INTERVAL_TIME;

    private MachineStateEventHandler machineStateEventHandler;

    private InputUpdaterThread updaterThread;

    private List<MachineEventHandler> registeredEventHandler = new ArrayList<>();

    @Activate
    protected void activateMachineStateService(BundleContext context) {
        this.outletControl = new OutletControlImpl(this);
        this.pumpControl = new PumpControlImpl(this, mbtController, logService);
        this.machineStateEventHandler = new MachineStateEventHandler(this);
        registerMachineEventHandler(machineStateEventHandler);
        this.updaterThread = new InputUpdaterThread();
        this.updaterThread.start();
    }

    @Deactivate
    protected void deactiveMachineStateService() {
        updaterThread.stop();
    }

    @Override
    public PumpControl getPumpControl() {
        return pumpControl;
    }

    @Override
    public OutletControl getOutletControl() {
        return outletControl;
    }

    @Override
    public List<Boolean> getDigitalOutputState() {
        if (System.currentTimeMillis() - lastDigitalOutputUpdateTime > UPDATE_INTERVAL_TIME) {
            updateDigitalOutputState();
        }
        return Collections.unmodifiableList(digitalOutputState);
    }

    @Override
    public List<Boolean> getDigitalInputState() {
        updateDigitalInputState();
        return Collections.unmodifiableList(digitalInputState);
    }

    @Override
    public List<Integer> getAnalogOutputState() {
        if (System.currentTimeMillis() - lastAnalogOutputUpdateTime > UPDATE_INTERVAL_TIME) {
            updateAnalogOutputState();
        }
        return Collections.unmodifiableList(analogOutputState);
    }

    @Override
    public List<Integer> getAnalogInputState() {
        updateAnalogInputState();
        return Collections.unmodifiableList(analogInputState);

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
            List<Boolean> currentState = mbtController.readDigOuts(0, WagoAddresses.DIGITAL_OUTPUT_MAX_ADDRESS + 1);
            synchronized (digitalOutputState) {
                digitalOutputState = currentState;
            }
            lastDigitalOutputUpdateTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateAnalogOutputState() {
        try {
            List<Integer> currentState = mbtController.readOutputRegister(0,
                    WagoAddresses.ANALOG_INPUT_MAX_ADDRESS + 1);
            synchronized (analogOutputState) {
                analogOutputState = currentState;
            }
            lastAnalogOutputUpdateTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDigitalInputState() {
        if (System.currentTimeMillis() - lastDigitalInputUpdateTime > UPDATE_INTERVAL_TIME) {
            try {
                List<Boolean> currentState = mbtController.readDigIns(0, WagoAddresses.DIGITAL_INPUT_MAX_ADDRESS + 1);
                for (int i = 0; i < currentState.size(); i++) {
                    if (currentState.get(i) != digitalInputState.get(i)) {
                        DigitalInput digitalInput = DigitalInput.getByAddress(i);
                        if (digitalInput != null) {
                            fireMachineStateEvent(
                                    new MachineStateEvent(Type.DIGITAL_INPUT_CHANGED, DigitalInput.getByAddress(i)));
                        } else {
                            logService.log(LogService.LOG_DEBUG, "Unkown Digital Input changed: " + i);
                        }

                    }
                }
                synchronized (digitalInputState) {
                    digitalInputState = currentState;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            lastDigitalInputUpdateTime = System.currentTimeMillis();
        }
    }

    public void updateAnalogInputState() {
        if (System.currentTimeMillis() - lastAnalogInputUpdateTime > UPDATE_INTERVAL_TIME) {
            try {
                List<Integer> currentState = mbtController.readInputRegister(0,
                        WagoAddresses.ANALOG_INPUT_MAX_ADDRESS + 1);
                for (int i = 0; i < currentState.size(); i++) {
                    if (currentState.get(i) != analogInputState.get(i)) {
                        fireMachineStateEvent(
                                new MachineStateEvent(Type.DIGITAL_INPUT_CHANGED, AnalogInput.getByAddress(i)));
                    }
                }
                synchronized (analogInputState) {
                    analogInputState = currentState;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        lastAnalogInputUpdateTime = System.currentTimeMillis();
    }

    // #################################
    // Declarative Service Specific Code
    // #################################

    @Reference(unbind = "unsetDataService")
    public synchronized void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public synchronized void unsetDataService(DataService dataService) {
        if (this.dataService == dataService) {
            this.dataService = null;
        }
    }

    @Reference(unbind = "unsetLogService")
    public synchronized void setLogService(LogService logService) {
        this.logService = logService;
    }

    public synchronized void unsetLogService(LogService logService) {
        if (this.logService == logService) {
            this.logService = null;
        }
    }

    @Reference(unbind = "unsetMBTController")
    public synchronized void setMBTController(MbtService mbtController) {
        this.mbtController = mbtController;
    }

    public synchronized void unsetMBTController(MbtService mbtController) {
        if (this.mbtController == mbtController) {
            this.mbtController = null;
        }
    }

    @Reference(unbind = "unsetSrmCommunicator")
    public synchronized void setSrmCommunicator(SrmCommunicator srmCommunicator) {
        this.srmCommunicator = srmCommunicator;
    }

    public synchronized void unsetSrmCommunicator(SrmCommunicator srmCommunicator) {
        if (this.srmCommunicator == srmCommunicator) {
            this.srmCommunicator = null;
        }
    }

    @Reference(unbind = "unsetStandardThreadPoolService")
    public synchronized void setStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        this.standardThreadPoolService = standardThreadPoolService;
    }

    public synchronized void unsetStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        if (this.standardThreadPoolService == standardThreadPoolService) {
            this.standardThreadPoolService = null;
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

                        digitalInputState = mbtController.readDigIns(0, WagoAddresses.DIGITAL_INPUT_MAX_ADDRESS + 1);
                        digitalOutputState = mbtController.readDigOuts(0, WagoAddresses.DIGITAL_OUTPUT_MAX_ADDRESS + 1);
                        analogInputState = mbtController.readInputRegister(0,
                                WagoAddresses.ANALOG_INPUT_MAX_ADDRESS + 1);
                        analogOutputState = mbtController.readOutputRegister(0,
                                WagoAddresses.ANALOG_OUTPUT_MAX_ADDRESS + 1);

                        while (running) {
                            updateDigitalInputState();
                            updateAnalogInputState();
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

    }

    public MbtService getMbtController() {
        return mbtController;
    }

    public DataService getDataService() {
        return dataService;
    }

    public LogService getLogService() {
        return logService;
    }

    public SrmCommunicator getSrmCommunicator() {
        return srmCommunicator;
    }

}

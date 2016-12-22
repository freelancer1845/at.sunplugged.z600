package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.util.ArrayList;
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
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.WaterControl;
import at.sunplugged.z600.core.machinestate.impl.eventhandling.MachineStateEventHandler;
import at.sunplugged.z600.mbt.api.MbtService;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

@Component
public class MachineStateServiceImpl implements MachineStateService {

    /** Update Tickrate. */
    private static final int INPUT_UPDATE_TICKRATE = 10;

    private static DataService dataService;

    private static LogService logService;

    private static MbtService mbtService;

    private static SrmCommunicator srmCommunicator;

    private static StandardThreadPoolService standardThreadPoolService;

    private OutletControl outletControl;

    private PumpControl pumpControl;

    private WaterControl waterControl;

    private List<Boolean> digitalOutputState = new ArrayList<>();

    private List<Boolean> digitalInputState = new ArrayList<>();

    private List<Integer> analogOutputState = new ArrayList<>();

    private List<Integer> analogInputState = new ArrayList<>();

    private MachineStateEventHandler machineStateEventHandler;

    private InputUpdaterThread updaterThread;

    private List<MachineEventHandler> registeredEventHandler = new ArrayList<>();

    @Activate
    protected void activateMachineStateService(BundleContext context) {
        this.outletControl = new OutletControlImpl(this);
        this.pumpControl = new PumpControlImpl(this, mbtService, logService);
        this.waterControl = new WaterControlImpl(this);
        this.machineStateEventHandler = new MachineStateEventHandler(this);
        registerMachineEventHandler(machineStateEventHandler);
        this.updaterThread = new InputUpdaterThread();
        this.updaterThread.start();
    }

    @Deactivate
    protected void deactivateMachineStateService() {
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
    public WaterControl getWaterControl() {
        return waterControl;
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
                if (currentState.get(i) != digitalOutputState.get(i)) {
                    DigitalOutput digitalOutput = DigitalOutput.getByAddress(i);
                    if (digitalOutput != null) {
                        fireMachineStateEvent(
                                new MachineStateEvent(Type.DIGITAL_OUTPUT_CHANGED, digitalOutput, currentState.get(i)));
                    } else {
                        logService.log(LogService.LOG_DEBUG, "Unkown Digital Output changed: " + i);
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
                if (currentState.get(i) != analogOutputState.get(i)) {
                    AnalogOutput analogOutput = AnalogOutput.getByAddress(i);
                    if (analogOutput != null) {
                        fireMachineStateEvent(
                                new MachineStateEvent(Type.ANALOG_OUTPUT_CHANGED, analogOutput, currentState.get(i)));
                    } else {
                        logService.log(LogService.LOG_DEBUG, "Unkown Analog Output changed: " + i);
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
                if (currentState.get(i) != digitalInputState.get(i)) {
                    DigitalInput digitalInput = DigitalInput.getByAddress(i);
                    if (digitalInput != null) {
                        fireMachineStateEvent(new MachineStateEvent(Type.DIGITAL_INPUT_CHANGED,
                                DigitalInput.getByAddress(i), currentState.get(i)));
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
    }

    public void updateAnalogInputState() {
        try {
            List<Integer> currentState = mbtService.readInputRegister(0, WagoAddresses.ANALOG_INPUT_MAX_ADDRESS + 1);
            for (int i = 0; i < currentState.size() - 1; i++) {
                if (currentState.get(i) != analogInputState.get(i)) {
                    fireMachineStateEvent(new MachineStateEvent(Type.ANALOG_INPUT_CHANGED, AnalogInput.getByAddress(i),
                            currentState.get(i)));
                }
            }
            synchronized (analogInputState) {
                analogInputState = currentState;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // #################################
    // Declarative Service Specific Code
    // #################################

    @Reference(unbind = "unsetDataService")
    public synchronized void setDataService(DataService dataService) {
        MachineStateServiceImpl.dataService = dataService;
    }

    public synchronized void unsetDataService(DataService dataService) {
        if (MachineStateServiceImpl.dataService == dataService) {
            MachineStateServiceImpl.dataService = null;
        }
    }

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

    private class InputUpdaterThread {

        private boolean running = false;

        private Thread thread;

        public InputUpdaterThread() {
            thread = new Thread() {
                @Override
                public void run() {
                    long lastTime = System.nanoTime();
                    try {

                        digitalInputState = mbtService.readDigIns(0, WagoAddresses.DIGITAL_INPUT_MAX_ADDRESS + 1);
                        digitalOutputState = mbtService.readDigOuts(0, WagoAddresses.DIGITAL_OUTPUT_MAX_ADDRESS + 1);
                        analogInputState = mbtService.readInputRegister(0, WagoAddresses.ANALOG_INPUT_MAX_ADDRESS + 1);
                        analogOutputState = mbtService.readOutputRegister(0,
                                WagoAddresses.ANALOG_OUTPUT_MAX_ADDRESS + 1);

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

    }

    public static DataService getDataService() {
        return dataService;
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

}

package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataService;
import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.core.machinestate.api.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.PumpControl;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses;
import at.sunplugged.z600.mbt.api.MBTController;
import at.sunplugged.z600.srm50.api.SrmCommunicator;

@Component
public class MachineStateServiceImpl implements MachineStateService {

    /** Value in ms. */
    private static final long UPDATE_INTERVAL_TIME = 250;

    private DataService dataService;

    private LogService logService;

    private MBTController mbtController;

    private SrmCommunicator srmCommunicator;

    private StandardThreadPoolService standardThreadPoolService;

    private final OutletControl outletControl;

    private final PumpControl pumpControl;

    private List<Boolean> digitalOutputState = new ArrayList<>();

    private Long lastDigitalOutputUpdateTime = -UPDATE_INTERVAL_TIME;

    private List<Boolean> digitalInputState = new ArrayList<>();

    private Long lastDigitalInputUpdateTime = -UPDATE_INTERVAL_TIME;

    private List<Integer> analogOutputState = new ArrayList<>();

    private long lastAnalogOutputUpdateTime = -UPDATE_INTERVAL_TIME;

    private List<Integer> analogInputState = new ArrayList<>();

    private long lastAnalogInputUpdateTime = -UPDATE_INTERVAL_TIME;

    public MachineStateServiceImpl() {
        this.outletControl = new OutletControlImpl(this, mbtController);
        this.pumpControl = new PumpControlImpl(this, mbtController, logService, standardThreadPoolService);
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
            try {
                List<Boolean> currentState = mbtController.readDigOuts(0, WagoAddresses.DIGITAL_OUTPUT_MAX_ADDRESS + 1);
                synchronized (digitalOutputState) {
                    digitalOutputState = currentState;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            lastDigitalOutputUpdateTime = System.currentTimeMillis();
        }
        return Collections.unmodifiableList(digitalOutputState);
    }

    @Override
    public List<Boolean> getDigitalInputState() {
        if (System.currentTimeMillis() - lastDigitalInputUpdateTime > UPDATE_INTERVAL_TIME) {
            try {
                List<Boolean> currentState = mbtController.readDigIns(0, WagoAddresses.DIGITAL_INPUT_MAX_ADDRESS + 1);
                synchronized (digitalInputState) {
                    digitalInputState = currentState;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            lastDigitalInputUpdateTime = System.currentTimeMillis();
        }
        return Collections.unmodifiableList(digitalInputState);
    }

    @Override
    public List<Integer> getAnalogOutputState() {
        if (System.currentTimeMillis() - lastAnalogOutputUpdateTime > UPDATE_INTERVAL_TIME) {
            try {
                List<Integer> currentState = mbtController.readOutputRegister(0,
                        WagoAddresses.ANALOG_INPUT_MAX_ADDRESS + 1);
                synchronized (analogOutputState) {
                    analogOutputState = currentState;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            lastAnalogOutputUpdateTime = System.currentTimeMillis();
        }
        return Collections.unmodifiableList(analogOutputState);
    }

    @Override
    public List<Integer> getAnalogInputState() {
        if (System.currentTimeMillis() - lastAnalogInputUpdateTime > UPDATE_INTERVAL_TIME) {
            try {
                List<Integer> currentState = mbtController.readInputRegister(0,
                        WagoAddresses.ANALOG_INPUT_MAX_ADDRESS + 1);
                synchronized (analogInputState) {
                    analogInputState = currentState;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            lastAnalogInputUpdateTime = System.currentTimeMillis();
        }
        return Collections.unmodifiableList(analogInputState);
    }

    @Override
    public void fireMachineStateEvent(MachineStateEvent event) {
        // TODO Auto-generated method stub

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
    public synchronized void setMBTController(MBTController mbtController) {
        this.mbtController = mbtController;
    }

    public synchronized void unsetMBTController(MBTController mbtController) {
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

}

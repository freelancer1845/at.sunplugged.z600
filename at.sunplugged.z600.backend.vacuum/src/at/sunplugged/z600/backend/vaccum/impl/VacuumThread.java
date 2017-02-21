package at.sunplugged.z600.backend.vaccum.impl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.Pump.PumpState;
import at.sunplugged.z600.core.machinestate.api.Pump;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FutureEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;

public class VacuumThread extends Thread {

    private enum VacuumStates {
    	CANCELED,
        FAILED,
    	STARTING_PRE_PUMPS,
    	PRE_EVACUATE_CHAMBER;
    }

    private VacuumStates vacuumState;

    private MachineStateService machineStateService;
    
    private OutletControl outletControl;
    
    private PumpRegistry pumpRegistry;

    private LogService logService;
    
    private SettingsService settings;

    private volatile boolean cancel = false;
    
    private Throwable lastException;
    
    public VacuumThread() {
        this.machineStateService = VacuumServiceImpl.getMachineStateService();
        this.logService = VacuumServiceImpl.getLogService();
        this.settings = VacuumServiceImpl.getSettingsService();
        
        this.outletControl = machineStateService.getOutletControl();
        this.pumpRegistry = machineStateService.getPumpRegistry();
    }
    
    public void cancel() {
    	cancel = true;
    	vacuumState = VacuumStates.CANCELED;
    	this.interrupt();
    }

    @Override
    public void run() {
    	while (!cancel) {
    		try {
        		switch (vacuumState) {
                case STARTING_PRE_PUMPS:
                	if (isCanceled()) {
                		break;
                	}
    				startPumps();
    				stateSelector();
                	break;
                case PRE_EVACUATE_CHAMBER:
                	if (isCanceled()) {
                		break;
                	}
                	preEvacuateChamber();
                	stateSelector();
                	break;
                	
                case FAILED:
                	 // TODO : Implement what happens on fail
                	break;
                case CANCELED:
                	// TODO : Implement what happens on cancel
                	break;
        		}
        		
        	} catch (Exception e) {
        		lastException = e;
        		logService.log(LogService.LOG_ERROR, "Unhandled Exception in VacuumService", e);
        		vacuumState = VacuumStates.FAILED;
        	}
    	}
    }
    
    private void stateSelector() {
    	if (isCanceled()) {
    		return;
    	}
    	switch (vacuumState) {
    	case STARTING_PRE_PUMPS:
    		double chamberPressure = machineStateService.getPressureMeasurmentControl().getCurrentValue(PressureMeasurementSite.CHAMBER);
    		double turboPumpTrigger = Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP)) * 0.8;
    		if (chamberPressure > turboPumpTrigger) {
    			vacuumState = VacuumStates.PRE_EVACUATE_CHAMBER;
    		}
    		break;
    	case PRE_EVACUATE_CHAMBER:
    		
    	}
    	
    }
    
    

	private boolean isCanceled() {
    	if (cancel == true) {
    		vacuumState = VacuumStates.CANCELED;
    	}
    	return cancel;
    }
	
	// Start Pumps Methods

    private void startPumps() throws IOException, InterruptedException, TimeoutException {
    	closeAllOutlets();
    	startPrePumpOne();
    	startPrePumpTwo();
    	startPrePumpRoots();
    	
    }
    
	private void startPrePumpOne() throws InterruptedException, TimeoutException {
    	Pump prePumpOne = pumpRegistry.getPump(PumpIds.PRE_PUMP_ONE);
    	if (prePumpOne.getState().equals(PumpState.ON)) {
    		return;
    	}
    	prePumpOne.startPump().get(10, TimeUnit.SECONDS);
	}
	
	private void startPrePumpTwo() throws InterruptedException, TimeoutException {
		Pump prePumpTwo = pumpRegistry.getPump(PumpIds.PRE_PUMP_TWO);
		if (prePumpTwo.getState().equals(PumpState.ON)) {
			return;
		}
		prePumpTwo.startPump().get(10, TimeUnit.SECONDS);
	}
	
	private void startPrePumpRoots() throws InterruptedException, TimeoutException {
		Pump prePumpRoots = pumpRegistry.getPump(PumpIds.PRE_PUMP_ROOTS);
		if (machineStateService.getDigitalInputState(DigitalInput.P_120_MBAR) == false) {
			FutureEvent p120TriggerEvent = new FutureEvent(machineStateService,
                    new MachineStateEvent(Type.DIGITAL_INPUT_CHANGED, DigitalInput.P_120_MBAR, true));
			p120TriggerEvent.get(10, TimeUnit.SECONDS);
		}
		prePumpRoots.startPump().get(10, TimeUnit.SECONDS);
	}
	
	// Pre-Evacuate Chamber Methods
	
	private void preEvacuateChamber() throws IOException {
		closeAllOutletsButSpecified(new Outlet[] { Outlet.OUTLET_FOUR, Outlet.OUTLET_FIVE });
		outletControl.openOutlet(Outlet.OUTLET_FOUR);
		outletControl.openOutlet(Outlet.OUTLET_THREE);
	}
	

	private void closeAllOutlets() throws IOException {
    	for (Outlet outlet : Outlet.values()) {
			outletControl.closeOutlet(outlet);
    	}
    }
	
	private void closeAllOutletsButSpecified(Outlet... outlets) throws IOException {
		for (Outlet outlet : Outlet.values()) {
			for (Outlet notToClose : outlets) {
				if (!outlet.equals(notToClose)) {
					outletControl.closeOutlet(outlet);
				} else {
					break;
				}
			}
    	}
	}

    

    
}

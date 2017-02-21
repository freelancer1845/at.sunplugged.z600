package at.sunplugged.z600.backend.vaccum.impl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.vaccum.api.VacuumService.Interlocks;
import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.settings.api.SettingsService;
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
		START_PRE_PUMP, EVACUATE_CRYO, EVACUATE_CHAMBER, START_COOLING, START_PUMPS, CRYO_RUNNING, CANCELED;
	}

	private CryoPumpsThreadState state = CryoPumpsThreadState.START_PRE_PUMP;

	private MachineStateService machineStateService;

	private OutletControl outletControl;

	private PumpRegistry pumpRegistry;

	private LogService logService;

	private SettingsService settings;

	private volatile boolean cancel = false;

	public CryoPumpsThread() {
		this.setName("CryoPumpsThread");

		this.machineStateService = VacuumServiceImpl.getMachineStateService();
		this.logService = VacuumServiceImpl.getLogService();
		this.settings = VacuumServiceImpl.getSettingsService();

		this.outletControl = machineStateService.getOutletControl();
		this.pumpRegistry = machineStateService.getPumpRegistry();

	}

	@Override
	public void run() {
		while (cancel == false) {
			try {
				switch (state) {
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
				case START_COOLING:
					if (isCanceled()) {
						break;
					}
					startCooling();
					stateSelector();
				}
			} catch (Exception e) {

			}

		}

	}

	private void stateSelector() {
		if (isCanceled()) {
			return;
		}
		switch (state) {
		case START_PRE_PUMP:
			state = CryoPumpsThreadState.EVACUATE_CRYO;
			break;
		case EVACUATE_CRYO:
			state = CryoPumpsThreadState.START_COOLING;
			break;
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
		FuturePressureReachedEvent cryoOneEvacuated = null;
		FuturePressureReachedEvent cryoTwoEvacuated = null;
		if (VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_ONE) == true) {
			outletControl.openOutlet(Outlet.OUTLET_FIVE);
			cryoOneEvacuated = new FuturePressureReachedEvent(machineStateService,
					PressureMeasurementSite.CRYO_PUMP_ONE,
					Double.valueOf(settings.getProperty(ParameterIds.CRYO_PUMP_PRESSURE_TRIGGER)) * 0.6);
		}
		if (VacuumServiceImpl.getInterlocksMap().get(Interlocks.CRYO_TWO) == true) {
			outletControl.openOutlet(Outlet.OUTLET_SIX);
			cryoTwoEvacuated = new FuturePressureReachedEvent(machineStateService,
					PressureMeasurementSite.CRYO_PUMP_TWO,
					Double.valueOf(settings.getProperty(ParameterIds.CRYO_PUMP_PRESSURE_TRIGGER)) * 0.6);
		}
		if (cryoOneEvacuated != null) {
			cryoOneEvacuated.get(10, TimeUnit.MINUTES);
		}
		if (cryoTwoEvacuated != null) {
			cryoTwoEvacuated.get(10, TimeUnit.MINUTES);
		}
	}

	private void startCooling() {
		// TODO Auto-generated method stub

	}

	private boolean isCanceled() {
		if (cancel == true) {
			state = CryoPumpsThreadState.CANCELED;
		}
		return cancel;
	}

}

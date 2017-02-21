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
import at.sunplugged.z600.core.machinestate.api.Pump;
import at.sunplugged.z600.core.machinestate.api.Pump.PumpState;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FutureEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.FuturePressureReachedEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;

public class TurboPumpThread extends Thread {

	private enum TurboPumpThreadState {
		START_PRE_PUMPS, EVACUATE_CHAMBER, EVACUATE_TURBO_PUMP, START_TURBO_PUMP, TURBO_PUMP_RUNNING, CANCELED;
	}

	private TurboPumpThreadState state = TurboPumpThreadState.START_PRE_PUMPS;

	private MachineStateService machineStateService;

	private OutletControl outletControl;

	private PumpRegistry pumpRegistry;

	private LogService logService;

	private SettingsService settings;

	private volatile boolean cancel = false;

	public TurboPumpThread() {
		this.machineStateService = VacuumServiceImpl.getMachineStateService();
		this.logService = VacuumServiceImpl.getLogService();
		this.settings = VacuumServiceImpl.getSettingsService();

		this.outletControl = machineStateService.getOutletControl();
		this.pumpRegistry = machineStateService.getPumpRegistry();

		this.setName("TurboPumpThread");
	}

	@Override
	public void run() {
		while (cancel == false) {
			try {
				switch (state) {
				case START_PRE_PUMPS:
					if (isCanceled()) {
						break;
					}
					startPumps();
					stateSelector();
					break;
				case EVACUATE_CHAMBER:
					if (isCanceled()) {
						break;
					}
					evacuateChamber();
					stateSelector();
				case EVACUATE_TURBO_PUMP:
					if (isCanceled()) {
						break;
					}
					evacuateTurboPump();
					stateSelector();
				case START_TURBO_PUMP:
					if (isCanceled()) {
						break;
					}
					startTurboPump();
					stateSelector();
				case TURBO_PUMP_RUNNING:
					while (true) {
						Thread.sleep(100);
						if (!state.equals(TurboPumpThreadState.TURBO_PUMP_RUNNING)) {
							break;
						}
					}
					break;
				case CANCELED:
					cancelProcess();
					break;
				}
			} catch (Exception e) {
				logService.log(LogService.LOG_ERROR, "Unhandled Exception in TurboPumpThread!", e);
				state = TurboPumpThreadState.CANCELED;
			}
		}
	}

	private void stateSelector() {
		if (isCanceled()) {
			return;
		}
		switch (state) {
		case START_PRE_PUMPS:
			double chamberPressure = machineStateService.getPressureMeasurmentControl()
					.getCurrentValue(PressureMeasurementSite.CHAMBER);
			double turboPumpTriggerLowered = Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP))
					* 0.8;

			double turboPumpPressure = machineStateService.getPressureMeasurmentControl()
					.getCurrentValue(PressureMeasurementSite.TURBO_PUMP);

			if (chamberPressure > turboPumpTriggerLowered) {
				state = TurboPumpThreadState.EVACUATE_CHAMBER;
			} else if (turboPumpPressure > turboPumpTriggerLowered) {
				state = TurboPumpThreadState.EVACUATE_TURBO_PUMP;
			} else {
				state = TurboPumpThreadState.START_TURBO_PUMP;
			}
			break;
		case EVACUATE_CHAMBER:
			state = TurboPumpThreadState.EVACUATE_TURBO_PUMP;
			break;
		case EVACUATE_TURBO_PUMP:
			chamberPressure = machineStateService.getPressureMeasurmentControl()
					.getCurrentValue(PressureMeasurementSite.CHAMBER);
			double turboPumpTrigger = Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP));
			if (chamberPressure > turboPumpTrigger) {
				state = TurboPumpThreadState.EVACUATE_CHAMBER;
			} else {
				state = TurboPumpThreadState.START_TURBO_PUMP;
			}
			break;
		case START_TURBO_PUMP:
			if (pumpRegistry.getPump(PumpIds.TURBO_PUMP).getState().equals(PumpState.ON)) {
				state = TurboPumpThreadState.TURBO_PUMP_RUNNING;
			} else {
				state = TurboPumpThreadState.START_PRE_PUMPS;
			}
			break;
		default:
			logService.log(LogService.LOG_ERROR, "StateSelector of TurboPumpThread is in an unexpected state: \""
					+ state.name() + "\"! Canceling...");
			cancel = true;
			break;
		}
	}

	private void startPumps() throws IOException, InterruptedException, TimeoutException {
		VacuumUtils.closeAllOutlets(outletControl);
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

	private void evacuateChamber() throws IOException, InterruptedException, TimeoutException {
		outletControl.closeOutlet(Outlet.OUTLET_ONE);
		outletControl.closeOutlet(Outlet.OUTLET_TWO);
		outletControl.closeOutlet(Outlet.OUTLET_NINE);

		Thread.sleep(500);

		outletControl.openOutlet(Outlet.OUTLET_THREE);
		FuturePressureReachedEvent turboPumpPressureTrigger = new FuturePressureReachedEvent(machineStateService,
				PressureMeasurementSite.CHAMBER,
				Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP)) * 0.8);
		turboPumpPressureTrigger.get(10, TimeUnit.MINUTES);
	}

	private void evacuateTurboPump() throws IOException, InterruptedException, TimeoutException {
		outletControl.closeOutlet(Outlet.OUTLET_ONE);
		outletControl.closeOutlet(Outlet.OUTLET_THREE);

		Thread.sleep(500);

		outletControl.openOutlet(Outlet.OUTLET_TWO);
		FuturePressureReachedEvent turboPumpChamberPressureTrigger = new FuturePressureReachedEvent(machineStateService,
				PressureMeasurementSite.TURBO_PUMP,
				Double.valueOf(settings.getProperty(ParameterIds.START_TRIGGER_TURBO_PUMP)) * 0.8);
		turboPumpChamberPressureTrigger.get(10, TimeUnit.MINUTES);

	}

	private void startTurboPump() throws InterruptedException, TimeoutException {
		Pump turboPump = pumpRegistry.getPump(PumpIds.TURBO_PUMP);
		if (turboPump.getState().equals(PumpState.OFF)) {
			turboPump.startPump().get(3, TimeUnit.MINUTES);
		}
	}

	private boolean isCanceled() {
		if (cancel == true) {
			state = TurboPumpThreadState.CANCELED;
		}
		return cancel;
	}

	private void cancelProcess() throws IOException {
		outletControl.closeOutlet(Outlet.OUTLET_ONE);
		outletControl.closeOutlet(Outlet.OUTLET_THREE);
		pumpRegistry.getPump(PumpIds.TURBO_PUMP).stopPump();

		cancel = true;

	}

}
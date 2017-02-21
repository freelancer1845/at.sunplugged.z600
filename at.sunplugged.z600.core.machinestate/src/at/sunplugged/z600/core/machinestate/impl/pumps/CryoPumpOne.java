package at.sunplugged.z600.core.machinestate.impl.pumps;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PumpRegistry.PumpIds;

public class CryoPumpOne extends AbstractCryoPump {

	public CryoPumpOne(MachineStateService machineStateService) {
		super(PumpIds.CRYO_ONE, machineStateService, DigitalOutput., compressorInput, lowInput, pressureSite,
				inOutlet, outOutlet);
		// TODO Auto-generated constructor stub
	}

}

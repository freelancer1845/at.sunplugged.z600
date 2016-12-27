package at.sunplugged.z600.core.machinestate.impl;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.core.machinestate.api.KathodeControl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.mbt.api.MbtService;

/**
 * Implementing class of the {@linkplain KathodeControl} Interface.
 * 
 * @author Jascha Riedel
 *
 */
public class KathodeControlImpl implements KathodeControl {

    private MachineStateService machineStateService;

    private MbtService mbtService;

    private LogService logService;

    public KathodeControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        this.mbtService = MachineStateServiceImpl.getMbtService();
        this.logService = MachineStateServiceImpl.getLogService();
    }

    @Override
    public void setSetPoint(Kathode kathode, double value) {

    }

}

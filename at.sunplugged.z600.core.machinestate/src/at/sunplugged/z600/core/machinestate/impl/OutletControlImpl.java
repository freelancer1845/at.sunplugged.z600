package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.mbt.api.MbtService;

public class OutletControlImpl implements OutletControl {

    private final MachineStateService machineStateService;

    private MbtService mbtController;

    public OutletControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
        this.mbtController = MachineStateServiceImpl.getMbtService();
    }

    @Override
    public boolean isOutletOpen(Outlet outlet) {
        return machineStateService.getDigitalOutputState(outlet.getDigitalOutput());
    }

    @Override
    public void closeOutlet(Outlet outlet) throws IOException {
        mbtController.writeDigOut(outlet.getDigitalOutput().getAddress(), false);
    }

    @Override
    public void openOutlet(Outlet outlet) throws IOException {
        mbtController.writeDigOut(outlet.getDigitalOutput().getAddress(), true);
    }

}

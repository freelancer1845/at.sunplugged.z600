package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.mbt.api.MBTController;

public class OutletControlImpl implements OutletControl {

    private final MachineStateService machineStateService;

    private final MBTController mbtController;

    public OutletControlImpl(MachineStateService machineStateService, MBTController mbtController) {
        this.machineStateService = machineStateService;
        this.mbtController = mbtController;
    }

    @Override
    public boolean isOutletOpen(Outlet outlet) {
        return machineStateService.getDigitalOutputState().get(outlet.getDigitalOutput().getAddress());
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

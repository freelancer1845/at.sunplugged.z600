package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.WaterControl;

public class WaterControlImpl implements WaterControl {

    private MachineStateService machineStateService;

    public WaterControlImpl(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
    }

    @Override
    public boolean getOutletState(WaterOutlet outlet) {
        return machineStateService.getDigitalOutputState(outlet.getDigitalOutput());
    }

    @Override
    public void setOutletState(WaterOutlet outlet, boolean state) throws IOException {
        MachineStateServiceImpl.getMbtService().writeDigOut(outlet.getDigitalOutput().getAddress(), state);

    }

    @Override
    public boolean getFlowCheckPointState(FlowCheckPoint checkPoint) {
        return machineStateService.getDigitalInputState(checkPoint.getDigitalInput());
    }

}

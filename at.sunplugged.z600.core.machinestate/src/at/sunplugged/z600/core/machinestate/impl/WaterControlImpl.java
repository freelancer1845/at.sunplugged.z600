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

    @Override
    public boolean isWaterOnAllCheckpoints() {
        if (getFlowCheckPointState(FlowCheckPoint.KATH_ONE) == true
                && getFlowCheckPointState(FlowCheckPoint.KATH_TWO) == true
                && getFlowCheckPointState(FlowCheckPoint.KATH_THREE) == true
                && getFlowCheckPointState(FlowCheckPoint.KATH_FOUR) == true) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean isKathodeWaterOn() {
        if (machineStateService.getDigitalOutputState(WaterOutlet.KATH_ONE.getDigitalOutput()) == true
                && machineStateService.getDigitalOutputState(WaterOutlet.KATH_TWO.getDigitalOutput()) == true
                && machineStateService.getDigitalOutputState(WaterOutlet.KATH_THREE.getDigitalOutput()) == true
                && machineStateService.getDigitalOutputState(WaterOutlet.SHIELD.getDigitalOutput()) == true) {
            return true;
        } else {
            return false;
        }
    }

}

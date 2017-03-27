package at.sunplugged.z600.conveyor.impl.position;

import java.io.IOException;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.mbt.api.MbtService;

public class PositionControl {

    private final MachineStateService machineStateService;

    private final MbtService mbtService;

    private final ConveyorControlService conveyorControlService;

    private Timer leftTimer = new Timer();

    private Timer rightTimer = new Timer();

    public PositionControl(MachineStateService machineStateService, MbtService mbtSerivce,
            ConveyorControlService conveyorControlService) {
        this.machineStateService = machineStateService;
        this.mbtService = mbtSerivce;
        this.conveyorControlService = conveyorControlService;
    }

    public void tick() throws IOException {

        if (conveyorControlService.getActiveMode() == Mode.LEFT_TO_RIGHT) {
            boolean lightSwitchLeftFront = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_LEFT_FRONT);
            boolean lightSwitchLeftBack = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_LEFT_BACK);
            boolean limitSwitchLeftFront = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_LEFT_FRONT);
            boolean limitSwitchLeftBack = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_LEFT_BACK);
            if (lightSwitchLeftFront == true && limitSwitchLeftFront == false) {
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), true);
                leftTimer.start(true);
            } else if (limitSwitchLeftFront == true
                    || machineStateService.getDigitalOutputState(DigitalOutput.BELT_LEFT_BACKWARDS_MOV) == true) {
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), false);
                leftTimer.stop();
            }
            if (lightSwitchLeftBack == false && limitSwitchLeftBack == false) {
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), true);
                leftTimer.start(false);
            } else if (limitSwitchLeftBack == true
                    || machineStateService.getDigitalOutputState(DigitalOutput.BELT_LEFT_FORWARD_MOV) == true) {
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), false);
                leftTimer.stop();
            }
        } else if (conveyorControlService.getActiveMode() == Mode.RIGHT_TO_LEFT) {
            boolean lightSwitchRightFront = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_RIGHT_FRONT);
            boolean lightSwitchRightBack = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIGHT_SWITCH_RIGHT_BACK);
            boolean limitSwitchRightFront = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_RIGHT_FRONT);
            boolean limitSwitchRightBack = machineStateService
                    .getDigitalInputState(DigitalInput.CONVEYOR_LIMIT_SWITCH_RIGHT_BACK);
            if (lightSwitchRightFront == true && limitSwitchRightFront == false) {
                rightTimer.start(true);
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(), true);
            } else if (limitSwitchRightBack == true
                    || machineStateService.getDigitalOutputState(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV) == true) {
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(), false);
                rightTimer.stop();
            }
            if (lightSwitchRightBack == false && limitSwitchRightBack == false) {
                rightTimer.start(false);
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), true);
            } else if (limitSwitchRightBack == true
                    || machineStateService.getDigitalOutputState(DigitalOutput.BELT_RIGHT_FORWARD_MOV)) {
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), false);
                rightTimer.stop();
            }
        } else {
            leftTimer.stop();
            rightTimer.stop();
            mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(), false);
        }
    }

    public long getRuntimeRight() {
        return rightTimer.getCurrentValue();
    }

    public void setRuntimeRight(long value) {
        rightTimer.setCurrentValue(value);
    }

    public long getRuntimeLeft() {
        return leftTimer.getCurrentValue();
    }

    public void setRuntimeLeft(long value) {
        leftTimer.setCurrentValue(value);
    }

    private class Timer {

        private long timeKeeper = 0;

        private long runtime = 0;

        private boolean negative = false;

        public void start(boolean negative) {
            this.negative = negative;
            if (timeKeeper == 0) {
                timeKeeper = System.currentTimeMillis();
            }
        }

        public void stop() {
            if (timeKeeper == 0) {
                if (negative) {
                    runtime -= System.currentTimeMillis() - timeKeeper;
                } else {
                    runtime += System.currentTimeMillis() - timeKeeper;
                }
                timeKeeper = 0;
            }
        }

        public long getCurrentValue() {
            if (timeKeeper != 0) {
                return runtime + System.currentTimeMillis() - timeKeeper;
            }
            return runtime;
        }

        public void setCurrentValue(long value) {
            runtime = value;
        }

    }

}

package at.sunplugged.z600.conveyor.impl.position;

import java.io.IOException;
import java.util.Arrays;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.mbt.api.MbtService;

public class PositionControl {

    private final static double DESIRED_POSITION_LEFT = 5;

    private final static double DESIRED_POSITION_RIGHT = 5;

    private final static double MAXIMUM_ERROR = 2;

    private final MachineStateService machineStateService;

    private final MbtService mbtService;

    private double[] leftPositions = new double[10];

    private double[] rightPositions = new double[10];

    public PositionControl(MachineStateService machineStateService, MbtService mbtSerivce) {
        this.machineStateService = machineStateService;
        this.mbtService = mbtSerivce;
        Arrays.fill(leftPositions, DESIRED_POSITION_LEFT);
        Arrays.fill(rightPositions, DESIRED_POSITION_RIGHT);
    }

    public void addLeftPosition(double position) {
        for (int i = 0; i < leftPositions.length - 1; i++) {
            leftPositions[i] = leftPositions[i + 1];
        }
        leftPositions[leftPositions.length] = position;
    }

    public void addRightPosition(double position) {
        for (int i = 0; i < rightPositions.length - 1; i++) {
            rightPositions[i] = rightPositions[i + 1];
        }
        rightPositions[rightPositions.length] = position;
    }

    public void tick() throws IOException {
        double rightPosition = mean(rightPositions);
        if (Math.abs(rightPosition - DESIRED_POSITION_RIGHT) > MAXIMUM_ERROR) {
            boolean limitRightFront = machineStateService.getDigitalInputState(DigitalInput.LIMIT_SWITCH_RIGHT_FRONT);
            boolean limitRightBack = machineStateService.getDigitalInputState(DigitalInput.LIMIT_SWITCH_RIGHT_BACK);
            if (rightPosition > DESIRED_POSITION_RIGHT && limitRightFront) {
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(), false);
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), true);
            } else if (rightPosition < DESIRED_POSITION_RIGHT && limitRightBack) {
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), false);
                mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(), true);
            }
        } else if (Math.abs(rightPosition - DESIRED_POSITION_RIGHT) / 10 < MAXIMUM_ERROR / 10) {
            mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), false);
        }

        double leftPosition = mean(leftPositions);
        if (Math.abs(leftPosition - DESIRED_POSITION_LEFT) > MAXIMUM_ERROR) {
            boolean limitLeftFront = machineStateService.getDigitalInputState(DigitalInput.LIMIT_SWITCH_LEFT_FRONT);
            boolean limitLeftBack = machineStateService.getDigitalInputState(DigitalInput.LIMIT_SWITCH_LEFT_BACK);
            if (leftPosition > DESIRED_POSITION_LEFT && limitLeftFront) {
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), false);
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), true);
            } else if (leftPosition < DESIRED_POSITION_LEFT && limitLeftBack) {
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), false);
                mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), true);
            }
        } else if (Math.abs(leftPosition - DESIRED_POSITION_LEFT) / 10 < MAXIMUM_ERROR / 10) {
            mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), false);
        }
    }

    private double mean(double[] array) {
        double mean = 0;
        for (int i = 0; i < array.length; i++) {
            mean += array[i];
        }
        return mean / array.length;
    }

    public double getLeftPosition() {
        return mean(leftPositions);
    }

    public double getRightPosition() {
        return mean(rightPositions);
    }
}

package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.mbt.api.MBTController;

public class OutletControlImpl implements OutletControl {

    private Object lock = new Object();

    private static final int NUMBER_OF_OUTLETS = 8;

    private static final int OUTLET_ONE_ADDRESS = 0;

    private static final int OUTLET_TWO_ADDRESS = 1;

    private static final int OUTLET_THREE_ADDRESS = 2;

    private static final int OUTLET_FOUR_ADDRESS = 3;

    private static final int OUTLET_FIVE_ADDRESS = 4;

    private static final int OUTLET_SIX_ADDRESS = 5;

    private static final int OUTLET_SEVEN_ADDRESS = 6;

    private static final int OUTLET_EIGHT_ADDRESS = 7;

    private boolean[] currentState = new boolean[NUMBER_OF_OUTLETS];

    private boolean[] desiredState = new boolean[NUMBER_OF_OUTLETS];

    private final MBTController mbtController;

    public OutletControlImpl(MBTController mbtController) {
        this.mbtController = mbtController;

        for (int i = 0; i < NUMBER_OF_OUTLETS; i++) {
            currentState[i] = false;
            desiredState[i] = false;
        }

    }

    @Override
    public boolean isOutletOpen(int number) {
        if (number < NUMBER_OF_OUTLETS) {
            synchronized (lock) {
                return currentState[number];
            }
        } else {
            throw new IndexOutOfBoundsException("No Outlet number: " + number);
        }

    }

    @Override
    public void closeOutlet(int number) {
        if (number < NUMBER_OF_OUTLETS) {
            synchronized (lock) {
                desiredState[number] = false;
            }
        } else {
            throw new IndexOutOfBoundsException("No Outlet number: " + number);
        }
    }

    @Override
    public void openOutlet(int number) {
        if (number < NUMBER_OF_OUTLETS) {
            synchronized (lock) {
                desiredState[number] = true;
            }
        } else {
            throw new IndexOutOfBoundsException("No Outlet number: " + number);
        }

    }

    @Override
    public void update() throws IOException {
        synchronized (lock) {
            updateOutletOne();
            updateOutletTwo();
            updateOutletThree();
            updateOutletFour();
            updateOutletFive();
            updateOutletSix();
            updateOutletSeven();
            updateOutletEight();
        }
    }

    private void updateOutletEight() throws IOException {
        if (desiredState[7] != currentState[7]) {
            mbtController.writeDigOut(0, OUTLET_EIGHT_ADDRESS, desiredState[7]);
        }
        currentState[7] = mbtController.readDigOut(0, OUTLET_EIGHT_ADDRESS);
    }

    private void updateOutletSeven() throws IOException {
        if (desiredState[6] != currentState[6]) {
            mbtController.writeDigOut(0, OUTLET_SEVEN_ADDRESS, desiredState[6]);
        }
        currentState[6] = mbtController.readDigOut(0, OUTLET_SEVEN_ADDRESS);
    }

    private void updateOutletSix() throws IOException {
        if (desiredState[5] != currentState[5]) {
            mbtController.writeDigOut(0, OUTLET_SIX_ADDRESS, desiredState[5]);
        }
        currentState[5] = mbtController.readDigOut(0, OUTLET_SIX_ADDRESS);
    }

    private void updateOutletFive() throws IOException {
        if (desiredState[4] != currentState[3]) {
            mbtController.writeDigOut(0, OUTLET_FIVE_ADDRESS, desiredState[4]);
        }
        currentState[4] = mbtController.readDigOut(0, OUTLET_FIVE_ADDRESS);
    }

    private void updateOutletFour() throws IOException {
        if (desiredState[3] != currentState[3]) {
            mbtController.writeDigOut(0, OUTLET_FOUR_ADDRESS, desiredState[3]);
        }
        currentState[3] = mbtController.readDigOut(0, OUTLET_FOUR_ADDRESS);
    }

    private void updateOutletThree() throws IOException {
        if (desiredState[2] != currentState[2]) {
            mbtController.writeDigOut(0, OUTLET_THREE_ADDRESS, desiredState[2]);
        }
        currentState[2] = mbtController.readDigOut(0, OUTLET_THREE_ADDRESS);
    }

    private void updateOutletTwo() throws IOException {
        if (desiredState[1] != currentState[1]) {
            mbtController.writeDigOut(0, OUTLET_TWO_ADDRESS, desiredState[1]);
        }
        currentState[1] = mbtController.readDigOut(0, OUTLET_TWO_ADDRESS);
    }

    private void updateOutletOne() throws IOException {
        if (desiredState[0] != currentState[0]) {
            mbtController.writeDigOut(0, OUTLET_ONE_ADDRESS, desiredState[0]);
        }
        currentState[0] = mbtController.readDigOut(0, OUTLET_ONE_ADDRESS);
    }

}

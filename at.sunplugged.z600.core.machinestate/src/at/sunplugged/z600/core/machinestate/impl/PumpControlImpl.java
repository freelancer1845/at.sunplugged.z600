package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;

import at.sunplugged.z600.common.execution.StandardThreadPool;
import at.sunplugged.z600.core.machinestate.api.PumpControl;
import at.sunplugged.z600.mbt.api.MBTController;

public class PumpControlImpl implements PumpControl {

    private static final int PUMP_ONE_ADDRESS = 10;

    private static final int PUMP_TWO_ADDRESS = 11;

    private static final int PUMP_TURBO_ADDRESS = 12;

    private final MBTController mbtController;

    private PumpState pumpOneState = PumpState.OFF;

    private PumpState pumpTwoState = PumpState.OFF;

    private PumpState pumpTurboState = PumpState.OFF;

    private PumpStarter pumpOneStarter;

    private PumpStarter pumpTwoStarter;

    private PumpStarter pumpTurboStarter;

    public PumpControlImpl(MBTController mbtController) {
        this.mbtController = mbtController;
    }

    @Override
    public void startPumpOne() {
        pumpOneStarter = new PumpStarter(PUMP_ONE_ADDRESS);
        pumpOneStarter.start();
    }

    @Override
    public void stopPumpOne() {
        // TODO Auto-generated method stub

    }

    @Override
    public void startPumpTwo() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopPumpTwo() {
        // TODO Auto-generated method stub

    }

    @Override
    public void startTurboPump() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopTurboPump() {
        // TODO Auto-generated method stub

    }

    @Override
    public void update() throws IOException {
        // TODO Auto-generated method stub

    }

    private class PumpStarter implements Runnable {

        private final int address;

        private PumpState state = PumpState.OFF;

        private boolean startPump = false;

        public PumpStarter(int address) {
            this.address = address;
        }

        public void start() {
            startPump = true;
            StandardThreadPool.getInstance().execute(this);
        }

        public void stop() {
            startPump = false;
            StandardThreadPool.getInstance().execute(this);
        }

        @Override
        public void run() {
            // Start the pump and change the state accordingly.
            if (startPump) {
                startPump();
            } else if (!startPump) {
                stopPump();
            }

        }

        public PumpState getState() {
            return state;
        }

        private void startPump() {
            state = PumpState.STARTING;
            try {
                mbtController.writeDigOut(0, address, true);
            } catch (IOException e) {
                state = PumpState.FAILED;
            }
            state = PumpState.ON;
        }

        private void stopPump() {
            state = PumpState.STOPPING;
            try {
                mbtController.writeDigOut(0, address, false);
            } catch (IOException e) {
                state = PumpState.FAILED;
            }
            state = PumpState.OFF;
        }

    }

}

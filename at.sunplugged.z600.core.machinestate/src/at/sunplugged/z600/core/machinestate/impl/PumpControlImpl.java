package at.sunplugged.z600.core.machinestate.impl;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.core.machinestate.api.PumpControl;
import at.sunplugged.z600.mbt.api.MBTController;

public class PumpControlImpl implements PumpControl {

    private final MBTController mbtController;

    private final LogService logService;

    private final StandardThreadPoolService standardThreadPoolService;

    public PumpControlImpl(MBTController mbtController, LogService logService,
            StandardThreadPoolService standardThreadPoolService) {
        this.mbtController = mbtController;
        this.logService = logService;
        this.standardThreadPoolService = standardThreadPoolService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<Boolean> startPump(Pumps pump) {
        return (Future<Boolean>) standardThreadPoolService.submit(new StartPumpCallable(pump));
    }

    @Override
    public Future<Boolean> stopPump(Pumps pump) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PumpState getState(Pumps pump) {
        // TODO Auto-generated method stub
        return null;
    }

    private class StartPumpCallable implements Callable<Boolean> {

        private static final int UPDATE_TIME = 100;

        private static final int WAIT_TIME = 60000;

        private final Pumps pump;

        public StartPumpCallable(Pumps pump) {
            this.pump = pump;
        }

        private void startTurboPump() {

        }

        private void startNormalPump() throws IOException {
            mbtController.writeDigOut(pump.getDigitalOutput().getAddress(), true);

            int timeWaited = 0;
            while (!mbtController.readDigIns(pump.getDigitalInput().getAddress(), 0)
                    .get(pump.getDigitalInput().getAddress()) || timeWaited > WAIT_TIME) {
                try {
                    Thread.sleep(UPDATE_TIME);
                    timeWaited += UPDATE_TIME;
                } catch (InterruptedException e) {
                    throw new IOException("Starting Pump failed. Thread Interrupted.", e);
                }
            }
            if (timeWaited > WAIT_TIME) {
                throw new IOException("Starting Pump failed. Not ok after " + WAIT_TIME / 1000 + " s.");
            }
        }

        @Override
        public Boolean call() throws Exception {
            try {
                if (pump.equals(Pumps.TURBO_PUMP)) {
                    startTurboPump();
                } else {
                    startNormalPump();
                }
            } catch (IOException e) {
                logService.log(LogService.LOG_ERROR, e.getMessage());
                return false;
            }
            return true;
        }

    }

}

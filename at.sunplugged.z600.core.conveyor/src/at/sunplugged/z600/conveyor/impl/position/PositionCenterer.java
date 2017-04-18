package at.sunplugged.z600.conveyor.impl.position;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;

public class PositionCenterer {

    public static final int CENTER_LEFT = 0;

    public static final int CENTER_RIGHT = 1;

    private static PositionCenterer instance = null;

    public static PositionCenterer getInstance() {
        if (instance == null) {
            instance = new PositionCenterer();
        }
        return instance;
    }

    private final LogService logService;

    private final StandardThreadPoolService threadPool;

    private ScheduledFuture<?> centeringFuture;

    public PositionCenterer() {
        this.logService = ConveyorPositionCorrectionServiceImpl.getLogService();
        this.threadPool = ConveyorPositionCorrectionServiceImpl.getThreadPool();
    }

    public void startCentering(int where, long runtime) {
        if (centeringFuture == null || centeringFuture.isDone() == true) {
            switch (where) {
            case CENTER_LEFT:
                startCenterLeft(runtime);
                break;
            case CENTER_RIGHT:
                startCenterRight(runtime);
                break;
            default:
                break;
            }

        } else {
            logService.log(LogService.LOG_ERROR, "Tried to start centering while centering is already running...");
        }

    }

    private void startCenterLeft(long runtime) {
        if (runtime < 0) {
            PositionControl.getInstance().startLeftMoveForward();
        } else if (runtime > 0) {
            PositionControl.getInstance().startLeftMoveBackward();
        }
        centeringFuture = threadPool.timedExecute(() -> stopCentering(), Math.abs(runtime), TimeUnit.MILLISECONDS);
    }

    private void startCenterRight(long runtime) {
        if (runtime < 0) {
            PositionControl.getInstance().startRightMoveForward();
        } else if (runtime > 0) {
            PositionControl.getInstance().startRightMoveBackward();
        }
        centeringFuture = threadPool.timedExecute(() -> stopCentering(), Math.abs(runtime), TimeUnit.MILLISECONDS);
    }

    public void stopCentering() {
        centeringFuture.cancel(true);
        PositionControl.getInstance().stopManualMove();
    }

    public boolean isCenteringRunning() {
        if (centeringFuture != null) {
            return !centeringFuture.isDone();
        } else {
            return false;
        }
    }

}

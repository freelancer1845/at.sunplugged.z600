package at.sunplugged.z600.conveyor.impl.position;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorPositionCorrectionService;
import at.sunplugged.z600.conveyor.impl.ConveyorControlServiceImpl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.mbt.api.MbtService;

@Component(immediate = true)
public class ConveyorPositionCorrectionServiceImpl implements ConveyorPositionCorrectionService {

    private static MachineStateService machineStateService;

    private static MbtService mbtService;

    private SettingsService settingsService;

    private static StandardThreadPoolService threadPool;

    private static ConveyorControlService conveyorControlService;

    private static LogService logService;

    private ScheduledFuture<?> scheduledFuture;

    private PositionControl positionControl;

    @Override
    public void start() {

        scheduledFuture = threadPool.timedPeriodicExecute(new Runnable() {

            @Override
            public void run() {

                try {
                    tick();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    stop();
                }
            }

        }, 100, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            positionControl.stopTimer();
        }
        try {
            mbtService.writeDigOut(DigitalOutput.BELT_LEFT_BACKWARDS_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_LEFT_FORWARD_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_BACKWARDS_MOV.getAddress(), false);
            mbtService.writeDigOut(DigitalOutput.BELT_RIGHT_FORWARD_MOV.getAddress(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRunning() {
        if (scheduledFuture != null) {
            return !scheduledFuture.isDone();
        } else {
            return false;
        }
    }

    @Override
    public long getRuntimeLeft() {
        return positionControl.getRuntimeLeft();
    }

    @Override
    public long getRuntimeRight() {
        return positionControl.getRuntimeRight();
    }

    @Override
    public void setRuntimeLeft(long ms) {
        positionControl.setRuntimeLeft(ms);
    }

    @Override
    public void setRuntimeRight(long ms) {
        positionControl.setRuntimeRight(ms);
    }

    @Override
    public void centerLeft() {
        if (conveyorControlService.getActiveMode() != ConveyorControlService.Mode.STOP) {
            logService.log(LogService.LOG_DEBUG, "Starting centering while conveyor is running is not allowed.");
        } else {
            logService.log(LogService.LOG_DEBUG, "Starting centering left.");
            PositionCenterer.getInstance().startCentering(PositionCenterer.CENTER_LEFT,
                    PositionControl.getInstance().getRuntimeLeft());
        }

    }

    @Override
    public void centerRight() {
        if (conveyorControlService.getActiveMode() != ConveyorControlService.Mode.STOP) {
            logService.log(LogService.LOG_DEBUG, "Starting centering while conveyor is running is not allowed.");
        } else {
            logService.log(LogService.LOG_DEBUG, "Starting centering right.");
            PositionCenterer.getInstance().startCentering(PositionCenterer.CENTER_RIGHT,
                    PositionControl.getInstance().getRuntimeRight());
        }
    }

    @Override
    public void startLeftForward() {
        if (conveyorControlService.getActiveMode() != ConveyorControlService.Mode.STOP) {
            logService.log(LogService.LOG_DEBUG, "Manual Move not allowed when conveyor is running.");
        } else {
            positionControl.startLeftMoveForward();
        }
    }

    @Override
    public void startLeftBackward() {
        if (conveyorControlService.getActiveMode() != ConveyorControlService.Mode.STOP) {
            logService.log(LogService.LOG_DEBUG, "Manual Move not allowed when conveyor is running.");
        } else {
            positionControl.startLeftMoveBackward();
        }
    }

    @Override
    public void startRightForward() {
        if (conveyorControlService.getActiveMode() != ConveyorControlService.Mode.STOP) {
            logService.log(LogService.LOG_DEBUG, "Manual Move not allowed when conveyor is running.");
        } else {
            positionControl.startRightMoveForward();
        }
    }

    @Override
    public void startRightBackward() {
        if (conveyorControlService.getActiveMode() != ConveyorControlService.Mode.STOP) {
            logService.log(LogService.LOG_DEBUG, "Manual Move not allowed when conveyor is running.");
        } else {
            positionControl.startRightMoveBackward();
        }
    }

    @Override
    public void stopManualMove() {
        positionControl.stopManualMove();
    }

    private void tick() throws IOException, InterruptedException {
        positionControl.tick();
    }

    @Activate
    protected void activate() {
        positionControl = PositionControl.getInstance();
    }

    @Reference(unbind = "unbindMachineStateService")
    public synchronized void bindMachineStateService(MachineStateService machineStateService) {
        ConveyorPositionCorrectionServiceImpl.machineStateService = machineStateService;
    }

    public synchronized void unbindMachineStateService(MachineStateService machineStateService) {
        if (ConveyorPositionCorrectionServiceImpl.machineStateService == machineStateService) {
            ConveyorPositionCorrectionServiceImpl.machineStateService = null;
        }
    }

    @Reference(unbind = "unbindSettingsService")
    public synchronized void bindSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public synchronized void unbindSettingsService(SettingsService settingsService) {
        if (this.settingsService == settingsService) {
            this.settingsService = null;
        }
    }

    @Reference(unbind = "unbindStandardThreadPoolService")
    public synchronized void bindStandardThreadPoolService(StandardThreadPoolService threadPool) {
        ConveyorPositionCorrectionServiceImpl.threadPool = threadPool;
    }

    public synchronized void unbindStandardThreadPoolService(StandardThreadPoolService threadPool) {
        if (ConveyorPositionCorrectionServiceImpl.threadPool == threadPool) {
            ConveyorPositionCorrectionServiceImpl.threadPool = null;
        }
    }

    @Reference(unbind = "unbindMbtService")
    public synchronized void bindMbtService(MbtService mbtService) {
        ConveyorPositionCorrectionServiceImpl.mbtService = mbtService;
    }

    public synchronized void unbindMbtService(MbtService mbtService) {
        if (ConveyorPositionCorrectionServiceImpl.mbtService == mbtService) {
            ConveyorPositionCorrectionServiceImpl.mbtService = null;
        }
    }

    @Reference(unbind = "unbindConveyorControlService")
    public synchronized void bindConveyorControlService(ConveyorControlService conveyorControlService) {
        ConveyorPositionCorrectionServiceImpl.conveyorControlService = conveyorControlService;
    }

    public synchronized void unbindConveyorControlService(ConveyorControlService conveyorControlService) {
        if (ConveyorPositionCorrectionServiceImpl.conveyorControlService == conveyorControlService) {
            ConveyorPositionCorrectionServiceImpl.conveyorControlService = null;
        }
    }

    @Reference(unbind = "unbindLogService")
    public synchronized void bindLogService(LogService logService) {
        ConveyorPositionCorrectionServiceImpl.logService = logService;
    }

    public synchronized void unbindLogService(LogService logService) {
        if (ConveyorPositionCorrectionServiceImpl.logService == logService) {
            ConveyorPositionCorrectionServiceImpl.logService = null;
        }
    }

    public static MbtService getMbtService() {
        return mbtService;
    }

    public static StandardThreadPoolService getThreadPool() {
        return threadPool;
    }

    public static ConveyorControlService getConveyorControlService() {
        return conveyorControlService;
    }

    public static LogService getLogService() {
        return logService;
    }

    public static MachineStateService getMachineStateService() {
        return machineStateService;
    }

}

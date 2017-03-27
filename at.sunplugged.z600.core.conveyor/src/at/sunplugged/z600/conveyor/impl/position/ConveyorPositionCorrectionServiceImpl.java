package at.sunplugged.z600.conveyor.impl.position;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.common.settings.api.SettingsService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.conveyor.api.ConveyorPositionCorrectionService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.mbt.api.MbtService;

@Component(immediate = true)
public class ConveyorPositionCorrectionServiceImpl implements ConveyorPositionCorrectionService {

    private MachineStateService machineStateService;

    private MbtService mbtService;

    private SettingsService settingsService;

    private StandardThreadPoolService threadPool;

    private ConveyorControlService conveyorControlService;

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
    public double getRuntimeLeft() {
        return positionControl.getRuntimeLeft();
    }

    @Override
    public double getRuntimeRight() {
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

    private void tick() throws IOException, InterruptedException {
        positionControl.tick();
    }

    @Activate
    protected void activate() {
        positionControl = new PositionControl(machineStateService, mbtService, conveyorControlService);
    }

    @Reference(unbind = "unbindMachineStateService")
    public synchronized void bindMachineStateService(MachineStateService machineStateService) {
        this.machineStateService = machineStateService;
    }

    public synchronized void unbindMachineStateService(MachineStateService machineStateService) {
        if (this.machineStateService == machineStateService) {
            this.machineStateService = null;
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
        this.threadPool = threadPool;
    }

    public synchronized void unbindStandardThreadPoolService(StandardThreadPoolService threadPool) {
        if (this.threadPool == threadPool) {
            this.threadPool = null;
        }
    }

    @Reference(unbind = "unbindMbtService")
    public synchronized void bindMbtService(MbtService mbtService) {
        this.mbtService = mbtService;
    }

    public synchronized void unbindMbtService(MbtService mbtService) {
        if (this.mbtService == mbtService) {
            this.mbtService = null;
        }
    }

    public synchronized void bindConveyorControlService(ConveyorControlService conveyorControlService) {
        this.conveyorControlService = conveyorControlService;
    }

    public synchronized void unbindConveyorControlService(ConveyorControlService conveyorControlService) {
        if (this.conveyorControlService == conveyorControlService) {
            this.conveyorControlService = null;
        }
    }

}

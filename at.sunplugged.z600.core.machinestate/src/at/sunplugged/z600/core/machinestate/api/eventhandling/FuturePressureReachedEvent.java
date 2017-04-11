package at.sunplugged.z600.core.machinestate.api.eventhandling;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;
import at.sunplugged.z600.core.machinestate.impl.MachineStateServiceImpl;

// TODO : The event should check whether the pressure was just a peak, or if it was really reached.
// TODO : Probably by checking whether the pressure still forfills condition after 5s

public class FuturePressureReachedEvent implements MachineEventHandler {
    private final MachineStateService machineStateService;

    private final PressureMeasurementSite site;

    private boolean pressureReached = false;

    private final double desiredPressure;

    private ScheduledFuture<?> checkForPressurePeakFuture;

    public FuturePressureReachedEvent(MachineStateService machineStateService, PressureMeasurementSite site,
            double desiredPressure) {
        this.machineStateService = machineStateService;
        this.site = site;
        this.desiredPressure = desiredPressure;

        if (machineStateService.getPressureMeasurmentControl().getCurrentValue(site) <= desiredPressure) {
            pressureReached = true;
        }
        machineStateService.registerMachineEventHandler(this);
    }

    public Boolean get(long arg0, TimeUnit arg1) throws InterruptedException, TimeoutException {
        long startTime = System.nanoTime();
        while (!pressureReached) {
            Thread.sleep(10);
            if (System.nanoTime() - startTime > arg1.toNanos(arg0)) {
                unregister();
                throw new TimeoutException("Timeout reached while waiting for event.");
            }
            if (Thread.interrupted()) {
                unregister();
                throw new InterruptedException();
            }
        }
        return true;
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType().equals(Type.PRESSURE_CHANGED)) {
            if (((PressureChangedEvent) event).getOrigin().equals(site)) {
                double currentPressure = (double) event.getValue();
                if (currentPressure <= desiredPressure) {
                    unregister();
                    checkForPeak();
                }
            }

        }
    }

    private void checkForPeak() {
        if (checkForPressurePeakFuture == null || checkForPressurePeakFuture.isDone()) {
            checkForPressurePeakFuture = MachineStateServiceImpl.getStandardThreadPoolService()
                    .timedExecute(new Runnable() {
                        @Override
                        public void run() {
                            double pressureAtSite = machineStateService.getPressureMeasurmentControl()
                                    .getCurrentValue(site);
                            if (pressureAtSite <= desiredPressure) {
                                pressureReached = true;
                            } else {
                                machineStateService.registerMachineEventHandler(FuturePressureReachedEvent.this);
                            }
                        }
                    }, 2, TimeUnit.SECONDS);
        }
    }

    private void unregister() {
        machineStateService.unregisterMachineEventHandler(this);
    }

}

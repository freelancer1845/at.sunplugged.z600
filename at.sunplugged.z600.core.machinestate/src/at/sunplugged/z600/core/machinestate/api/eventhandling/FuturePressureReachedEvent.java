package at.sunplugged.z600.core.machinestate.api.eventhandling;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PressureMeasurement.PressureMeasurementSite;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent.Type;

public class FuturePressureReachedEvent implements MachineEventHandler {
    private final MachineStateService machineStateService;

    private final PressureMeasurementSite site;

    private boolean pressureReached = false;

    private final double desiredPressure;

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
                throw new TimeoutException("Timeout reached while waiting for event.");
            }
        }
        return true;
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.getType().equals(Type.PRESSURE_CHANGED)) {
            if (((PressureChangedEvent) event).getSite().equals(site)) {
                double currentPressure = (double) event.getValue();
                if (currentPressure <= desiredPressure) {
                    pressureReached = true;
                    machineStateService.unregisterMachineEventHandler(this);
                }
            }

        }
    }

}

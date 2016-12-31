package at.sunplugged.z600.core.machinestate.api.eventhandling;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;

public class FutureEvent implements MachineEventHandler {

    private final MachineStateService machineStateService;

    private final MachineStateEvent event;

    private boolean eventCatched = false;

    /**
     * 
     * @param machineStateService that fires the desired event.
     * @param event that should be waited for.
     */
    public FutureEvent(MachineStateService machineStateService, MachineStateEvent event) {
        this.machineStateService = machineStateService;
        this.event = event;

        machineStateService.registerMachineEventHandler(this);
    }

    public Boolean get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
        long startTime = System.nanoTime();
        while (!eventCatched) {
            Thread.sleep(10);
            if (System.nanoTime() - startTime > arg1.toNanos(arg0)) {
                throw new TimeoutException("Timeout reached while waiting for event.");
            }
        }

        return true;
    }

    public boolean isDone() {
        return eventCatched;
    }

    @Override
    public void handleEvent(MachineStateEvent event) {
        if (event.equals(this.event)) {
            eventCatched = true;
            machineStateService.unregisterMachineEventHandler(this);
        }
    }

}

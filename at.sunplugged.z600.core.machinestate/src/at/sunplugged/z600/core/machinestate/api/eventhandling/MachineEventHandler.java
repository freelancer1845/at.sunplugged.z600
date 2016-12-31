package at.sunplugged.z600.core.machinestate.api.eventhandling;

public interface MachineEventHandler {

    public void handleEvent(MachineStateEvent event);

}

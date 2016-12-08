package at.sunplugged.z600.core.machinestate.api;

import java.util.List;

/**
 * Contains getters and setters for all states, like V1 Open or closed and so
 * on. Also contains all measurement values. By that the interface provides
 * access to all values of the machine.
 * 
 * @author Jascha Riedel
 *
 */
public interface MachineStateService {

    /**
     * 
     * @return the {@linkplain OutletControl} Interface.
     */
    public OutletControl getOutletControl();

    /**
     * 
     * @return the {@linkplain PumpControl} Interface.
     */
    public PumpControl getPumpControl();

    public List<Boolean> getDigitalOutputState();

    public List<Boolean> getDigitalInputState();

    public List<Integer> getAnalogOutputState();

    public List<Integer> getAnalogInputState();

    public void fireMachineStateEvent(MachineStateEvent event);

    public void registerMachineEventHandler(MachineEventHandler eventHandler);

    public void unregisterMachineEventHandler(MachineEventHandler eventHandler);

}

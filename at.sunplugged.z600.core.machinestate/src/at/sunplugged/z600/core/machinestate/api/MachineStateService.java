package at.sunplugged.z600.core.machinestate.api;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

/**
 * 
 * Main Interface to the Machine in reality. Everything should be accessible via
 * this interface combined with the conveyor control.
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

    /**
     * 
     * @return the {@linkplain WaterControl} Interface.
     */
    public WaterControl getWaterControl();

    public boolean getDigitalOutputState(DigitalOutput digitalOutput);

    public boolean getDigitalInputState(DigitalInput digitalInput);

    public Integer getAnalogOutputState(AnalogOutput analogOutput);

    public Integer getAnalogInputState(AnalogInput analogInput);

    public void fireMachineStateEvent(MachineStateEvent event);

    public void registerMachineEventHandler(MachineEventHandler eventHandler);

    public void unregisterMachineEventHandler(MachineEventHandler eventHandler);

}

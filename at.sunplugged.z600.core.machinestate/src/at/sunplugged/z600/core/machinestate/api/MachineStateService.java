package at.sunplugged.z600.core.machinestate.api;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineEventHandler;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

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
     * Starts the automatic updating of the machineStateService.
     */
    public void start();

    /**
     * Stops the machineStateService
     */
    public void stop();

    /**
     * 
     * @return the {@linkplain OutletControl} Interface.
     */
    public OutletControl getOutletControl();

    /**
     * 
     * @return the {@linkplain PumpRegistry} Interface.
     */
    public PumpRegistry getPumpRegistry();

    /**
     * 
     * @return the {@linkplain WaterControl} Interface.
     */
    public WaterControl getWaterControl();

    /**
     * 
     * @return the {@linkplain PressureMeasurement} Interface.
     */
    public PressureMeasurement getPressureMeasurmentControl();

    /**
     * 
     * @return the {@linkplain GasFlowControl} Interface.
     */
    public GasFlowControl getGasFlowControl();

    /**
     * 
     * @return the {@linkplain PowerSourceRegistry} Interface.
     */
    public PowerSourceRegistry getPowerSourceRegistry();

    public boolean getDigitalOutputState(DigitalOutput digitalOutput);

    public boolean getDigitalInputState(DigitalInput digitalInput);

    public Integer getAnalogOutputState(AnalogOutput analogOutput);

    public Integer getAnalogInputState(AnalogInput analogInput);

    public void fireMachineStateEvent(MachineStateEvent event);

    public void registerMachineEventHandler(MachineEventHandler eventHandler);

    public void unregisterMachineEventHandler(MachineEventHandler eventHandler);

    /**
     * Shortcut method to access mbtService
     * 
     * @param digitalOutput
     * @throws IOException
     */
    public void writeDigitalOutput(DigitalOutput digitalOutput, boolean value) throws IOException;

    /**
     * Shortcut method to access mbtService
     * 
     * @param digitalOutput
     * @return
     * @throws IOException
     */
    public boolean readDigitalOutput(DigitalOutput digitalOutput) throws IOException;

    /**
     * Shortcut method to access mbtService
     * 
     * @param digitalInput
     * @return
     * @throws IOException
     */
    public boolean readDigitalIntput(DigitalInput digitalInput) throws IOException;

    /**
     * Shortcut method to access mbtService
     * 
     * @param analogOutput
     * @param value between 0-4096
     * @throws IOException
     */
    public void writeAnalogOutput(AnalogOutput analogOutput, int value) throws IOException;

    /**
     * Shortcut method to access mbtService
     * 
     * @param analogOutput
     * @throws IOException
     */
    public int readAnalogOutput(AnalogOutput analogOutput) throws IOException;

    /**
     * Shortcut method to access mbtService
     * 
     * @param analogInput
     * @throws IOException
     */
    public int readAnalogInput(AnalogInput analogInput) throws IOException;

}

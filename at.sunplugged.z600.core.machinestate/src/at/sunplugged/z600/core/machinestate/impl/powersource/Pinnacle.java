package at.sunplugged.z600.core.machinestate.impl.powersource;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.eventhandling.MachineStateEvent;

public class Pinnacle extends AbstractPowerSource {

    private static final DigitalOutput ON_OUTPUT = DigitalOutput.PINNACLE_START;

    private static final DigitalOutput OFF_OUTPUT = DigitalOutput.PINNACLE_OFF;

    private static final DigitalInput OK_INPUT = DigitalInput.PINNACLE_OUT;

    public Pinnacle(MachineStateService machineStateService) {
        super(machineStateService, PowerSourceId.PINNACLE);
    }

    @Override
    protected void powerSourceSpecificOn() throws IOException {
        mbtService.writeDigOut(OFF_OUTPUT.getAddress(), false);
        mbtService.writeDigOut(ON_OUTPUT.getAddress(), true);
    }

    @Override
    protected void powerSourceSpecificOff() throws IOException {
        setPower(0);
        mbtService.writeDigOut(ON_OUTPUT.getAddress(), false);
        mbtService.writeDigOut(OFF_OUTPUT.getAddress(), true);
    }

    @Override
    protected void powerSourceSpecificControlTick() {
        // TODO Auto-generated method stub

    }

    @Override
    public double getPower() {
        // TODO Auto-generated method stub
        return 0;
    }

}

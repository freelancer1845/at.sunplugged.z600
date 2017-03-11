package at.sunplugged.z600.core.machinestate.impl.powersource;

import java.io.IOException;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.utils.Conversion;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;

public abstract class AbstractSSVPowerSource extends AbstractPowerSource {

    private static final double ANALOG_VOLTAGE_INPUT_MAX = 1000;

    private static final double ANALOG_CURRENT_INPUT_MAX = 35;

    private static final double ANALOG_CURRENT_OUTPUT_MAX = 30;

    private final DigitalOutput startOutput;

    private final AnalogOutput currentOutput;

    private final AnalogInput currentInput;

    private final AnalogInput voltageInput;

    public AbstractSSVPowerSource(MachineStateService machineStateService, PowerSourceId id, DigitalOutput start_output,
            AnalogOutput currentOutput, AnalogInput currentInput, AnalogInput voltageInput) {
        super(machineStateService, id);
        this.startOutput = start_output;
        this.currentOutput = currentOutput;
        this.currentInput = currentInput;
        this.voltageInput = voltageInput;
    }

    @Override
    public double getPower() {
        return getVoltage() * getCurrent() / 1000;
    }

    @Override
    public double getCurrent() {
        int analogValue = machineStateService.getAnalogInputState(currentInput);
        return Conversion.clipConversionIn(analogValue, 0, ANALOG_CURRENT_INPUT_MAX);
    }

    @Override
    public double getVoltage() {
        int analogValue = machineStateService.getAnalogInputState(voltageInput);
        return Conversion.clipConversionIn(analogValue, 0, ANALOG_VOLTAGE_INPUT_MAX);
    }

    @Override
    protected void powerSourceSpecificOn() throws Exception {
        writeControlValue(settings.getPropertAsDouble(ParameterIds.INITIAL_CURRENT_SSV));
        Thread.sleep(500);
        mbtService.writeDigOut(startOutput.getAddress(), true);
    }

    @Override
    protected void powerSourceSpecificOff() throws IOException {
        mbtService.writeDigOut(startOutput.getAddress(), false);
    }

    @Override
    protected void writeSourceSpecificControlValue(double value) throws IOException {
        int analogValue = (int) Conversion.clipConversionOut(value, 0, ANALOG_CURRENT_OUTPUT_MAX);
        mbtService.writeOutputRegister(currentOutput.getAddress(), analogValue);
    }

    @Override
    protected void powerSourceSpecificControlTick() throws Exception {
        double currentPower = getPower();
        if (currentPower < (setPoint - 0.01)) {
            writeControlValue(currentControlValue + settings.getPropertAsDouble(ParameterIds.CURRENT_CHANGE_SSV));
        } else if (currentPower > (setPoint + 0.01)) {
            writeControlValue(currentControlValue + settings.getPropertAsDouble(ParameterIds.CURRENT_CHANGE_SSV));
        }
    }

}

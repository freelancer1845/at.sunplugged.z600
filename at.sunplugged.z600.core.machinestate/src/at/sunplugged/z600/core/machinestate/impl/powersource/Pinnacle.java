package at.sunplugged.z600.core.machinestate.impl.powersource;

import java.io.IOException;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.common.utils.Conversion;
import at.sunplugged.z600.core.machinestate.api.GasFlowControl;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogInput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.AnalogOutput;
import at.sunplugged.z600.core.machinestate.api.WagoAddresses.DigitalOutput;
import at.sunplugged.z600.core.machinestate.api.exceptions.InvalidPowerSourceStateException;

public class Pinnacle extends AbstractPowerSource {

    private static final DigitalOutput ON_OUTPUT = DigitalOutput.PINNACLE_START;

    private static final DigitalOutput OFF_OUTPUT = DigitalOutput.PINNACLE_OFF;

    private static final DigitalOutput REG_ONE_OUTPUT = DigitalOutput.PINNACLE_REG_ONE;

    private static final DigitalOutput REG_TWO_OUTPUT = DigitalOutput.PINNACLE_REG_TWO;

    private static final DigitalOutput INTERLOCK = DigitalOutput.PINNACLE_INTERLOCK;

    private static final AnalogOutput POWER_OUTPUT = AnalogOutput.PINNACLE_SETPOINT;

    private static final double ANALOG_OUTPUT_MAX = 5;

    private static final double ANALOG_POWER_INPUT_MAX = 6;

    private static final double ANALOG_VOLTAGE_INPUT_MAX = 1500;

    public Pinnacle(MachineStateService machineStateService) {
        super(machineStateService, PowerSourceId.PINNACLE);
    }

    @Override
    protected void powerSourceSpecificOn() throws Exception {
        checkPowerSourceStartConditions();

        double initialPower = settings.getPropertAsDouble(ParameterIds.INITIAL_POWER_PINNACLE);
        writeControlValue(initialPower);

        mbtService.writeDigOut(INTERLOCK.getAddress(), true);
        mbtService.writeDigOut(REG_ONE_OUTPUT.getAddress(), false);
        mbtService.writeDigOut(REG_TWO_OUTPUT.getAddress(), true);
        mbtService.writeDigOut(OFF_OUTPUT.getAddress(), false);
        mbtService.writeDigOut(ON_OUTPUT.getAddress(), true);

        Thread.sleep(2000);

        mbtService.writeDigOut(ON_OUTPUT.getAddress(), false);

    }

    private void checkPowerSourceStartConditions() throws InvalidPowerSourceStateException {
        if (!machineStateService.getGasFlowControl().getState().equals(GasFlowControl.State.RUNNING)) {
            throw new InvalidPowerSourceStateException(
                    "GasflowControl is not running! Won't start powersource pinnacle.");
        }
        if (machineStateService.getWaterControl().isWaterOnAllCheckpoints() == false) {
            throw new InvalidPowerSourceStateException("Not every kathode is cooled. Wont start powersource pinnacle!");
        }
    }

    @Override
    protected void powerSourceSpecificOff() throws IOException {
        mbtService.writeDigOut(ON_OUTPUT.getAddress(), false);
        mbtService.writeDigOut(OFF_OUTPUT.getAddress(), true);
        mbtService.writeDigOut(INTERLOCK.getAddress(), false);
    }

    @Override
    protected void powerSourceSpecificControlTick() throws Exception {
        double currentPower = getPower();
        if (currentPower < (setPoint - 0.01)) {
            writeControlValue(currentControlValue + settings.getPropertAsDouble(ParameterIds.POWER_CHANGE_PINNACLE));
        } else if (currentPower > (setPoint + 0.01)) {
            writeControlValue(currentControlValue + settings.getPropertAsDouble(ParameterIds.POWER_CHANGE_PINNACLE));
        }
    }

    @Override
    protected void writeSourceSpecificControlValue(double value) throws IOException {
        double convertedValue = Conversion.clipConversionOut(value, 0, ANALOG_OUTPUT_MAX);
        mbtService.writeOutputRegister(POWER_OUTPUT.getAddress(), (int) convertedValue);
    }

    @Override
    public double getPower() {
        int analogPowerValue = machineStateService.getAnalogInputState(AnalogInput.POWER_PINNACLE);
        return Conversion.clipConversionIn(analogPowerValue, 0.0, ANALOG_POWER_INPUT_MAX);
    }

    @Override
    public double getCurrent() {
        return 1000 * getPower() / getVoltage();
    }

    @Override
    public double getVoltage() {
        int analogVoltageValue = machineStateService.getAnalogInputState(AnalogInput.VOLTAGE_PINNACLE);
        return Conversion.clipConversionIn(analogVoltageValue, 0.0, ANALOG_VOLTAGE_INPUT_MAX);
    }

}

package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.concurrent.TimeUnit;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptExecutionException;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class WaitForStablePowersourceCommand extends AbstractCommand {

    private final PowerSourceId id;

    public WaitForStablePowersourceCommand(PowerSourceId id) {
        this.id = id;
    }

    @Override
    public String name() {
        return Commands.WAIT_FOR_STABLE_POWERSOURCE + "(" + id.toString() + ")";
    }

    @Override
    public long getEstimededTimeNeeded(TimeUnit unit) {
        return unit.convert(10, TimeUnit.SECONDS);
    }

    @Override
    protected void executeCommandSpecific() throws InterruptedException, ScriptExecutionException {
        PowerSource powerSource = ScriptInterpreterServiceImpl.getMachineStateService().getPowerSourceRegistry()
                .getPowerSource(id);
        int counter = 0;
        while (counter < 10) {
            if (powerSource.getState() == PowerSource.State.OFF) {
                break;
            }
            double windowSize = ScriptInterpreterServiceImpl.getSettingsService()
                    .getPropertAsDouble(ParameterIds.POWER_SOURCE_POWER_STABLE_WINDOW_PERCENTAGE) / 100
                    * powerSource.getSetPointpower();
            if (Math.abs(powerSource.getPower() - powerSource.getSetPointpower()) < windowSize) {
                counter++;
            } else {
                counter = 0;
            }

            Thread.sleep(100);
        }
    }

}

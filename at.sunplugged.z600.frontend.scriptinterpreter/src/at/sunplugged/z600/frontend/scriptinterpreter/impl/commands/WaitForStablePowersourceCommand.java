package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSource.State;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class WaitForStablePowersourceCommand implements Command {

    private final PowerSourceId id;

    public WaitForStablePowersourceCommand(PowerSourceId id) {
        this.id = id;
    }

    @Override
    public void execute() throws Exception {
        PowerSource powerSource = ScriptInterpreterServiceImpl.getMachineStateService().getPowerSourceRegistry()
                .getPowerSource(id);
        int counter = 0;
        while (counter < 10) {
            PowerSource.State state = powerSource.getState();
            if (state == State.OFF) {
                break;
            }
            if (state == State.ON) {
                counter++;
            } else {
                counter = 0;
            }
            Thread.sleep(100);
        }

    }

    @Override
    public String name() {
        return Commands.WAIT_FOR_STABLE_POWERSOURCE + "(" + id.toString() + ")";
    }

}

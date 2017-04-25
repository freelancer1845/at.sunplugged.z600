package at.sunplugged.z600.frontend.scriptinterpreter.impl;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.core.machinestate.api.PowerSource;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.commands.Command;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.commands.SetpointPowersourceCommand;

/**
 * This thread is used to monitor the script execution. After the script is
 * finished the monitor should be stopped. Command supervision should be handled
 * here.
 * 
 * @author Jascha Riedel
 *
 */
public class ScriptMonitor extends Thread {

    private Map<PowerSourceId, SetpointPowersourceCommand> powerSourceCommands = new HashMap<>();

    private volatile boolean running = false;

    private final LogService logService;

    public ScriptMonitor() {
        this.setName("Script Monitor Thread");
        logService = ScriptInterpreterServiceImpl.getLogService();
    }

    @Override
    public void run() {
        running = true;
        while (running == true) {
            if (checkPowersources() == false) {
                running = false;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                running = false;
                logService.log(LogService.LOG_DEBUG, "Script Monitor interrupted.");
            }

        }

    }

    private boolean checkPowersources() {
        for (PowerSourceId id : PowerSourceId.values()) {
            if (powerSourceCommands.containsKey(id)) {
                PowerSource.State state = ScriptInterpreterServiceImpl.getMachineStateService().getPowerSourceRegistry()
                        .getPowerSource(id).getState();
                if (state == PowerSource.State.OFF) {

                    interruptScriptExecution(
                            String.format("Powersource with id \"%s\" stopped unexpected!", id.toString()));
                    return false;
                }
            }
        }
        return true;
    }

    private void interruptScriptExecution(String string) {
        logService.log(LogService.LOG_ERROR, "ScriptMonitor: \"" + string + "\"");
        ScriptExecutor.getInstance().stopExecution();
    }

    public void stopMonitor() {
        running = false;
    }

    public void addCommand(Command command) {
        if (command instanceof SetpointPowersourceCommand) {
            handleSetpointPowersourceCommand((SetpointPowersourceCommand) command);
        }
    }

    private void handleSetpointPowersourceCommand(SetpointPowersourceCommand command) {
        if (command.getSetpoint() > 0) {
            powerSourceCommands.put(command.getId(), command);
        } else {
            powerSourceCommands.remove(command.getId());
        }
    }

}

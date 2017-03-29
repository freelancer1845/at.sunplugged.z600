package at.sunplugged.z600.frontend.scriptinterpreter.impl;

import java.util.concurrent.Future;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ParseError;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptInterpreterService;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.commands.Command;

@Component
public class ScriptInterpreterServiceImpl implements ScriptInterpreterService {

    private static MachineStateService machineStateService;

    private static LogService logService;

    private static StandardThreadPoolService standardThreadPoolService;

    private Future<?> scriptExecutionFuture;

    @Override
    public Future<?> executeScript(String script) {
        if (scriptExecutionFuture != null && scriptExecutionFuture.isDone() == false) {
            logService.log(LogService.LOG_ERROR, "Parallel Script execution is not allowed, though possible.");
            return scriptExecutionFuture;
        }
        scriptExecutionFuture = standardThreadPoolService.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    ScriptExecutor.executeScript(script);
                } catch (Exception e) {
                    logService.log(LogService.LOG_ERROR, "Script execution failed due to unhandled exception.", e);
                }
            }

        });

        return scriptExecutionFuture;
    }

    @Override
    public String checkScript(String script) throws ParseError {
        LexicalInterpreter.checkScript(script);
        return null;
    }

    @Reference(unbind = "unbindMachineStateService")
    public synchronized void bindMachineStateService(MachineStateService machineStateService) {
        ScriptInterpreterServiceImpl.machineStateService = machineStateService;
    }

    public synchronized void unbindMachineStateService(MachineStateService machineStateService) {
        if (ScriptInterpreterServiceImpl.machineStateService == machineStateService) {
            ScriptInterpreterServiceImpl.machineStateService = null;
        }
    }

    public static MachineStateService getMachineStateService() {
        return machineStateService;
    }

    @Reference(unbind = "unbindLogService")
    public synchronized void bindLogService(LogService logService) {
        ScriptInterpreterServiceImpl.logService = logService;
    }

    public synchronized void unbindLogService(LogService logService) {
        if (ScriptInterpreterServiceImpl.logService == logService) {
            ScriptInterpreterServiceImpl.logService = null;
        }
    }

    public static LogService getLogService() {
        return logService;
    }

    @Reference(unbind = "unbindStandardThreadPoolService")
    public synchronized void bindStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        ScriptInterpreterServiceImpl.standardThreadPoolService = standardThreadPoolService;
    }

    public synchronized void unbindStandardThreadPoolService(StandardThreadPoolService standardThreadPoolService) {
        if (ScriptInterpreterServiceImpl.standardThreadPoolService == standardThreadPoolService) {
            ScriptInterpreterServiceImpl.standardThreadPoolService = null;
        }
    }

    @Override
    public String getCurrentCommandName() {
        Command command = ScriptExecutor.getCurrentCommand();
        if (command == null) {
            return null;
        } else {
            return command.name();
        }
    }

}

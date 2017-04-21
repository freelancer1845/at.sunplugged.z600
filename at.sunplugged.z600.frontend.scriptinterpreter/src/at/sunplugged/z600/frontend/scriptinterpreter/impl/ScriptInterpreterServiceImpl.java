package at.sunplugged.z600.frontend.scriptinterpreter.impl;

import java.util.concurrent.Future;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.conveyor.api.ConveyorControlService;
import at.sunplugged.z600.core.machinestate.api.MachineStateService;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ParseError;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ScriptInterpreterService;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.commands.Command;

@Component
public class ScriptInterpreterServiceImpl implements ScriptInterpreterService {

    private static MachineStateService machineStateService;

    private static ConveyorControlService conveyorControlService;

    private static LogService logService;

    private static StandardThreadPoolService standardThreadPoolService;

    @Override
    public Future<?> executeScript(String script) throws ParseError {
        return ScriptExecutor.getInstance().executeScript(script);
    }

    @Override
    public void stopExecution() {
        ScriptExecutor.getInstance().stopExecution();
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

    public static StandardThreadPoolService getStandardThreadPoolService() {
        return standardThreadPoolService;
    }

    @Reference(unbind = "unbindConveyorControlService")
    public synchronized void bindConveyorControlService(ConveyorControlService conveyorControlService) {
        ScriptInterpreterServiceImpl.conveyorControlService = conveyorControlService;
    }

    public synchronized void unbindConveyorControlService(ConveyorControlService conveyorControlService) {
        if (ScriptInterpreterServiceImpl.conveyorControlService == conveyorControlService) {
            ScriptInterpreterServiceImpl.conveyorControlService = null;
        }
    }

    public static ConveyorControlService getConveyorControlService() {
        return conveyorControlService;
    }

    @Override
    public String getCurrentCommandName() {
        Command command = ScriptExecutor.getInstance().getCurrentCommand();
        if (command == null) {
            return null;
        } else {
            return command.name();
        }
    }

}

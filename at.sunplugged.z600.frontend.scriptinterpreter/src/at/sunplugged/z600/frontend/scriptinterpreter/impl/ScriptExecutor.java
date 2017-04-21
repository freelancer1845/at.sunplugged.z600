package at.sunplugged.z600.frontend.scriptinterpreter.impl;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptException;

import org.osgi.service.log.LogService;

import at.sunplugged.z600.common.execution.api.StandardThreadPoolService;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ParseError;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.commands.Command;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.commands.SetPressureCommand;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.commands.SetpointPowersourceCommand;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.commands.StopConveyorCommand;

public class ScriptExecutor {

    private static ScriptExecutor instance;

    public static ScriptExecutor getInstance() {
        if (instance == null) {
            instance = new ScriptExecutor();
        }
        return instance;
    }

    private Command currentCommand = null;

    private Future<?> scriptExecutionFuture;

    private final LogService logService;

    private final StandardThreadPoolService standardThreadPoolService;

    public ScriptExecutor() {
        this.logService = ScriptInterpreterServiceImpl.getLogService();
        this.standardThreadPoolService = ScriptInterpreterServiceImpl.getStandardThreadPoolService();
    }

    public Future<?> executeScript(String script) throws ParseError {

        if (scriptExecutionFuture != null && scriptExecutionFuture.isDone() == false) {
            logService.log(LogService.LOG_ERROR, "Parallel Script execution is not allowed, though possible.");
            return scriptExecutionFuture;
        }
        LexicalInterpreter.checkScript(script);
        List<Command> commands;
        commands = LexicalInterpreter.parseScript(script);

        long estimededTimeNeededInSeconds = 0;
        for (Command command : commands) {
            estimededTimeNeededInSeconds += command.getEstimededTimeNeeded(TimeUnit.SECONDS);
        }

        logService.log(LogService.LOG_INFO, constructInfoString(commands, estimededTimeNeededInSeconds));
        ScriptMonitor scriptMonitor = new ScriptMonitor();

        scriptExecutionFuture = standardThreadPoolService.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    scriptMonitor.start();
                    executeCommands(commands, scriptMonitor);
                } catch (Exception e) {
                    logService.log(LogService.LOG_ERROR, "Script execution failed due to unhandled exception.", e);
                    try {
                        new StopConveyorCommand().execute();
                    } catch (Exception e1) {
                        logService.log(LogService.LOG_ERROR, "Error while cleaning up script execution", e1);
                    }
                    for (PowerSourceId id : PowerSourceId.values()) {
                        try {
                            new SetpointPowersourceCommand(id, 0).execute();
                        } catch (Exception e1) {
                            logService.log(LogService.LOG_ERROR, "Error while cleaning up script execution", e1);
                        }
                    }
                    try {
                        new SetPressureCommand(0).execute();
                    } catch (Exception e1) {
                        logService.log(LogService.LOG_ERROR, "Error while cleaning up script execution", e1);
                    }
                } finally {
                    scriptMonitor.stopMonitor();
                }
            }

        });

        return scriptExecutionFuture;

    }

    public void stopExecution() {

        logService.log(LogService.LOG_INFO, "Stopping script execution. Sleep interrupted errors are expected.");
        if (scriptExecutionFuture != null && scriptExecutionFuture.isDone() == false) {
            scriptExecutionFuture.cancel(true);
        } else {
            logService.log(LogService.LOG_DEBUG, "Tried to stop execution, but no script running...");
            return;
        }
    }

    public Command getCurrentCommand() {
        return currentCommand;
    }

    private void executeCommands(List<Command> commands, ScriptMonitor scriptMonitor) throws Exception {
        try {

            for (Command command : commands) {
                if (Thread.interrupted() == true) {
                    throw new ScriptException("Script execution interrupted!");
                }
                currentCommand = command;
                scriptMonitor.addCommand(command);
                command.execute();

            }
            logService.log(LogService.LOG_INFO, "Script execution finished successfully.");
        } finally {
            currentCommand = null;

        }

    }

    private String constructInfoString(List<Command> commands, long timeNeededInSeconds) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Starting Script execution...");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("Commands:");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("******************" + System.lineSeparator());
        commands.stream()
                .forEach(command -> stringBuilder.append("    " + command.toString() + System.lineSeparator()));
        stringBuilder.append("******************" + System.lineSeparator());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(String.format("--- Estimeted runtime \"%d\" seconds ---", timeNeededInSeconds));
        stringBuilder.append(System.lineSeparator());
        LocalTime now = LocalTime.now();
        now = now.plusSeconds(timeNeededInSeconds);
        stringBuilder.append(String.format("--- Estimeted time of finish %02d:%02d:%02d ---", now.getHour(),
                now.getMinute(), now.getSecond()));
        stringBuilder.append(System.lineSeparator());

        return stringBuilder.toString();
    }

}

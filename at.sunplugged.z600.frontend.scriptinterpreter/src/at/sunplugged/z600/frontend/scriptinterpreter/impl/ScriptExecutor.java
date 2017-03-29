package at.sunplugged.z600.frontend.scriptinterpreter.impl;

import java.util.List;

import at.sunplugged.z600.frontend.scriptinterpreter.impl.commands.Command;

public class ScriptExecutor {

    private static Command currentCommand = null;

    public static void executeScript(String script) throws Exception {
        List<Command> commands;
        commands = LexicalInterpreter.parseScript(script);
        try {

            for (Command command : commands) {
                currentCommand = command;
                command.execute();

            }
        } finally {
            currentCommand = null;
        }

    }

    public static Command getCurrentCommand() {
        return currentCommand;
    }

}

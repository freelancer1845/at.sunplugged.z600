package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ParseError;

public class CommandParser {

    private static Pattern parameterPattern = Pattern.compile("\\(([a-zA-Z0-9\\.\\,]+)\\)");

    public static Command parseCommand(String command) throws ParseError {

        if (command.startsWith(Commands.SET_PRESSURE)) {
            return interpretPressureCommand(command);
        } else if (command.startsWith(Commands.WAIT)) {
            return interpretWaitCommand(command);
        } else {
            throw new ParseError("Failed to parse command: \"" + command + "\"");
        }

    }

    private static Command interpretPressureCommand(String command) throws ParseError {
        String[] parameters = getParameters(command);
        testParameterCount(command, parameters, 1);
        try {
            double pressure = Double.valueOf(parameters[0]);
            return new SetPressureCommand(pressure);
        } catch (NumberFormatException e) {
            throw new ParseError("Wrong format for pressure parameter.");
        }

    }

    private static Command interpretWaitCommand(String command) throws ParseError {
        String[] parameters = getParameters(command);
        testParameterCount(command, parameters, 1);

        try {
            int secondsToWait = Integer.valueOf(parameters[0]);
            return new WaitCommand(secondsToWait);
        } catch (NumberFormatException e) {
            throw new ParseError("Wrong format for wait parameter.");
        }
    }

    private static String[] getParameters(String command) throws ParseError {
        Matcher matcher = parameterPattern.matcher(command);
        if (matcher.find()) {
            return matcher.group(1).split(",");
        } else {
            throw new ParseError("Failed to parse parameters for command: \"" + command + "\"");
        }

    }

    private static void testParameterCount(String command, String[] parameters, int count) throws ParseError {
        if (parameters.length != count) {
            throw new ParseError("Invaild count of parameters for command: \"" + command + "\"");
        }
    }

    private CommandParser() {

    }
}

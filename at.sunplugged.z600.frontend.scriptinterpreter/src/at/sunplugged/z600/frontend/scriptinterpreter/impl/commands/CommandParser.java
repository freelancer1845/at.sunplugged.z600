package at.sunplugged.z600.frontend.scriptinterpreter.impl.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.sunplugged.z600.common.settings.api.ParameterIds;
import at.sunplugged.z600.conveyor.api.ConveyorControlService.Mode;
import at.sunplugged.z600.core.machinestate.api.PowerSourceRegistry.PowerSourceId;
import at.sunplugged.z600.frontend.scriptinterpreter.api.Commands;
import at.sunplugged.z600.frontend.scriptinterpreter.api.ParseError;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.ScriptInterpreterServiceImpl;

public class CommandParser {

    private static Pattern parameterPattern = Pattern.compile("\\(([a-zA-Z0-9_\\.\\,]+)\\)");

    public static Command parseCommand(String command) throws ParseError {

        if (command.startsWith(Commands.SET_PRESSURE)) {
            return interpretPressureCommand(command);
        } else if (command.startsWith(Commands.WAIT_FOR_CONVEYOR)) {
            return interpretWaitForConveyorCommand(command);
        } else if (command.startsWith(Commands.WAIT_FOR_STABLE_POWERSOURCE)) {
            return interpretWaitForStablePowersourceCommand(command);
        } else if (command.startsWith(Commands.WAIT)) {
            return interpretWaitCommand(command);
        } else if (command.startsWith(Commands.START_CONVEYOR_SIMPLE)) {
            return interpretStartConveyorSimpleCommand(command);
        } else if (command.startsWith(Commands.START_CONVEYOR_DISTANCE)) {
            return interpretStartConveyorDistanceCommand(command);
        } else if (command.startsWith(Commands.START_CONVEYOR_TIME_UNDER_CATHODE)) {
            return interpretStartConveyorTimeUnderCathodeCommand(command);
        } else if (command.startsWith(Commands.START_CONVEYOR_TIME)) {
            return interpretStartConveyorTimeCommand(command);
        } else if (command.startsWith(Commands.STOP_CONVEYOR)) {
            return interpretStopConveyorCommand(command);
        } else if (command.startsWith(Commands.SETPOINT_POWERSOURCE)) {
            return interpretPowerSurceCommand(command);
        }

        else {
            throw new ParseError("Failed to parse command: \"" + command + "\"");
        }

    }

    private static Command interpretStartConveyorTimeUnderCathodeCommand(String command) throws ParseError {
        String[] parameters = getParameters(command);
        testParameterCount(command, parameters, 3);
        try {
            Mode mode = Mode.valueOf(parameters[0]);
            double distance = Double.valueOf(parameters[1]);
            long timeUnderCathodeInSeconds = Long.valueOf(parameters[2]);

            return new StartConveyorTimeUnderCathodeCommand(mode, distance, timeUnderCathodeInSeconds);
        } catch (IllegalArgumentException e) {
            throw new ParseError(String
                    .format("Mode, distance or timeUnderCathode parameter not provide properly. \"%s\"", command));
        }

    }

    private static Command interpretWaitForStablePowersourceCommand(String command) throws ParseError {
        String[] parameters = getParameters(command);
        testParameterCount(command, parameters, 1);
        try {
            PowerSourceId id = PowerSourceId.valueOf(parameters[0]);
            return new WaitForStablePowersourceCommand(id);
        } catch (IllegalArgumentException e) {
            throw new ParseError(String.format("Failed to parse parameters. \"%s\"", command));
        }
    }

    private static Command interpretWaitForConveyorCommand(String command) throws ParseError {
        return new WaitForConveyorCommand();
    }

    private static Command interpretPowerSurceCommand(String command) throws ParseError {
        String[] parameters = getParameters(command);
        testParameterCount(command, parameters, 2);
        try {
            PowerSourceId id = PowerSourceId.valueOf(parameters[0]);
            double setpoint = Double.valueOf(parameters[1]);
            return new SetpointPowersourceCommand(id, setpoint);
        } catch (IllegalArgumentException e) {
            throw new ParseError(String.format("Failed to parse parameters. \"%s\"", command));
        }
    }

    private static Command interpretStopConveyorCommand(String command) throws ParseError {
        String[] parameters = getParameters(command);
        testParameterCount(command, parameters, 0);
        return new StopConveyorCommand();
    }

    private static Command interpretStartConveyorTimeCommand(String command) throws ParseError {
        String[] parameters = getParameters(command);
        testParameterCount(command, parameters, 3);
        try {
            Mode mode = Mode.valueOf(parameters[0]);
            double speed = Double.valueOf(parameters[1]);
            long time = Long.valueOf(parameters[2]);
            return new StartConveyorTimeCommand(mode, speed, time);
        } catch (NumberFormatException e) {
            throw new ParseError(String.format("Mode, Speed or time parameter not provide properly. \"%s\"", command));
        }
    }

    private static Command interpretStartConveyorDistanceCommand(String command) throws ParseError {
        String[] parameters = getParameters(command);
        testParameterCount(command, parameters, 3);
        try {
            Mode mode = Mode.valueOf(parameters[0]);
            double speed = Double.valueOf(parameters[1]);
            double distance = Double.valueOf(parameters[2]);
            return new StartConveyorDistanceCommand(mode, speed, distance);
        } catch (IllegalArgumentException e) {
            throw new ParseError(
                    String.format("Mode, Speed or distance parameter not provide properly. \"%s\"", command));
        }
    }

    private static Command interpretStartConveyorSimpleCommand(String command) throws ParseError {
        String[] parameters = getParameters(command);
        testParameterCount(command, parameters, 2);
        try {

            Mode mode = Mode.valueOf(parameters[0]);

            double speed = Double.valueOf(parameters[1]);
            return new StartConveyorSimpleCommand(mode, speed);
        } catch (IllegalArgumentException e) {
            throw new ParseError(String.format("Mode or Speed parameter not provided properly. \"%s\"", command));
        }

    }

    private static Command interpretPressureCommand(String command) throws ParseError {
        String[] parameters = getParameters(command);
        testParameterCount(command, parameters, 1);
        try {
            double pressure = Double.valueOf(parameters[0]);
            if (pressure > 0) {
                double minPressure = ScriptInterpreterServiceImpl.getSettingsService()
                        .getPropertAsDouble(ParameterIds.VACUUM_LOWER_LIMIT_MBAR);
                if (pressure < minPressure) {
                    throw new ParseError(
                            String.format("Pressure parameter is lower than min value. \"%f\"", minPressure));
                }
                double maxPressure = ScriptInterpreterServiceImpl.getSettingsService()
                        .getPropertAsDouble(ParameterIds.VACUUM_UPPER_LIMIT_MBAR);
                if (pressure > maxPressure) {
                    throw new ParseError(
                            String.format("Pressure parameter is higher than max value. \"%f\"", maxPressure));
                }
            }
            return new SetPressureCommand(pressure);
        } catch (NumberFormatException e) {
            throw new ParseError(String.format("Wrong format for pressure parameter. \"%s\"", command));
        }

    }

    private static Command interpretWaitCommand(String command) throws ParseError {
        String[] parameters = getParameters(command);
        testParameterCount(command, parameters, 1);

        try {
            int secondsToWait = Integer.valueOf(parameters[0]);
            return new WaitCommand(secondsToWait);
        } catch (NumberFormatException e) {
            throw new ParseError(String.format("Wrong format for wait parameter. \"%s\"", command));
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

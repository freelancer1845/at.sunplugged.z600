package at.sunplugged.z600.frontend.scriptinterpreter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.sunplugged.z600.frontend.scriptinterpreter.api.ParseError;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.commands.Command;
import at.sunplugged.z600.frontend.scriptinterpreter.impl.commands.CommandParser;

public class LexicalInterpreter {

    private static Pattern commentsLinePattern = Pattern.compile(".*(\\/\\/.*)$");

    private static Pattern commandPattern = Pattern.compile("[a-zA-Z0-9\\.\\,\\(\\)]+\\)$");

    public static String checkScript(String string) throws ParseError {
        String[] commands = splitCommands(string);
        String[] trimmedCommands = removeCommentsAndSpaces(commands);
        examineCommands(trimmedCommands);

        return null;
    }

    public static List<Command> parseScript(String string) throws ParseError {
        String[] commands = splitCommands(string);
        String[] trimmedCommands = removeCommentsAndSpaces(commands);
        return parseCommands(trimmedCommands);
    }

    private static String[] splitCommands(String string) {
        return string.split("\r\n");
    }

    private static String[] removeCommentsAndSpaces(String[] string) {
        String current;
        String[] returnArray = new String[string.length];
        for (int i = 0; i < string.length; i++) {
            current = string[i];
            current = current.replace(" ", "");
            Matcher match = commentsLinePattern.matcher(current);
            if (match.matches()) {
                returnArray[i] = current.substring(0, match.start()).trim();
            } else {
                returnArray[i] = current.trim();
            }
        }
        return returnArray;
    }

    private static void examineCommands(String[] commands) throws ParseError {
        for (int i = 0; i < commands.length; i++) {
            if (commands[i].trim().isEmpty()) {
                continue;
            }

            Matcher match = commandPattern.matcher(commands[i]);
            if (match.matches()) {
                try {
                    CommandParser.parseCommand(match.group());
                } catch (ParseError e) {
                    throw new ParseError(i, e);
                }

            } else {
                throw new ParseError(String.format("Wrong Command Structure \"%s\"", commands[i]), i);
            }
        }
    }

    private static List<Command> parseCommands(String[] trimmedCommands) throws ParseError {
        List<Command> returnList = new ArrayList<>();

        for (int i = 0; i < trimmedCommands.length; i++) {
            if (trimmedCommands[i].trim().isEmpty()) {
                continue;
            }

            Matcher match = commandPattern.matcher(trimmedCommands[i]);
            if (match.matches()) {
                try {
                    returnList.add(CommandParser.parseCommand(match.group()));
                } catch (ParseError e) {
                    throw new ParseError(i, e);
                }

            } else {
                throw new ParseError(String.format("Wrong Command Structure \"%s\"", trimmedCommands[i]), i);
            }
        }
        return returnList;
    }

}

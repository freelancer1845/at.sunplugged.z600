package at.sunplugged.z600.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileAccessUtils {

    public static String[] getScriptNames() {
        File scriptFolder = new File("scripts");
        if (scriptFolder.exists() == false) {
            scriptFolder.mkdir();
        }
        File[] listOfFiles = scriptFolder.listFiles();
        if (listOfFiles.length == 0) {
            return new String[0];
        }

        List<String> listOfFileNames = new ArrayList<String>();

        Arrays.asList(listOfFiles).stream().filter(file -> file.getName().matches(".+\\.sc$"))
                .forEach(file -> listOfFileNames.add(file.getName()));
        return listOfFileNames.toArray(new String[0]);
    }

    public static String getScriptByName(String scriptName) throws IOException {
        File scriptFile = new File("scripts/" + scriptName);
        if (scriptFile.exists() == false) {
            throw new IOException(String.format("Script file with name \"%s\" does not exist.", scriptName));
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(scriptFile))) {
            fileReader.lines().forEach(line -> stringBuilder.append(line).append(System.lineSeparator()));
        }
        return stringBuilder.toString();
    }

    private FileAccessUtils() {

    }
}

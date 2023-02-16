package bam.exe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;

public class Run {

    private static final String os = System.getProperty("os.name").toLowerCase();
    private static final String configFileName = "Config_BaM.txt";
    private static final String exeDir = "./exe/";
    private static final String exeName = "BaM";
    private static final String exeCommand = os.startsWith("windows")
            ? Path.of(exeDir, String.format("%s.exe", exeName)).toString()
            : exeName;

    static private Process exeProc = null;

    public static void run(ConfigFile configFile) {

        configFile.writeToFile(Path.of(exeDir, configFileName).toString());

        String[] cmd = { exeCommand };
        File exeDirectory = new File(exeDir);
        try {
            exeProc = Runtime.getRuntime().exec(cmd, null, exeDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream inputStream = exeProc.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferReader = new BufferedReader(inputStreamReader);
        ArrayList<String> consoleLines = new ArrayList<String>();
        String currentLine = null;
        try {
            while ((currentLine = bufferReader.readLine()) != null) {
                System.out.println(String.format("\"%s\"", currentLine));
                consoleLines.add(currentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.flush();
        int exitcode = -1;
        try {
            exitcode = exeProc.waitFor();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (exitcode != 0) {
            String errorMessage = "Unknown Error Message";
            boolean hasMessage = false;
            for (int k = 0; k < consoleLines.size(); k++) {
                currentLine = consoleLines.get(k);
                if (currentLine.contains("FATAL ERROR has occured")) {
                    errorMessage = "";
                    hasMessage = true;
                } else if (currentLine.contains("Execution will stop")) {
                    break;
                }
                if (hasMessage) {
                    errorMessage = String.format("%s\n%s", errorMessage, currentLine);
                }
            }
            // ConsoleLines.forEach(null);
            // throw new InterruptedException(errorMessage);
        }
    }
}

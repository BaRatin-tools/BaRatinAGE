package org.baratinage.jbam.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.baratinage.utils.ConsoleLogger;

public class ExeRun implements Runnable {

    private File exeDirFile;
    private String[] cmd;
    private final List<Consumer<String>> consoleOutputConsumers = new ArrayList<>();
    private int exitValue = -999;
    private Process process;
    private List<String> lastRunConsoleOutputs;

    public void setExeDir(String exeDir) {
        exeDirFile = new File(exeDir);
    }

    public void setCommand(String... command) {
        cmd = command;
    }

    public int getExitValue() {
        return exitValue;
    }

    public void addCommandArg(String argName, String argValue) {
        int n = cmd.length;
        String[] command = new String[n + 2];
        for (int k = 0; k < n; k++) {
            command[k] = cmd[k];
        }
        command[n] = argName;
        command[n + 1] = argValue;
    }

    public void removeArgument(String argName) {
        int n = cmd.length;
        boolean hasValue = true;
        int index = -1;
        for (int k = 0; k < n; k++) {
            if (cmd[k].equals(argName)) {
                hasValue = (k != n - 1) && (!cmd[k + 1].startsWith("-"));
                index = k;
                break;
            }
        }
        if (index > 0) {
            String[] command = new String[n - (hasValue ? 2 : 1)];
            int offset = 0;
            for (int k = 0; k < n; k++) {
                if (k == index || (hasValue && k == index + 1)) {
                    offset++;
                } else {
                    command[k - offset] = cmd[k];
                }

            }
        }
    }

    public void addConsoleOutputConsumer(Consumer<String> consoleOutputConsumer) {
        consoleOutputConsumers.add(consoleOutputConsumer);
    }

    public void removeConsoleOutputConsumer(Consumer<String> consoleOutputConsumer) {
        consoleOutputConsumers.remove(consoleOutputConsumer);
    }

    private void publishConsoleOutput(String output) {
        for (Consumer<String> consoleOutputConsumer : consoleOutputConsumers) {
            consoleOutputConsumer.accept(output);
        }
    }

    public List<String> getLastRunConsoleOutputs() {
        return lastRunConsoleOutputs;
    }

    public Process getProcess() {
        return process;
    }

    @Override
    public void run() {
        if (exeDirFile == null || cmd == null) {
            ConsoleLogger.error(
                    "ExeRun Error: exeDir and command must be specified in constructor or with setter methods!");
            return;
        }
        String cmdStr = String.join(" ", cmd);
        ConsoleLogger.log("runnning command '" + cmdStr + "'...");

        try {
            process = Runtime.getRuntime().exec(cmd, null, exeDirFile);

            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferReader = new BufferedReader(inputStreamReader);
            lastRunConsoleOutputs = new ArrayList<>();
            String currentLine = null;

            try {
                while ((currentLine = bufferReader.readLine()) != null) {
                    lastRunConsoleOutputs.add(currentLine);
                    publishConsoleOutput(currentLine);
                }
            } catch (IOException e) {
                ConsoleLogger.error(e);
            }

            boolean hasFinished = true;

            hasFinished = process.waitFor(250, TimeUnit.MILLISECONDS);

            if (hasFinished) {
                exitValue = process.exitValue();
            }

        } catch (IOException | InterruptedException | IllegalThreadStateException e) {
            ConsoleLogger.error(e);
            return;
        }
    }
}

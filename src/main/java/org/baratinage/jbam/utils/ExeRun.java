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

public class ExeRun implements Runnable {

    private File exeDirFile;
    private String[] cmd;
    private final List<Consumer<String>> consolOutputConsumers = new ArrayList<>();
    private int exitValue = -999;
    private Process process;

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

    public void addConsolOutputConsumer(Consumer<String> consolOutputConsumer) {
        consolOutputConsumers.add(consolOutputConsumer);
    }

    public void removeConsolOutputConsumer(Consumer<String> consolOutputConsumer) {
        consolOutputConsumers.remove(consolOutputConsumer);
    }

    private void publishConsolOutput(String output) {
        for (Consumer<String> consolOutputConsumer : consolOutputConsumers) {
            consolOutputConsumer.accept(output);
        }
    }

    public Process getProcess() {
        return process;
    }

    @Override
    public void run() {
        if (exeDirFile == null || cmd == null) {
            System.err.println(
                    "ExeRun Error: exeDir and command must be specified in constructor or with setter methods!");
            return;
        }
        String cmdStr = String.join(" ", cmd);
        System.out.println("ExeRun: runnning command '" + cmdStr + "'...");

        try {
            process = Runtime.getRuntime().exec(cmd, null, exeDirFile);

            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferReader = new BufferedReader(inputStreamReader);
            List<String> consoleLines = new ArrayList<String>();
            String currentLine = null;

            try {
                while ((currentLine = bufferReader.readLine()) != null) {
                    consoleLines.add(currentLine);
                    publishConsolOutput(currentLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean hasFinished = true;
            try {
                hasFinished = process.waitFor(250, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            if (hasFinished) {
                try {
                    exitValue = process.exitValue();
                } catch (IllegalThreadStateException e) {
                    e.printStackTrace();
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}

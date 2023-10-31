package org.baratinage.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class ConsoleLogger {

    private static String id;
    private static Path logFilePath;

    public static void init() {
        id = Misc.getTimeStampedId();

        String exePath = System.getProperty("jpackage.app-path");
        String appRootDir = exePath;
        if (exePath == null) {
            appRootDir = Paths.get("").toAbsolutePath().toString();
        } else {
            appRootDir = Path.of(exePath).getParent().toString();
        }

        logFilePath = Path.of(appRootDir, id + ".log");
        try {
            logFilePath.toFile().createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // FIXME: not sure how/when this method should be called
    // Runtime.getRuntime().addShutdownHook() is an option
    public static void cleanup() {
        File f = logFilePath.toFile();
        if (f.exists()) {
            f.delete();
        }
        logFilePath = null;
    }

    private static void addLogToLogFile(String log) {
        if (logFilePath == null) {
            return;
        }
        try {
            Files.write(logFilePath, Arrays.asList(log), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getCallerInfo() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length > 4) {
            String[] className = trace[4].getClassName().split("\\.");
            // String methodName = trace[4].getMethodName();
            return className[className.length - 1];
        }
        return "";
    }

    private static String parseMessage(String prefix, String message) {
        return Misc.getTimeStamp("yyyy-MM-dd HH:mm:ss") + " : (" + getCallerInfo() + ")"
                + (prefix.length() == 0 ? " " : " ***" + prefix + "*** ") + message;
    }

    public static void log(String message) {
        String parsedMessage = parseMessage("", message);
        System.out.println(parsedMessage);
        addLogToLogFile(parsedMessage);
    }

    public static void error(String error) {
        String parsedMessage = parseMessage("ERROR", error);
        System.out.println(parsedMessage);
        addLogToLogFile(parsedMessage);
    }

    public static void error(Exception exception) {
        String parsedMessage = parseMessage("ERROR", "");
        System.out.println(parsedMessage);
        System.out.println(exception);
        addLogToLogFile(parsedMessage + exception.toString());
    }

    public static void stackTrace(Exception exception) {
        String parsedMessage = parseMessage("ERROR", "");
        System.out.println(parsedMessage);
        exception.printStackTrace();
        addLogToLogFile(parsedMessage + exception.toString());
    }

    public static void debuggingStackTrace() {
        String parsedMessage = parseMessage("DEBUG", "");
        System.out.println(parsedMessage);
        Thread.dumpStack();
    }

}

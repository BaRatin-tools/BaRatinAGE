package org.baratinage.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private static String getCallerClassName() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length < 1) {
            return "";
        }
        for (int k = 1; k < trace.length; k++) {
            String[] fullClassName = trace[k].getClassName().split("\\.");
            String className = fullClassName[fullClassName.length - 1];
            if (!className.equals("ConsoleLogger")) {
                return className;
            }
        }
        return "";
    }

    private static String parseMessage(String callerClassName, String prefix, String message) {
        return Misc.getTimeStamp("yyyy-MM-dd HH:mm:ss") + " : (" + callerClassName + ")"
                + (prefix.length() == 0 ? " " : " ***" + prefix + "*** ") + message;
    }

    public static void log(String message) {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "", message);
        out(callerClassName, parsedMessage);
        addLogToLogFile(parsedMessage);
    }

    public static void error(String error) {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "ERROR", error);
        System.err.println(parsedMessage);
        addLogToLogFile(parsedMessage);
    }

    public static void error(Exception exception) {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "ERROR", "");
        System.err.println(parsedMessage);
        System.err.println(exception);
        addLogToLogFile(parsedMessage + exception.toString());
    }

    public static void stackTrace(Exception exception) {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "ERROR", "");
        out(callerClassName, parsedMessage);
        exception.printStackTrace();
        addLogToLogFile(parsedMessage + exception.toString());
    }

    public static void debuggingStackTrace() {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "DEBUG", "");
        out(callerClassName, parsedMessage);
        Thread.dumpStack();
    }

    private static List<String> showFilters = new ArrayList<>();

    public static void addShowFilter(String filter) {
        showFilters.add(filter);
    }

    public static void clearShowFilters() {
        showFilters.clear();
    }

    private static List<String> hideFilters = new ArrayList<>();

    public static void addHideFilters(String filter) {
        hideFilters.add(filter);
    }

    public static void clearHideFilters() {
        hideFilters.clear();
    }

    private static void out(String className, String message) {
        if (showFilters.size() > 0) {
            for (String f : showFilters) {
                if (className.equals(f)) {
                    System.out.println(message);
                }
            }
        } else if (hideFilters.size() > 0) {
            boolean show = true;
            for (String f : hideFilters) {
                if (className.equals(f)) {
                    show = false;
                    break;
                }
            }
            if (show) {
                System.out.println(message);
            }
        } else {
            System.out.println(message);
        }

    }

}

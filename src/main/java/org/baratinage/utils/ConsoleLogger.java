package org.baratinage.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lightweight console logger used by the application.
 * <p>
 * Logs messages to the console and to a log file under the application's log
 * directory.
 * It supports different levels (log, warn, error) and can print stack traces
 * with
 * configurable formatting.
 * </p>
 */
public class ConsoleLogger {

    private static String id;
    private static Path logFilePath;
    private static Path logFolderPath;

    /**
     * Initialize the logger by creating a new log file and preparing the log
     * folder.
     */
    public static void init() {
        id = Misc.getTimeStampedId();

        String exePath = System.getProperty("jpackage.app-path");
        String appRootDir = exePath;
        if (exePath == null) {
            appRootDir = Paths.get("").toAbsolutePath().toString();
        } else {
            appRootDir = Path.of(exePath).getParent().toString();
        }

        logFolderPath = Path.of(appRootDir, "log");
        logFilePath = Path.of(appRootDir, "log", id + ".log");
        cleanup();
        createLogFile();
    }

    /** Clean up old log files in the log directory. */
    private static void cleanup() {
        for (File file : logFolderPath.toFile().listFiles()) {
            if (file.getName().endsWith(".log")) {
                try {
                    BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    long elapsed = System.currentTimeMillis() - attr.lastModifiedTime().toMillis();
                    if (elapsed > 86400000) {
                        file.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Ensure the log file exists before writing. */
    private static void createLogFile() {
        File f = logFilePath.toFile();
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Append a log line to the on-disk log file if available. */
    private static void addLogToLogFile(String log) {
        if (logFilePath == null) {
            return;
        }
        createLogFile();
        try {
            Files.write(logFilePath, Arrays.asList(log), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Retrieve the caller class and line number for log formatting. */
    private static String getCallerClassName() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length < 1) {
            return "";
        }
        for (int k = 1; k < trace.length; k++) {
            String className = trace[k].getClassName();
            if (!className.equals("org.baratinage.utils.ConsoleLogger")) {
                String[] splitClassName = trace[k].getClassName().split("\\.");
                return splitClassName[splitClassName.length - 1] + "#" + trace[k].getLineNumber() + "";
            }
        }
        return "";
    }

    /** Build the final log message with timestamp and caller info. */
    private static String parseMessage(String callerClassName, String prefix, String message) {
        return Misc.getTimeStamp("yyyy-MM-dd HH:mm:ss") + " : (" + callerClassName + ")"
                + (prefix.length() == 0 ? " " : " ***" + prefix + "*** ") + message;
    }

    /** Log an object by its string representation. */
    public static void log(Object object) {
        log(object.toString());
    }

    /** Log a string message. */
    public static void log(String message) {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "", message);
        out(callerClassName, parsedMessage);
        addLogToLogFile(parsedMessage);
    }

    /** Log a warning message. */
    public static void warn(String error) {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "WARNING", error);
        System.out.println(parsedMessage);
        addLogToLogFile(parsedMessage);
    }

    /** Log a warning with an exception. */
    public static void warn(Exception exception) {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "WARNING", "");
        System.out.println(parsedMessage);
        System.out.println(exception);
        addLogToLogFile(parsedMessage + exception.toString());
    }

    /** Log an error message. */
    public static void error(String error) {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "ERROR", error);
        System.err.println(parsedMessage);
        addLogToLogFile(parsedMessage);
    }

    /** Log an error with an exception. */
    public static void error(Exception exception) {
        error(exception, false);
    }

    /** Log an error with an exception, with optional full stack trace. */
    public static void error(Exception exception, boolean fullErrorStack) {
        String traceMsg = exception.toString();

        StackTraceElement[] trace = exception.getStackTrace();
        boolean lastItem = false;
        for (StackTraceElement e : trace) {
            String msg = e.toString();
            if (!msg.startsWith("org.baratinage") && !fullErrorStack) {
                lastItem = true;
            }
            traceMsg += ("\n > " + msg);
            if (lastItem) {
                traceMsg += ("\n > ...");
                break;
            }
        }

        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "ERROR", traceMsg);
        System.err.println(parsedMessage);
        addLogToLogFile(parsedMessage + exception.toString());
    }

    /** Dump the current stack trace for debugging. */
    public static void debuggingStackTrace() {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "DEBUG", "");
        out(callerClassName, parsedMessage);
        Thread.dumpStack();
    }

    private static List<String> showFilters = new ArrayList<>();

    /** Add a show filter to force logging of specific classes. */
    public static void addShowFilter(String filter) {
        showFilters.add(filter);
    }

    /** Clear all show filters. */
    public static void clearShowFilters() {
        showFilters.clear();
    }

    private static List<String> hideFilters = new ArrayList<>();

    /** Add a filter to hide logs from a specific class. */
    public static void addHideFilters(String filter) {
        hideFilters.add(filter);
    }

    /** Clear all hide filters. */
    public static void clearHideFilters() {
        hideFilters.clear();
    }

    /** Conditional print based on show/hide filters. */
    private static void out(String className, String message) {
        if (showFilters.size() > 0) {
            for (String f : showFilters) {
                if (className.equals(f)) {
                    System.out.println(message);
                    return;
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
                return;
            }
        } else {
            System.out.println(message);
            return;
        }

    }

}

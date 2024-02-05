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

public class ConsoleLogger {

    private static String id;
    private static Path logFilePath;
    private static Path logFolderPath;

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

    private static String parseMessage(String callerClassName, String prefix, String message) {
        return Misc.getTimeStamp("yyyy-MM-dd HH:mm:ss") + " : (" + callerClassName + ")"
                + (prefix.length() == 0 ? " " : " ***" + prefix + "*** ") + message;
    }

    public static void log(Object object) {
        log(object.toString());
    }

    public static void log(String message) {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "", message);
        out(callerClassName, parsedMessage);
        addLogToLogFile(parsedMessage);
    }

    public static void warn(String error) {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "WARNING", error);
        System.out.println(parsedMessage);
        addLogToLogFile(parsedMessage);
    }

    public static void warn(Exception exception) {
        String callerClassName = getCallerClassName();
        String parsedMessage = parseMessage(callerClassName, "WARNING", "");
        System.out.println(parsedMessage);
        System.out.println(exception);
        addLogToLogFile(parsedMessage + exception.toString());
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

package org.baratinage.utils.perf;

import java.util.HashMap;

import org.baratinage.utils.ConsoleLogger;

public class Performance {

    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        ConsoleLogger.log("Used memory in bytes (megabytes): "
                + memory + " (" + bytesToMegabytes(memory) + "mb)");

    }

    private static HashMap<String, Long> startedTimeMonitoring = new HashMap<>();

    public static void startTimeMonitoring(String key) {
        startedTimeMonitoring.put(key, System.currentTimeMillis());
    }

    public static void endTimeMonitoring(String key) {
        Long startTime = startedTimeMonitoring.get(key);
        if (startTime != null) {
            Long endTime = System.currentTimeMillis();
            Long duration = endTime - startTime;
            String message = String.format("Duration for '%s' is %d ms", key, duration);
            ConsoleLogger.log(message);
            startedTimeMonitoring.remove(key);
        }
    }

}

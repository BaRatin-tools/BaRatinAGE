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

    private static HashMap<Object, Long> startedTimeMonitoring = new HashMap<>();

    public static void startTimeMonitoring(Object key) {
        startedTimeMonitoring.put(key, System.currentTimeMillis());
    }

    public static void endTimeMonitoring(Object key) {
        Long startTime = startedTimeMonitoring.get(key);
        if (startTime != null) {
            Long endTime = System.currentTimeMillis();
            Long duration = endTime - startTime;
            ConsoleLogger.log("Time elapsed in milliseconds (seconds): " +
                    duration + "ms (" + duration / 1000.0 + "s)");
            startedTimeMonitoring.remove(key);
        }
    }

}

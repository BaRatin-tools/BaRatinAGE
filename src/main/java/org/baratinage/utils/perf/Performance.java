package org.baratinage.utils.perf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private static HashMap<String, List<Long>> startedTimeMonitoring = new HashMap<>();

    public static void startTimeMonitoring(String key) {
        ArrayList<Long> timeMonitoring = new ArrayList<>();
        timeMonitoring.add(System.currentTimeMillis());
        startedTimeMonitoring.put(key, timeMonitoring);
    }

    public static void checkTimeMonitoring(String key, String checkpoint) {
        List<Long> monitoringTimes = startedTimeMonitoring.get(key);
        if (monitoringTimes != null) {
            Long endTime = System.currentTimeMillis();
            Long duration = endTime - monitoringTimes.get(monitoringTimes.size() - 1);
            monitoringTimes.add(endTime);
            startedTimeMonitoring.put(key, monitoringTimes);
            String message = String.format("Duration for '%s' at '%s' is %d ms", key, checkpoint, duration);
            ConsoleLogger.log(message);
        }
    }

    public static void endTimeMonitoring(String key) {
        List<Long> monitoringTimes = startedTimeMonitoring.get(key);
        if (monitoringTimes != null) {
            Long endTime = System.currentTimeMillis();
            Long duration = endTime - monitoringTimes.get(0);
            String message = String.format("Duration for '%s' is %d ms", key, duration);
            ConsoleLogger.log(message);
            startedTimeMonitoring.remove(key);
        }
    }

}

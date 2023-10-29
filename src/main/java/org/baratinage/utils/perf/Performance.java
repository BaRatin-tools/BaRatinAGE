package org.baratinage.utils.perf;

import java.util.HashMap;

public class Performance {

    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is bytes:         " + memory);
        System.out.println("Used memory is megabytes:     " +
                bytesToMegabytes(memory));
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
            System.out.println("Time elapsed in milliseconds: " + duration);
            System.out.println("Time elapsed in seconds:      " + duration / 1000.0);
            startedTimeMonitoring.remove(key);
        }
    }

    public static void printTimeElapsed(double startTime) {
        double endTime = System.currentTimeMillis();
        double elapsedTime = endTime - startTime;
        System.out.println("Time elapsed in milliseconds: " + elapsedTime);
        System.out.println("Time elapsed in seconds:      " + elapsedTime / 1000.0);
    }
}

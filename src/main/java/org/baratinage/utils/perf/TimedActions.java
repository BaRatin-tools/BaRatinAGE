package org.baratinage.utils.perf;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;

public class TimedActions {

    private static Map<String, Runnable> throttledActions = new HashMap<>();

    public static void throttle(String id, int delayMilliseconds, Runnable action) {

        if (throttledActions.containsKey(id)) {
            ConsoleLogger.log("Updating throttled action... (" + id + ")");
            throttledActions.put(id, action);
            return;
        }

        ConsoleLogger.log("Setting up throttler and running action... (" + id + ")");
        action.run();

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(
                        () -> {
                            Runnable throttledAction = throttledActions.get(id);
                            if (throttledAction != null) {
                                ConsoleLogger.log("Running throttled action ... (" + id + ")");
                                throttledAction.run();
                            }
                            throttledActions.remove(id);
                        });
            }

        };
        throttledActions.put(id, null);

        timer.schedule(task, delayMilliseconds);
    }

    private static Map<String, Timer> debouncedActions = new HashMap<>();

    public static void debounce(String id, int delayMilliseconds, Runnable action) {

        if (debouncedActions.containsKey(id)) {
            Timer timer = debouncedActions.get(id);
            ConsoleLogger.log("Canceling debounced action... (" + id + ")");
            timer.cancel();
            timer.purge();
        }

        ConsoleLogger.log("Setting up debouncer... (" + id + ")");
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(
                        () -> {
                            ConsoleLogger.log("Running debounced action... (" + id + ")");
                            action.run();
                            debouncedActions.remove(id);
                        });
            }

        };
        debouncedActions.put(id, timer);
        timer.schedule(task, delayMilliseconds);
    }

    private static Map<String, Timer> delayedActions = new HashMap<>();

    public static void delay(int delayMilliseconds, Runnable action) {

        String id = Misc.getTimeStampedId();

        ConsoleLogger.log("Setting up delayed action... (" + id + ")");
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(
                        () -> {
                            ConsoleLogger.log("Running delayed action... (" + id + ")");
                            action.run();
                            delayedActions.remove(id);
                        });
            }

        };
        delayedActions.put(id, timer);
        timer.schedule(task, delayMilliseconds);
    }

    private static Map<String, Timer> intervalAction = new HashMap<>();

    public static void interval(String id, int intervalDelayMilliseconds, Runnable action) {

        if (intervalAction.containsKey(id)) {
            stopInterval(id);
        }

        ConsoleLogger.log("Setting up interval action... (" + id + ")");
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(
                        () -> {
                            action.run();
                        });
            }

        };
        intervalAction.put(id, timer);
        timer.scheduleAtFixedRate(task, 0, intervalDelayMilliseconds);
    }

    public static void stopInterval(String id) {
        Timer timer = intervalAction.get(id);
        if (timer != null) {
            ConsoleLogger.log("Canceling interval action... (" + id + ")");
            timer.cancel();
            timer.purge();
            intervalAction.remove(id);
        }
    }
}

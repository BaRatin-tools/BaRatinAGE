package org.baratinage.utils.perf;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

public class Throttler {

    private static Map<String, Runnable> throttledActions = new HashMap<>();

    public static void throttle(String id, int delayMilliseconds, Runnable action) {

        if (throttledActions.containsKey(id)) {
            System.out.println("Updating throttled action... (" + id + ")");
            throttledActions.put(id, action);
            return;
        }

        System.out.println("Running action to throttle... (" + id + ")");
        action.run();

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(
                        () -> {
                            Runnable throttledAction = throttledActions.get(id);
                            if (throttledAction != null) {
                                System.out.println("Running throttled action ... (" + id + ")");
                                throttledAction.run();
                            }
                            throttledActions.remove(id);
                        });
            }

        };
        throttledActions.put(id, null);

        timer.schedule(task, delayMilliseconds);
    }

}

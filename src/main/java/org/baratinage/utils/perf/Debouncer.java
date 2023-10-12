package org.baratinage.utils.perf;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

public class Debouncer {

    private static Map<String, Timer> debouncedActions = new HashMap<>();

    public static void debounce(String id, int delayMilliseconds, Runnable action) {

        if (debouncedActions.containsKey(id)) {
            Timer timer = debouncedActions.get(id);
            System.out.println("Canceling debounced action... (" + id + ")");
            timer.cancel();
        }

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(
                        () -> {
                            System.out.println("Running debounced action... (" + id + ")");
                            action.run();
                            debouncedActions.remove(id);
                        });
            }

        };
        debouncedActions.put(id, timer);
        timer.schedule(task, delayMilliseconds);
    }
}

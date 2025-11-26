package org.baratinage.utils.perf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

public class TasksWorker<T> {

  private final List<T> objects = new ArrayList<>();
  private final Map<T, Consumer<T>> tasks = new HashMap<>();
  private final Map<T, Consumer<T>> befores = new HashMap<>();
  private final Map<T, Consumer<T>> afters = new HashMap<>();

  private record Listener<T>(Map<T, Consumer<T>> consumers, T object) {

  }

  public void addTask(T object, Consumer<T> task, Consumer<T> before, Consumer<T> after) {
    objects.add(object);
    tasks.put(object, task);
    befores.put(object, before);
    afters.put(object, after);
  }

  public void run() {
    SwingWorker<Void, Listener<T>> worker = new SwingWorker<>() {

      @Override
      protected Void doInBackground() throws Exception {

        for (int k = 0; k < tasks.size(); k++) {
          T object = objects.get(k);
          Consumer<T> task = tasks.get(k);
          publish(new Listener<T>(befores, object));
          task.accept(object);
          publish(new Listener<T>(afters, object));
        }

        return null;
      }

      @Override
      protected void process(List<Listener<T>> listeners) {
        for (Listener<T> listener : listeners) {
          T object = listener.object;
          Consumer<T> consumer = listener.consumers.get(object);
          if (consumer != null) {
            listener.consumers.get(object).accept(object);
          }
        }
      }

    };

    worker.execute();
  }

}

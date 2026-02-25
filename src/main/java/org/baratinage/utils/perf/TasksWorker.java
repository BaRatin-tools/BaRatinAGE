package org.baratinage.utils.perf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.SwingWorker;

public class TasksWorker<A, B> {

  private boolean canceled = false;

  private final List<A> objects = new ArrayList<>();
  private final Map<A, Function<A, B>> tasks = new HashMap<>();
  private final Map<A, Consumer<A>> befores = new HashMap<>();
  private final Map<A, Consumer<B>> afters = new HashMap<>();
  private Runnable onDone = () -> {
  };

  private record Listener<A, B>(A input, B output) {

  }

  public void addTask(A object, Function<A, B> task, Consumer<A> before, Consumer<B> after) {
    objects.add(object);
    tasks.put(object, task);
    befores.put(object, before);
    afters.put(object, after);
  }

  public void setOnDoneAction(Runnable onDone) {
    this.onDone = onDone;
  }

  public void run() {

    SwingWorker<Void, Listener<A, B>> worker = new SwingWorker<>() {

      @Override
      protected Void doInBackground() throws Exception {

        canceled = false;
        for (int k = 0; k < tasks.size(); k++) {
          if (canceled) {
            publish(new Listener<A, B>(null, null));
            return null;
          }
          A input = objects.get(k);
          Function<A, B> task = tasks.get(input);
          publish(new Listener<A, B>(input, null));
          B output = task.apply(input);
          publish(new Listener<A, B>(input, output));
        }

        publish(new Listener<A, B>(null, null));
        return null;
      }

      @Override
      protected void process(List<Listener<A, B>> listeners) {
        for (Listener<A, B> listener : listeners) {
          if (listener.input == null) {
            onDone.run();
            return;
          }
          if (listener.output == null) {
            Consumer<A> before = befores.get(listener.input);
            if (before != null) {
              before.accept(listener.input);
            }
          } else {
            Consumer<B> after = afters.get(listener.input);
            if (after != null) {
              after.accept(listener.output);
            }
          }
        }
      }

    };

    worker.execute();
  }

  public void cancel() {
    canceled = true;
  }

  public boolean wasCanceled() {
    return canceled;
  }

}

package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class that stores a value of some type A.
 * Any change to the value triggers listeners that may be attached.
 * 
 * This is typically useful when a value can change for different
 * reason and when the change should propagate to other users of
 * the value.
 */
public class ReactiveValue<A> {
  private A value;

  public ReactiveValue(A v) {

    set(v);
  }

  public void set(A v) {
    if (!v.equals(value)) {
      value = v;
      fireListeners();
    }
  }

  public A get() {
    return value;
  }

  private final List<Consumer<A>> listeners = new ArrayList<>();

  public void addListener(Consumer<A> consumer) {
    listeners.add(consumer);
  }

  public void remListener(Consumer<A> consumer) {
    listeners.remove(consumer);
  }

  public void clearListeners() {
    listeners.clear();
  }

  private boolean doNotFireListeners = false;

  private void fireListeners() {
    if (doNotFireListeners) {
      return;
    }
    doNotFireListeners = true;
    for (Consumer<A> l : listeners) {
      l.accept(value);
    }
    doNotFireListeners = false;
  }

}

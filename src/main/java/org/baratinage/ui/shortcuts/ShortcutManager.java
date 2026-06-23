package org.baratinage.ui.shortcuts;

import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.baratinage.AppSetup;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.WriteFile;
import org.json.JSONObject;

public class ShortcutManager {

  // a shortcut binding is defined by
  // - a String id
  // - a Shortcut definition (key, modifiers)
  // - Context (defined by arbitrary object) dependent Runnables
  // (note: if no context is provided (or null), this is used as a default)

  // id -> binding
  private final Map<String, Binding> bindings = new HashMap<>();

  // id -> context object -> action
  private final Map<String, HashMap<Object, Runnable>> actions = new HashMap<>();

  // Context stack (used to know the priority of shortcuts)
  private final Deque<Object> contextStack = new ArrayDeque<>();

  // Reverse reference to retrieve binding ids from a context
  private final Map<Object, Set<String>> contextBindings = new HashMap<>();

  public ShortcutManager() {
    contextStack.push(this); // global context is defined with the ShortcutManager
    KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .addKeyEventDispatcher(event -> {
          if (event.getID() != KeyEvent.KEY_PRESSED)
            return false;
          Shortcut pressed = Shortcut.of(event.getKeyCode(), event.getModifiersEx());
          return tryDispatch(pressed);
        });

    registerBinding("global.open_project", false,
        Shortcut.of("O", true, false, false));
    registerBinding("global.new_project", false,
        Shortcut.of("N", true, false, false));
    registerBinding("global.save_project", false,
        Shortcut.of("S", true, false, false));
    registerBinding("global.save_project_as", false,
        Shortcut.of("S", true, true, false));
    registerBinding("global.close_project", false,
        Shortcut.of("W", true, false, false));
    registerBinding("global.import_v2_project", false,
        Shortcut.of("I", true, false, false));
    registerBinding("global.export_report", false,
        Shortcut.of("E", true, false, false));
    registerBinding("global.exit", false,
        Shortcut.of("F4", false, false, true));
    registerBinding("global.help", false,
        Shortcut.of("F1", false, false, false));

    registerBinding("component.create_hydraulic_config", true,
        Shortcut.of("C", true, true, false));
    registerBinding("component.create_gaugings", true,
        Shortcut.of("G", true, true, false));
    registerBinding("component.create_rating_curve", true,
        Shortcut.of("R", true, true, false));
    registerBinding("component.create_limnigraph", true,
        Shortcut.of("H", true, true, false));
    registerBinding("component.create_hydrograph", true,
        Shortcut.of("Q", true, true, false));

    registerBinding(
        "rc_comparator.toggle_item_display",
        true,
        Shortcut.of("D", false, false, true));
    registerBinding(
        "rc_comparator.toggle_item_lgd_display",
        true,
        Shortcut.of("L", false, false, true));

  }

  /**
   * Handles swithing context using a focusable element
   * 
   * @param component a focusable JComponenent
   * @param ctx       an arbitrary object defining the context
   */
  public void setFocusDepedentContext(JComponent component, Object ctx) {
    component.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        pushContext(ctx == null ? this : ctx);
      }

      @Override
      public void focusLost(FocusEvent e) {
        popContext(ctx == null ? this : ctx);
      }
    });
  }

  private void pushContext(Object ctx) {
    contextStack.push(ctx == null ? this : ctx);
  }

  private void popContext(Object ctx) {
    contextStack.remove(ctx == null ? this : ctx); // removes first occurance
  }

  public void registerBinding(String id, boolean editable, Shortcut defaultShortcut) {
    Binding binding = new Binding(id, editable, defaultShortcut);
    bindings.put(id, binding);
  }

  public void setBindingAction(String bindingId, Runnable action) {
    setBindingAction(bindingId, this, action); // global context
  }

  public void setBindingAction(String bindingId, Object ctx, Runnable action) {
    Binding binding = getBinding(bindingId);
    if (binding == null) {
      return;
    }
    HashMap<Object, Runnable> runnables = actions.get(binding.id);
    if (runnables == null) {
      runnables = new HashMap<>();
      actions.put(binding.id, runnables);
    }
    Object ctxNotNull = ctx == null ? this : ctx;
    runnables.put(ctxNotNull, action);
    if (!contextBindings.containsKey(ctxNotNull)) {
      contextBindings.put(ctxNotNull, new HashSet<>());
    }
    contextBindings.get(ctxNotNull).add(binding.id);
  }

  public Binding getBinding(String id) {
    Binding binding = bindings.get(id);
    if (binding == null) {
      String msg = "No binding with id '%s' found".formatted(id);
      ConsoleLogger.warn(msg);
    }
    return binding;
  }

  private Runnable getAction(Binding binding, Object ctx) {
    Map<Object, Runnable> runnables = actions.get(binding.id);
    if (runnables == null) {
      ConsoleLogger.warn("No action found for binding with id '%s'".formatted(binding.id));
      return null; // should not happend
    }
    Runnable action = runnables.get(ctx == null ? this : ctx);
    if (action == null) {
      ConsoleLogger.warn("No action found for binding with id '%s' in the provided context".formatted(binding.id));
      return null; // should not happend
    }
    return action;
  }

  /**
   * Walk the context stack top-down and fire the first matching action (if any).
   */
  private boolean tryDispatch(Shortcut pressed) {
    for (Object ctx : contextStack) { // for each context, top to bottom
      Set<String> bindingIds = contextBindings.get(ctx);
      if (bindingIds == null) {
        return false; // should not happend
      }
      for (String id : bindingIds) { // for each action id
        Binding binding = getBinding(id);
        if (binding == null) {
          continue;
        }
        if (pressed.equals(binding.getShortcut())) {
          Runnable action = getAction(binding, ctx);
          SwingUtilities.invokeLater(action);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * helper function to create a menu item which suppress default key binding
   * response to let the shortcut manager be the unique response handler
   * 
   * @param id action id
   * @return a configured JMenuItem
   */
  public JMenuItem createMenuItem(String bindingId) {
    JMenuItem item = new JMenuItem() {
      @Override
      protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        return false;
      }
    };
    Binding binding = getBinding(bindingId);
    if (binding == null) {
      return item;
    }
    Shortcut shortcut = binding.getShortcut();
    if (shortcut == null) {
      return item;
    }
    item.setAccelerator(shortcut.getKeyStroke());
    Runnable action = getAction(binding, this);
    if (action == null) {
      ConsoleLogger.warn("No action linked to binding '%s' found!".formatted(binding.id));
      return item;
    }
    item.addActionListener(e -> action.run());
    return item;
  }

  public void loadShortcuts() {
    try {
      String jsonString = ReadFile.getStringContent(AppSetup.PATH_SHORTCUTS_FILE, true);
      loadFromString(jsonString);
    } catch (IOException e) {
      ConsoleLogger.warn(e);
    }
  }

  private void loadFromString(String configString) {
    JSONObject config = new JSONObject(configString);
    for (String key : config.keySet()) {
      if (!bindings.containsKey(key)) {
        ConsoleLogger.warn("Unknown binding found in config file: '%s'".formatted(key));
        continue;
      }
      Binding binding = getBinding(key);
      binding.setShortcut(Shortcut.of(config.getJSONObject(key)));
    }
  }

  public String getProjectConfigString() {
    return saveToString();
  }

  public void saveConfig() {
    try {
      WriteFile.writeStringContent(AppSetup.PATH_SHORTCUTS_FILE, saveToString());
    } catch (IOException e) {
      ConsoleLogger.error(e);
    }
  }

  private String saveToString() {
    JSONObject configuration = new JSONObject();
    for (Binding binding : bindings.values()) {
      if (binding.editable) {
        Shortcut shortcut = binding.getShortcut();
        if (shortcut != null) {
          configuration.put(binding.id, shortcut.toJSON());
        }
      }
    }
    return configuration.toString(4);
  }

}

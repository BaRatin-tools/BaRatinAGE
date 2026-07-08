package org.baratinage.ui.shortcuts;

import org.baratinage.utils.ConsoleLogger;

public class Binding {
  public final String id;
  public final Shortcut defaultShortcut;
  public final boolean editable;

  private Shortcut shortcut = null;

  public Binding(String id, boolean editable, Shortcut defaultShortcut) {
    this.id = id;
    // this.editable = editable; // disabled for now, all shortcuts are editable
    this.editable = true;
    this.defaultShortcut = defaultShortcut;
  }

  public Shortcut getShortcut() {
    return shortcut;
  }

  public void setShortcut(Shortcut shortcut) {
    if (editable) {
      this.shortcut = shortcut;
    } else {
      ConsoleLogger.error("Cannot modify not editable Binding '%s'".formatted(id));
    }
  }

  public void resetToDefault() {
    shortcut = defaultShortcut;
  }

}

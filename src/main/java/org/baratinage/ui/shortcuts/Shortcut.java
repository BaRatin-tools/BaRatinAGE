package org.baratinage.ui.shortcuts;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import javax.swing.KeyStroke;

import org.baratinage.utils.ConsoleLogger;
import org.json.JSONObject;

public record Shortcut(int keyCode, int modifiers) {

  private static final Map<String, Integer> NAMED_KEYS = new HashMap<>();

  static {
    // Function keys
    NAMED_KEYS.put("F1", KeyEvent.VK_F1);
    NAMED_KEYS.put("F2", KeyEvent.VK_F2);
    NAMED_KEYS.put("F3", KeyEvent.VK_F3);
    NAMED_KEYS.put("F4", KeyEvent.VK_F4);
    NAMED_KEYS.put("F5", KeyEvent.VK_F5);
    NAMED_KEYS.put("F6", KeyEvent.VK_F6);
    NAMED_KEYS.put("F7", KeyEvent.VK_F7);
    NAMED_KEYS.put("F8", KeyEvent.VK_F8);
    NAMED_KEYS.put("F9", KeyEvent.VK_F9);
    NAMED_KEYS.put("F10", KeyEvent.VK_F10);
    NAMED_KEYS.put("F11", KeyEvent.VK_F11);
    NAMED_KEYS.put("F12", KeyEvent.VK_F12);
    // Navigation keys
    NAMED_KEYS.put("ENTER", KeyEvent.VK_ENTER);
    NAMED_KEYS.put("ESCAPE", KeyEvent.VK_ESCAPE);
    NAMED_KEYS.put("ESC", KeyEvent.VK_ESCAPE);
    NAMED_KEYS.put("TAB", KeyEvent.VK_TAB);
    NAMED_KEYS.put("BACKSPACE", KeyEvent.VK_BACK_SPACE);
    NAMED_KEYS.put("DELETE", KeyEvent.VK_DELETE);
    NAMED_KEYS.put("DEL", KeyEvent.VK_DELETE);
    NAMED_KEYS.put("INSERT", KeyEvent.VK_INSERT);
    NAMED_KEYS.put("HOME", KeyEvent.VK_HOME);
    NAMED_KEYS.put("END", KeyEvent.VK_END);
    NAMED_KEYS.put("PAGEUP", KeyEvent.VK_PAGE_UP);
    NAMED_KEYS.put("PAGEDOWN", KeyEvent.VK_PAGE_DOWN);
    NAMED_KEYS.put("UP", KeyEvent.VK_UP);
    NAMED_KEYS.put("DOWN", KeyEvent.VK_DOWN);
    NAMED_KEYS.put("LEFT", KeyEvent.VK_LEFT);
    NAMED_KEYS.put("RIGHT", KeyEvent.VK_RIGHT);
    // Whitespace / misc
    NAMED_KEYS.put("SPACE", KeyEvent.VK_SPACE);
    NAMED_KEYS.put("PAUSE", KeyEvent.VK_PAUSE);
    NAMED_KEYS.put("CAPS", KeyEvent.VK_CAPS_LOCK);
    NAMED_KEYS.put("CAPSLOCK", KeyEvent.VK_CAPS_LOCK);
    NAMED_KEYS.put("PRINTSCREEN", KeyEvent.VK_PRINTSCREEN);
    NAMED_KEYS.put("SCROLLLOCK", KeyEvent.VK_SCROLL_LOCK);
    NAMED_KEYS.put("NUMLOCK", KeyEvent.VK_NUM_LOCK);
    // Windows / Meta key
    NAMED_KEYS.put("WINDOWS", KeyEvent.VK_WINDOWS);
    NAMED_KEYS.put("WIN", KeyEvent.VK_WINDOWS);
    NAMED_KEYS.put("META", KeyEvent.VK_META);
    // Numpad keys
    NAMED_KEYS.put("NUMPAD-0", KeyEvent.VK_NUMPAD0);
    NAMED_KEYS.put("NUMPAD-1", KeyEvent.VK_NUMPAD1);
    NAMED_KEYS.put("NUMPAD-2", KeyEvent.VK_NUMPAD2);
    NAMED_KEYS.put("NUMPAD-3", KeyEvent.VK_NUMPAD3);
    NAMED_KEYS.put("NUMPAD-4", KeyEvent.VK_NUMPAD4);
    NAMED_KEYS.put("NUMPAD-5", KeyEvent.VK_NUMPAD5);
    NAMED_KEYS.put("NUMPAD-6", KeyEvent.VK_NUMPAD6);
    NAMED_KEYS.put("NUMPAD-7", KeyEvent.VK_NUMPAD7);
    NAMED_KEYS.put("NUMPAD-8", KeyEvent.VK_NUMPAD8);
    NAMED_KEYS.put("NUMPAD-9", KeyEvent.VK_NUMPAD9);
    NAMED_KEYS.put("NUMPAD-ADD", KeyEvent.VK_ADD);
    NAMED_KEYS.put("NUMPAD-SUBTRACT", KeyEvent.VK_SUBTRACT);
    NAMED_KEYS.put("NUMPAD-MULTIPLY", KeyEvent.VK_MULTIPLY);
    NAMED_KEYS.put("NUMPAD-DIVIDE", KeyEvent.VK_DIVIDE);
    NAMED_KEYS.put("NUMPAD-DECIMAL", KeyEvent.VK_DECIMAL);
    NAMED_KEYS.put("NUMPAD-ENTER", KeyEvent.VK_ENTER);
  }

  private static final Set<Integer> MODIFIER_KEY_CODES = Set.of(
      KeyEvent.VK_SHIFT,
      KeyEvent.VK_CONTROL,
      KeyEvent.VK_ALT,
      KeyEvent.VK_ALT_GRAPH,
      KeyEvent.VK_META,
      KeyEvent.VK_WINDOWS,
      KeyEvent.VK_CONTEXT_MENU);

  public static boolean isModifierKey(int keyCode) {
    return MODIFIER_KEY_CODES.contains(keyCode);
  }

  public static Shortcut of(int keyCode, int modifiers) {
    return new Shortcut(keyCode, modifiers);
  }

  public static Shortcut of(String key, boolean ctrl, boolean shift, boolean alt) {
    int keyCode = resolveKeyCode(key);
    if (keyCode == KeyEvent.VK_UNDEFINED) {
      ConsoleLogger.error("Cannot create shortcut: unknown key '%s'".formatted(key));
      return null;
    }

    int modifiers = 0;
    if (ctrl)
      modifiers |= KeyEvent.CTRL_DOWN_MASK;
    if (shift)
      modifiers |= KeyEvent.SHIFT_DOWN_MASK;
    if (alt)
      modifiers |= KeyEvent.ALT_DOWN_MASK;

    return new Shortcut(keyCode, modifiers);
  }

  public static Shortcut of(JSONObject json) {
    String key = json.optString("key", "");
    boolean ctrl = json.optBoolean("ctrl", false);
    boolean shift = json.optBoolean("shift", false);
    boolean alt = json.optBoolean("alt", false);
    return of(key, ctrl, shift, alt);
  }

  private static int resolveKeyCode(String key) {
    String normalized = key.trim().toUpperCase();
    Integer mapped = NAMED_KEYS.get(normalized);
    if (mapped != null)
      return mapped;
    if (normalized.length() == 1)
      return KeyEvent.getExtendedKeyCodeForChar(normalized.charAt(0));
    return KeyEvent.VK_UNDEFINED;
  }

  KeyStroke getKeyStroke() {
    return KeyStroke.getKeyStroke(keyCode, modifiers);
  }

  boolean ctrl() {
    return (modifiers & KeyEvent.CTRL_DOWN_MASK) != 0;
  }

  boolean shift() {
    return (modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0;
  }

  boolean alt() {
    return (modifiers & KeyEvent.ALT_DOWN_MASK) != 0;
  }

  String keyText() {
    return KeyEvent.getKeyText(keyCode);
  }

  String toDisplayString() {
    var parts = new StringJoiner("+");
    if (ctrl())
      parts.add("Ctrl");
    if (shift())
      parts.add("Shift");
    if (alt())
      parts.add("Alt");
    parts.add(keyText());
    return parts.toString();
  }

  JSONObject toJSON() {
    JSONObject json = new JSONObject();
    // saving in a humane readable format
    json.put("key", keyText());
    json.put("ctrl", ctrl());
    json.put("shift", shift());
    json.put("alt", alt());
    return json;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Shortcut)) {
      return false;
    }
    Shortcut shortcut = (Shortcut) obj;
    return shortcut.keyCode == keyCode && shortcut.modifiers == modifiers;
  }
}

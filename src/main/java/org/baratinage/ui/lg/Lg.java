package org.baratinage.ui.lg;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JLabel;

public class Lg {

    static private Map<Object, LgTranslator> registered;
    static private LgResources resources;
    static private Locale currentLocale;

    static public void init() {
        resources = new LgResources();
        registered = new HashMap<>();
        setLocale(LgResources.DEFAULT_LOCAL_KEY);
    }

    static public void setLocale(String localeKey) {
        currentLocale = Locale.forLanguageTag(localeKey);
        updateRegisteredObjects();
    }

    static public Locale getLocale() {
        return currentLocale;
    }

    static public String getLocaleKey() {
        return currentLocale.getLanguage();
    }

    static public List<String> getAvailableLocales() {
        return resources.getAvailableLocales();
    }

    static public String text(String itemKey) {
        return resources.getTranslation(getLocaleKey(), itemKey);
    }

    static public String text(String itemKey, Object... args) {
        MessageFormat msgFormat = new MessageFormat(text(itemKey));
        msgFormat.setLocale(currentLocale);
        return msgFormat.format(args);
    }

    static public String html(String itemKey) {
        return "<html><nobr>" + text(itemKey) + "</nobr></html>";
    }

    static public String html(String itemKey, Object... args) {
        return "<html><nobr>" + text(itemKey, args) + "</nobr></html>";
    }

    static public void register(Object object, LgTranslator translator) {
        if (registered.containsKey(object)) {
            System.out.println("Overwritting translator.");
        }
        registered.put(object, translator);
        translator.setTranslatedText();
        System.out.println("There are " + registered.size() + " registered objects with translator.");
    }

    static public void register(JLabel label, String itemKey) {
        register(label, itemKey, false);
    }

    static public void register(JLabel label, String itemKey, boolean useHtml) {
        register(label, () -> {
            label.setText(useHtml ? html(itemKey) : text(itemKey));
        });
    }

    static public void register(AbstractButton button, String itemKey) {
        register(button, itemKey, false);
    }

    static public void register(AbstractButton button, String itemKey, boolean useHtml) {
        register(button, () -> {
            button.setText(useHtml ? html(itemKey) : text(itemKey));
        });
    }

    static public void updateRegisteredObjects() {
        for (LgTranslator translators : registered.values()) {
            translators.setTranslatedText();
        }
    }

    static public void updateRegisteredObject(Object obj) {
        LgTranslator translator = registered.get(obj);
        if (translator != null) {
            translator.setTranslatedText();
        }
    }
}

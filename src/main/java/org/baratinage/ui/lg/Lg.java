package org.baratinage.ui.lg;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;

@Deprecated
public class Lg {

    static private Map<String, Map<Object, LgTranslator>> registered;
    static private LgResources resources;
    static private Locale currentLocale;
    static private String defaultOwnerKey;

    static public void init() {
        resources = new LgResources();
        registered = new HashMap<>();
        setLocale(LgResources.DEFAULT_LOCAL_KEY);
        setDefaultOwnerKey("default_owner_key");
    }

    static public void reloadResources() {
        resources = new LgResources();
        updateRegisteredObjects();
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
        String rawText = text(itemKey);
        // escaping single quotes, see: https://stackoverflow.com/q/17544794
        rawText = rawText.replaceAll("(?<!')'(?!')", "''");
        MessageFormat msgFormat = new MessageFormat(rawText);
        msgFormat.setLocale(currentLocale);
        String formattedText = msgFormat.format(args);
        return formattedText;
    }

    static public String html(String itemKey) {
        return "<html><nobr>" + text(itemKey) + "</nobr></html>";
    }

    static public String html(String itemKey, Object... args) {
        return "<html><nobr>" + text(itemKey, args) + "</nobr></html>";
    }

    static public void setDefaultOwnerKey(String ownerKey) {
        defaultOwnerKey = ownerKey;
    }

    static public String getDefaultOwnerKey() {
        return defaultOwnerKey;
    }

    static public void register(Object object, LgTranslator translator) {
        register(defaultOwnerKey, object, translator);
    }

    static public void register(String ownerKey, Object object, LgTranslator translator) {
        if (registered.containsKey(object)) { // for debugging purposes
            String c = object.getClass().toString();
            System.out.println("Lg: Overwritting translator (" + c + ")");
        }
        if (!registered.containsKey(ownerKey)) {
            registered.put(ownerKey, new HashMap<>());
        }
        registered.get(ownerKey).put(object, translator);
        translator.setTranslatedText();
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
        for (Map<Object, LgTranslator> reg : registered.values()) {
            for (LgTranslator translators : reg.values()) {
                translators.setTranslatedText();
            }
        }

    }

    static public void updateRegisteredObject(Object obj) {
        for (Map<Object, LgTranslator> reg : registered.values()) {
            LgTranslator translator = reg.get(obj);
            if (translator != null) {
                translator.setTranslatedText();
            }
        }
    }

    static public void unregisterOwner(String ownerKey) {
        Map<Object, LgTranslator> reg = registered.get(ownerKey);
        if (reg != null) {
            reg.clear();
            registered.remove(ownerKey);
        }
    }

    static public void unregister(Object obj) {
        for (Map<Object, LgTranslator> reg : registered.values()) {
            reg.remove(obj);
        }
    }

    static public void printInfo() {
        System.out.println("=== Lg info ================================");
        int n = 0;
        int m = 0;
        for (Object owner : registered.keySet()) {
            m++;
            System.out.println("------------------------------------------");
            System.out.println("For owner '" + owner + "'");
            Map<Object, LgTranslator> reg = registered.get(owner);
            for (Object obj : reg.keySet()) {
                n++;
                if (obj.getClass().equals(JLabel.class)) {
                    String t = ((JLabel) obj).getText();
                    System.out.println("> JLabel with text '" + t + "'");
                } else if (obj.getClass().equals(JButton.class)) {
                    String t = ((JButton) obj).getText();
                    System.out.println("> JButton with text '" + t + "'");
                } else {
                    String c = obj.getClass().toString();
                    System.out.println("> Object (" + c + ")");
                }
            }
        }
        System.out.println("============================================");
        System.out.println("There are " + m + " owners and " + n + " registed objects.");
        System.out.println("============================================");
    }
}

package ui.lg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.swing.AbstractButton;
import javax.swing.JButton;

import java.util.PropertyResourceBundle;

public class Lg {

    static private final String I18N_RESSOURCE_DIR = "ressources/i18n";
    static private final String DEFAULT_RESSOURCE_KEY = "en";
    static private final String[] RESSOURCE_IDS = new String[] { "ui" };

    // public abstract class LgSetTranslatedText {
    // public Component component;

    // public LgSetTranslatedText(Component component) {
    // this.component = component;
    // }

    // public void setTranslatedText() {

    // }
    // }

    static private Lg instance;

    static private Lg getInstance() {
        if (instance == null) {
            instance = new Lg();
        }
        return instance;
    }

    static public void setLocale(String languageKey) {
        Lg instance = getInstance();
        instance.setLocaleFromKey(languageKey);
        for (String k : instance.elements.keySet()) {
            // instance.elements.get(k);
            updateText(k);
        }
    }

    static public String getLocaleKey() {
        Lg instance = getInstance();
        return instance.key;
    }

    static public Locale getLocale() {
        Lg instance = getInstance();
        return instance.locale;
    }

    public static String getText(String resourceKey, String textKey) {
        Lg instance = getInstance();
        ResourceBundle resource = instance.text.get(resourceKey);
        if (resource != null) {
            if (resource.containsKey(textKey)) {
                return resource.getString(textKey);
            } else {
                resource = instance.textFallback.get(resourceKey);
                if (resource != null) {
                    if (resource.containsKey(textKey)) {
                        return resource.getString(textKey);
                    }
                }
            }
        }
        return "<not-found>";
    }

    public static String register(LgElement<? extends Object> element) {
        String id = UUID.randomUUID().toString();
        Lg instance = getInstance();
        instance.elements.put(id, element);
        updateText(id);
        return id;
    }

    public static String registerButton(AbstractButton button, String ressourceKey, String textKey) {
        return register(new LgElement<AbstractButton>(button) {
            @Override
            public void setTranslatedText() {
                String text = Lg.getText(ressourceKey, textKey);
                component.setText(text);
            }
        });
    }

    public static void updateText(String id) {
        Lg instance = getInstance();
        for (LgElement<? extends Object> e : instance.elements.values()) {
            e.setTranslatedText();
        }
    }

    public static String format(String template, Object... args) {
        MessageFormat msgFormat = new MessageFormat(template);
        msgFormat.setLocale(getLocale());
        return msgFormat.format(args);
    }

    private HashMap<String, LgElement<? extends Object>> elements;
    private Map<String, ResourceBundle> textFallback;
    private Map<String, ResourceBundle> text;
    private String key;
    private Locale locale;

    private Lg() {

        elements = new HashMap<>();
        textFallback = new HashMap<>();
        text = new HashMap<>();
        try {
            for (String ressourceId : RESSOURCE_IDS) {
                textFallback.put(ressourceId, new PropertyResourceBundle(
                        Files.newInputStream(
                                Path.of(I18N_RESSOURCE_DIR,
                                        ressourceId + "_" + DEFAULT_RESSOURCE_KEY + ".properties"))));
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        setLocaleFromKey(Locale.getDefault().getLanguage());
    }

    private void setLocaleFromKey(String languageKey) {

        key = languageKey;
        locale = new Locale(languageKey);

        try {
            for (String ressourceId : RESSOURCE_IDS) {
                Path p = Path.of(I18N_RESSOURCE_DIR, ressourceId + "_" + key + ".properties");
                if (!Files.exists(p)) {
                    System.err.println("Ressource not found: " + p.toString() + "\nDefault used instead.");
                    p = Path.of(I18N_RESSOURCE_DIR, ressourceId + "_" + DEFAULT_RESSOURCE_KEY + ".properties");
                }
                text.put(ressourceId, new PropertyResourceBundle(
                        Files.newInputStream(p)));
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        System.out.println("There are " + elements.size() + " element(s)...");

    }

}

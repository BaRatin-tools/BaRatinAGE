package org.baratinage.ui.lg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.JLabel;

import java.util.PropertyResourceBundle;

public class Lg {

    static private final String I18N_RESSOURCE_DIR = "resources/i18n";
    static private final String DEFAULT_RESSOURCE_KEY = "en";
    static private final String[] RESSOURCE_IDS = new String[] { "ui" };

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
        for (LgElement<?> lge : instance.elements) {
            lge.setTranslatedText();
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
        return getText(resourceKey, textKey, false);
    }

    public static String getText(String resourceKey, String textKey, boolean html) {
        Lg instance = getInstance();
        ResourceBundle resource = instance.text.get(resourceKey);
        String rawText = "<not-found>";
        if (resource != null) {
            if (resource.containsKey(textKey)) {
                rawText = resource.getString(textKey);
            } else {
                resource = instance.textFallback.get(resourceKey);
                if (resource != null) {
                    if (resource.containsKey(textKey)) {
                        rawText = resource.getString(textKey);
                    }
                }
            }
        }
        return html ? "<html>" + rawText + "</html>" : rawText;
    }

    public static void register(LgElement<?> element) {
        ;
        Lg instance = getInstance();
        LgElement<?> elementToDelete = null;
        for (LgElement<?> lge : instance.elements) {
            if (lge.object == element.object) {
                System.out.println("Lg // Duplicated registered element overwritten!");
                elementToDelete = lge;
            }
        }
        if (elementToDelete != null) {
            instance.elements.remove(elementToDelete);
        }
        instance.elements.add(element);
        System.out.println("Lg // Registered elements: " + instance.elements.size());
        element.setTranslatedText();
    }

    public static void registerButton(AbstractButton button, String resourceKey, String textKey) {
        register(new LgElement<AbstractButton>(button) {

            @Override
            public void setTranslatedText() {
                String text = Lg.getText(resourceKey, textKey);
                object.setText(text);
            }
        });
    }

    public static void registerLabel(JLabel label, String resourceKey, String textKey) {
        register(new LgElement<JLabel>(label) {
            @Override
            public void setTranslatedText() {
                String text = Lg.getText(resourceKey, textKey);
                object.setText(text);
            }
        });
    }

    public static String format(String template, Object... args) {
        MessageFormat msgFormat = new MessageFormat(template);
        msgFormat.setLocale(getLocale());
        return msgFormat.format(args);
    }

    private List<LgElement<?>> elements;

    private Map<String, ResourceBundle> textFallback;
    private Map<String, ResourceBundle> text;
    private String key;
    private Locale locale;

    private Lg() {
        // elements = new HashMap<>();
        elements = new ArrayList<>();

        textFallback = new HashMap<>();
        text = new HashMap<>();
        try {
            for (String resourceId : RESSOURCE_IDS) {
                textFallback.put(resourceId, new PropertyResourceBundle(
                        Files.newInputStream(
                                Path.of(I18N_RESSOURCE_DIR,
                                        resourceId + "_" + DEFAULT_RESSOURCE_KEY + ".properties"))));
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        setLocaleFromKey(Locale.getDefault().getLanguage());
    }

    private void setLocaleFromKey(String languageKey) {
        key = languageKey;
        locale = Locale.forLanguageTag(languageKey);
        try {
            for (String resourceId : RESSOURCE_IDS) {
                Path p = Path.of(I18N_RESSOURCE_DIR, resourceId + "_" + key + ".properties");
                if (!Files.exists(p)) {
                    System.err.println("Ressource not found: " + p.toString() + "\nDefault used instead.");
                    p = Path.of(I18N_RESSOURCE_DIR, resourceId + "_" + DEFAULT_RESSOURCE_KEY + ".properties");
                }
                text.put(resourceId, new PropertyResourceBundle(
                        Files.newInputStream(p)));
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

}

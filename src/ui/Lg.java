package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JTabbedPane;

public class Lg {

    private ResourceBundle uiTextBackup;
    private ResourceBundle uiText;

    private String lgKey;

    static Lg instance;

    public static synchronized Lg getInstance() {
        if (instance == null) {
            instance = new Lg();
        }
        return instance;
    }

    private Lg() {

        Locale currentLocale = Locale.getDefault();

        System.out.println(currentLocale.getDisplayCountry());
        System.out.println(currentLocale.getDisplayLanguage());
        System.out.println(currentLocale.getISO3Country());
        System.out.println(currentLocale.getISO3Language());
        System.out.println(currentLocale.getCountry());
        System.out.println(currentLocale.getLanguage());

        lgKey = currentLocale.getLanguage();

        Path uiPath = Path.of("ressources/i18n/ui_" + lgKey + ".properties");
        if (!Files.exists(uiPath)) {
            lgKey = "en";
            uiPath = Path.of("ressources/i18n/ui_en.properties");
        }
        try {
            uiText = new PropertyResourceBundle(Files.newInputStream(uiPath));
            uiTextBackup = new PropertyResourceBundle(
                    Files.newInputStream(
                            Path.of("ressources/i18n/ui_en.properties")));
        } catch (IOException e) {
            System.err.println(e);
        }

    }

    static public String getLanguageKey() {
        Lg instance = Lg.getInstance();
        return instance.lgKey;
    }

    public static String getText(String key) {
        Lg instance = Lg.getInstance();
        return instance.getTextFromKey(key);
    }

    private String getTextFromKey(String key) {
        if (uiText.containsKey(key)) {
            return uiText.getString(key);
        }
        if (uiTextBackup.containsKey(key)) {
            return uiTextBackup.getString(key);
        }
        return "<nokey>";
    }

    static public void setLanguage(String languageKey) {
        Path uiPath = Path.of("ressources/i18n/ui_" +
                languageKey + ".properties");
        if (!Files.exists(uiPath)) {
            return;
        }

        Lg instance = Lg.getInstance();
        try {
            instance.uiText = new PropertyResourceBundle(Files.newInputStream(uiPath));
            instance.lgKey = languageKey;
        } catch (IOException e) {
            System.err.println(e);
        }

        instance.updateButtons();
        instance.updateTabbedPane();

    }

    record CompSetText<T>(T component, String key) {

    }

    record CompSetTextAt<T>(T component, String key, int index) {

    }

    private Set<CompSetText<AbstractButton>> buttons = new HashSet<>();

    private void updateButtons() {
        for (CompSetText<AbstractButton> cst : buttons) {
            System.out.println(cst.component.getText());
            // FIXME: do not do this, instead, add a method to explicitly remove a
            // componenent when needed
            // if (!cst.component().isDisplayable()) {
            // buttons.remove(cst);
            // }
            cst.component().setText(getTextFromKey(cst.key()));
            cst.component().repaint();
        }
    }

    static public void setText(AbstractButton button, String key) {
        Lg instance = Lg.getInstance();
        instance.buttons.add(new CompSetText<AbstractButton>(button, key));
        // button.setText(instance.getTextFromKey(key));
        instance.updateButtons();
    }

    private Set<CompSetTextAt<JTabbedPane>> tabbedPanel = new HashSet<>();

    private void updateTabbedPane() {
        for (CompSetTextAt<JTabbedPane> csta : tabbedPanel) {
            System.out.println(csta.component.getTitleAt(csta.index()));
            csta.component().setTitleAt(csta.index(), getTextFromKey(csta.key()));
            csta.component().repaint();
        }
    }

    // FIXME: should be rename setTitleAt to be more explicit about what it does...
    static public void setText(JTabbedPane tabbedPanel, String key, int index) {
        Lg instance = Lg.getInstance();
        instance.tabbedPanel.add(new CompSetTextAt<JTabbedPane>(tabbedPanel, key, index));
        instance.updateTabbedPane();
        // tabbedPanel.
        // button.setText(instance.getTextFromKey(key));
    }

}

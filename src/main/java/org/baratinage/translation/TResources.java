package org.baratinage.translation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.baratinage.ui.AppConfig;
import org.baratinage.utils.ConsoleLogger;

public class TResources {

    // Data structure is:
    // - RessourceBundle: contains key, value pairs to retrieve a text from a key
    // - Map<String, ResourceBundle>: One RessourceBundle per source ressource file
    // - Map<String, Map<String, ResourceBundle>>: One entry per Locale
    private Map<String, Map<String, ResourceBundle>> translations;

    private Set<String> localKeys;
    private Set<String> filekeys;

    public TResources() {

        File resourceDir = new File(AppConfig.AC.I18N_RESOURCES_DIR);
        File[] files = resourceDir.listFiles();
        filekeys = new HashSet<>();
        localKeys = new HashSet<>();
        for (File f : files) {
            // Ressources are loaded from files named as follows: {1}_{2}.properties
            // where {1} is the locale String (e.g. fr, en) and {2} any key
            Matcher m = Pattern.compile("(\\w*)_(\\w*).properties").matcher(f.getName());
            while (m.find()) {
                if (m.groupCount() == 2) {
                    filekeys.add(m.group(1));
                    localKeys.add(m.group(2));
                }
            }
        }
        translations = new HashMap<>();
        for (String localeKey : localKeys) {
            Map<String, ResourceBundle> fileTranslations = new HashMap<>();
            for (String fileKey : filekeys) {
                String resourceName = String.format("%s_%s.properties", fileKey, localeKey);
                Path resourcePath = Path.of(AppConfig.AC.I18N_RESOURCES_DIR, resourceName);
                try {
                    ResourceBundle resourceBundle = new PropertyResourceBundle(
                            Files.newInputStream(
                                    resourcePath));
                    fileTranslations.put(fileKey, resourceBundle);
                } catch (IOException e) {
                    ConsoleLogger.error(
                            "Failed to read expected resource bundle '" + resourceName + "'!");
                }
            }
            translations.put(localeKey, fileTranslations);
        }
    }

    public String getTranslation(String localeKey, String itemKey) {
        // get all translations files in given locale for
        Map<String, ResourceBundle> fileTranslations = translations.get(localeKey);
        if (fileTranslations == null) {
            if (localeKey.equals(AppConfig.AC.DEFAULT_LOCALE_KEY)) {
                ConsoleLogger.error(
                        String.format(
                                "SHOULD NOT HAPPEN - no translation bundle file found! itemKey='%s', localeKey='%s'",
                                itemKey, localeKey));
                return "<no-translation-found>";
            } else {
                ConsoleLogger.log(
                        String.format(
                                "No resource translation bundle file found for locale '%s' found. Looking with default locale '%s' instead.",
                                localeKey, AppConfig.AC.DEFAULT_LOCALE_KEY));
                return getTranslation(AppConfig.AC.DEFAULT_LOCALE_KEY, itemKey);
            }
        }

        ResourceBundle resourceBundle = fileTranslations.get(AppConfig.AC.DEFAULT_RESSOURCE_FILE_KEY);
        String translation = getTranslation(resourceBundle, itemKey);
        if (translation == null) {
            for (String fileTranslationKeys : fileTranslations.keySet()) {
                if (!fileTranslationKeys.equals(AppConfig.AC.DEFAULT_RESSOURCE_FILE_KEY)) {
                    translation = getTranslation(fileTranslations.get(fileTranslationKeys), itemKey);
                    if (translation != null) {
                        return translation;
                    }
                }
            }
        } else {
            return translation;
        }
        if (translation == null && localeKey.equals(AppConfig.AC.DEFAULT_LOCALE_KEY)) {
            ConsoleLogger.error(
                    String.format(
                            "No item with key '%s' found in all resource translation bundle files  for (default) locale '%s'!",
                            itemKey, localeKey));
            return "<no-translation-found>";
        } else {
            ConsoleLogger.log(
                    String.format(
                            "No item with key '%s' found in resource translation bundle files for locale '%s'. Looking with default locale '%s' instead.",
                            itemKey, localeKey, AppConfig.AC.DEFAULT_LOCALE_KEY));
            return getTranslation(AppConfig.AC.DEFAULT_LOCALE_KEY, itemKey);
        }
    }

    private String getTranslation(ResourceBundle resourceBundle, String itemKey) {
        if (!resourceBundle.containsKey(itemKey)) {
            return null;
        }
        String text = resourceBundle.getString(itemKey);
        return text.equals("") ? null : text;
    }

    public List<String> getAvailableLocales() {
        return new ArrayList<>(localKeys);
    }

}

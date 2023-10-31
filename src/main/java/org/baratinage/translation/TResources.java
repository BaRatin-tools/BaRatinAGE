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
        for (String localKey : localKeys) {
            Map<String, ResourceBundle> fileTranslations = new HashMap<>();
            for (String fileKey : filekeys) {
                String resourceName = String.format("%s_%s.properties", fileKey, localKey);
                Path resourcePath = Path.of(AppConfig.AC.I18N_RESOURCES_DIR, resourceName);
                try {
                    ResourceBundle resourceBundle = new PropertyResourceBundle(
                            Files.newInputStream(
                                    resourcePath));
                    fileTranslations.put(fileKey, resourceBundle);
                } catch (IOException e) {
                    ConsoleLogger.error(
                            "TResources Error: Failed to read expected resource bundle '" + resourceName + "'!");
                }
            }
            translations.put(localKey, fileTranslations);
        }
    }

    public String getTranslation(String localKey, String fileKey, String itemKey) {
        Map<String, ResourceBundle> fileTranslations = translations.get(localKey);
        if (fileTranslations == null) {
            if (localKey.equals(AppConfig.AC.DEFAULT_RESSOURCE_FILE_LOCALE_KEY)) {
                ConsoleLogger.error(
                        String.format(
                                "TResources Error: no translation found for key '%s' and default locale '%s' in bundle '%s'!",
                                itemKey, localKey, fileKey));
                return "<no-translation-found>";
            } else {
                ConsoleLogger.log(
                        String.format("TResources: No locale '%s' found! Looking in default locale '%s' instead.",
                                localKey, AppConfig.AC.DEFAULT_RESSOURCE_FILE_LOCALE_KEY));
                return getTranslation(AppConfig.AC.DEFAULT_RESSOURCE_FILE_LOCALE_KEY, fileKey, itemKey);
            }
        }
        ResourceBundle resourceBundle = fileTranslations.get(fileKey);
        if (resourceBundle == null) {
            ConsoleLogger.error(
                    String.format("TResources Error: No bundle for locale '%s' named '%s' found!",
                            localKey, fileKey));
            return "<no-translation-found>";
        }
        if (!resourceBundle.containsKey(itemKey)) {
            ConsoleLogger.error(
                    String.format(
                            "TResources Error: no item with key '%s' found for locale '%s' and bundle '%s'!",
                            itemKey, localKey, fileKey));
            return "<no-translation-found>";
        }
        return resourceBundle.getString(itemKey);
    }

    public String getTranslation(String localKey, String itemKey) {
        return getTranslation(localKey, AppConfig.AC.DEFAULT_RESSOURCE_FILE_KEY, itemKey);
    }

    public List<String> getAvailableLocales() {
        return new ArrayList<>(localKeys);
    }

}

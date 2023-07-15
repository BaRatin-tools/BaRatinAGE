package org.baratinage.ui.lg;

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
import org.baratinage.ui.MainFrame;

public class LgResources {

    public static final String DEFAULT_LOCAL_KEY = "en";
    public static final String DEFAULT_FILE_KEY = "ui";

    private Map<String, Map<String, ResourceBundle>> translations;

    private Set<String> localKeys;
    private Set<String> filekeys;

    public LgResources() {
        File resourceDir = new File(AppConfig.AC.I18N_RESOURCES_DIR);
        File[] files = resourceDir.listFiles();
        filekeys = new HashSet<>();
        localKeys = new HashSet<>();
        for (File f : files) {
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
                    System.err.println("Failed to read expected resource bundle '" + resourceName + "'!");
                }
            }
            translations.put(localKey, fileTranslations);
        }
    }

    public String getTranslation(String localKey, String fileKey, String itemKey) {
        Map<String, ResourceBundle> fileTranslations = translations.get(localKey);
        if (fileTranslations == null) {
            if (localKey.equals(DEFAULT_LOCAL_KEY)) {
                System.err.println(
                        String.format(
                                "Error: no translation found for key '%s' and default locale '%s' in bundle '%s'!",
                                itemKey, localKey, fileKey));
                return "<no-translation-found>";
            } else {
                System.out.println(
                        String.format("No locale '%s' found! Looking in default locale '%s' instead.",
                                localKey, DEFAULT_LOCAL_KEY));
                return getTranslation(DEFAULT_LOCAL_KEY, fileKey, itemKey);
            }
        }
        ResourceBundle resourceBundle = fileTranslations.get(fileKey);
        if (resourceBundle == null) {
            System.err.println(
                    String.format("Error: No bundle for locale '%s' named '%s' found!",
                            localKey, fileKey));
            return "<no-translation-found>";
        }
        if (!resourceBundle.containsKey(itemKey)) {
            System.err.println(
                    String.format(
                            "Error: no item with key '%s' found for locale '%s' and bundle '%s'!",
                            itemKey, localKey, fileKey));
            return "<no-translation-found>";
        }
        return resourceBundle.getString(itemKey);
    }

    // public String getTranslationOld(String localKey, String fileKey, String
    // itemKey) {
    // Map<String, ResourceBundle> fileTranslations = translations.get(localKey);
    // if (fileTranslations == null) {
    // if (localKey.equals(DEFAULT_LOCAL_KEY)) {
    // System.err.println(
    // String.format("Error: no translation found for key '%s' and locale '%s' in
    // bundle '%s' found!",
    // itemKey, localKey, fileKey));
    // return "<no-translation-found>";
    // } else {
    // System.out.println(
    // String.format("No local '%s' found! Looking in default local '%s' instead.",
    // localKey, DEFAULT_LOCAL_KEY));
    // return getTranslation(DEFAULT_LOCAL_KEY, fileKey, itemKey);
    // }
    // }
    // ResourceBundle resourceBundle = fileTranslations.get(fileKey);
    // if (resourceBundle == null) {
    // System.out.println(
    // String.format("No bundle for local '%s' named '%s' found! Looking in default
    // bundle '%s' instead.",
    // localKey, fileKey, DEFAULT_FILE_KEY));
    // return getTranslation(localKey, DEFAULT_FILE_KEY, itemKey);
    // }
    // if (!resourceBundle.containsKey(itemKey)) {
    // System.out.println(
    // String.format(
    // "No item with key '%s' found for local '%s' and bundle '%s'! Using default
    // bundle '%s' instead.",
    // itemKey, localKey, fileKey, DEFAULT_FILE_KEY));
    // return getTranslation(localKey, DEFAULT_FILE_KEY, itemKey);
    // }
    // return resourceBundle.getString(itemKey);
    // }

    public String getTranslation(String localKey, String itemKey) {
        return getTranslation(localKey, DEFAULT_FILE_KEY, itemKey);
    }

    public List<String> getAvailableLocales() {
        return new ArrayList<>(localKeys);
    }

}

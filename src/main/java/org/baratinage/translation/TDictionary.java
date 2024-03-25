package org.baratinage.translation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.baratinage.utils.ConsoleLogger;

public class TDictionary {
    public final Locale locale;
    public final String lgKey;
    private final HashMap<String, String> translations;
    private final List<String> keys;

    public TDictionary(String lgKey) {
        this.locale = Locale.forLanguageTag(lgKey);
        this.lgKey = lgKey;
        this.translations = new HashMap<>();
        this.keys = new ArrayList<>();
    }

    public TDictionary(String lgKey, String[] keys, String[] translations) {
        this(lgKey);
        if (keys.length != translations.length) {
            throw new IllegalArgumentException("the length of 'keys' and 'translations' must match");
        }
        for (int k = 0; k < keys.length; k++) {
            this.keys.add(keys[k]);
            this.translations.put(keys[k], translations[k]);
        }
    }

    public String getTranslation(String key) {
        return translations.containsKey(key) ? translations.get(key) : null;
    }

    public String[] getKeys() {
        return keys.toArray(new String[keys.size()]);
    }

    static public List<String[]> rawDictionnaries;

    public static List<TDictionary> loadResourceBundlesFromDir(String directoryPath) {
        List<TDictionary> translations = new ArrayList<>();

        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            ConsoleLogger.error("Directory does not exist or is not a directory: " + directoryPath);
            return translations;
        }

        File[] files = directory.listFiles((dir, name) -> name.matches(".+_\\w{2,3}\\.properties"));
        if (files == null || files.length == 0) {
            ConsoleLogger.error("No matching files found in directory: " + directoryPath);
            return translations;
        }

        for (File file : files) {
            try {
                String localeKey = file.getName().split("_")[1].split("\\.")[0];
                Locale locale = Locale.forLanguageTag(localeKey);
                try (FileInputStream fis = new FileInputStream(file)) {
                    ResourceBundle bundle = new PropertyResourceBundle(fis);
                    List<String> keys = new ArrayList<>();
                    List<String> translationsList = new ArrayList<>();
                    bundle.keySet().forEach(key -> {
                        keys.add(key);
                        translationsList.add(bundle.getString(key));
                    });
                    TDictionary tDict = new TDictionary(
                            locale.toLanguageTag(),
                            keys.toArray(new String[0]),
                            translationsList.toArray(new String[0]));
                    translations.add(tDict);
                }
            } catch (IOException e) {
                ConsoleLogger.error("Error reading properties file: " + file.getAbsolutePath());
            }
        }

        return translations;
    }

}

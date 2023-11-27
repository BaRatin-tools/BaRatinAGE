package org.baratinage.translation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;

public class Dictionnary {
    public final Locale locale;
    public final String lgKey;
    private final HashMap<String, String> translations;
    private final List<String> keys;

    public Dictionnary(String lgKey) {
        this.locale = Locale.forLanguageTag(lgKey);
        this.lgKey = lgKey;
        this.translations = new HashMap<>();
        this.keys = new ArrayList<>();
    }

    public Dictionnary(String lgKey, String[] keys, String[] translations) {
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

    public static List<Dictionnary> readDictionnariesFromCSV(String csvFilePath) {
        try {
            List<String[]> rawDictionnaries = ReadFile.readStringMatrix(
                    csvFilePath,
                    "\\t",
                    0,
                    true,
                    false);
            List<Dictionnary> dictionnaries = new ArrayList<>();
            String[] keys = rawDictionnaries.get(0);
            // skipping first two columns (key, comment)
            for (int k = 2; k < rawDictionnaries.size(); k++) {
                String[] translations = rawDictionnaries.get(k);
                dictionnaries.add(new Dictionnary(translations[0], keys, translations));
            }
            return dictionnaries;
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
        return null;
    }
}

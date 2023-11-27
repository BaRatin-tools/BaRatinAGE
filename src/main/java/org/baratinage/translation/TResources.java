package org.baratinage.translation;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.baratinage.AppSetup;
import org.baratinage.utils.ConsoleLogger;

public class TResources {

    private final List<TDictionary> dictionnariesList;
    private final HashMap<String, TDictionary> dictionnariesMap;
    private final TDictionary defaultDictionnary;

    public TResources() {
        dictionnariesList = TDictionary.readDictionnariesFromCSV(AppSetup.PATH_TRANSLATIONS_FILE);
        dictionnariesMap = new HashMap<>();
        for (TDictionary d : dictionnariesList) {
            dictionnariesMap.put(d.lgKey, d);
        }
        defaultDictionnary = dictionnariesMap.get(AppSetup.TRANSLATIONS_DEFAULT_KEY);
    }

    public Locale[] getAllLocales() {
        int n = dictionnariesList.size();
        Locale[] locales = new Locale[n];
        for (int k = 0; k < n; k++) {
            locales[k] = dictionnariesList.get(k).locale;
        }
        return locales;
    }

    public String getTranslation(String lgKey, String key) {
        if (!dictionnariesMap.containsKey(lgKey)) {
            ConsoleLogger.error("No dictionnary found for language tag '" + lgKey + "'");
            return getDefaultTranslation(key);
        }
        TDictionary dictionnary = dictionnariesMap.get(lgKey);
        String translation = dictionnary.getTranslation(key);
        if (translation == null) {
            ConsoleLogger.error("No translation found for key '" + key + "' and language tag '" + lgKey + "'.");
            return getDefaultTranslation(key);
        }
        return translation;
    }

    private String getDefaultTranslation(String key) {
        String translation = defaultDictionnary.getTranslation(key);
        if (translation == null) {
            ConsoleLogger.error("No default translation found for key '" + key + "'");
        }
        return translation;
    }
}

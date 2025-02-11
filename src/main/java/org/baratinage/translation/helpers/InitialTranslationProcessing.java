package org.baratinage.translation.helpers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.baratinage.translation.TDictionary;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.WriteFile;

/**
 * This class is a stand alone program used to create the first version of the
 * global dictionnary.csv file using french/english translations of ui_fr, ui_en
 * resource bundle files, hand made ui_* resource bundle file containing google
 * translate translation, v2 translations (dico.txt) and a hand made key mapping
 * csv file.
 */
public class InitialTranslationProcessing {
    public static void main(String[] args) {

        ConsoleLogger.init();

        /**
         * note that the following keys where modified in the original v2 dico.txt:
         * br => pt
         * cn => zh
         * cz => cs
         * hb => he
         * jp => ja
         * mg => hu
         * sb => sr
         * sw => sv
         * am => hy
         */

        // main paths
        Path dicoPath = Path.of("./resources/i18n/raw/dico.csv");
        Path englishTranslationPath = Path.of("./resources/i18n/ui_en.properties");
        Path frenchTranslationPath = Path.of("./resources/i18n/ui_fr.properties");
        Path allTranslationFolderPath = Path.of("./resources/i18n/raw/");
        Path mappingPath = Path.of("./resources/i18n/raw/mapping.csv");
        Path finalDictionnaryPath = Path.of("./resources/i18n/translations.csv");

        // read dico.txt
        List<TDictionary> v2Dictionnaries = readDictionnaries(dicoPath);

        // read english translations
        TDictionary enTranslations = readResourceBundleDictionnary("en", englishTranslationPath);

        // read french translations
        TDictionary frTranslations = readResourceBundleDictionnary("fr", frenchTranslationPath);

        // read mapping file
        HashMap<String, KeyMap> mapping = readKeyMapping(mappingPath);

        // read all google translated files
        HashMap<String, TDictionary> googleTranslateTranslations = new HashMap<>();
        for (int k = 0; k < v2Dictionnaries.size(); k++) {
            String lgKey = v2Dictionnaries.get(k).lgKey;
            Path path = Path.of(allTranslationFolderPath.toString(), String.format("ui_%s.properties", lgKey));
            if (path.toFile().exists()) {
                googleTranslateTranslations.put(lgKey, readResourceBundleDictionnary(lgKey, path));
            } else {
                ConsoleLogger.log("File " + path.toString() + " doesn't exist. Skipping");
            }
        }

        // build final translations csv file
        // - KEY COLUMN, -HTML SUPPORT - COMMENT COLUMN, - LANGUAGE COLUMNS
        // - for fr and and en, use existing translations
        // - use v2 when mapping existing (with comment)
        // - use google translation when there is no mapping (with comment)
        List<String[]> finalDico = new ArrayList<>();
        List<String> finalDicoHeaders = new ArrayList<>();

        String[] translationKeys = enTranslations.getKeys();
        int n = translationKeys.length;
        String[] comments = new String[n];
        for (int i = 0; i < n; i++) {
            String key = translationKeys[i];
            KeyMap map = mapping.get(key);
            if (map != null) {
                if (map.comment != null) {
                    comments[i] = map.comment;
                } else {
                    if (map.keyV2 == null) {
                        comments[i] = "Google translate used except for 'fr' and 'en'";
                    } else {
                        comments[i] = "";
                    }
                }
            } else {
                ConsoleLogger.log("No mapping found for key '" + key + "'");
                comments[i] = "to be translated except for 'fr' and 'en'";
            }
        }
        finalDico.add(translationKeys);
        finalDicoHeaders.add("key");
        finalDico.add(comments);
        finalDicoHeaders.add("comment");
        for (int k = 0; k < v2Dictionnaries.size(); k++) {
            TDictionary v2Dictionnary = v2Dictionnaries.get(k);
            String lgKey = v2Dictionnary.lgKey;
            TDictionary gtDictionnary = googleTranslateTranslations.get(lgKey);
            if (lgKey.equals("en")) {
                finalDico.add(buildTranslationArray(translationKeys, enTranslations));
            } else if (lgKey.equals("fr")) {
                finalDico.add(buildTranslationArray(translationKeys, frTranslations));
            } else {
                String[] translations = new String[n];
                for (int i = 0; i < n; i++) {
                    String key = translationKeys[i];
                    KeyMap map = mapping.get(key);
                    if (map != null) {
                        String tr = map.keyV2 != null ? v2Dictionnary.getTranslation(map.keyV2)
                                : gtDictionnary.getTranslation(map.keyV3);
                        if (tr != null) {
                            translations[i] = tr;
                        } else {
                            ConsoleLogger.error("No translation found for key '" + key + "'");
                            translations[i] = "";
                        }
                    } else {
                        translations[i] = "";
                    }
                }
                finalDico.add(translations);
            }
            finalDicoHeaders.add(lgKey);
        }
        try {
            WriteFile.writeMatrix(finalDictionnaryPath.toString(),
                    finalDico,
                    "\t",
                    finalDicoHeaders.toArray(new String[finalDicoHeaders.size()]));
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
    }

    private static record KeyMap(String keyV3, String keyV2, String comment) {
    };

    private static List<TDictionary> readDictionnaries(Path path) {
        try {
            List<String[]> rawDico = ReadFile.readStringMatrix(
                    path.toString(),
                    "\\t",
                    0,
                    true,
                    true);
            List<TDictionary> dictionnaries = new ArrayList<>();
            String[] keys = rawDico.get(0);
            for (int k = 1; k < rawDico.size(); k++) {
                String[] translations = rawDico.get(k);
                dictionnaries.add(new TDictionary(translations[0], keys, translations));
            }
            return dictionnaries;
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
        return null;
    }

    private static TDictionary readResourceBundleDictionnary(String lgKey, Path path) {
        try {
            String[] lines = ReadFile.getLines(path.toString(), Integer.MAX_VALUE, true);
            List<String> keys = new ArrayList<>();
            List<String> translations = new ArrayList<>();
            for (String line : lines) {
                if (!line.startsWith("#")) {
                    String[] parsedLine = ReadFile.parseString(line, "=", true);
                    if (parsedLine.length == 2) {
                        keys.add(parsedLine[0].trim());
                        translations.add(parsedLine[1].trim());
                    } else {
                        ConsoleLogger.error("Error parsing row ' " + line + "'");
                    }
                }
            }
            int n = keys.size();
            return new TDictionary(lgKey, keys.toArray(new String[n]), translations.toArray(new String[n]));
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
        return null;
    }

    private static HashMap<String, KeyMap> readKeyMapping(Path path) {
        try {
            HashMap<String, KeyMap> keyMapping = new HashMap<>();
            String[] keyMappingLines = ReadFile.getLines(path.toString(), Integer.MAX_VALUE, true);
            for (int k = 1; k < keyMappingLines.length; k++) {
                String[] parsedLine = ReadFile.parseString(keyMappingLines[k], ";", true);
                String v3key = parsedLine[0].trim();
                String v2Key = parsedLine[1].trim();
                String comment = null;
                if (v2Key.equals("null")) {
                    v2Key = null;
                } else {
                    String[] s = v2Key.split("#");
                    if (s.length > 1) {
                        v2Key = s[0].trim();
                        comment = s[1].trim();
                    }
                }
                keyMapping.put(v3key, new KeyMap(v3key, v2Key, comment));
            }
            return keyMapping;
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
        return null;
    }

    private static String[] buildTranslationArray(String[] translationKeys, TDictionary dictionnary) {
        int n = translationKeys.length;
        String[] translations = new String[n];
        for (int k = 0; k < n; k++) {
            translations[k] = dictionnary.getTranslation(translationKeys[k]);
        }
        return translations;
    }
}

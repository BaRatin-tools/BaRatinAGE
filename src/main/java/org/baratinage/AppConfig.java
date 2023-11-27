package org.baratinage;

import java.io.IOException;
import java.util.Locale;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.json.JSONObject;

public class AppConfig {

    public final int THROTTLED_DELAY_MS = 250;
    public final String FALLBACK_LANGUAGE_KEY;
    public String LANGUAGE_KEY;
    public String THEME_KEY;
    public int FONT_SIZE;

    public AppConfig() {
        JSONObject configuration = new JSONObject();
        try {
            String jsonString = ReadFile.getStringContent(AppSetup.PATH_CONFIGURATION_FILE, true);
            configuration = new JSONObject(jsonString);
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }

        FALLBACK_LANGUAGE_KEY = "en";
        THEME_KEY = configuration.has("theme_key") ? configuration.getString("theme_key") : "FlatLightLaf";
        FONT_SIZE = configuration.has("font_size") ? configuration.getInt("font_size") : 14;
        LANGUAGE_KEY = configuration.has("language_key") ? configuration.getString("language_key")
                : Locale.getDefault().getLanguage();

        Locale.setDefault(Locale.forLanguageTag(LANGUAGE_KEY));
    }
}

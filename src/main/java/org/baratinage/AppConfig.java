package org.baratinage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.WriteFile;
import org.json.JSONObject;

public class AppConfig {

    public static class ConfigItem<T> {
        private final String key;
        private final Class<T> c;
        private final T defaultValue;
        private T customValue;

        private ConfigItem(String key, Class<T> c, T defaultValue) {
            this.key = key;
            this.c = c;
            this.defaultValue = defaultValue;
        }

        public T get() {
            return customValue == null ? defaultValue : customValue;
        }

        public void set(T value) {
            customValue = value;
        }

        public void reset() {
            customValue = null;
        }

        private String getKey() {
            return key;
        }

        private boolean isSet() {
            return customValue != null;
        }

        public boolean is(T value) {
            if (isSet()) {
                return customValue.equals(value);
            } else {
                return defaultValue.equals(value);
            }
        }

        private void castAndSet(Object o) {
            try {
                customValue = c.cast(o);
            } catch (ClassCastException e) {
                ConsoleLogger.error(e);
            }
        }
    }

    public final int INT_MISSING_VALUE = -999999999;
    public final int N_REPLICATES = 200;
    public final int THROTTLED_DELAY_MS = 250;
    public final int DEBOUNCED_DELAY_MS = 250;
    public final String FALLBACK_LANGUAGE_KEY;

    private JSONObject configuration;

    private final List<ConfigItem<?>> allConfigItems;

    public final ConfigItem<String> THEME_KEY;
    public final ConfigItem<Integer> FONT_SIZE;
    public final ConfigItem<String> LANGUAGE_KEY;
    public final ConfigItem<Integer> N_SAMPLES;

    public AppConfig() {

        FALLBACK_LANGUAGE_KEY = "en";

        allConfigItems = new ArrayList<>();

        THEME_KEY = buildAndRegisterConfigItem("theme_key", String.class, "FlatLightLaf");
        // THEME_KEY = buildAndRegisterConfigItem("theme_key", String.class,
        // "FlatDarkLaf");
        FONT_SIZE = buildAndRegisterConfigItem("font_size", Integer.class, 14);
        LANGUAGE_KEY = buildAndRegisterConfigItem("language_key", String.class, Locale.getDefault().getLanguage());
        N_SAMPLES = buildAndRegisterConfigItem("n_samples", Integer.class, 200);

        loadConfiguration();

    }

    private <T> ConfigItem<T> buildAndRegisterConfigItem(String key, Class<T> c, T defaultValue) {
        ConfigItem<T> item = new ConfigItem<T>(key, c, defaultValue);
        allConfigItems.add(item);
        return item;
    }

    public void loadConfiguration() {
        configuration = new JSONObject();
        try {
            String jsonString = ReadFile.getStringContent(AppSetup.PATH_CONFIGURATION_FILE, true);
            configuration = new JSONObject(jsonString);
            for (ConfigItem<?> item : allConfigItems) {
                if (configuration.has(item.getKey())) {
                    item.castAndSet(configuration.get(item.getKey()));
                }
            }
        } catch (IOException e) {
            ConsoleLogger.warn(e);
        }
    }

    public void saveConfiguration() {
        for (ConfigItem<?> item : allConfigItems) {
            if (item.isSet()) {
                configuration.put(item.getKey(), item.get());
            }
        }
        try {
            WriteFile.writeStringContent(AppSetup.PATH_CONFIGURATION_FILE, configuration.toString(4));
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
    }

}

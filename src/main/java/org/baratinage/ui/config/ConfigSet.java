package org.baratinage.ui.config;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.ui.container.TitledPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.WriteFile;
import org.json.JSONObject;

public class ConfigSet {

    // NOT MODIFIABLE

    public final int INT_MISSING_VALUE = -999999999;
    public final int THROTTLED_DELAY_MS = 250;
    public final int DEBOUNCED_DELAY_MS = 250;
    public final String FALLBACK_LANGUAGE_KEY = "en";

    private final HashMap<String, ConfigItem> configItems = new HashMap<>();

    // UI PREFERENCES

    public final ConfigItemBoolean DARK_MODE = addConfigItem(new ConfigItemBoolean("dark_mode", false));

    public final ConfigItemInteger FONT_SIZE = addConfigItem(new ConfigItemInteger("font_size", 14));

    public final ConfigItemList LANGUAGE_KEY = addConfigItem(
            new ConfigItemList("lg_key", Locale.getDefault().getLanguage(),
                    () -> {
                        Locale[] locales = T.getAvailableLocales();
                        String[] ids = new String[locales.length];
                        for (int k = 0; k < locales.length; k++) {
                            ids[k] = locales[k].getLanguage();
                        }
                        return ids;
                    },
                    () -> {
                        Locale[] locales = T.getAvailableLocales();
                        JLabel[] labels = new JLabel[locales.length];
                        for (int k = 0; k < locales.length; k++) {
                            labels[k] = new JLabel(T.getLocaleLabelString(locales[k]));
                        }
                        return labels;
                    }));

    public final ConfigItemBoolean HIDE_BAM_CONSOLE = addConfigItem(new ConfigItemBoolean("hide_bam_console", false));

    // BAM PREFERENCES

    public final ConfigItemInteger N_SAMPLES = addConfigItem(new ConfigItemInteger("n_samples", 200));

    //

    private <A extends ConfigItem> A addConfigItem(A item) {
        configItems.put(item.id, item);
        return item;
    }

    public void loadConfiguration() {
        try {
            String jsonString = ReadFile.getStringContent(AppSetup.PATH_CONFIGURATION_FILE, true);
            loadFromString(jsonString);
        } catch (IOException e) {
            ConsoleLogger.warn(e);
        }
    }

    private void loadFromString(String configString) {
        JSONObject configuration = new JSONObject();
        configuration = new JSONObject(configString);
        for (ConfigItem item : configItems.values()) {
            item.setFromJSON(configuration);
        }
    }

    public void saveConfiguration() {
        try {
            WriteFile.writeStringContent(AppSetup.PATH_CONFIGURATION_FILE, saveToString());
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
    }

    private String saveToString() {
        JSONObject configuration = new JSONObject();
        for (ConfigItem item : configItems.values()) {
            configuration.put(item.id, item.get());
        }
        return configuration.toString(4);
    }

    public void openConfigDialog() {
        JDialog dialog = new JDialog(AppSetup.MAIN_FRAME, true);

        String stringBackup = saveToString();

        RowColPanel panel = new RowColPanel(RowColPanel.AXIS.COL);
        panel.setGap(5);
        panel.setPadding(5);

        TabContainer configTabs = new TabContainer(
                TabContainer.SIDE.TOP);

        configTabs.addTab(buildConfigPanel(T.text("pref_ui"), null, LANGUAGE_KEY, DARK_MODE, FONT_SIZE));
        configTabs.addTab(buildConfigPanel(T.text("pref_bam"), null, N_SAMPLES));

        panel.appendChild(configTabs, 1000);

        RowColPanel actionsPanel = new RowColPanel();
        actionsPanel.setGap(5);
        JButton cancelButton = new JButton();
        cancelButton.setText(T.text("cancel"));
        cancelButton.addActionListener(l -> {
            loadFromString(stringBackup);
            dialog.dispose();
        });
        JButton saveButton = new JButton();
        saveButton.setText(T.text("save"));
        saveButton.addActionListener(l -> {
            saveConfiguration();
            T.setLocale();
            dialog.dispose();
        });
        actionsPanel.appendChild(saveButton);
        actionsPanel.appendChild(cancelButton);
        panel.appendChild(actionsPanel);

        dialog.setContentPane(panel);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                loadFromString(stringBackup);
            }
        });

        dialog.setTitle(T.text("preferences"));
        Dimension dim = new Dimension(700, 400);
        dialog.setPreferredSize(dim);
        dialog.pack();
        dialog.setLocationRelativeTo(AppSetup.MAIN_FRAME);
        dialog.setVisible(true);

    }

    private TitledPanel buildConfigPanel(String title, Icon icon, ConfigItem... items) {
        GridPanel configItemsPanel = new GridPanel();
        configItemsPanel.setPadding(5);
        configItemsPanel.setGap(5);
        configItemsPanel.setColWeight(1, 1);
        configItemsPanel.setAnchor(GridPanel.ANCHOR.N);
        int k = 0;
        for (ConfigItem item : items) {
            JLabel label = new JLabel(T.text(String.format("pref_%s", item.id)));
            configItemsPanel.insertChild(label, 0, k);
            configItemsPanel.insertChild(item.getField(), 1, k);
            k++;
        }

        JScrollPane content = new JScrollPane();
        content.setBorder(new EmptyBorder(0, 0, 0, 0));
        content.setViewportView(configItemsPanel);

        TitledPanel panel = new TitledPanel(content);
        panel.setText(title);
        panel.setIcon(icon);

        return panel;
    }

}

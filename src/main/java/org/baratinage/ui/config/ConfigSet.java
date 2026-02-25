package org.baratinage.ui.config;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.text.DefaultCaret;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.config.ConfigItem.SCOPE;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.SimpleFlowPanel;
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

    private final HashMap<String, ConfigItem<?, ?>> configItems = new HashMap<>();

    // UI PREFERENCES

    public final ConfigItemBoolean DARK_MODE = addConfigItem(
            new ConfigItemBoolean("dark_mode", true, false));

    public final ConfigItemInteger FONT_SIZE = addConfigItem(
            new ConfigItemInteger("font_size", true, 14));

    public final ConfigItemInteger ICON_SIZE = addConfigItem(
            new ConfigItemInteger("icon_size", true, 28));

    public final ConfigItemList LANGUAGE_KEY = addConfigItem(
            new ConfigItemList("lg_key", false,
                    Locale.getDefault().getLanguage(),
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

    public final ConfigItemBoolean USE_MANNING_COEF = addConfigItem(
            new ConfigItemBoolean("use_manning_coef", false, false));

    public final ConfigItemBoolean HIDE_BAM_CONSOLE = addConfigItem(
            new ConfigItemBoolean("hide_bam_console", false, false));
    public final ConfigItemBoolean CLOSE_BAM_DIALOG_ON_SUCCESS = addConfigItem(
            new ConfigItemBoolean("close_bam_console_on_success", false, false));

    public final ConfigItemBoolean LOG_DISCHARGE_AXIS = addConfigItem(
            new ConfigItemBoolean("log_discharge_axis", false, false));

    // BAM PREFERENCES

    public final ConfigItemInteger N_SAMPLES_LIMNI_ERRORS = addConfigItem(
            new ConfigItemInteger("n_samples_limni_error", false, 200));
    public final ConfigItemInteger N_SAMPLES_PRIOR_RUN = addConfigItem(
            new ConfigItemInteger("n_samples_prior_run", false, 200));

    public ConfigSet() {
        loadConfig();
    }

    private <A extends ConfigItem<?, ?>> A addConfigItem(A item) {
        configItems.put(item.id, item);
        return item;
    }

    public void resetProjectDefaultConfig() {
        resetDefault(SCOPE.PROJECT);
    }

    private void resetDefault(SCOPE scope) {
        for (ConfigItem<?, ?> item : configItems.values()) {
            item.unset(scope);
        }
    }

    public void loadProjectConfig(String config) {
        loadFromString(config, SCOPE.PROJECT);
    }

    public void loadConfig() {
        try {
            String jsonString = ReadFile.getStringContent(AppSetup.PATH_CONFIGURATION_FILE, true);
            loadFromString(jsonString, SCOPE.GLOBAL);
        } catch (IOException e) {
            ConsoleLogger.warn(e);
        }
    }

    private void loadFromString(String configString, SCOPE scope) {
        JSONObject config = new JSONObject();
        config = new JSONObject(configString);
        for (ConfigItem<?, ?> item : configItems.values()) {
            item.setFromJSON(config, scope);
        }
    }

    public String getProjectConfigString() {
        return saveToString(SCOPE.PROJECT);
    }

    public void saveConfig() {
        try {
            WriteFile.writeStringContent(AppSetup.PATH_CONFIGURATION_FILE, saveToString(SCOPE.GLOBAL));
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
    }

    private String saveToString(SCOPE scope) {
        JSONObject configuration = new JSONObject();
        for (ConfigItem<?, ?> item : configItems.values()) {
            if (item.isSet(scope)) {
                configuration.put(item.id, item.get(scope));
            }
        }
        return configuration.toString(4);
    }

    public void openConfigDialog() {
        JDialog dialog = new JDialog(AppSetup.MAIN_FRAME, true);

        String stringBackupGlobal = saveToString(SCOPE.GLOBAL);
        String stringBackupProject = saveToString(SCOPE.PROJECT);

        SimpleFlowPanel panel = new SimpleFlowPanel(true);
        panel.setGap(5);
        panel.setPadding(5);

        TabContainer tab = new TabContainer();

        // global settings
        SimpleFlowPanel globalPanelContent = new SimpleFlowPanel(true);
        globalPanelContent.setPadding(5);
        globalPanelContent.setGap(5);
        JScrollPane globalPanelContentScroller = new JScrollPane();
        globalPanelContentScroller.setViewportView(globalPanelContent);

        TitledPanel globalPanel = new TitledPanel(globalPanelContentScroller);
        globalPanel.setText(T.text("pref_global"));
        tab.addTab(globalPanel);

        TitledPanel globalLook = buildConfigPanel(
                T.text("pref_look"),
                null,
                SCOPE.GLOBAL,
                DARK_MODE,
                FONT_SIZE,
                ICON_SIZE);
        globalPanelContent.addChild(globalLook.getTitle(), false);
        globalPanelContent.addChild(globalLook.getContent(), false);

        TitledPanel globalBehavior = buildConfigPanel(
                T.text("pref_behavior"),
                null,
                SCOPE.GLOBAL,
                USE_MANNING_COEF,
                HIDE_BAM_CONSOLE,
                CLOSE_BAM_DIALOG_ON_SUCCESS);
        globalPanelContent.addChild(globalBehavior.getTitle(), false);
        globalPanelContent.addChild(globalBehavior.getContent(), false);

        TitledPanel globalBaM = buildConfigPanel(
                T.text("pref_bam"),
                null,
                SCOPE.GLOBAL,
                N_SAMPLES_LIMNI_ERRORS,
                N_SAMPLES_PRIOR_RUN);
        globalPanelContent.addChild(globalBaM.getTitle(), false);
        globalPanelContent.addChild(globalBaM.getContent(), false);

        // project settings
        SimpleFlowPanel projectPanelContent = new SimpleFlowPanel(true);
        projectPanelContent.setPadding(5);
        projectPanelContent.setGap(5);
        JScrollPane projectPanelContentScroller = new JScrollPane();
        projectPanelContentScroller.setViewportView(projectPanelContent);

        TitledPanel projectPanel = new TitledPanel(projectPanelContentScroller);
        projectPanel.setText(T.text("pref_project"));
        tab.addTab(projectPanel);

        TitledPanel projectBehavior = buildConfigPanel(
                T.text("pref_behavior"),
                null,
                SCOPE.PROJECT,
                USE_MANNING_COEF,
                HIDE_BAM_CONSOLE,
                CLOSE_BAM_DIALOG_ON_SUCCESS);

        projectPanelContent.addChild(projectBehavior.getTitle(), false);
        projectPanelContent.addChild(projectBehavior.getContent(), false);

        TitledPanel projectBaM = buildConfigPanel(
                T.text("pref_bam"),
                null,
                SCOPE.PROJECT,
                N_SAMPLES_LIMNI_ERRORS,
                N_SAMPLES_PRIOR_RUN);
        projectPanelContent.addChild(projectBaM.getTitle(), false);
        projectPanelContent.addChild(projectBaM.getContent(), false);

        // Overall panel
        String text = "<html><body><p align='justify'>%s %s</p></body></html>".formatted(
                T.text("pref_message"),
                T.text("pref_default_pref"));
        JEditorPane topMessageLabel = new JEditorPane("text/html", text);
        topMessageLabel.setEditable(false);
        topMessageLabel.setCaret(new DefaultCaret() {
            @Override
            public void paint(Graphics g) {
                // do nothing to hide the caret
            }
        });

        JLabel requireRestartLabel = new JLabel("* " + T.text("require_restart"));

        panel.addChild(topMessageLabel, false);
        panel.addChild(tab, true);
        panel.addChild(requireRestartLabel, false);

        SimpleFlowPanel actionsPanel = new SimpleFlowPanel();
        actionsPanel.setGap(5);
        JButton cancelButton = new JButton();
        cancelButton.setText(T.text("cancel"));
        cancelButton.addActionListener(l -> {
            loadFromString(stringBackupGlobal, SCOPE.GLOBAL);
            loadFromString(stringBackupProject, SCOPE.PROJECT);
            dialog.dispose();
        });
        JButton saveButton = new JButton();
        saveButton.setText(T.text("save"));
        saveButton.addActionListener(l -> {
            saveConfig();
            T.setLocale();
            dialog.dispose();
        });

        actionsPanel.addChild(saveButton, false);
        actionsPanel.addExtensor();
        actionsPanel.addChild(cancelButton, false);
        panel.addChild(actionsPanel, false);

        tab.setEnabledAt(1, AppSetup.MAIN_FRAME.currentProject != null);

        dialog.setContentPane(panel);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                loadFromString(stringBackupGlobal, SCOPE.GLOBAL);
            }
        });

        dialog.setTitle(T.text("preferences"));
        Dimension dim = new Dimension(700, 600);
        dialog.setPreferredSize(dim);
        dialog.pack();
        dialog.setLocationRelativeTo(AppSetup.MAIN_FRAME);
        dialog.setVisible(true);

    }

    private TitledPanel buildConfigPanel(String title, Icon icon, SCOPE scope, ConfigItem<?, ?>... items) {
        GridPanel configItemsPanel = new GridPanel();
        configItemsPanel.setGap(5);
        configItemsPanel.setColWeight(0, 0);
        configItemsPanel.setColWeight(1, 1);
        configItemsPanel.setColWeight(2, 0);
        configItemsPanel.setAnchor(GridPanel.ANCHOR.N);
        int k = 0;
        configItemsPanel.insertChild(new SimpleSep(), 0, k, 3, 1);
        k++;
        for (int index = 0; index < items.length; index++) {
            ConfigItem<?, ?> item = items[index];
            JLabel label = new JLabel();
            boolean inherited1 = (scope == SCOPE.PROJECT) && item.isInherited(scope);
            label.setText(configItemLabelString(
                    T.text("pref_%s".formatted(item.id)),
                    item.requireRestart,
                    !item.isSet(scope),
                    inherited1));
            JButton resetButton = new JButton(T.text("reset"));
            resetButton.addActionListener(l -> item.unset(scope));
            resetButton.setEnabled(item.isSet(scope));
            item.subscribe(this, l -> {
                resetButton.setEnabled(item.isSet(scope));
                boolean inherited2 = (scope == SCOPE.PROJECT) && item.isInherited(scope);
                String txt = configItemLabelString(
                        T.text("pref_%s".formatted(item.id)),
                        item.requireRestart,
                        !item.isSet(scope),
                        inherited2);
                label.setText(txt);
            });
            configItemsPanel.insertChild(label, 0, k);
            configItemsPanel.insertChild(item.getField(scope), 1, k,
                    1, 1,
                    GridPanel.ANCHOR.C, GridPanel.FILL.H);
            configItemsPanel.insertChild(resetButton, 2, k,
                    1, 1,
                    GridPanel.ANCHOR.C, GridPanel.FILL.NONE);
            k++;
            configItemsPanel.insertChild(new SimpleSep(), 0, k, 3, 1);
            k++;
        }

        TitledPanel panel = new TitledPanel(configItemsPanel);
        panel.setText(title);
        panel.setIcon(icon);

        return panel;
    }

    private static String configItemLabelString(String txt, boolean star, boolean italic, boolean inherited) {
        txt = inherited ? "%s (%s)".formatted(txt, T.text("inherited")) : txt;
        txt = italic ? "<i>%s</i>".formatted(txt) : txt;
        txt = String.format("%s%s", txt, star ? "*" : "");
        return "<html><div style='width: 250px'>%s</div></html>".formatted(txt);
    }

}

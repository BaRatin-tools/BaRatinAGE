package org.baratinage.ui.baratin.hydraulic_control;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Parameter;

import org.baratinage.ui.baratin.hydraulic_control.control_panel.KAC;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.PriorControlPanel;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.WeirOrifice;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.WeirParabola;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.WeirRect;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.WeirTriangle;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.ChannelParabola;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.ChannelRect;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.ChannelTriangle;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.translation.T;
import org.json.JSONArray;
import org.json.JSONObject;

public class OneHydraulicControl extends JScrollPane {

    public static final ImageIcon parabolaWeirIcon = AppConfig.AC.ICONS
            .getCustomAppImageIcon(
                    "hc_parabola_weir.svg");

    public static final ImageIcon triangleWeirIcon = AppConfig.AC.ICONS
            .getCustomAppImageIcon(
                    "hc_triangle_weir.svg");

    public static final ImageIcon rectWeirIcon = AppConfig.AC.ICONS
            .getCustomAppImageIcon(
                    "hc_rect_weir.svg");

    public static final ImageIcon orificeWeirIcon = AppConfig.AC.ICONS
            .getCustomAppImageIcon(
                    "hc_orifice_weir.svg");

    public static final ImageIcon rectChannelIcon = AppConfig.AC.ICONS
            .getCustomAppImageIcon(
                    "hc_rect_channel.svg");

    public static final ImageIcon triangleChannelIcon = AppConfig.AC.ICONS
            .getCustomAppImageIcon(
                    "hc_triangle_channel.svg");

    public static final ImageIcon parabolaChannelIcon = AppConfig.AC.ICONS
            .getCustomAppImageIcon(
                    "hc_parabola_channel.svg");

    public final int controlNumber;
    private final RowColPanel physicalParametersPanel;
    private final RowColPanel hydraulicControlPanel;
    private final JButton switchModeButton;
    private final SimpleComboBox controlTypeComboBox;
    private final KAC kacControlPanel;

    private boolean kacMode = false;
    private String toKACmodeText = "to kac";
    private String toPhysicalModeText = "to physical mode";

    // FIXME: refactor?
    private record HydraulicControlOption(
            String lgKey,
            ImageIcon icon,
            PriorControlPanel panel) {
    };

    private final List<HydraulicControlOption> allControlOptions;
    private int currentPriorControlPanelIndex = 0;

    public OneHydraulicControl(int controlNumber) {
        // super(AXIS.COL, ALIGN.START);
        super();

        setBorder(new EmptyBorder(0, 0, 0, 0));
        RowColPanel mainPanel = new RowColPanel(RowColPanel.AXIS.COL, RowColPanel.ALIGN.START);
        setViewportView(mainPanel);

        kacControlPanel = new KAC();
        kacControlPanel.setPadding(5);
        kacControlPanel.addChangeListener((chEvt) -> {
            fireChangeListeners();
        });

        allControlOptions = new ArrayList<>();
        allControlOptions.add(
                new HydraulicControlOption(
                        "rectangular_weir", rectWeirIcon,
                        new WeirRect()));
        allControlOptions.add(
                new HydraulicControlOption(
                        "triangular_weir", triangleWeirIcon,
                        new WeirTriangle()));
        allControlOptions.add(
                new HydraulicControlOption(
                        "parabola_weir", parabolaWeirIcon,
                        new WeirParabola()));
        allControlOptions.add(
                new HydraulicControlOption(
                        "orifice_weir", orificeWeirIcon,
                        new WeirOrifice()));
        allControlOptions.add(
                new HydraulicControlOption(
                        "rectangular_channel",
                        rectChannelIcon,
                        new ChannelRect()));
        allControlOptions.add(
                new HydraulicControlOption(
                        "triangular_channel",
                        triangleChannelIcon,
                        new ChannelTriangle()));
        allControlOptions.add(
                new HydraulicControlOption(
                        "parabola_channel",
                        parabolaChannelIcon,
                        new ChannelParabola()));
        for (HydraulicControlOption hco : allControlOptions) {
            T.updateHierarchy(this, hco.panel);
            hco.panel.addChangeListener((ChangeEvent chEvt) -> {
                updateKACfromPhysicalControl();
            });
        }

        this.controlNumber = controlNumber;

        physicalParametersPanel = new RowColPanel(RowColPanel.AXIS.COL);
        physicalParametersPanel.setPadding(5);

        GridPanel physicalControlParametersLabelsPanel = new GridPanel();
        int gapAndPadding = 10;
        physicalControlParametersLabelsPanel.setGap(gapAndPadding);
        physicalControlParametersLabelsPanel.setPadding(gapAndPadding, 0, gapAndPadding, 0);
        JLabel arrowLeftUp = new JLabel(AppConfig.AC.ICONS.LEFT_UP_ARROW_ICON);
        JLabel arrowRightUp = new JLabel(AppConfig.AC.ICONS.RIGHT_UP_ARROW_ICON);
        JLabel physicalParLabel = new JLabel();
        JLabel arrowLeftDown = new JLabel(AppConfig.AC.ICONS.LEFT_DOWN_ARROW_ICON);
        JLabel arrowRightDown = new JLabel(AppConfig.AC.ICONS.RIGHT_DOWN_ARROW_ICON);
        JLabel controlParLabel = new JLabel();
        physicalControlParametersLabelsPanel.insertChild(arrowLeftUp, 0, 0);
        physicalControlParametersLabelsPanel.insertChild(physicalParLabel, 1, 0);
        physicalControlParametersLabelsPanel.insertChild(arrowRightUp, 2, 0);
        physicalControlParametersLabelsPanel.insertChild(arrowLeftDown, 0, 1);
        physicalControlParametersLabelsPanel.insertChild(controlParLabel, 1, 1);
        physicalControlParametersLabelsPanel.insertChild(arrowRightDown, 2, 1);

        Font f = physicalParLabel.getFont().deriveFont(Font.BOLD);
        physicalParLabel.setFont(f);
        controlParLabel.setFont(f);

        hydraulicControlPanel = new RowColPanel(RowColPanel.AXIS.COL);

        controlTypeComboBox = new SimpleComboBox();
        controlTypeComboBox.setEmptyItem(null);
        controlTypeComboBox.addChangeListener((chEvt) -> {
            int index = controlTypeComboBox.getSelectedIndex();
            currentPriorControlPanelIndex = index;
            updatePhysicalControl();
        });
        setControlTypeCombobox();

        physicalParametersPanel.appendChild(controlTypeComboBox);
        physicalParametersPanel.appendChild(hydraulicControlPanel);
        physicalParametersPanel.appendChild(physicalControlParametersLabelsPanel);

        switchModeButton = new JButton("Switch to KAC");
        switchModeButton.addActionListener((e) -> {
            boolean proceed = true;
            if (kacMode) {
                proceed = false;
                int response = JOptionPane.showOptionDialog(this,
                        T.text("kac_to_physical_parameters_warning") + "\n" +
                                T.text("proceed_question"),
                        T.text("warning"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        new String[] { T.text("continue"), T.text("cancel") },
                        null);
                if (response == JOptionPane.YES_OPTION) {
                    proceed = true;
                }
            }
            if (proceed) {
                kacMode = !kacMode;
                updateMode();
            }
        });

        RowColPanel buttonsPanel = new RowColPanel();
        buttonsPanel.setPadding(5);
        buttonsPanel.appendChild(switchModeButton);

        mainPanel.appendChild(physicalParametersPanel, 0);
        mainPanel.appendChild(kacControlPanel, 0);
        mainPanel.appendChild(buttonsPanel, 0);

        controlTypeComboBox.setSelectedItem(0);
        updatePhysicalControl();

        updateMode();

        T.updateHierarchy(this, kacControlPanel);
        T.t(this, physicalParLabel, false, "physical_parameters");
        T.t(this, controlParLabel, false, "control_parameters");
        T.t(this, () -> {
            setControlTypeCombobox();
            toKACmodeText = T.text("switch_to_kac_mode");
            toPhysicalModeText = T.text("switch_to_physical_mode");
            switchModeButton.setText(kacMode ? toPhysicalModeText : toKACmodeText);
        });
    }

    private void setControlTypeCombobox() {
        int nOption = allControlOptions.size();
        JLabel[] options = new JLabel[nOption];
        for (int k = 0; k < nOption; k++) {
            options[k] = new JLabel();
            options[k].setText(T.text(allControlOptions.get(k).lgKey));
            options[k].setIcon(allControlOptions.get(k).icon);
            options[k].setBorder(new EmptyBorder(5, 5, 5, 5));
        }
        int index = controlTypeComboBox.getSelectedIndex();
        controlTypeComboBox.setItems(options, true);
        if (index != -1) {
            controlTypeComboBox.setSelectedItem(index, true);
        }
    }

    private void updatePhysicalControl() {
        if (currentPriorControlPanelIndex >= 0) {
            hydraulicControlPanel.clear();
            hydraulicControlPanel.appendChild(allControlOptions.get(currentPriorControlPanelIndex).panel);
        }
        updateKACfromPhysicalControl();
        updateUI();
    }

    private void updateKACfromPhysicalControl() {
        PriorControlPanel panel = allControlOptions.get(currentPriorControlPanelIndex).panel;
        kacControlPanel.setFromKACGaussianConfig(panel.toKACGaussianConfig());
    }

    private void updateMode() {
        switchModeButton.setText(kacMode ? toPhysicalModeText : toKACmodeText);
        physicalParametersPanel.setVisible(!kacMode);
        kacControlPanel.setGlobalLock(!kacMode);
        if (!kacMode) {
            updateKACfromPhysicalControl();
        }
        updateUI();
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    public Parameter[] getParameters() {
        return kacControlPanel.getParameters();
    }

    public void fromJSON(JSONObject json) {

        controlTypeComboBox.setSelectedItem(json.getInt("controlTypeIndex"));
        JSONArray allControlOptionsJSON = json.getJSONArray("allControlOptions");
        int nOptions = allControlOptionsJSON.length();
        for (int k = 0; k < nOptions; k++) {
            allControlOptions.get(k).panel.fromJSON(allControlOptionsJSON.getJSONArray(k));
        }

        kacControlPanel.fromJSON(json.getJSONArray("kacControl"));
        kacMode = json.getBoolean("isKACmode");
        updateMode();

    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        json.put("isKACmode", kacMode);
        json.put("controlTypeIndex", controlTypeComboBox.getSelectedIndex());

        JSONArray allControlOptionsJSON = new JSONArray();
        int nOptions = allControlOptions.size();
        for (int k = 0; k < nOptions; k++) {
            allControlOptionsJSON.put(k, allControlOptions.get(k).panel.toJSON());
        }

        json.put("allControlOptions", allControlOptionsJSON);

        json.put("kacControl", kacControlPanel.toJSON());

        return json;
    }

}
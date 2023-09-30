package org.baratinage.ui.baratin.hydraulic_control;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Parameter;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.KACPriorControlChangel;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.PriorControlPanel;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.RectChannelPriorControlPanel;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.RectWeirPriorControlPanel;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.json.JSONArray;
import org.json.JSONObject;

public class OneHydraulicControl extends RowColPanel implements ChangeListener {

    public static final ImageIcon rectWeirIcon = SvgIcon.buildCustomAppImageIcon(
            "hc_rect_weir.svg", AppConfig.AC.ICON_SIZE);

    public static final ImageIcon rectChannelIcon = SvgIcon.buildCustomAppImageIcon(
            "hc_rect_channel.svg", AppConfig.AC.ICON_SIZE);

    public static final ImageIcon arrowLeftDownIcon = SvgIcon.buildFeatherAppImageIcon(
            "corner-left-down.svg", AppConfig.AC.ICON_SIZE);

    public static final ImageIcon arrowRightDownIcon = SvgIcon.buildFeatherAppImageIcon(
            "corner-right-down.svg", AppConfig.AC.ICON_SIZE);

    public static final ImageIcon arrowLeftUpIcon = SvgIcon.buildFeatherAppImageIcon(
            "corner-left-up.svg", AppConfig.AC.ICON_SIZE);

    public static final ImageIcon arrowRightUpIcon = SvgIcon.buildFeatherAppImageIcon(
            "corner-right-up.svg", AppConfig.AC.ICON_SIZE);

    public final int controlNumber;
    private final RowColPanel physicalParametersPanel;
    private final RowColPanel hydraulicControlPanel;
    private final JButton switchModeButton;
    private final JButton resetButton;
    private final SimpleComboBox controlTypeComboBox;
    private final PriorControlPanel kacControlPanel;

    private boolean kacMode = false;
    private String toKACmodeText = "to kac";
    private String toPhysicalModeText = "to physical mode";

    // FIXME: refactor!
    private record HydraulicControlOption(
            String lgKey, ImageIcon icon,
            PriorControlPanel panel) {
    };

    private final List<HydraulicControlOption> allControlOptions;

    public OneHydraulicControl(int controlNumber) {
        super(AXIS.COL, ALIGN.START);
        setGap(5);

        allControlOptions = new ArrayList<>();
        allControlOptions.add(
                new HydraulicControlOption(
                        "rectangular_weir", rectWeirIcon,
                        new RectWeirPriorControlPanel()));
        allControlOptions.add(
                new HydraulicControlOption(
                        "rectangular_channel",
                        rectChannelIcon,
                        new RectChannelPriorControlPanel()));

        this.controlNumber = controlNumber;

        physicalParametersPanel = new RowColPanel(AXIS.COL);

        GridPanel physicalControlParametersLabelsPanel = new GridPanel();
        int gapAndPadding = 10;
        physicalControlParametersLabelsPanel.setGap(gapAndPadding);
        physicalControlParametersLabelsPanel.setPadding(gapAndPadding, 0, gapAndPadding, 0);
        JLabel arrowLeftUp = new JLabel(arrowLeftUpIcon);
        JLabel arrowRightUp = new JLabel(arrowRightUpIcon);
        JLabel physicalParLabel = new JLabel();
        JLabel arrowLeftDown = new JLabel(arrowLeftDownIcon);
        JLabel arrowRightDown = new JLabel(arrowRightDownIcon);
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

        hydraulicControlPanel = new RowColPanel(AXIS.COL);

        controlTypeComboBox = new SimpleComboBox();
        controlTypeComboBox.setEmptyItem(null);
        controlTypeComboBox.addChangeListener((chEvt) -> {
            hydraulicControlPanel.clear();
            int index = controlTypeComboBox.getSelectedIndex();
            setPhysicalControlType(index);
        });
        setControlTypeCombobox();

        physicalParametersPanel.appendChild(controlTypeComboBox);
        physicalParametersPanel.appendChild(hydraulicControlPanel);
        physicalParametersPanel.appendChild(physicalControlParametersLabelsPanel);

        kacControlPanel = new KACPriorControlChangel();

        switchModeButton = new JButton("Switch to expert mode");
        switchModeButton.addActionListener((e) -> {
            kacMode = !kacMode;
            updateMode();
        });
        resetButton = new JButton("Reset all fields");

        RowColPanel buttonsPanel = new RowColPanel();
        buttonsPanel.appendChild(switchModeButton);
        buttonsPanel.appendChild(resetButton);

        Lg.register(this, () -> {

            setControlTypeCombobox();

            physicalParLabel.setText(Lg.text("physical_parameters"));
            controlParLabel.setText(Lg.text("control_parameters"));

            toKACmodeText = Lg.text("switch_to_kac_mode");
            toPhysicalModeText = Lg.text("switch_to_physical_mode");
            switchModeButton.setText(kacMode ? toPhysicalModeText : toKACmodeText);

            resetButton.setText(Lg.text("reset"));
        });

        appendChild(physicalParametersPanel, 0);
        appendChild(kacControlPanel, 0);
        appendChild(buttonsPanel, 0);

        controlTypeComboBox.setSelectedItem(0);
        setPhysicalControlType(0);

        updateMode();
    }

    private void setControlTypeCombobox() {
        int nOption = allControlOptions.size();
        JLabel[] options = new JLabel[nOption];
        for (int k = 0; k < nOption; k++) {
            options[k] = new JLabel();
            options[k].setText(Lg.text(allControlOptions.get(k).lgKey));
            options[k].setIcon(allControlOptions.get(k).icon);
            options[k].setBorder(new EmptyBorder(5, 5, 5, 5));
        }
        int index = controlTypeComboBox.getSelectedIndex();
        controlTypeComboBox.setItems(options, true);
        if (index != -1) {
            controlTypeComboBox.setSelectedItem(index, true);
        }
    }

    private void setPhysicalControlType(int index) {
        if (index >= 0) {
            hydraulicControlPanel.appendChild(allControlOptions.get(index).panel);
        }
        hydraulicControlPanel.updateUI();
    }

    private void updateMode() {
        switchModeButton.setText(kacMode ? toPhysicalModeText : toKACmodeText);
        physicalParametersPanel.setVisible(!kacMode);
        kacControlPanel.setGlobalLock(!kacMode);
        revalidate();
    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        fireChangeListeners();
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
        kacMode = json.getBoolean("isKACmode");
        switchModeButton.setText(kacMode ? toPhysicalModeText : toKACmodeText);

        controlTypeComboBox.setSelectedItem(json.getInt("controlTypeIndex"));
        JSONArray allControlOptionsJSON = json.getJSONArray("allControlOptions");
        int nOptions = allControlOptionsJSON.length();
        for (int k = 0; k < nOptions; k++) {
            allControlOptions.get(k).panel.fromJSON(allControlOptionsJSON.getJSONArray(k));
        }

        kacControlPanel.fromJSON(json.getJSONArray("kacControl"));

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
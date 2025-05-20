package org.baratinage.ui.baratin.hydraulic_control;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.jbam.Parameter;

import org.baratinage.ui.baratin.hydraulic_control.control_panel.KBAC;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.PriorControlPanel;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.WeirOrifice;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.WeirParabola;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.WeirRect;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.WeirTriangle;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.ChannelParabola;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.ChannelRect;
import org.baratinage.ui.baratin.hydraulic_control.control_panel.ChannelTriangle;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.translation.T;
import org.json.JSONArray;
import org.json.JSONObject;

public class OneHydraulicControl extends JScrollPane {

    public static final Icon parabolaWeirIcon = AppSetup.ICONS
            .getCustomAppImageIcon(
                    "hc_parabola_weir.svg");

    public static final Icon triangleWeirIcon = AppSetup.ICONS
            .getCustomAppImageIcon(
                    "hc_triangle_weir.svg");

    public static final Icon rectWeirIcon = AppSetup.ICONS
            .getCustomAppImageIcon(
                    "hc_rect_weir.svg");

    public static final Icon orificeWeirIcon = AppSetup.ICONS
            .getCustomAppImageIcon(
                    "hc_orifice_weir.svg");

    public static final Icon rectChannelIcon = AppSetup.ICONS
            .getCustomAppImageIcon(
                    "hc_rect_channel.svg");

    public static final Icon triangleChannelIcon = AppSetup.ICONS
            .getCustomAppImageIcon(
                    "hc_triangle_channel.svg");

    public static final Icon parabolaChannelIcon = AppSetup.ICONS
            .getCustomAppImageIcon(
                    "hc_parabola_channel.svg");

    public final int controlNumber;
    private final SimpleFlowPanel mainPanel;
    private final SimpleFlowPanel physicalParametersPanel;
    private final SimpleFlowPanel hydraulicControlPanel;
    private final JButton switchModeButton;
    private final SimpleComboBox controlTypeComboBox;
    private final KBAC kbacControlPanel;

    private boolean bkacMode = false;
    private String toBKACmodeText = "to bkac";
    private String toPhysicalModeText = "to physical mode";

    // FIXME: refactor?
    private record HydraulicControlOption(
            String lgKey,
            Icon icon,
            PriorControlPanel panel) {
    };

    private final List<HydraulicControlOption> allControlOptions;
    private int currentPriorControlPanelIndex = 0;

    private final SimpleTextField descriptionField;

    public OneHydraulicControl(int controlNumber) {
        this(true, controlNumber);
    }

    public OneHydraulicControl(boolean kMode, int controlNumber) {
        // super(AXIS.COL, ALIGN.START);
        super();

        setBorder(new EmptyBorder(0, 0, 0, 0));
        mainPanel = new SimpleFlowPanel(true);
        setViewportView(mainPanel);

        kbacControlPanel = new KBAC(kMode);
        kbacControlPanel.setPadding(5);
        kbacControlPanel.addChangeListener((chEvt) -> {
            fireChangeListeners();
        });

        allControlOptions = new ArrayList<>();
        allControlOptions.add(
                new HydraulicControlOption(
                        "rectangular_weir", rectWeirIcon,
                        new WeirRect(kMode)));
        if (kMode) {

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
                            "orifice_ctrl", orificeWeirIcon,
                            new WeirOrifice()));
        }
        allControlOptions.add(
                new HydraulicControlOption(
                        "rectangular_channel",
                        rectChannelIcon,
                        new ChannelRect(kMode)));

        if (kMode) {
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
        }
        for (HydraulicControlOption hco : allControlOptions) {
            T.updateHierarchy(this, hco.panel);
            hco.panel.addChangeListener((ChangeEvent chEvt) -> {
                updateKACfromPhysicalControl();
            });
        }

        this.controlNumber = controlNumber;

        physicalParametersPanel = new SimpleFlowPanel(true);
        physicalParametersPanel.setPadding(5);

        GridPanel physicalControlParametersLabelsPanel = new GridPanel();
        int gapAndPadding = 10;
        physicalControlParametersLabelsPanel.setGap(gapAndPadding);
        physicalControlParametersLabelsPanel.setPadding(gapAndPadding, 0, gapAndPadding, 0);
        JLabel arrowLeftUp = new JLabel(AppSetup.ICONS.LEFT_UP_ARROW);
        JLabel arrowRightUp = new JLabel(AppSetup.ICONS.RIGHT_UP_ARROW);
        JLabel physicalParLabel = new JLabel();
        JLabel arrowLeftDown = new JLabel(AppSetup.ICONS.LEFT_DOWN_ARROW);
        JLabel arrowRightDown = new JLabel(AppSetup.ICONS.RIGHT_DOWN_ARROW);
        JLabel controlParLabel = new JLabel();
        physicalControlParametersLabelsPanel.insertChild(arrowLeftUp, 0, 0);
        physicalControlParametersLabelsPanel.insertChild(physicalParLabel, 1, 0, GridPanel.ANCHOR.C,
                GridPanel.FILL.NONE);
        physicalControlParametersLabelsPanel.insertChild(arrowRightUp, 2, 0);
        physicalControlParametersLabelsPanel.insertChild(arrowLeftDown, 0, 1);
        physicalControlParametersLabelsPanel.insertChild(controlParLabel, 1, 1, GridPanel.ANCHOR.C,
                GridPanel.FILL.NONE);
        physicalControlParametersLabelsPanel.insertChild(arrowRightDown, 2, 1);

        Font f = physicalParLabel.getFont().deriveFont(Font.BOLD);
        physicalParLabel.setFont(f);
        controlParLabel.setFont(f);

        hydraulicControlPanel = new SimpleFlowPanel(true);

        controlTypeComboBox = new SimpleComboBox();
        controlTypeComboBox.setEmptyItem(null);
        controlTypeComboBox.addChangeListener((chEvt) -> {
            int index = controlTypeComboBox.getSelectedIndex();
            currentPriorControlPanelIndex = index;
            updatePhysicalControl();
        });
        setControlTypeCombobox();

        physicalParametersPanel.addChild(controlTypeComboBox, false);
        physicalParametersPanel.addChild(hydraulicControlPanel, false);
        physicalParametersPanel.addChild(physicalControlParametersLabelsPanel, false);

        switchModeButton = new JButton("Switch to BKAC");
        switchModeButton.addActionListener((e) -> {
            boolean proceed = true;
            if (bkacMode) {
                proceed = false;
                int response = JOptionPane.showOptionDialog(this,
                        T.text(kMode ? "kac_to_physical_parameters_warning" : "bac_to_physical_parameters_warning")
                                + "\n" +
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
                bkacMode = !bkacMode;
                updateMode();
            }
        });

        // SimpleFlowPanel buttonsPanel = new SimpleFlowPanel();
        // buttonsPanel.setPadding(5);
        // buttonsPanel.addChild(switchModeButton, true);

        descriptionField = new SimpleTextField();
        T.t(this, () -> {
            descriptionField.setPlaceholder(T.text("description"));
        });

        controlTypeComboBox.setSelectedItem(0);
        updatePhysicalControl();

        updateMode();

        T.updateHierarchy(this, kbacControlPanel);
        T.t(this, physicalParLabel, false, "physical_parameters");
        T.t(this, controlParLabel, false, "resulting_control_parameters");
        T.t(this, () -> {
            setControlTypeCombobox();
            toBKACmodeText = T.text(kMode ? "switch_to_kac_mode" : "switch_to_bac_mode");
            toPhysicalModeText = T.text("switch_to_physical_mode");
            switchModeButton.setText(bkacMode ? toPhysicalModeText : toBKACmodeText);
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
            hydraulicControlPanel.removeAll();
            hydraulicControlPanel.addChild(allControlOptions.get(currentPriorControlPanelIndex).panel, false);
        }
        updateKACfromPhysicalControl();
        updateUI();
    }

    private void updateKACfromPhysicalControl() {
        PriorControlPanel panel = allControlOptions.get(currentPriorControlPanelIndex).panel;
        kbacControlPanel.setFromKACGaussianConfig(panel.toKACGaussianConfig());
    }

    private void updateMode() {
        switchModeButton.setText(bkacMode ? toPhysicalModeText : toBKACmodeText);
        kbacControlPanel.setGlobalLock(!bkacMode);

        mainPanel.removeAll();
        mainPanel.addChild(descriptionField, 0, 5);
        if (!bkacMode) {
            mainPanel.addChild(physicalParametersPanel, false);
        }
        mainPanel.addChild(kbacControlPanel, false);
        mainPanel.addChild(switchModeButton, false);
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
        return kbacControlPanel.getParameters();
    }

    public void fromJSON(JSONObject json) {

        controlTypeComboBox.setSelectedItem(json.getInt("controlTypeIndex"));
        JSONArray allControlOptionsJSON = json.getJSONArray("allControlOptions");
        int nOptions = allControlOptionsJSON.length();
        for (int k = 0; k < nOptions; k++) {
            allControlOptions.get(k).panel.fromJSON(allControlOptionsJSON.getJSONArray(k));
        }

        kbacControlPanel.fromJSON(json.getJSONArray("kacControl"));
        bkacMode = json.getBoolean("isKACmode");

        if (json.has("description")) {
            descriptionField.setText(json.getString("description"));
        }
        updateMode();

    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        json.put("isKACmode", bkacMode);
        json.put("controlTypeIndex", controlTypeComboBox.getSelectedIndex());

        JSONArray allControlOptionsJSON = new JSONArray();
        int nOptions = allControlOptions.size();
        for (int k = 0; k < nOptions; k++) {
            allControlOptionsJSON.put(k, allControlOptions.get(k).panel.toJSON());
        }

        json.put("allControlOptions", allControlOptionsJSON);

        json.put("kacControl", kbacControlPanel.toJSON());

        json.put("description", descriptionField.getText());

        return json;
    }

}
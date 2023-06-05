package org.baratinage.ui.baratin.hydraulic_control;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.Distribution.DISTRIB;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.container.RowColPanel;

public class AllHydraulicControls extends RowColPanel implements IPriors {

    JList<OneHydraulicControl> controlSelector;
    DefaultListModel<OneHydraulicControl> controlSelectorModel;

    List<OneHydraulicControl> hydraulicControlList;
    RowColPanel currentHydraulicControl;

    JSplitPane splitPaneContainer;

    public AllHydraulicControls() {
        super(AXIS.COL);

        hydraulicControlList = new ArrayList<>();

        controlSelector = new JList<>();
        controlSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        controlSelector.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                OneHydraulicControl selectedHydraulicControl = controlSelector.getSelectedValue();
                if (selectedHydraulicControl != null) {
                    hydraulicControlList.stream()
                            .filter(hc -> hc.equals(selectedHydraulicControl))
                            .findFirst()
                            .ifPresentOrElse((hc) -> {
                                currentHydraulicControl.clear();
                                currentHydraulicControl.appendChild(hc);
                                currentHydraulicControl.updateUI();
                            }, () -> {
                                currentHydraulicControl.clear();
                                currentHydraulicControl.updateUI();
                            });
                } else {
                    currentHydraulicControl.clear();
                    currentHydraulicControl.updateUI();
                }

            }
        });
        controlSelectorModel = new DefaultListModel<>() {

        };
        controlSelector.setModel(controlSelectorModel);
        // NOTE: I could have juste implemented toString for OneHydraulicControl
        // but this way, we can do much more things. DefaultListCellRenderer
        // extends a JLabel so we can customize the label with icons and so on while
        // keeping the nice default formatting when selected/focused that I think
        // comes from the LookAndFeel...
        controlSelector.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Object> list,
                    Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel comp = (JLabel) super.getListCellRendererComponent(list, value,
                        index, isSelected, cellHasFocus);
                OneHydraulicControl hc = (OneHydraulicControl) value;
                comp.setText(hc.getName());
                return comp;
            }
        });

        currentHydraulicControl = new RowColPanel();
        currentHydraulicControl.setMinimumSize(new Dimension(500, 300));
        RowColPanel listPanel = new RowColPanel(AXIS.COL);

        JScrollPane listScrollContainer = new JScrollPane(listPanel);
        listScrollContainer.setBorder(BorderFactory.createEmptyBorder());
        listScrollContainer.setMinimumSize(new Dimension(100, 100));
        listPanel.appendChild(controlSelector);
        JSplitPane splitPaneContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPaneContainer.setBorder(BorderFactory.createEmptyBorder());

        splitPaneContainer.setLeftComponent(listScrollContainer);
        splitPaneContainer.setRightComponent(currentHydraulicControl);

        this.appendChild(splitPaneContainer);

    }

    public void updateHydraulicControlListFromNumberOfControls(int n) {
        int m = controlSelectorModel.size();
        if (m > n) {
            int d = m - n;
            for (int k = 0; k < d; k++) {
                OneHydraulicControl hc = controlSelectorModel.get(m - k - 1);
                hydraulicControlList = hydraulicControlList.stream()
                        .filter(ctrl -> !ctrl.equals(hc))
                        .collect(Collectors.toCollection(ArrayList::new));
                controlSelectorModel.remove(m - k - 1);
            }

        } else if (m < n) {
            for (int k = m; k < n; k++) {

                OneHydraulicControl newHydraulicControl = new OneHydraulicControl();
                newHydraulicControl.setName("Contrôle #" + (k + 1));
                newHydraulicControl.addPropertyChangeListener("hydraulicControl", (e) -> {
                    firePropertyChange("hydraulicControl", null, null);
                });

                controlSelectorModel.addElement(newHydraulicControl);
                hydraulicControlList.add(newHydraulicControl);

            }
        }
    }

    @Override
    public Parameter[] getParameters() {
        int nControls = hydraulicControlList.size();
        Parameter[] parameters = new Parameter[nControls * 3];
        for (int k = 0; k < nControls; k++) {
            OneHydraulicControl hc = hydraulicControlList.get(k);
            Distribution activationStageDistribution = new Distribution(
                    DISTRIB.GAUSSIAN,
                    hc.getActivationStage(),
                    hc.getActivationStageUncertainty() / 2);
            Distribution coefficientDistribution = new Distribution(
                    DISTRIB.GAUSSIAN,
                    hc.getCoefficient(),
                    hc.getCoefficientUncertainty() / 2);
            Distribution exponentDistribution = new Distribution(
                    DISTRIB.GAUSSIAN,
                    hc.getExponent(),
                    hc.getExponentUncertainty() / 2);

            parameters[k * 3 + 0] = new Parameter("k_" + k,
                    hc.getActivationStage(),
                    activationStageDistribution);
            parameters[k * 3 + 1] = new Parameter("a_" + k,
                    hc.getCoefficient(),
                    coefficientDistribution);
            parameters[k * 3 + 2] = new Parameter("c_" + k,
                    hc.getExponent(),
                    exponentDistribution);
        }
        return parameters;
    }

    public void setHydraulicControls(List<OneHydraulicControl> hydraulicControls) {
        hydraulicControlList.clear();
        controlSelectorModel.clear();
        for (OneHydraulicControl hc : hydraulicControls) {
            hc.addPropertyChangeListener("hydraulicControl", (e) -> {
                firePropertyChange("hydraulicControl", null, null);
            });
            controlSelectorModel.addElement(hc);
            hydraulicControlList.add(hc);
        }
    }

    public List<OneHydraulicControl> getHydraulicControls() {
        return this.hydraulicControlList;
    }

}

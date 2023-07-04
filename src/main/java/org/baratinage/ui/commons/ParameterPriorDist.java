package org.baratinage.ui.commons;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.Distribution.DISTRIB;
import org.baratinage.ui.component.NumberField;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

public class ParameterPriorDist {

    public String shortName;
    public JLabel nameLabel;
    public JComboBox<DISTRIB> distComboBox;
    public NumberField initialGuessField;
    public RowColPanel parametersInputsPanel;
    public List<NumberField> parameterPriorFields;

    public ParameterPriorDist(String shortName) {

        this.shortName = shortName;

        DefaultComboBoxModel<DISTRIB> distComboBoxModel = new DefaultComboBoxModel<>();
        for (DISTRIB d : DISTRIB.values()) {
            distComboBoxModel.addElement(d);
        }

        nameLabel = new JLabel();
        initialGuessField = new NumberField();
        initialGuessField.setPlaceholder(Lg.getText("ui", "initial_guess"));
        initialGuessField.addPropertyChangeListener("value", (e) -> {
            fireChangeListener();
        });

        parametersInputsPanel = new RowColPanel();
        parametersInputsPanel.setGap(5);
        parameterPriorFields = new ArrayList<>();

        distComboBox = new JComboBox<>(distComboBoxModel);
        distComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                DISTRIB d = (DISTRIB) value;
                String text = Lg.getText("ui", "dist_" + d.name);
                super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                return this;
            }
        });

        distComboBox.addActionListener((e) -> {
            Object o = distComboBoxModel.getSelectedItem();
            System.out.println(o);
            if (o != null) {
                DISTRIB currentDistribution = (DISTRIB) o;
                parametersInputsPanel.clear();
                parameterPriorFields.clear();
                for (String parameterName : currentDistribution.parameterNames) {
                    NumberField numberField = new NumberField();
                    numberField.setPlaceholder(Lg.getText("ui", parameterName));
                    numberField.addPropertyChangeListener("value", (v) -> {
                        fireChangeListener();
                    });
                    parameterPriorFields.add(numberField);
                    parametersInputsPanel.appendChild(numberField);

                }
                parametersInputsPanel.repaint();
            }
            fireChangeListener();
        });

        distComboBox.setSelectedItem(DISTRIB.GAUSSIAN);
    }

    public void set(DISTRIB dist, double initialGuess, double[] distParPriors) {
        distComboBox.setSelectedItem(dist);
        initialGuessField.setValue(initialGuess);
        initialGuessField.updateTextField();
        if (distParPriors.length != parameterPriorFields.size()) {
            System.out.println("Inconsistencies in distribution parameter! Aborting.");
            return;
        }
        for (int k = 0; k < distParPriors.length; k++) {
            NumberField p = parameterPriorFields.get(k);
            p.setValue(distParPriors[k]);
            p.updateTextField();
        }
    }

    public Parameter getParameter() {
        // if (distComboBox.getSelectedItem() == null) {
        // return null;
        // }
        double initialGuess = initialGuessField.getValue();

        // if (initialGuess == NumberField.NaN) {
        // return null;
        // }
        DISTRIB d = (DISTRIB) distComboBox.getSelectedItem();
        int n = d.parameterNames.length;
        double[] parameterValues = new double[n];
        for (int k = 0; k < n; k++) {
            parameterValues[k] = parameterPriorFields.get(k).getValue();
        }
        Distribution distribution = new Distribution(d, parameterValues);

        return new Parameter(shortName, initialGuess, distribution);
    }

    private List<ChangeListener> changeListenerers = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListenerers.add(l);
    }

    private void fireChangeListener() {
        for (ChangeListener l : changeListenerers) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

}

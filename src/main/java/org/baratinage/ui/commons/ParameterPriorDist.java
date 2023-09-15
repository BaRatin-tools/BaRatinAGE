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

// FIXME: refactor to be more Lg compatible and more memory efficient
public class ParameterPriorDist implements ChangeListener {

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
        Lg.register(initialGuessField, () -> {
            initialGuessField.setPlaceholder(Lg.text("initial_guess"));
        });
        initialGuessField.addChangeListener(this);

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
                String text = Lg.text("dist_" + d.name);
                super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                return this;
            }
        });

        distComboBox.addActionListener((e) -> {
            Object o = distComboBoxModel.getSelectedItem();
            if (o != null) {
                DISTRIB currentDistribution = (DISTRIB) o;
                parametersInputsPanel.clear();
                parameterPriorFields.clear();
                for (String parameterName : currentDistribution.parameterNames) {
                    NumberField numberField = new NumberField();
                    numberField.setPlaceholder(Lg.text(parameterName));
                    numberField.addChangeListener(this);
                    parameterPriorFields.add(numberField);
                    parametersInputsPanel.appendChild(numberField);

                }
                parametersInputsPanel.repaint();
            }
            fireChangeListeners();
        });

        distComboBox.setSelectedItem(DISTRIB.GAUSSIAN);
    }

    public void set(DISTRIB dist, double initialGuess, double[] distParPriors) {
        distComboBox.setSelectedItem(dist);
        initialGuessField.setValue(initialGuess);
        initialGuessField.updateTextField();
        if (distParPriors.length != parameterPriorFields.size()) {
            System.out.println("ParameterPriorDist: Inconsistencies in distribution parameter! Aborting.");
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

    @Override
    public void stateChanged(ChangeEvent arg0) {
        fireChangeListeners();
    }
}

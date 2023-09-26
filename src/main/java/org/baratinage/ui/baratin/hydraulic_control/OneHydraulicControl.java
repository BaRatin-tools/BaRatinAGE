package org.baratinage.ui.baratin.hydraulic_control;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.component.SimpleNumberField;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

public class OneHydraulicControl extends RowColPanel implements ChangeListener {

    private GridPanel parametersPanel;

    public final JLabel nameLabel = new JLabel("");

    public final SimpleNumberField activationStage;
    public final SimpleNumberField activationStageUncertainty;
    public final SimpleNumberField coefficient;
    public final SimpleNumberField coefficientUncertainty;
    public final SimpleNumberField exponent;
    public final SimpleNumberField exponentUncertainty;

    public final int controlNumber;

    public OneHydraulicControl(int controlNumber) {
        super(AXIS.COL);

        this.controlNumber = controlNumber;
        this.appendChild(nameLabel, 0, 5);
        this.appendChild(new JSeparator(), 0);

        parametersPanel = new GridPanel();
        parametersPanel.setAnchor(ANCHOR.N);
        parametersPanel.setGap(5);
        parametersPanel.setPadding(5);
        parametersPanel.setColWeight(1, 1);
        parametersPanel.setColWeight(2, 1);

        parametersPanel.insertChild(new JLabel("<html>Valeur <i>a priori</i></html> "), 1, 0);
        parametersPanel.insertChild(new JLabel("+/- (Incertitude élargie)"), 2, 0);

        JLabel activationStageLabel = new JLabel("k - Hauteur d'activation");
        Lg.register(activationStageLabel, "activate_stage_k", true);
        activationStage = new SimpleNumberField();
        activationStage.addChangeListener(this);
        activationStageUncertainty = new SimpleNumberField();
        activationStageUncertainty.addChangeListener(this);
        parametersPanel.insertChild(activationStageLabel, 0, 1);
        parametersPanel.insertChild(activationStage, 1, 1);
        parametersPanel.insertChild(activationStageUncertainty, 2, 1);

        JLabel coefficientLabel = new JLabel("a - Coefficient");
        Lg.register(coefficientLabel, "coefficient_a", true);
        coefficient = new SimpleNumberField();
        coefficient.addChangeListener(this);
        coefficientUncertainty = new SimpleNumberField();
        coefficientUncertainty.addChangeListener(this);
        parametersPanel.insertChild(coefficientLabel, 0, 2);
        parametersPanel.insertChild(coefficient, 1, 2);
        parametersPanel.insertChild(coefficientUncertainty, 2, 2);

        JLabel exponentLabel = new JLabel("c - Exposant");
        Lg.register(exponentLabel, "exponent_c", true);
        exponent = new SimpleNumberField();
        exponent.addChangeListener(this);
        exponentUncertainty = new SimpleNumberField();
        exponentUncertainty.addChangeListener(this);
        parametersPanel.insertChild(exponentLabel, 0, 3);
        parametersPanel.insertChild(exponent, 1, 3);
        parametersPanel.insertChild(exponentUncertainty, 2, 3);

        this.appendChild(parametersPanel);

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

}
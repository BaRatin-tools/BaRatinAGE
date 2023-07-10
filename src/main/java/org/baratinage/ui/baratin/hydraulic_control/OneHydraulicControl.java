package org.baratinage.ui.baratin.hydraulic_control;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.component.NumberField;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

public class OneHydraulicControl extends RowColPanel implements ChangeListener {

    private GridPanel parametersPanel;

    public final JLabel nameLabel = new JLabel("");

    private NumberField activationStage;
    private NumberField activationStageUncertainty;
    private NumberField coefficient;
    private NumberField coefficientUncertainty;
    private NumberField exponent;
    private NumberField exponentUncertainty;

    public OneHydraulicControl() {
        super(AXIS.COL);

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
        activationStage = new NumberField();
        activationStage.addChangeListener(this);
        activationStageUncertainty = new NumberField();
        activationStageUncertainty.addChangeListener(this);
        parametersPanel.insertChild(activationStageLabel, 0, 1);
        parametersPanel.insertChild(activationStage, 1, 1);
        parametersPanel.insertChild(activationStageUncertainty, 2, 1);

        JLabel coefficientLabel = new JLabel("a - Coefficient");
        coefficient = new NumberField();
        coefficient.addChangeListener(this);
        coefficientUncertainty = new NumberField();
        coefficientUncertainty.addChangeListener(this);
        parametersPanel.insertChild(coefficientLabel, 0, 2);
        parametersPanel.insertChild(coefficient, 1, 2);
        parametersPanel.insertChild(coefficientUncertainty, 2, 2);

        JLabel exponentLabel = new JLabel("c - Exposant");
        exponent = new NumberField();
        exponent.addChangeListener(this);
        exponentUncertainty = new NumberField();
        exponentUncertainty.addChangeListener(this);
        parametersPanel.insertChild(exponentLabel, 0, 3);
        parametersPanel.insertChild(exponent, 1, 3);
        parametersPanel.insertChild(exponentUncertainty, 2, 3);

        this.appendChild(parametersPanel);

    }

    public double getActivationStage() {
        return activationStage.getValue();
    }

    public double getActivationStageUncertainty() {
        return activationStageUncertainty.getValue();
    }

    public double getCoefficient() {
        return coefficient.getValue();
    }

    public double getCoefficientUncertainty() {
        return coefficientUncertainty.getValue();
    }

    public double getExponent() {
        return exponent.getValue();
    }

    public double getExponentUncertainty() {
        return exponentUncertainty.getValue();
    }

    public void setActivationStage(double value) {
        activationStage.setValue(value);
        activationStage.updateTextField();
    }

    public void setActivationStageUncertainty(double value) {
        activationStageUncertainty.setValue(value);
        activationStageUncertainty.updateTextField();
    }

    public void setCoefficient(double value) {
        coefficient.setValue(value);
        coefficient.updateTextField();
    }

    public void setCoefficientUncertainty(double value) {
        coefficientUncertainty.setValue(value);
        coefficientUncertainty.updateTextField();
    }

    public void setExponent(double value) {
        exponent.setValue(value);
        exponent.updateTextField();
    }

    public void setExponentUncertainty(double value) {
        exponentUncertainty.setValue(value);
        exponentUncertainty.updateTextField();
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
package org.baratinage.ui.baratin.hydraulic_control;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.ui.component.NumberField;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

public class OneHydraulicControl extends RowColPanel implements PropertyChangeListener {

    private GridPanel parametersPanel;
    private JLabel nameLabel;
    private NumberField activationStage;
    private NumberField activationStageUncertainty;
    private NumberField coefficient;
    private NumberField coefficientUncertainty;
    private NumberField exponent;
    private NumberField exponentUncertainty;

    public OneHydraulicControl() {
        super(AXIS.COL);

        nameLabel = new JLabel("");
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
        activationStage.addPropertyChangeListener("value", this);
        activationStageUncertainty = new NumberField();
        activationStageUncertainty.addPropertyChangeListener("value", this);
        parametersPanel.insertChild(activationStageLabel, 0, 1);
        parametersPanel.insertChild(activationStage, 1, 1);
        parametersPanel.insertChild(activationStageUncertainty, 2, 1);

        JLabel coefficientLabel = new JLabel("a - Coefficient");
        coefficient = new NumberField();
        coefficient.addPropertyChangeListener("value", this);
        coefficientUncertainty = new NumberField();
        coefficientUncertainty.addPropertyChangeListener("value", this);
        parametersPanel.insertChild(coefficientLabel, 0, 2);
        parametersPanel.insertChild(coefficient, 1, 2);
        parametersPanel.insertChild(coefficientUncertainty, 2, 2);

        JLabel exponentLabel = new JLabel("c - Exposant");
        exponent = new NumberField();
        exponent.addPropertyChangeListener("value", this);
        exponentUncertainty = new NumberField();
        exponentUncertainty.addPropertyChangeListener("value", this);
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

    public String getName() {
        return nameLabel == null ? "" : nameLabel.getText();
    }

    public void setName(String name) {
        this.nameLabel.setText(name);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange("hydraulicControl", null, null);
    }

}
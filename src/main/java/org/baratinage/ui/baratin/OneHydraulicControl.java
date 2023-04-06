package org.baratinage.ui.baratin;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.ui.component.NumberField;
import org.baratinage.ui.container.ChangingRowColPanel;
import org.baratinage.ui.container.GridPanel;
// import org.json.JSONObject;

public class OneHydraulicControl extends ChangingRowColPanel {

    // private String name;

    private GridPanel parametersPanel;
    private JLabel nameLabel;
    private NumberField activationStage;
    private NumberField activationStageUncertainty;
    private NumberField coefficient;
    private NumberField coefficientUncertainty;
    private NumberField exponent;
    private NumberField exponentUncertainty;

    private ToBeNotified toBeNotified = (ChangingRowColPanel panel) -> {
        notifyFollowers();
    };

    public OneHydraulicControl() {
        super(AXIS.COL);

        // this.name = "";

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
        // activationStage.addChangeListener(() -> fireChangeListeners());
        activationStage.addFollower(toBeNotified);
        activationStageUncertainty = new NumberField();
        // activationStageUncertainty.addChangeListener(() -> fireChangeListeners());
        activationStageUncertainty.addFollower(toBeNotified);
        parametersPanel.insertChild(activationStageLabel, 0, 1);
        parametersPanel.insertChild(activationStage, 1, 1);
        parametersPanel.insertChild(activationStageUncertainty, 2, 1);

        JLabel coefficientLabel = new JLabel("a - Coefficient");
        coefficient = new NumberField();
        // .addChangeListener(() -> fireChangeListeners());
        coefficient.addFollower(toBeNotified);
        coefficientUncertainty = new NumberField();
        // coefficientUncertainty.addChangeListener(() -> fireChangeListeners());
        coefficientUncertainty.addFollower(toBeNotified);
        parametersPanel.insertChild(coefficientLabel, 0, 2);
        parametersPanel.insertChild(coefficient, 1, 2);
        parametersPanel.insertChild(coefficientUncertainty, 2, 2);

        JLabel exponentLabel = new JLabel("c - Exposant");
        exponent = new NumberField();
        // exponent.addChangeListener(() -> fireChangeListeners());
        exponent.addFollower(toBeNotified);
        exponentUncertainty = new NumberField();
        // exponentUncertainty.addChangeListener(() -> fireChangeListeners());
        exponentUncertainty.addFollower(toBeNotified);
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
        return this.nameLabel.getText();
    }

    public void setName(String name) {
        this.nameLabel.setText(name);
    }

}
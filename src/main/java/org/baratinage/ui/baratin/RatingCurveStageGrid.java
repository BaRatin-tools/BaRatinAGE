package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

// import java.awt.Color;
// import javax.swing.plaf.basic.BasicBorders;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.baratinage.jbam.PredictionInput;
import org.baratinage.ui.bam.IPredictionData;

// import javax.swing.SwingUtilities;

// import org.baratinage.ui.component.DoubleNumberField;
// import org.baratinage.ui.component.IntegerNumberField;
import org.baratinage.ui.component.NumberField;
// import org.baratinage.ui.component.ToBeNotified;
import org.baratinage.ui.container.ChangingRowColPanel;
import org.baratinage.ui.container.GridPanel;

// public class RatingCurveStageGrid extends ChangingRowColPanel implements ToBeNotified {
public class RatingCurveStageGrid extends ChangingRowColPanel implements IPredictionData {

    private NumberField minStageField;
    private NumberField maxStageField;
    private NumberField nbrStepField;
    private NumberField valStepField;

    // private double[] stageGrid;
    // private double minValue;
    // private double maxValue;
    // private double stepValue;
    private StageGridConfig stageGridConfig;

    public record StageGridConfig(double min, double max, double step) {
    }

    private boolean isValueValid;

    public RatingCurveStageGrid() {
        super(AXIS.COL);

        GridPanel gridPanel = new GridPanel();
        setGap(5);
        setPadding(5);
        this.appendChild(new JLabel("Grille de hauteurs d'eau"), 0);
        this.appendChild(gridPanel, 1);

        minStageField = new NumberField();
        minStageField.addFollower(toBeNotified);

        maxStageField = new NumberField();
        maxStageField.addFollower(toBeNotified);

        nbrStepField = new NumberField(true);
        nbrStepField.addFollower(toBeNotified);
        nbrStepField.addValidator(n -> n > 0);

        valStepField = new NumberField();
        valStepField.addFollower(toBeNotified);
        valStepField.addValidator(n -> n > 0);

        gridPanel.setGap(5);
        gridPanel.setColWeight(0, 1);
        gridPanel.setColWeight(1, 1);
        gridPanel.setColWeight(2, 1);
        gridPanel.setColWeight(3, 1);
        gridPanel.insertChild(minStageField, 0, 1);
        gridPanel.insertChild(maxStageField, 1, 1);
        gridPanel.insertChild(nbrStepField, 2, 1);
        gridPanel.insertChild(valStepField, 3, 1);
        gridPanel.insertChild(new JLabel("Min"), 0, 0);
        gridPanel.insertChild(new JLabel("Max"), 1, 0);
        gridPanel.insertChild(new JLabel("N"), 2, 0);
        gridPanel.insertChild(new JLabel("Step"), 3, 0);

        isValueValid = false;
        stageGridConfig = new StageGridConfig(0, 0, 0);

        // updateApperanceBasedOnValueValidity();
    }

    ToBeNotified toBeNotified = (ChangingRowColPanel followed) -> {

        isValueValid = false;
        stageGridConfig = new StageGridConfig(0, 0, 0);
        if (minStageField.isValueValid() && maxStageField.isValueValid()) {
            double min = minStageField.getValue();
            double max = maxStageField.getValue();
            if (followed == nbrStepField) {
                int n = (int) nbrStepField.getValue();
                if (n > 0) {
                    double step = (max - min) / n;
                    isValueValid = true;
                    stageGridConfig = new StageGridConfig(min, max, step);
                    SwingUtilities.invokeLater(() -> {
                        valStepField.setValue(step, true);
                        valStepField.updateTextField();
                        // updateApperanceBasedOnValueValidity();
                    });

                }

            } else {
                if (valStepField.isValueValid()) {
                    double step = valStepField.getValue();
                    if (step > 0) {
                        int n = (int) Math.floor((max - min) / step);
                        nbrStepField.setValue(n, true);
                        nbrStepField.updateTextField();
                        if (max > min && step > 0 && step <= ((max - min) / 1)) {
                            isValueValid = true;
                            stageGridConfig = new StageGridConfig(min, max, step);
                        }
                    }
                }
            }
        }

        notifyFollowers();
    };

    public double[] getStageGrid() {
        if (!isValueValid()) {
            System.err.println("Invalid stage grid");
            return new double[0];
        }
        int n = (int) ((stageGridConfig.max - stageGridConfig.min) / stageGridConfig.step);
        double[] stageGrid = new double[n];
        for (int k = 0; k < n; k++) {
            stageGrid[k] = stageGridConfig.min + stageGridConfig.step * k;
        }
        return stageGrid;
    }

    public boolean isValueValid() {
        return isValueValid;
    }

    public StageGridConfig getStageGridConfig() {
        return stageGridConfig;
    }

    public void setStageGridConfig(StageGridConfig stagegridConfig) {
        this.stageGridConfig = new StageGridConfig(
                stageGridConfig.min(),
                stageGridConfig.max(),
                stageGridConfig.step());
    }

    @Override
    public PredictionInput[] getPredictionInputs() {
        List<double[]> stageGrid = new ArrayList<>();
        stageGrid.add(getStageGrid());
        PredictionInput predInput = new PredictionInput("stage_grid", stageGrid);
        return new PredictionInput[] { predInput };
    }

}

package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
// import javax.swing.SwingUtilities;

import org.baratinage.jbam.PredictionInput;
import org.baratinage.ui.bam.IPredictionData;

import org.baratinage.ui.component.NumberField;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

public class RatingCurveStageGrid extends RowColPanel implements IPredictionData {

    private NumberField minStageField;
    private NumberField maxStageField;
    private NumberField nbrStepField;
    private NumberField valStepField;

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
        minStageField.addPropertyChangeListener("value", (e) -> {
            // updateStepNbr();
            updateStepVal();
            updateStageGridConfig();
        });

        maxStageField = new NumberField();
        maxStageField.addPropertyChangeListener("value", (e) -> {
            updateStepVal();
            updateStageGridConfig();
        });

        nbrStepField = new NumberField(true);
        nbrStepField.addPropertyChangeListener("value", (e) -> {
            updateStepVal();
            updateStageGridConfig();

        });
        nbrStepField.addValidator(n -> n > 0);

        valStepField = new NumberField();
        valStepField.addPropertyChangeListener("value", (e) -> {
            updateStepNbr();
            updateStageGridConfig();
        });
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
    }

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

    private void updateStepVal() {
        if (!nbrStepField.isValueValid() || !minStageField.isValueValid() || !maxStageField.isValueValid()) {
            valStepField.setValue(NumberField.NaN, false);
            valStepField.updateTextField();
            return;
        }
        double n = nbrStepField.getValue();
        if (n <= 0) {
            valStepField.setValue(NumberField.NaN, false);
            valStepField.updateTextField();
            return;
        }
        double min = minStageField.getValue();
        double max = maxStageField.getValue();
        double step = (max - min) / n;
        valStepField.setValue(step, false);
        valStepField.updateTextField();
    }

    private void updateStepNbr() {
        if (!valStepField.isValueValid() || !minStageField.isValueValid() || !maxStageField.isValueValid()) {
            nbrStepField.setValue(NumberField.NaN, false);
            nbrStepField.updateTextField();
            return;
        }
        double step = valStepField.getValue();
        if (step <= 0) {
            nbrStepField.setValue(NumberField.NaN, false);
            nbrStepField.updateTextField();
            return;
        }
        double min = minStageField.getValue();
        double max = maxStageField.getValue();
        double n = (max - min) / step;
        nbrStepField.setValue(n, false);
        nbrStepField.updateTextField();
    }

    private void updateStageGridConfig() {
        double min = minStageField.getValue();
        double max = maxStageField.getValue();
        double n = nbrStepField.getValue();
        double step = valStepField.getValue();

        System.out.println("MIN  => " + min);
        System.out.println("MAX  => " + max);
        System.out.println("N    => " + n);
        System.out.println("STEP => " + step);

        // check validity and update config
        if (!nbrStepField.isValueValid() || !valStepField.isValueValid() || !minStageField.isValueValid()
                || !maxStageField.isValueValid()) {
            isValueValid = false;
            stageGridConfig = new StageGridConfig(0, 0, 0);
        } else {
            isValueValid = true;
            stageGridConfig = new StageGridConfig(min, max, step);
        }

    }

}

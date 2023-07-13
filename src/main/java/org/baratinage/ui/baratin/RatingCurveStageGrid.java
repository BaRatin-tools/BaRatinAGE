package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.ui.bam.IPredictionData;

import org.baratinage.ui.component.NumberField;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

// FIXME: should have a fromJSON / toJSON methods
public class RatingCurveStageGrid extends RowColPanel implements IPredictionData {

    private NumberField minStageField;
    private NumberField maxStageField;
    private NumberField nbrStepField;
    private NumberField valStepField;

    private StageGridConfig stageGridConfig;

    private record StageGridConfig(Double min, Double max, Double step) {
    }

    private boolean isValueValid;

    public RatingCurveStageGrid() {
        super(AXIS.COL);

        GridPanel gridPanel = new GridPanel();
        setGap(5);
        setPadding(5);
        JLabel stageGridLabel = new JLabel();
        Lg.register(stageGridLabel, "stage_grid");
        this.appendChild(stageGridLabel, 0);
        this.appendChild(gridPanel, 1);

        minStageField = new NumberField();
        minStageField.addChangeListener((e) -> {
            updateStepVal();
            updateStageGridConfig();
        });

        maxStageField = new NumberField();
        maxStageField.addChangeListener((e) -> {
            updateStepVal();
            updateStageGridConfig();
        });

        nbrStepField = new NumberField(true);
        nbrStepField.addChangeListener((e) -> {
            updateStepVal();
            updateStageGridConfig();

        });
        nbrStepField.addValidator(n -> n > 0);

        valStepField = new NumberField();
        valStepField.addChangeListener((e) -> {
            updateStepNbr();
            updateStageGridConfig();
        });
        valStepField.addValidator(n -> n > 0);

        JLabel minLabel = new JLabel();
        Lg.register(minLabel, "min");

        JLabel maxLabel = new JLabel();
        Lg.register(maxLabel, "max");

        JLabel nLabel = new JLabel();
        Lg.register(nLabel, "n");

        JLabel stepLabel = new JLabel();
        Lg.register(stepLabel, "step");

        gridPanel.setGap(5);
        gridPanel.setColWeight(1, 1);
        gridPanel.setColWeight(3, 1);
        gridPanel.setColWeight(5, 1);
        gridPanel.setColWeight(7, 1);
        gridPanel.insertChild(minStageField, 1, 0);
        gridPanel.insertChild(maxStageField, 3, 0);
        gridPanel.insertChild(nbrStepField, 5, 0);
        gridPanel.insertChild(valStepField, 7, 0);

        gridPanel.insertChild(minLabel, 0, 0);
        gridPanel.insertChild(maxLabel, 2, 0);
        gridPanel.insertChild(nLabel, 4, 0);
        gridPanel.insertChild(stepLabel, 6, 0);

        isValueValid = false;
        stageGridConfig = new StageGridConfig(null, null, null);

        nbrStepField.setValue(100);
        nbrStepField.updateTextField();
        updateStageGridConfig();
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

    public Double getMinValue() {
        return stageGridConfig.min;
    }

    public void setMinValue(double value) {
        minStageField.setValue(value);
        minStageField.updateTextField();
        updateStageGridConfig();
    }

    public Double getMaxValue() {
        return stageGridConfig.max;
    }

    public void setMaxValue(double value) {
        maxStageField.setValue(value);
        maxStageField.updateTextField();
        updateStageGridConfig();
    }

    public Double getStepValue() {
        return stageGridConfig.step;
    }

    public void setStepValue(double value) {
        valStepField.setValue(value);
        valStepField.updateTextField();
        updateStepNbr();
        updateStageGridConfig();
    }

    @Override
    public PredictionInput[] getPredictionInputs() {
        List<double[]> stageGrid = new ArrayList<>();
        stageGrid.add(getStageGrid());
        String predictionInputName = "stage_grid";
        PredictionInput predInput = new PredictionInput(
                predictionInputName,
                String.format(BamFilesHelpers.DATA_PREDICTION, predictionInputName),
                stageGrid);
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
        double step = valStepField.getValue();

        // check validity and update config
        if (!nbrStepField.isValueValid() || !valStepField.isValueValid() || !minStageField.isValueValid()
                || !maxStageField.isValueValid()) {
            isValueValid = false;
            stageGridConfig = new StageGridConfig(null, null, null);
        } else {
            isValueValid = true;
            stageGridConfig = new StageGridConfig(min, max, step);
        }

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

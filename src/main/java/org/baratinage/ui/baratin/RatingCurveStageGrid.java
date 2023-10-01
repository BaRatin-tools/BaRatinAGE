package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.PredictionInput;
import org.baratinage.ui.bam.IPredictionData;

import org.baratinage.ui.component.SimpleNumberField;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

// FIXME: should have a fromJSON / toJSON methods
public class RatingCurveStageGrid extends RowColPanel implements IPredictionData {

    private SimpleNumberField minStageField;
    private SimpleNumberField maxStageField;
    private SimpleNumberField nbrStepField;
    private SimpleNumberField valStepField;

    private StageGridConfig stageGridConfig;

    private record StageGridConfig(Double min, Double max, Double step) {
    }

    private boolean isValueValid;

    public RatingCurveStageGrid() {
        super(AXIS.COL, ALIGN.STRETCH);

        GridPanel gridPanel = new GridPanel();
        setGap(5);
        setPadding(5);

        JLabel stageGridLabel = new JLabel();
        appendChild(stageGridLabel, 0);
        appendChild(gridPanel, 1);

        minStageField = new SimpleNumberField();
        minStageField.addChangeListener((e) -> {
            updateStepVal();
            updateStageGridConfig();
        });

        maxStageField = new SimpleNumberField();
        maxStageField.addChangeListener((e) -> {
            updateStepVal();
            updateStageGridConfig();
        });

        nbrStepField = new SimpleNumberField(true);
        nbrStepField.addChangeListener((e) -> {
            updateStepVal();
            updateStageGridConfig();

        });

        valStepField = new SimpleNumberField();
        valStepField.addChangeListener((e) -> {
            updateStepNbr();
            updateStageGridConfig();
        });
        nbrStepField.addValidator((v) -> {
            Integer i = v.intValue();
            return i != null && i > 0;
        });
        valStepField.addValidator((v) -> {
            Double d = v.doubleValue();
            return d != null && d > 0;
        });

        JLabel minLabel = new JLabel();

        JLabel maxLabel = new JLabel();

        JLabel nLabel = new JLabel();

        JLabel stepLabel = new JLabel();

        Lg.register(this, () -> {
            stageGridLabel.setText(Lg.text("stage_grid"));
            maxLabel.setText(Lg.text("max"));
            minLabel.setText(Lg.text("min"));
            nLabel.setText(Lg.text("n"));
            stepLabel.setText(Lg.text("step"));
        });

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
        updateStageGridConfig();
    }

    public double[] getStageGrid() {
        if (!isValueValid()) {
            System.err.println("RatingCurveStageGrid Error: Invalid stage grid");
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
        updateStageGridConfig();
    }

    public Double getMaxValue() {
        return stageGridConfig.max;
    }

    public void setMaxValue(double value) {
        maxStageField.setValue(value);
        updateStageGridConfig();
    }

    public Double getStepValue() {
        return stageGridConfig.step;
    }

    public void setStepValue(double value) {
        valStepField.setValue(value);
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
                stageGrid);
        return new PredictionInput[] { predInput };
    }

    private void updateStepVal() {
        if (!nbrStepField.isValueValid() || !minStageField.isValueValid() || !maxStageField.isValueValid()) {
            valStepField.unsetValue();
            return;
        }
        double n = (double) nbrStepField.getIntegerValue();
        if (n <= 0) {
            valStepField.unsetValue();
            return;
        }
        double min = minStageField.getDoubleValue();
        double max = maxStageField.getDoubleValue();
        double step = (max - min) / n;
        valStepField.setValue(step);
    }

    private void updateStepNbr() {
        if (!valStepField.isValueValid() || !minStageField.isValueValid() ||
                !maxStageField.isValueValid()) {
            if (!valStepField.isValueValid() && !minStageField.isValueValid() &&
                    !maxStageField.isValueValid()) {
                // if all is unset, reset to default 100
                nbrStepField.setValue(100);
            } else {
                nbrStepField.unsetValue();
            }
            return;
        }
        double step = valStepField.getDoubleValue();
        if (step <= 0) {
            nbrStepField.unsetValue();
            return;
        }
        double min = minStageField.getDoubleValue();
        double max = maxStageField.getDoubleValue();
        double n = (max - min) / step;
        int nInt = (int) Math.round(n);
        nbrStepField.setValue(nInt);
    }

    private void updateStageGridConfig() {

        Double min = minStageField.getDoubleValue();
        Double max = maxStageField.getDoubleValue();
        Double step = valStepField.getDoubleValue();

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

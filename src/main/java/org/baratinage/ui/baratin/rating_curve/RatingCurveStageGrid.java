package org.baratinage.ui.baratin.rating_curve;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.PredictionInput;
import org.baratinage.ui.bam.IPredictionData;

import org.baratinage.ui.component.SimpleNumberField;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.translation.T;
import org.json.JSONObject;

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

        setCrossAxisAlign(ALIGN.CENTER);
        setGap(5);
        setPadding(5);

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

        JLabel stageGridLabel = new JLabel();

        appendChild(stageGridLabel, 0);
        appendChild(minStageField, 1);
        appendChild(maxStageField, 1);
        appendChild(nbrStepField, 1);
        appendChild(valStepField, 1);

        isValueValid = false;
        stageGridConfig = new StageGridConfig(null, null, null);

        nbrStepField.setValue(100);
        updateStageGridConfig();

        T.t(this, stageGridLabel, false, "stage_grid");
        T.t(this, () -> {
            minStageField.setInnerLabel(T.text("min"));
            maxStageField.setInnerLabel(T.text("max"));
            nbrStepField.setInnerLabel(T.text("n"));
            valStepField.setInnerLabel(T.text("step"));
        });
        T.updateHierarchy(this, minStageField);
        T.updateHierarchy(this, maxStageField);
        T.updateHierarchy(this, nbrStepField);
        T.updateHierarchy(this, valStepField);
    }

    public double[] getStageGrid() {
        if (!isValueValid()) {
            System.err.println("RatingCurveStageGrid Error: Invalid stage grid");
            return null;
        }
        int n = (int) ((stageGridConfig.max - stageGridConfig.min) / stageGridConfig.step);
        double[] stageGrid = new double[n];
        for (int k = 0; k < n; k++) {
            stageGrid[k] = stageGridConfig.min + stageGridConfig.step * k;
        }
        return stageGrid;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("min", stageGridConfig.min);
        json.put("max", stageGridConfig.max);
        json.put("step", stageGridConfig.step);
        return json;
    }

    public void fromJSON(JSONObject json) {
        minStageField.setValue(json.optDouble("min"));
        maxStageField.setValue(json.optDouble("max"));
        valStepField.setValue(json.optDouble("step"));
        updateStepNbr();
        updateStageGridConfig();
    }

    public boolean isValueValid() {
        return isValueValid;
    }

    @Override
    public PredictionInput[] getPredictionInputs() {
        List<double[]> stageGrid = new ArrayList<>();
        double[] hValues = getStageGrid();
        if (hValues == null) {
            return null;
        }
        stageGrid.add(hValues);
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

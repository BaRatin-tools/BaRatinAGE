package org.baratinage.ui.baratin;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.ICalibratedModel;
import org.baratinage.ui.bam.IPredictionData;
import org.baratinage.ui.bam.IPredictionExperiment;
import org.json.JSONObject;

public class Hydrograph extends BamItem implements IPredictionExperiment {

    public Hydrograph() {
        super(ITEM_TYPE.HYDROGRAPH);
    }

    @Override
    public PredictionConfig getPredictionConfig() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPredictionConfig'");
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public void parentHasChanged(BamItem parent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parentHasChanged'");
    }

    @Override
    public JSONObject toJSON() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toJSON'");
    }

    @Override
    public void fromJSON(JSONObject jsonString) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromJSON'");
    }

    @Override
    public void setCalibrationModel(ICalibratedModel cm) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCalibrationModel'");
    }

    @Override
    public void setPredictionData(IPredictionData pd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPredictionData'");
    }

}

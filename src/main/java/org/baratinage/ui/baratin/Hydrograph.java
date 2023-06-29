package org.baratinage.ui.baratin;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.ICalibratedModel;
import org.baratinage.ui.bam.IPredictionData;
import org.baratinage.ui.bam.IPredictionExperiment;
import org.json.JSONObject;

public class Hydrograph extends BamItem implements IPredictionExperiment {

    public Hydrograph(String uuid, BaratinProject project) {
        super(BamItemType.HYDROGRAPH, uuid, project);
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

    @Override
    public BamItem clone(String uuid) {
        Hydrograph cloned = new Hydrograph(uuid, (BaratinProject) PROJECT);
        cloned.fromFullJSON(toFullJSON());
        return cloned;
    }

}

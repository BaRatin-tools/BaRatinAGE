package org.baratinage.ui.baratin;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IPredictionExperiment;
import org.json.JSONObject;

public class Hydrograph extends BamItem implements IPredictionExperiment {

    public static final int TYPE = (int) Math.floor(Math.random() * Integer.MAX_VALUE);

    public Hydrograph() {
        super(TYPE);
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

}

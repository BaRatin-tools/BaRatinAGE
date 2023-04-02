package org.baratinage.ui.baratin;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IPredictionExperiment;

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
    public String toJsonString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toJsonString'");
    }

    @Override
    public void fromJsonString(String jsonString) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromJsonString'");
    }

    @Override
    public void setPredictionData(PredictionInput[] inputs, PredictionOutput[] outputs) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPredictionData'");
    }

}

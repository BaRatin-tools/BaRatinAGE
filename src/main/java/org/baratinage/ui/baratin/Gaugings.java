package org.baratinage.ui.baratin;

import org.baratinage.jbam.UncertainData;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.ICalibrationData;
import org.json.JSONObject;

public class Gaugings extends BamItem implements ICalibrationData {

    public static final int TYPE = (int) Math.floor(Math.random() * Integer.MAX_VALUE);

    public Gaugings() {
        super(TYPE);

    }

    @Override
    public UncertainData[] getInputs() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getInputs'");
    }

    @Override
    public UncertainData[] getOutputs() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOutputs'");
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
    public void fromJSON(JSONObject jsonString) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromJSON'");
    }

    @Override
    public JSONObject toJSON() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toJSON'");
    }

}

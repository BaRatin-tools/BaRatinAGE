package org.baratinage.ui.baratin;

import org.baratinage.jbam.PredictionInput;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IPredictionData;
import org.json.JSONObject;

public class Limnigraph extends BamItem implements IPredictionData {

    public Limnigraph(String uuid) {
        super(BamItemType.LIMNIGRAPH, uuid);
    }

    @Override
    public PredictionInput[] getPredictionInputs() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPredictionInputs'");
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
    public BamItem clone(String uuid) {
        Limnigraph cloned = new Limnigraph(uuid);
        cloned.fromFullJSON(toFullJSON());
        return cloned;
    }

}

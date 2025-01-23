package org.baratinage.ui.baratin.rc_compare;

import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.baratin.BaratinProject;

public class RatingCurveCompare extends BamItem {

    public RatingCurveCompare(String uuid, BaratinProject project) {
        super(BamItemType.COMPARING_RATING_CURVES, uuid, project);
    }

    @Override
    public BamConfig save(boolean writeFiles) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void load(BamConfig config) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'load'");
    }

}

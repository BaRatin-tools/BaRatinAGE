package org.baratinage.ui.baratin;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.ui.bam.ICalibratedModel;
import org.baratinage.ui.bam.IMcmc;
import org.baratinage.ui.container.RowColPanel;

public class PosteriorRatingCurve extends RowColPanel implements ICalibratedModel, IMcmc {

    public PosteriorRatingCurve() {
        super(AXIS.COL);
        RatingCurveStageGrid ratingCurveGrid = new RatingCurveStageGrid();
        appendChild(ratingCurveGrid, 0);
        appendChild(new JSeparator(JSeparator.VERTICAL), 0);
        appendChild(new JLabel("Posterior rating curve"), 1);

    }

    @Override
    public McmcConfig getMcmcConfig() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMcmcConfig'");
    }

    @Override
    public McmcCookingConfig getMcmcCookingConfig() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMcmcCookingConfig'");
    }

    @Override
    public CalibrationConfig getCalibrationConfig() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCalibrationConfig'");
    }

    @Override
    public boolean isCalibrated() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isCalibrated'");
    }

    @Override
    public CalibrationResult getCalibrationResults() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCalibrationResults'");
    }

}

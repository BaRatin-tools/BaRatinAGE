package org.baratinage.ui.bam;

import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationResult;

public interface ICalibratedModel {
    public CalibrationConfig getCalibrationConfig();

    public boolean isCalibrated();

    public CalibrationResult getCalibrationResults();

    // add public void runBaM()?

}
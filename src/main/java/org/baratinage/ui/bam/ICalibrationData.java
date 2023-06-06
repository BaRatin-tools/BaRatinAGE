package org.baratinage.ui.bam;

import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.UncertainData;

public interface ICalibrationData {
    public UncertainData[] getInputs();

    public UncertainData[] getOutputs();

    public CalibrationData getCalibrationData();
}
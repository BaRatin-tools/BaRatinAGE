package org.baratinage.ui.bam;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionResult;

public class PredExp {

    public final PredictionConfig predConfig;
    public final PredictionResult predResult;

    public PredExp(PredictionConfig predConfig) {
        this.predConfig = predConfig;
        this.predResult = null;
    }

    public PredExp(PredictionResult predResult) {
        this.predConfig = predResult.predictionConfig;
        this.predResult = predResult;
    }
}

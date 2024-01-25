package org.baratinage.ui.bam;

import java.util.List;
import java.util.stream.Stream;

import org.baratinage.jbam.PredictionConfig;

public class PredExpSet {

    public final List<double[]> extraData;
    public final PredExp[] predExperiments;

    public PredExpSet(List<double[]> extraData, PredExp... predExperiments) {
        this.extraData = extraData;
        this.predExperiments = predExperiments;
    }

    public PredExpSet(PredExp... predExperiments) {
        this.extraData = null;
        this.predExperiments = predExperiments;
    }

    public PredExpSet(List<double[]> extraData, List<PredExp> predExperiments) {
        this.extraData = extraData;
        this.predExperiments = predExperiments.toArray(new PredExp[predExperiments.size()]);
    }

    public PredExpSet(List<PredExp> predExperiments) {
        this.extraData = null;
        this.predExperiments = predExperiments.toArray(new PredExp[predExperiments.size()]);
    }

    public PredictionConfig[] getPredictionConfigs() {
        return Stream.of(predExperiments).map(predExp -> predExp.predConfig).toArray(PredictionConfig[]::new);
    }

}

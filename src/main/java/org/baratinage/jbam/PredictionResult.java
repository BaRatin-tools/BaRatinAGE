package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import org.baratinage.jbam.utils.BamFileNames;
import org.baratinage.jbam.utils.Read;

public class PredictionResult {

    public static final String[] ENV_COLUMN_NAMES = new String[] {
            "Median", "q2.5", "q97.5", "q16", "q84", "Mean", "Stdev"
    };

    // FIXME: should have a 'name' argument as well? So we don't need a hashmap key!
    public record PredictionOutputResult(List<double[]> env, List<double[]> spag) {
        @Override
        public String toString() {
            int nEnv = 0;
            if (env != null && env.size() > 0)
                nEnv = env.get(0).length;
            int nSpag = 0;
            if (spag != null && spag.size() > 0)
                nSpag = spag.size();
            return "Output results (envelops + samples/replications) with " + nEnv + " rows and " + nSpag + " samples.";
        }
    }

    // FIXME: using a HashMap may not be required since the outputs order
    // FIXME: are set during configuration
    // FIXME: (+ a list is more consistent with configuration approach)
    private HashMap<String, PredictionOutputResult> outputResults;
    private PredictionConfig predictionConfig; // FIXME: should this be here?
    private boolean isValid;

    public String getName() {
        return this.predictionConfig.getName();
    }

    public PredictionConfig getPredictionConfig() {
        return this.predictionConfig;
    }

    public HashMap<String, PredictionOutputResult> getOutputResults() {
        return this.outputResults;
    }

    public boolean getIsValid() {
        return this.isValid;
    }

    public PredictionResult(String workspace, PredictionConfig predictionConfig) {
        this.isValid = false;
        this.predictionConfig = predictionConfig;
        this.outputResults = new HashMap<>();
        PredictionOutput[] outputConfigs = predictionConfig.getPredictionOutputs();
        for (PredictionOutput outConfig : outputConfigs) {
            String outputName = outConfig.getName();
            String envFileName = String.format(BamFileNames.RESULTS_OUTPUT_ENV, predictionConfig.getName(), outputName);
            String spagFileName = String.format(BamFileNames.RESULTS_OUTPUT_SPAG, predictionConfig.getName(),
                    outputName);
            List<double[]> env = null;
            List<double[]> spag = null;
            try {
                env = Read.readMatrix(Path.of(workspace, envFileName).toString(), 1);

            } catch (IOException e) {
                System.err.println(e);
                // return;
            }

            try {
                spag = Read.readMatrix(Path.of(workspace, spagFileName).toString(), 0);
            } catch (IOException e) {
                System.err.println(e);
                // return;
            }

            this.outputResults.put(outputName, new PredictionOutputResult(env, spag));
        }
        this.isValid = true;
    }

    @Override
    public String toString() {
        String str = String.format("PredictionResults '%s' :\n",
                this.getName());
        str += " Outputs: \n";
        for (String key : this.outputResults.keySet()) {
            str += " > " + key + ":\n";
            str += this.outputResults.get(key).toString() + "\n";
        }
        return str;
    }
}

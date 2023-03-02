package bam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import bam.utils.ConfigFile;
import bam.utils.Read;

public class PredictionResult {

    private static final String[] ENV_COLUMN_NAMES = new String[] {
            "Median", "q2.5", "q97.5", "q16", "q84", "Mean", "Stdev"
    };

    private record PredictionOutputResult(List<double[]> env, List<double[]> spag) {
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

    private HashMap<String, PredictionOutputResult> outputResults;
    private String name;

    public PredictionResult(String workspace, PredictionConfig predictionConfig) {
        this.name = predictionConfig.getName();
        this.outputResults = new HashMap<>();
        PredictionOutput[] outputConfigs = predictionConfig.getPredictionOutputs();
        for (PredictionOutput outConfig : outputConfigs) {
            String outputName = outConfig.getName();
            String envFileName = String.format(ConfigFile.RESULTS_OUTPUT_ENV, predictionConfig.getName(), outputName);
            String spagFileName = String.format(ConfigFile.RESULTS_OUTPUT_SPAG, predictionConfig.getName(), outputName);
            List<double[]> env = null;
            List<double[]> spag = null;
            try {
                env = Read.readMatrix(Path.of(workspace, envFileName).toString(), 1);
                spag = Read.readMatrix(Path.of(workspace, spagFileName).toString(), 0);

            } catch (IOException e) {
                System.err.println(e);
            }

            this.outputResults.put(outputName, new PredictionOutputResult(env, spag));
        }
    }

    @Override
    public String toString() {
        String str = String.format("PredictionResults '%s' :\n",
                this.name);
        str += " Outputs: \n";
        for (String key : this.outputResults.keySet()) {
            str += " > " + key + ":\n";
            str += this.outputResults.get(key).toString() + "\n";
        }
        return str;
    }
}

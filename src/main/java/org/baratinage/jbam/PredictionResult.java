package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.utils.Read;
import org.baratinage.utils.ConsoleLogger;

public class PredictionResult {

    public static final String[] ENV_COLUMN_NAMES = new String[] {
            "Median", "q2.5", "q97.5", "q16", "q84", "Mean", "Stdev"
    };

    public record PredictionOutputResult(String name, List<double[]> env, List<double[]> spag) {
        @Override
        public String toString() {
            int nEnv = 0;
            if (env != null && env.size() > 0)
                nEnv = env.get(0).length;
            int nSpag = 0;
            if (spag != null && spag.size() > 0)
                nSpag = spag.size();
            return "Output results '" +
                    name +
                    "' (envelops + samples/replications) with " +
                    nEnv +
                    " rows and " +
                    nSpag +
                    " samples.";
        }

        public List<double[]> get95UncertaintyInterval() {
            return env.subList(1, 3);
        }
    }

    public final String name;
    public final List<PredictionOutputResult> outputResults;
    public final PredictionConfig predictionConfig;

    public PredictionResult(String workspace, PredictionConfig predictionConfig) {
        this.name = predictionConfig.name;
        this.predictionConfig = predictionConfig;
        this.outputResults = new ArrayList<>();
        PredictionOutput[] outputConfigs = predictionConfig.outputs;
        for (PredictionOutput outConfig : outputConfigs) {
            String outputName = outConfig.name;
            String envFileName = outConfig.envFileName;
            String spagFileName = outConfig.spagFileName;
            List<double[]> env = null;
            List<double[]> spag = null;
            try {
                env = Read.readMatrix(Path.of(workspace, envFileName).toString(), 1);
            } catch (IOException e) {
                ConsoleLogger.error("Failed to read envelop file '" + envFileName + "'");
            }

            try {
                spag = Read.readMatrix(Path.of(workspace, spagFileName).toString(), 0);
            } catch (IOException e) {
                ConsoleLogger.error("Failed to read spaghetti file '" + spagFileName + "'");
            }

            this.outputResults.add(new PredictionOutputResult(outputName, env, spag));
        }
    }

    @Override
    public String toString() {
        String str = String.format("PredictionResults '%s' :\n",
                name);
        str += " Outputs: \n";
        for (PredictionOutputResult por : this.outputResults) {
            str += " > " + por.toString() + "\n";
        }
        return str;
    }
}

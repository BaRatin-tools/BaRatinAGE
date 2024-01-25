package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;

public class PredictionResult {

    public static final String[] ENV_COLUMN_NAMES = new String[] {
            "Median", "q2.5", "q97.5", "q16", "q84", "Mean", "Stdev"
    };

    public record PredictionOutputResult(List<double[]> env, List<double[]> spag) {
        @Override
        public String toString() {
            int nEnv = 0;
            if (env != null && env.size() > 0)
                nEnv = env.get(0).length;
            int nSpag = 0;
            if (spag != null && spag.size() > 0)
                nSpag = spag.size();
            return "Output results '" +

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

    public final List<PredictionOutputResult> outputResults;
    public final PredictionConfig predictionConfig;

    public PredictionResult(String workspace, PredictionConfig predictionConfig) {
        this.predictionConfig = predictionConfig;
        this.outputResults = new ArrayList<>();
        PredictionOutput[] outputConfigs = predictionConfig.outputs;
        for (PredictionOutput outConfig : outputConfigs) {
            String envFileName = outConfig.envFileName;
            String spagFileName = outConfig.spagFileName;
            List<double[]> env = null;
            List<double[]> spag = null;
            try {
                // env = Read.readMatrix( Path.of(workspace, envFileName).toString(), 1);
                env = ReadFile.readMatrix(
                        Path.of(workspace, envFileName).toString(),
                        BamFilesHelpers.BAM_COLUMN_SEPARATOR,
                        1, Integer.MAX_VALUE,
                        BamFilesHelpers.BAM_IMPOSSIBLE_SIMULATION_CODE,
                        false, true);
            } catch (IOException e) {
                ConsoleLogger.warn("Failed to read envelop file '" + envFileName + "'");
            }

            try {
                spag = ReadFile.readMatrix(
                        Path.of(workspace, spagFileName).toString(),
                        BamFilesHelpers.BAM_COLUMN_SEPARATOR,
                        0, Integer.MAX_VALUE,
                        BamFilesHelpers.BAM_IMPOSSIBLE_SIMULATION_CODE,
                        false, true);
            } catch (IOException e) {
                ConsoleLogger.error("Failed to read spaghetti file '" + spagFileName + "'");
            }

            this.outputResults.add(new PredictionOutputResult(env, spag));
        }
    }

    @Override
    public String toString() {
        String str = String.format("PredictionResults '%s' :\n",
                predictionConfig.predictionConfigFileName);
        str += " Outputs: \n";
        for (PredictionOutputResult por : this.outputResults) {
            str += " > " + por.toString() + "\n";
        }
        return str;
    }
}

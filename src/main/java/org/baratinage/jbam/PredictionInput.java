package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.baratinage.jbam.utils.BamFilesHelpers;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.WriteFile;

public class PredictionInput {

    public final String fileName;
    public final List<double[]> dataColumns;
    public final int nObs;
    public final int nSpag;

    public PredictionInput(String fileName, List<double[]> dataColumns) {

        nSpag = dataColumns.size();

        if (nSpag == 0) {
            throw new IllegalArgumentException("In prediction input data, at least one vector/sample is expected");
        }

        int n = dataColumns.get(0).length;
        for (double[] col : dataColumns) {
            if (col.length != n) {
                throw new IllegalArgumentException(
                        "In prediction input data, all vectors/samples must have the same length!");
            }
        }

        nObs = n;

        for (int k = 0; k < nSpag; k++) {
            if (Misc.containsMissingValue(dataColumns.get(k))) {
                throw new IllegalArgumentException("In prediction input data, no mising values allowed.");
            }
        }

        this.dataColumns = dataColumns;
        this.fileName = fileName;
    }

    public String toDataFile(String workspace) {
        String dataFilePath = Path.of(workspace, fileName).toAbsolutePath().toString();
        try {
            WriteFile.writeMatrix(
                    dataFilePath,
                    dataColumns,
                    " ",
                    "", // no missing values allowed
                    null);
        } catch (IOException e) {
            ConsoleLogger.stackTrace(e);
        }
        return dataFilePath;
    }

    @Override
    public String toString() {
        return String.format(
                "Prediction input '%s' contains  %d observations and %d replications ",
                this.fileName, this.nObs, this.nSpag);
    }

    public static PredictionInput readPredictionInput(String workspace, String fileName) {

        String dataFilePath = Path.of(workspace, fileName).toString();
        List<double[]> data;
        try {
            data = ReadFile.readMatrix(
                    dataFilePath,
                    BamFilesHelpers.BAM_COLUMN_SEPARATOR,
                    0, Integer.MAX_VALUE,
                    "", // no missing values allowed
                    false, true);
        } catch (IOException e) {
            ConsoleLogger.error(
                    "PredictionInput Error: Failed to read input data file '" +
                            fileName + "'. Returning null!");
            return null;
        }
        return new PredictionInput(fileName, data);
    }
}

package org.baratinage.jbam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.baratinage.jbam.utils.BamFilesHelpers;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.WriteFile;

public class PredictionInput {
    public final String name;
    public final String fileName;
    public final String extFileName;
    public final List<double[]> dataColumns;
    public final List<double[]> extraData;
    public final int nObs;
    public final int nSpag;

    public PredictionInput(String name, List<double[]> dataColumns) {
        this(name, dataColumns, null);
    }

    public PredictionInput(String name, List<double[]> dataColumns, List<double[]> extraData) {

        nSpag = dataColumns.size();
        if (nSpag == 0) {
            throw new IllegalArgumentException("dataColumns must have at least one element!");
        }
        int n = dataColumns.get(0).length;
        for (double[] col : dataColumns) {
            if (col.length != n) {
                throw new IllegalArgumentException("All arrays of dataColumns must have the same length!");
            }
        }
        if (extraData != null) {
            for (double[] col : extraData) {
                if (col.length != n) {
                    throw new IllegalArgumentException(
                            "Lengths of columns in extraData must must match lengths of dataColumns columns");
                }
            }
        }
        nObs = n;

        this.name = name;
        this.dataColumns = dataColumns;
        this.extraData = extraData;

        this.fileName = String.format(BamFilesHelpers.DATA_PREDICTION, name);
        this.extFileName = String.format(BamFilesHelpers.DATA_PREDICTION_EXTRA, name);
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
        if (extraData != null) {
            String additionalDataFilePath = Path.of(workspace, extFileName).toAbsolutePath().toString();
            try {
                WriteFile.writeMatrix(
                        additionalDataFilePath,
                        extraData,
                        " ",
                        "",
                        null);
            } catch (IOException e) {
                ConsoleLogger.stackTrace(e);
            }
        }
        return dataFilePath;
    }

    @Override
    public String toString() {
        return String.format(
                "Prediction input '%s' contains  %d observations and %d replications ",
                this.name, this.nObs, this.nSpag);
    }

    public static PredictionInput readPredictionInput(String workspace, String name) {

        String fileName = String.format(BamFilesHelpers.DATA_PREDICTION, name);
        String extFileName = String.format(BamFilesHelpers.DATA_PREDICTION_EXTRA, name);

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
        File extraDataFile = Path.of(workspace, extFileName).toFile();
        List<double[]> extraData = null;
        if (extraDataFile.exists()) {
            try {
                extraData = ReadFile.readMatrix(
                        extraDataFile.getAbsolutePath().toString(),
                        BamFilesHelpers.BAM_COLUMN_SEPARATOR,
                        0, Integer.MAX_VALUE,
                        "",
                        false, true);
            } catch (IOException e) {
                ConsoleLogger.error(
                        "PredictionInput Error: Failed to read input extra data file '" +
                                extFileName + "'!");
            }
        }

        return new PredictionInput(name, data, extraData);
    }
}

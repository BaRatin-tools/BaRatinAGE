package org.baratinage.jbam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.Read;
import org.baratinage.jbam.utils.Write;

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

        this.nSpag = dataColumns.size();
        if (this.nSpag == 0) {
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
        this.nObs = n;
        this.name = name;
        this.dataColumns = dataColumns;
        this.extraData = extraData;

        this.fileName = String.format(BamFilesHelpers.DATA_PREDICTION, name);
        this.extFileName = String.format(BamFilesHelpers.DATA_PREDICTION_EXTRA, name);
    }

    public String toDataFile(String workspace) {
        String dataFilePath = Path.of(workspace, fileName).toAbsolutePath().toString();
        try {
            Write.writeMatrix(
                    dataFilePath,
                    dataColumns,
                    " ",
                    "-9999",
                    null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (extraData != null) {
            String additionalDataFilePath = Path.of(workspace, extFileName).toAbsolutePath().toString();
            try {
                Write.writeMatrix(
                        additionalDataFilePath,
                        extraData,
                        " ",
                        "-9999",
                        null);
            } catch (IOException e) {
                e.printStackTrace();
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
            data = Read.readMatrix(dataFilePath, 0);
        } catch (IOException e) {
            System.err.println(
                    "PredictionInput Error: Failed to read input data file '" +
                            fileName + "'. Returning null!");
            return null;
        }
        File extraDataFile = Path.of(workspace, extFileName).toFile();
        List<double[]> extraData = null;
        if (extraDataFile.exists()) {
            try {
                extraData = Read.readMatrix(extraDataFile.getAbsolutePath().toString(), 0);
            } catch (IOException e) {
                System.err.println(
                        "PredictionInput Error: Failed to read input extra data file '" +
                                extFileName + "'!");
            }
        }

        return new PredictionInput(name, data, extraData);
    }
}

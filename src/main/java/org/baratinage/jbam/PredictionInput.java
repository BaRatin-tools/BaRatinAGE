package org.baratinage.jbam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.baratinage.jbam.utils.Read;
import org.baratinage.jbam.utils.Write;

public class PredictionInput {
    public final String name;
    public final String fileName;
    public final List<double[]> dataColumns;
    public final int nObs;
    public final int nSpag;

    public PredictionInput(String name, String fileName, List<double[]> dataColumns) {
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
        this.nObs = n;
        this.name = name;
        this.fileName = fileName;
        this.dataColumns = dataColumns;
    }

    public String toDataFile(String workspace) {
        String dataFilePath = Path.of(workspace, fileName).toAbsolutePath().toString();
        try {
            Write.writeMatrix(
                    dataFilePath,
                    this.dataColumns,
                    " ",
                    "-9999",
                    null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataFilePath;
    }

    @Override
    public String toString() {
        return String.format(
                "Prediction input '%s' contains  %d observations and %d replications ",
                this.name, this.nObs, this.nSpag);
    }

    public static PredictionInput readPredictionInput(String dataFilePath) {
        String dataFileName = Path.of(dataFilePath).getFileName().toString();
        try {
            List<double[]> data = Read.readMatrix(dataFilePath, 0);
            return new PredictionInput(dataFileName, dataFileName, data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}

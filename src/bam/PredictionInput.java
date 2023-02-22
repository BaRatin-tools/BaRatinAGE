package bam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import bam.utils.ConfigFile;
import bam.utils.Write;

public class PredictionInput {
    private String name;
    private List<double[]> dataColumns;
    private int nObs;
    private int nSpag;

    public PredictionInput(String name, List<double[]> dataColumns) {
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
        this.dataColumns = dataColumns;
    }

    public String getDataFileName() {
        String fileName = String.format(ConfigFile.DATA_PREDICTION, this.name);
        return fileName;
    }

    public int getNobs() {
        return this.nObs;
    }

    public int getNspag() {
        return this.nSpag;
    }

    public void toDataFile(String workspace) {
        String fileName = this.getDataFileName();
        try {
            Write.writeMatrix(
                    Path.of(workspace, fileName).toString(), this.dataColumns, " ", "-9999", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return String.format(
                "Prediction input '%s' contains  %d observations and %d replications ",
                this.name, this.nObs, this.nSpag);
    }
}

package bam;

import java.io.IOException;
import java.nio.file.Path;
// import java.util.UUID;

import bam.exe.ConfigFile;
import utils.FileReadWrite;

public class PredictionInput {
    // private String id;
    private String name;
    private double[][] data;
    private int nObs;
    private int nSpag;

    public PredictionInput(String name, double[][] data) {
        // FIXME: should check input data here!
        this.nSpag = data.length;
        this.nObs = data[0].length;
        // this.id = UUID.randomUUID().toString(); // should change any time the data
        // changes
        this.name = name;
        this.data = data;
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

    public void writeDataFile(String workspace) {
        String fileName = this.getDataFileName();
        try {
            FileReadWrite.writeMatrix(
                    Path.of(workspace, fileName).toString(), this.data, " ", "-9999", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log() {
        int nRow = data.length;
        int nCol = 0;
        if (nRow > 0) {
            nCol = data[0].length;
        }
        System.out.println(
                String.format(
                        "Prediction input '%s' contains a %dx%d dataset ",
                        this.name, nRow, nCol));
    }
}

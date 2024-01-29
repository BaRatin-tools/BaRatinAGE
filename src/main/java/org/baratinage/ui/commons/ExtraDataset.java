package org.baratinage.ui.commons;

import java.util.List;

import org.baratinage.AppSetup;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.WriteFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class ExtraDataset {

    public final String id;
    public final List<double[]> data;

    public ExtraDataset(double[]... vectors) {
        data = new ArrayList<>();
        for (double[] v : vectors) {
            data.add(v);
        }
        id = Misc.getTimeStampedId();
    }

    public ExtraDataset(List<double[]> data) {
        this.data = data;
        id = Misc.getTimeStampedId();
    }

    public ExtraDataset(String id) {
        data = readData(id);
        this.id = id;
    }

    private static List<double[]> readData(String id) {
        List<double[]> data = new ArrayList<>();
        String fileName = id + ".txt";
        Path filePath = Path.of(AppSetup.PATH_APP_TEMP_DIR, fileName);
        try {
            data = ReadFile.readMatrixHorizontally(filePath.toString(), ";", "NA");
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
        return data;
    }

    public String writeData() {
        String fileName = id + ".txt";
        Path filePath = Path.of(AppSetup.PATH_APP_TEMP_DIR, fileName);
        String filePathString = filePath.toString();
        try {
            WriteFile.writeMatrixHorizontally(filePathString, data, ";", "NA");
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
        return filePathString;
    }

}

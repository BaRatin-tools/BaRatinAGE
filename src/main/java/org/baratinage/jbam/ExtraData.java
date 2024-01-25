package org.baratinage.jbam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.fs.WriteFile;

public class ExtraData {

    public static void writeExtraData(String filePath, List<double[]> data) {
        if (data == null) {
            ConsoleLogger.log("No extra data to write.");
            return;
        }
        try {
            WriteFile.writeMatrix(
                    filePath,
                    data,
                    ";",
                    "NA",
                    null);
        } catch (IOException e) {
            ConsoleLogger.stackTrace(e);
        }
    }

    public static List<double[]> readExtraData(String filePath) {
        File extraDataFile = Path.of(filePath).toFile();
        List<double[]> extraData = null;
        if (extraDataFile.exists()) {
            try {
                extraData = ReadFile.readMatrix(
                        extraDataFile.getAbsolutePath().toString(),
                        ";",
                        0,
                        Integer.MAX_VALUE,
                        "NA",
                        false,
                        true);
            } catch (IOException e) {
                ConsoleLogger.error("Failed to read extra data file '" + filePath + "'!");
            }
        }
        return extraData;
    }

}

package org.baratinage.project_importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.baratin.hydraulic_control.OneHydraulicControl;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.ReadFile;
import org.json.JSONArray;
import org.json.JSONObject;

public class BaratinageV2Builders {

    public static String findBamItemIdFromName(BaratinProject project, String name, BamItemType type) {
        for (BamItem item : project.BAM_ITEMS.filterByType(type)) {
            String itemName = item.bamItemNameField.getText();
            if (itemName.equals(name)) {
                return item.ID;
            }
        }
        return null;
    }

    public static File getSubFolder(File[] allSubFiles, String folderName) {
        for (File f : allSubFiles) {
            if (f.getName().equals(folderName)) {
                return f;
            }
        }
        return null;
    }

    public static int countNumberOfBamItems(File[] allSubFiles, String... folderNames) {
        int n = 0;
        for (File folder : allSubFiles) {
            for (String folderName : folderNames) {
                if (folder.getName().equals(folderName)) {
                    File[] subFolders = folder.listFiles();
                    if (subFolders != null) {
                        n += subFolders.length;
                    }
                    break;
                }
            }
        }
        return n;
    }

    public static String[] readConfigFileLines(File folder, String filename) {
        try {
            String[] results = ReadFile.getLines(Path.of(folder.getAbsolutePath(), filename).toString(),
                    Integer.MAX_VALUE, true);
            return results;
        } catch (IOException e) {
            ConsoleLogger.stackTrace(e);
            return null;
        }
    }

    public static List<double[]> readMatrixConfigFile(File folder, String filename) {
        try {
            List<double[]> matrix = ReadFile.readMatrix(
                    Path.of(folder.getAbsolutePath(), filename).toString(),
                    ";", 0, Integer.MAX_VALUE, "",
                    true, true);
            return matrix;
        } catch (IOException e) {
            ConsoleLogger.stackTrace(e);
            return null;
        }

    }

    public static void setBamItemNameAndDescription(BamItem bamItem, String[] properties) {
        bamItem.bamItemNameField.setText(properties[0]);
        bamItem.bamItemDescriptionField.setText(properties[1]);

    }

    public static String getBamItemNameFromFolder(File folder) {

        String[] properties = BaratinageV2Builders.readConfigFileLines(folder, "Properties.txt");

        if (properties.length < 1) {
            ConsoleLogger.error(
                    "Properties.txt file should contain at least one row (name,  ...).");
            return "";
        }

        return properties[0];
    }

    public static JSONObject buildBamItemParentConfig(BaratinProject project, String id) {
        JSONObject json = new JSONObject();

        if (id != null) {
            // > bamItemId
            json.put("bamItemId", id);
            // > bamItemBackup
            // *ignored*
        } else {
            // non blocking error
            ConsoleLogger.error("Cannot find parent item with id '" + id + "'...");
        }
        return json;
    }

    public static JSONObject buildStageGridConfigFromFile(File folder, boolean prior) {

        JSONObject json = new JSONObject();

        String expectedRows = "(min, max, step, nstep)";
        String fileName = "PosteriorOptions.txt";
        int offset = 0;
        if (prior) {
            expectedRows = "(nsim, min, max, step, nstep)";
            fileName = "PriorOptions.txt";
            offset = 1;
        }

        String[] options = readConfigFileLines(folder, fileName);

        if (options.length < (4 + offset)) {
            ConsoleLogger.error(fileName + " file should contain " + (5 + offset) + " rows (" + expectedRows + ")");
            return json; // empty JSONObject supported by RatingCurveStageGrid class
        }

        json.put("min", Double.parseDouble(options[0 + offset]));
        json.put("max", Double.parseDouble(options[1 + offset]));
        json.put("step", Double.parseDouble(options[2 + offset]));

        return json;
    }

    public static JSONObject buildOneHydraulicControlConfig(File folder, int controlIndex) {

        String[] controlConfigStrings = readConfigFileLines(folder, "" + controlIndex + "_Control.txt");

        Parameter[] parameters = buildParametersFromControlFile(controlConfigStrings);

        JSONObject json = new JSONObject();
        // > controlTypeIndex
        int controlTypeIndex = Integer.parseInt(controlConfigStrings[1]); // same indexing
        json.put("controlTypeIndex", controlTypeIndex);

        // > allControlOptions
        OneHydraulicControl ohc = new OneHydraulicControl(0);
        JSONArray allControlOptions = ohc.toJSON().getJSONArray("allControlOptions");
        json.put("allControlOptions", allControlOptions);

        JSONArray oneOfTheControlOptions = null;
        // source always starting with K, A, C
        if (controlTypeIndex == 0 ||
                controlTypeIndex == 1 ||
                controlTypeIndex == 2 ||
                controlTypeIndex == 3 ||
                controlTypeIndex == 4 ||
                controlTypeIndex == 5 ||
                controlTypeIndex == 6 ||
                controlTypeIndex == 7) {
            // weir rect: Cr, Bw, G, C, K => K, Cr, Bw, G, C
            // weir triangle: Ct, V, G, C, K => K, Ct, V, G, C
            // weir parabola: Cp, Bp, Hp, G, C, K => K, Cp, Bp, Hp, G, C
            // weir orifice: Cp, Bp, Hp, G, C, K => K, Cp, Bp, Hp, G, C
            // rect channel: K, A, C, Ks, Bw, S, C, K => K, Ks, Bw, S, C,
            // FIXME: should check all controls...

            oneOfTheControlOptions = buildPriorControlPanelConfig(
                    false,
                    parameters[7],
                    parameters[3],
                    parameters[4],
                    parameters[5],
                    parameters[6]);

        } else {
            ConsoleLogger.error("Unimplemented case");
        }

        if (oneOfTheControlOptions != null) {
            allControlOptions.put(controlTypeIndex, oneOfTheControlOptions); // modify only one of the item
        }

        // > kacControl
        JSONArray kacControl = buildPriorControlPanelConfig(false, parameters[0], parameters[1], parameters[2]);
        json.put("kacControl", kacControl);

        // > isKACmode
        json.put("isKACmode", false);

        return json;
    }

    public static JSONArray buildPriorControlPanelConfig(boolean lock, Parameter... parameters) {
        JSONArray config = new JSONArray();

        for (int k = 0; k < parameters.length; k++) {
            JSONObject parConfig = new JSONObject();
            config.put(k, parConfig);

            // > distributionBamName
            parConfig.put("distributionBamName", parameters[k].distribution.type.bamName);

            // > initialGuess
            parConfig.put("initialGuess", parameters[k].initalGuess);

            // > distributionParameters[]
            int nPars = parameters[k].distribution.parameterValues.length;
            JSONArray distributionParameters = new JSONArray();
            for (int i = 0; i < nPars; i++) {
                distributionParameters.put(i, parameters[k].distribution.parameterValues[i]);
            }
            parConfig.put("distributionParameters", distributionParameters);
            parConfig.put("isLocked", lock);

        }

        return config;
    }

    public static Parameter[] buildParametersFromControlFile(String[] controlConfigStrings) {

        int n = controlConfigStrings.length;
        int m = (n - 3) / 4;
        Parameter[] parameters = new Parameter[m];

        for (int k = 0; k < m; k++) {
            int offset = k > 2 ? 3 : 2;
            String name = controlConfigStrings[k * 4 + offset + 0];
            double initialGuess = Double.parseDouble(controlConfigStrings[k * 4 + 2 + 1]);
            // we assume only gaussian distribution
            // String distName = controlConfigStrings[k * 4 + offset + 2];
            String[] parStr = controlConfigStrings[k * 4 + offset + 3].split(",");
            double[] pars = new double[parStr.length];
            for (int i = 0; i < pars.length; i++) {
                pars[i] = Double.parseDouble(parStr[i]);
            }

            parameters[k] = new Parameter(name, initialGuess,
                    new Distribution(DistributionType.GAUSSIAN, pars));
        }
        return parameters;
    }
}

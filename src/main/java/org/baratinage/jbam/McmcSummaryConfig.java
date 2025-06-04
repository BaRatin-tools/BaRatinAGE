package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;

public class McmcSummaryConfig {
    public final String fileName;
    public final String summaryFileName;
    public final String DICFileName;
    public final String extendedMCMCFileName;

    public McmcSummaryConfig(String fileName, String summaryFileName, String DICFileName, String extendedMCMCFileName) {
        // https://github.com/BaM-tools/BaM/commit/1c22189af4cd00d0e8267c2b95e22c38ac56a517
        this.fileName = fileName;
        this.summaryFileName = summaryFileName;
        this.DICFileName = DICFileName;
        this.extendedMCMCFileName = extendedMCMCFileName;
    }

    public McmcSummaryConfig(String fileName, String outputFileName) {
        this(fileName, outputFileName, null, null);
    }

    public McmcSummaryConfig(boolean dic, boolean extended) {
        this(
                BamFilesHelpers.CONFIG_MCMC_SUMMARY,
                BamFilesHelpers.RESULTS_MCMC_SUMMARY,
                dic || extended ? BamFilesHelpers.RESULTS_MCMC_DIC : null, // need to include DIC if extended is true
                extended ? BamFilesHelpers.RESULTS_MCMC_EXTENDED : null);
    }

    public McmcSummaryConfig() {
        this(false, false);
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(summaryFileName, "Result file");
        if (DICFileName != null) {
            configFile.addItem(DICFileName, "DIC file");
        }
        if (extendedMCMCFileName != null) {
            configFile.addItem(extendedMCMCFileName, "Extended MCMC file ");
        }
        configFile.writeToFile(workspace, fileName);
    }

    @Override
    public String toString() {
        String str = "Config - McmcSummaryConfig: ";
        str += String.format("%s", this.summaryFileName);
        if (this.DICFileName != null) {
            str += String.format(", %s", this.DICFileName);
        }
        if (this.extendedMCMCFileName != null) {
            str += String.format(", %s", this.extendedMCMCFileName);
        }
        str += ".\n";
        return str;
    }

    static public McmcSummaryConfig readMcmcSummaryConfig(String workspace, String mcmcSummaryConfigFileName) {
        ConfigFile configFile = ConfigFile.readConfigFile(workspace, mcmcSummaryConfigFileName);
        String outputFileName = configFile.getString(0);
        String DICFileName = null;
        String extendedFileName = null;
        if (configFile.getNumberOfItems() > 1) {
            DICFileName = configFile.getString(1);
        }
        if (configFile.getNumberOfItems() > 2) {
            extendedFileName = configFile.getString(2);
        }
        return new McmcSummaryConfig(mcmcSummaryConfigFileName, outputFileName, DICFileName, extendedFileName);
    }
}

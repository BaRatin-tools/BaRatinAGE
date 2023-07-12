package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;

public class McmcConfig {
    private String outputFileName;
    private int nAdapt;
    private int nCycle;
    private double minMoveRate;
    private double maxMoveRate;
    private double downMult;
    private double upMult;
    // private int mode;
    // private double multFactor;
    // private double[] rcMultFactor;
    // private double[] rembMultFactor;

    public McmcConfig() {

        this(
                BamFilesHelpers.RESULTS_MCMC,
                100,
                100,
                0.1,
                0.5,
                0.9,
                1.1);

    }

    public McmcConfig(
            String outputFileName,
            int nAdapt,
            int nCycle,
            double minMoveRate,
            double maxMoveRate,
            double downMult,
            double upMult) {
        this.outputFileName = outputFileName;
        this.nAdapt = nAdapt;
        this.nCycle = nCycle;
        this.minMoveRate = minMoveRate;
        this.maxMoveRate = maxMoveRate;
        this.downMult = downMult;
        this.upMult = upMult;
        // this.mode = 0;
        // this.multFactor = 0.1;
        // this.rcMultFactor = new double[]{0.1, 0.1, 0.1};
        // this.remnMultFactor = new double[]{0.1, 0.1};
    }

    public int numberOfMcmcSamples() {
        return this.nAdapt * this.nCycle;
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.outputFileName, "File name", true);
        configFile.addItem(this.nAdapt, "NAdapt");
        configFile.addItem(this.nCycle, "Ncycles");
        configFile.addItem(this.minMoveRate, "MinMoveRate");
        configFile.addItem(this.maxMoveRate, "MaxMoveRate");
        configFile.addItem(this.downMult, "DownMult");
        configFile.addItem(this.upMult, "UpMult");
        configFile.addItem(0, "Mode for setting the initial Std of the jump distribution");
        configFile.addItem("****	  DEFINITION OF INITIAL JUMP STD	 **** ", "Cosmetics");
        configFile.addItem(0.1, "MultFactor in default mode (ignored in manual mode)");
        configFile.addItem(new double[] { 0.1, 0.1, 0.1 }, "RC MultFactor in manual mode (ignored in auto mode)");
        configFile.addItem(new double[] { 0.1, 0.1, 0.1 }, "Remnant MultFactor in manual mode (ignored in auto mode)");
        configFile.writeToFile(workspace, BamFilesHelpers.CONFIG_MCMC);
    }

    @Override
    public String toString() {
        String str = "Config - McmcConfig: ";
        str += String.format("%s; ", this.outputFileName);
        str += String.format("%d; ", this.nAdapt);
        str += String.format("%d; ", this.nCycle);
        str += String.format("%f; ", this.minMoveRate);
        str += String.format("%f; ", this.maxMoveRate);
        str += String.format("%f; ", this.downMult);
        str += String.format("%f.\n", this.upMult);
        return str;
    }

    static public McmcConfig readMcmc(String workspace, String mcmcConfigFileName) {
        ConfigFile configFile = ConfigFile.readConfigFile(workspace, mcmcConfigFileName);
        String outputFileName = configFile.getString(0);
        int nAdapt = configFile.getInt(1);
        int nCycle = configFile.getInt(2);
        double minMoveRate = configFile.getDouble(3);
        double maxMoveRate = configFile.getDouble(4);
        double downMult = configFile.getDouble(5);
        double upMult = configFile.getDouble(6);

        return new McmcConfig(outputFileName, nAdapt, nCycle, minMoveRate, maxMoveRate, downMult, upMult);
    }
}

package bam;

import bam.exe.ConfigFile;

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
        this.outputFileName = ConfigFile.RESULTS_MCMC;
        this.nAdapt = 100;
        this.nCycle = 100;
        this.minMoveRate = 0.1;
        this.maxMoveRate = 0.5;
        this.downMult = 0.9;
        this.upMult = 1.1;
        // this.mode = 0;
        // this.multFactor = 0.1;
        // this.rcMultFactor = new double[]{0.1, 0.1, 0.1};
        // this.remnMultFactor = new double[]{0.1, 0.1};
    }

    public void writeConfig(String workspace) {
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
        configFile.writeToFile(workspace, ConfigFile.CONFIG_MCMC);
    }

    public void log() {
        System.out.print("Config - McmcConfig: ");
        System.out.print(String.format("%s; ", this.outputFileName));
        System.out.print(String.format("%d; ", this.nAdapt));
        System.out.print(String.format("%d; ", this.nCycle));
        System.out.print(String.format("%f; ", this.minMoveRate));
        System.out.print(String.format("%f; ", this.maxMoveRate));
        System.out.print(String.format("%f; ", this.downMult));
        System.out.print(String.format("%f; ", this.upMult));
        System.out.print(".\n");
    }
}

package controleur;

import java.io.IOException;

import Utils.Defaults;

public class Prediction {
    private String configFileName;
    private String name;
    private InputVarConfig[] inputVarConfig;
    private OutputVarConfig[] outputVarConfig;
    // private int nReplication;
    // private boolean includeInputNonSystematicErrors;
    // private boolean includeInputSystematicErrors;
    private boolean includeParametricUncertainty;

    public Prediction(
            String name,
            InputVarConfig[] inputVarConfig,
            OutputVarConfig[] outputVarConfig,
            boolean includeParametricUncertainty) {
        this.name = name;
        this.configFileName = String.format("%s%s%s", Defaults.configPredSuffix, name, ".txt");
        this.inputVarConfig = inputVarConfig;
        this.outputVarConfig = outputVarConfig;
        this.includeParametricUncertainty = includeParametricUncertainty;
    }

    public Prediction(String name, InputVarConfig[] inputVarConfig,
            OutputVarConfig[] outputVarConfig) {
        this(name, inputVarConfig, outputVarConfig, false);
    }

    public String getName() {
        return name;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public InputVarConfig[] getInputVariables() {
        return inputVarConfig;
    }

    public OutputVarConfig[] getOutputVariables() {
        return outputVarConfig;
    }

    public boolean includeParametricUncertainty() {
        return includeParametricUncertainty;
    }

    public void writeConfigFile(String workspace) throws IOException {

        int nInput = inputVarConfig.length;
        int nOutput = outputVarConfig.length;

        int nObs = inputVarConfig[0].getNumberOfObs();

        String[] inputFilePaths = new String[nInput];
        int[] nSpag = new int[nInput];
        for (int k = 0; k < nInput; k++) {
            if (inputVarConfig[k].getNumberOfObs() != nObs) {
                // FIXME: these check should be better formalized
                // FIXME: with different levels (e.g. prediction level and whole config levels)
                System.err.println("Number of observations must be equal accross all input variables!");
            }
            nSpag[k] = inputVarConfig[k].getNumberOfSpag();
            inputFilePaths[k] = inputVarConfig[k].getFilePath();
        }

        String[] spagOutputFilenames = new String[nOutput];
        String[] envOutputFilenames = new String[nOutput];
        boolean[] propagateRemnantErrors = new boolean[nOutput];
        boolean[] transposeSpagMatrix = new boolean[nOutput];
        boolean[] createEnvelopFiles = new boolean[nOutput];
        for (int k = 0; k < nOutput; k++) {
            String outName = String.format("%s_%s", name, outputVarConfig[k].getName());
            // FIXME: should not depend on Defaults!
            outputVarConfig[k].setSpagFilePath(String.format("%s%s%s", Defaults.resultsSpagPrefix, outName, ".txt"));
            outputVarConfig[k].setEnvFilePath(String.format("%s%s%s", Defaults.resultsEnvPrefix, outName, ".txt"));

            spagOutputFilenames[k] = outputVarConfig[k].getSpagFilePath();
            envOutputFilenames[k] = outputVarConfig[k].getEnvFilePath();
            propagateRemnantErrors[k] = outputVarConfig[k].shouldPropagateRemnantUncertainty();
            transposeSpagMatrix[k] = outputVarConfig[k].shouldTranspose();
            createEnvelopFiles[k] = outputVarConfig[k].shouldCreateEnvelopFile();
        }

        BaMconfigFile configFile = new BaMconfigFile(workspace, configFileName);
        configFile.addItem(inputFilePaths, "Files containing spaghettis for each input variable (size nX)", true);
        configFile.addItem(nObs, "Nobs, number of observations per spaghetti (common to all files!)");
        configFile.addItem(nSpag, "Nspag, number of spaghettis for each input variable (size nX)");
        configFile.addItem(includeParametricUncertainty(), "Propagate parametric uncertainty?");
        configFile.addItem(propagateRemnantErrors, "Propagate remnant uncertainty for each output variable? (size nY)");
        // FIXME: should handle prior propagation!
        configFile.addItem(-1,
                "Nsim[prior]. If <=0: posterior sampling (nsim is given by mcmc sample); if >0: sample nsim replicates from prior distribution");
        configFile.addItem(spagOutputFilenames, "Files containing spaghettis for each output variable (size nY)");
        configFile.addItem(transposeSpagMatrix,
                "Post-processing: transpose spag file (so that each column is a spaghetti)? (size nY)");
        configFile.addItem(createEnvelopFiles, "Post-processing: create envelops? (size nY)");
        configFile.addItem(envOutputFilenames, "Post-processing: name of envelop files (size nY)");
        configFile.addItem(true, "Print progress in console during computations?");
        // FIXME: not shure how to handle this case
        configFile.addItem(false, "Do state prediction? (size nState)");
        configFile.writeToFile();

    }

    // public void readEnvResults(String workspace) {
    // int nOutput = outputVarConfig.length;
    // Double[][][] result;
    // for (int k = 0; k < nOutput; k++) {
    // String outName = String.format("%s_%s", name, outputVarConfig[k].getName());
    // // String spagFileName = String.format("%s%s%s", Defaults.resultsSpagPrefix,
    // outName, ".spag");
    // if (!outputVarConfig[k].shouldCreateEnvelopFile()) {
    // String envFileName = String.format("%s%s%s", Defaults.resultsEnvPrefix,
    // outName, ".env");
    // // result[k]
    // }

    // outputVarConfig[k].shouldCreateEnvelopFile();
    // }
    // }

}

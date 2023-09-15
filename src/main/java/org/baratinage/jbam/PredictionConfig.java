package org.baratinage.jbam;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;

public class PredictionConfig {
    public final String name;
    public final String predictionConfigFileName;
    public final PredictionInput[] inputs;
    public final PredictionOutput[] outputs;
    public final PredictionOutput[] states; // FIXME: should state have its own class?
    public final boolean propagateParametricUncertainty;
    public final boolean printProgress;
    public final int nPriorReplicates;

    public PredictionConfig(
            String name,
            String predictionConfigFileName,
            PredictionInput[] inputs,
            PredictionOutput[] outputs,
            PredictionOutput[] states,
            boolean propagateParametricUncertainty,
            boolean printProgress,
            int nPriorReplicates) {

        this.name = name;
        this.predictionConfigFileName = predictionConfigFileName;
        this.inputs = inputs;
        this.outputs = outputs;
        this.states = states;
        this.propagateParametricUncertainty = propagateParametricUncertainty;
        this.printProgress = printProgress;
        this.nPriorReplicates = nPriorReplicates;
    }

    public void toFiles(String workspace) {
        int n = inputs.length;
        String[] inputFilePaths = new String[n];
        int nObs = 0; // FIXME: is zero data allowed?
        int[] nSpag = new int[n];
        for (int k = 0; k < n; k++) {
            inputFilePaths[k] = inputs[k].toDataFile(workspace);
            int tmpNobs = inputs[k].nObs;
            if (nObs == 0) {
                nObs = tmpNobs;
            } else {
                if (nObs != tmpNobs) {
                    System.err
                            .println("Number of observations should be equal accross all prediction input variables!");
                    if (nObs < tmpNobs) {
                        nObs = tmpNobs;
                    }
                }
            }
            nSpag[k] = inputs[k].nSpag;

        }

        n = outputs.length;
        String[] spagOutputFileName = new String[n];
        String[] envOutputFileName = new String[n];
        boolean[] includeOutputStructuralError = new boolean[n];
        boolean[] transposeOutput = new boolean[n];
        boolean[] createOutputEnvelop = new boolean[n];
        for (int k = 0; k < n; k++) {
            spagOutputFileName[k] = outputs[k].spagFileName;
            envOutputFileName[k] = outputs[k].envFileName;
            includeOutputStructuralError[k] = outputs[k].structuralError;
            transposeOutput[k] = outputs[k].transpose;
            createOutputEnvelop[k] = outputs[k].createEnvelop;
        }

        n = states.length;
        boolean[] doStatePredictions = new boolean[n];
        String[] spagStateFileName = new String[n];
        String[] envStateFileName = new String[n];
        boolean[] transposeState = new boolean[n];
        boolean[] createStateEnvelop = new boolean[n];
        for (int k = 0; k < n; k++) {
            doStatePredictions[k] = true;
            spagStateFileName[k] = states[k].spagFileName;
            envStateFileName[k] = states[k].envFileName;
            transposeState[k] = states[k].transpose;
            createStateEnvelop[k] = states[k].createEnvelop;
        }

        ConfigFile configFile = new ConfigFile();
        configFile.addItem(inputFilePaths, "Files containing spaghettis for each input variable (size nX)", true);
        configFile.addItem(nObs, "Nobs, number of observations per spaghetti (common to all files!)");
        configFile.addItem(nSpag, "Nspag, number of spaghettis for each input variable (size nX)");
        configFile.addItem(propagateParametricUncertainty, "Propagate parametric uncertainty?");
        configFile.addItem(includeOutputStructuralError,
                "Propagate remnant uncertainty for each output variable? (size nY)");
        configFile.addItem(nPriorReplicates,
                "Nsim[prior]. If <=0: posterior sampling (nsim is given by mcmc sample); if >0: sample nsim replicates from prior distribution");
        configFile.addItem(spagOutputFileName, "Files containing spaghettis for each output variable (size nY)");
        configFile.addItem(transposeOutput,
                "Post-processing: transpose spag file (so that each column is a spaghetti)? (size nY)");
        configFile.addItem(createOutputEnvelop, "Post-processing: create envelops? (size nY)");
        configFile.addItem(envOutputFileName, "Post-processing: name of envelop files (size nY)");
        configFile.addItem(printProgress, "Print progress in console during computations?");
        if (states.length == 0) {
            configFile.addItem(false, "Do state prediction? (size nState)");
        } else {
            configFile.addItem(doStatePredictions, "Do state prediction? (size nState)");
            configFile.addItem(spagStateFileName, "Files containing spaghettis for each state variable (size nState)");
            configFile.addItem(transposeState,
                    " Post-processing: transpose spag file (so that each column is a spaghetti)? (size nState)");
            configFile.addItem(createStateEnvelop, "Post-processing: create envelops? (size nState)");
            configFile.addItem(envStateFileName, "Post-processing: name of envelop files (size nState)");
        }
        configFile.writeToFile(workspace, predictionConfigFileName);
    }

    @Override
    public String toString() {
        String str = String.format("PredictionConfig '%s' (%b, %b, %d):\n",
                predictionConfigFileName,
                propagateParametricUncertainty,
                printProgress,
                nPriorReplicates);
        str += " Inputs: \n";
        for (PredictionInput i : inputs) {
            str += i.toString() + "\n";
        }
        str += " Outputs: \n";
        for (PredictionOutput o : outputs) {
            str += o.toString();
        }
        return str;
    }

    public static PredictionConfig readPredictionConfig(String workspace, String predictionFileName) {
        ConfigFile configFile = ConfigFile.readConfigFile(
                workspace,
                predictionFileName);

        String[] inputFilePaths = configFile.getStringArray(0);
        int nInput = inputFilePaths.length;

        PredictionInput[] inputs = new PredictionInput[nInput];

        for (int k = 0; k < nInput; k++) {
            // FIXME: for simplicity sake, assuming that data file is in workspace folder!
            String dataFilePath = BamFilesHelpers.findDataFilePath(inputFilePaths[k], workspace);
            if (dataFilePath == null) {
                System.err.println(
                        "PredictionConfig Error: Cannot find prediction data file '" + inputFilePaths[k] + "'!");
                return null;
            }
            inputs[k] = PredictionInput.readPredictionInput(dataFilePath);
        }

        boolean[] propagateStructuralErrors = configFile.getBooleanArray(4);
        boolean[] transposeRes = configFile.getBooleanArray(7);
        boolean[] createEnvelops = configFile.getBooleanArray(8);
        String[] outSpagFileNames = configFile.getStringArray(6);
        String[] outEnvFileNames = configFile.getStringArray(9);
        int nOutput = outSpagFileNames.length;
        if (propagateStructuralErrors.length != nOutput ||
                transposeRes.length != nOutput ||
                createEnvelops.length != nOutput) {
            System.err.println(
                    "PredictionConfig Error: Number of outputs is inconsistant in config file '" +
                            predictionFileName
                            + "'! ");
            return null;
        }

        PredictionOutput[] predictionOutputs = new PredictionOutput[nOutput];
        for (int k = 0; k < nOutput; k++) {
            String outputName = "Output_" + (k + 1);
            predictionOutputs[k] = new PredictionOutput(
                    outputName,
                    outSpagFileNames[k],
                    outEnvFileNames[k],
                    propagateStructuralErrors[k],
                    transposeRes[k],
                    createEnvelops[k]);
        }

        PredictionOutput[] predictionState = new PredictionOutput[] {};

        boolean[] doStatePredictions = configFile.getBooleanArray(11);
        int nState = doStatePredictions.length;
        if (!(nState == 1 && !doStatePredictions[0])) {
            String[] stateSpagFileNames = configFile.getStringArray(12);
            String[] stateEnvFileNames = configFile.getStringArray(15);
            boolean[] stateTransposeRes = configFile.getBooleanArray(13);
            boolean[] stateCreateEnvelops = configFile.getBooleanArray(14);

            if (stateSpagFileNames.length != nState ||
                    stateTransposeRes.length != nState ||
                    stateCreateEnvelops.length != nState) {
                System.err.println(
                        "PredictionConfig Error: Number of states is inconsistant in config file '" +
                                predictionFileName
                                + "'! ");
                return null;
            }

            predictionState = new PredictionOutput[nState];
            for (int k = 0; k < nState; k++) {
                String stateName = "State_" + (k + 1);
                predictionState[k] = new PredictionOutput(
                        stateName,
                        stateSpagFileNames[k],
                        stateEnvFileNames[k],
                        false,
                        stateTransposeRes[k],
                        stateCreateEnvelops[k]);
            }

        }

        boolean propagateParametricUncertainty = configFile.getBoolean(3);
        int nPriorReplicates = configFile.getInt(5);
        boolean printProgress = configFile.getBoolean(10);

        return new PredictionConfig(
                predictionFileName,
                predictionFileName,
                inputs,
                predictionOutputs,
                predictionOutputs,
                propagateParametricUncertainty,
                printProgress,
                nPriorReplicates);
    }
}

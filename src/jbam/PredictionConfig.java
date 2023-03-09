package jbam;

import java.nio.file.Path;

import jbam.utils.ConfigFile;

public class PredictionConfig {
    private String name;
    private PredictionInput[] inputs;
    private PredictionOutput[] outputs;
    private PredictionOutput[] states; // FIXME: should state have its own class?
    private boolean propagateParametricUncertainty;
    private boolean printProgress;
    private int nPriorReplicates;

    public PredictionConfig(
            String name,
            PredictionInput[] inputs,
            PredictionOutput[] outputs,
            PredictionOutput[] states,
            boolean propagateParametricUncertainty,
            boolean printProgress,
            int nPriorReplicates) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.states = states;
        this.propagateParametricUncertainty = propagateParametricUncertainty;
        this.printProgress = printProgress;
        this.nPriorReplicates = nPriorReplicates;
    }

    public String getName() {
        return this.name;
    }

    public String getConfigFileName() {
        return String.format(ConfigFile.CONFIG_PREDICTION, this.name);
    }

    public PredictionOutput[] getPredictionOutputs() {
        return this.outputs;
    }

    public PredictionInput[] getPredictionInputs() {
        return this.inputs;
    }

    public void toFiles(String workspace) {
        int n = this.inputs.length;
        String[] inputFilePaths = new String[n];
        int nObs = 0; // FIXME: is zero data allowed?
        int[] nSpag = new int[n];
        for (int k = 0; k < n; k++) {
            inputFilePaths[k] = Path.of(workspace, this.inputs[k].getDataFileName()).toAbsolutePath().toString();
            int tmpNobs = this.inputs[k].getNobs();
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
            nSpag[k] = this.inputs[k].getNspag();
            this.inputs[k].toDataFile(workspace);
        }

        // FIXME: should refactor code?
        n = this.outputs.length;
        String[] spagOutputFileName = new String[n];
        String[] envOutputFileName = new String[n];
        boolean[] includeOutputStructuralError = new boolean[n];
        boolean[] transposeOutput = new boolean[n];
        boolean[] createOutputEnvelop = new boolean[n];
        for (int k = 0; k < n; k++) {
            String name = this.outputs[k].getName();
            spagOutputFileName[k] = String.format(ConfigFile.RESULTS_OUTPUT_SPAG, this.name, name);
            envOutputFileName[k] = String.format(ConfigFile.RESULTS_OUTPUT_ENV, this.name, name);
            includeOutputStructuralError[k] = this.outputs[k].getSructuralError();
            transposeOutput[k] = this.outputs[k].getTranspose();
            createOutputEnvelop[k] = this.outputs[k].getCreateEnvelop();
        }

        n = this.states.length;
        boolean[] doStatePredictions = new boolean[n];
        String[] spagStateFileName = new String[n];
        String[] envStateFileName = new String[n];
        boolean[] transposeState = new boolean[n];
        boolean[] createStateEnvelop = new boolean[n];
        for (int k = 0; k < n; k++) {
            doStatePredictions[k] = true;
            String name = this.states[k].getName();
            spagStateFileName[k] = String.format(ConfigFile.RESULTS_STATE_SPAG, this.name, name);
            envStateFileName[k] = String.format(ConfigFile.RESULTS_STATE_ENV, this.name, name);
            transposeState[k] = this.states[k].getTranspose();
            createStateEnvelop[k] = this.states[k].getCreateEnvelop();
        }

        ConfigFile configFile = new ConfigFile();
        configFile.addItem(inputFilePaths, "Files containing spaghettis for each input variable (size nX)", true);
        configFile.addItem(nObs, "Nobs, number of observations per spaghetti (common to all files!)");
        configFile.addItem(nSpag, "Nspag, number of spaghettis for each input variable (size nX)");
        configFile.addItem(this.propagateParametricUncertainty, "Propagate parametric uncertainty?");
        configFile.addItem(includeOutputStructuralError,
                "Propagate remnant uncertainty for each output variable? (size nY)");
        configFile.addItem(this.nPriorReplicates,
                "Nsim[prior]. If <=0: posterior sampling (nsim is given by mcmc sample); if >0: sample nsim replicates from prior distribution");
        configFile.addItem(spagOutputFileName, "Files containing spaghettis for each output variable (size nY)");
        configFile.addItem(transposeOutput,
                "Post-processing: transpose spag file (so that each column is a spaghetti)? (size nY)");
        configFile.addItem(createOutputEnvelop, "Post-processing: create envelops? (size nY)");
        configFile.addItem(envOutputFileName, "Post-processing: name of envelop files (size nY)");
        configFile.addItem(this.printProgress, "Print progress in console during computations?");
        if (this.states.length == 0) {
            configFile.addItem(false, "Do state prediction? (size nState)");
        } else {
            configFile.addItem(doStatePredictions, "Do state prediction? (size nState)");
            configFile.addItem(spagStateFileName, "Files containing spaghettis for each state variable (size nState)");
            configFile.addItem(transposeState,
                    " Post-processing: transpose spag file (so that each column is a spaghetti)? (size nState)");
            configFile.addItem(createStateEnvelop, "Post-processing: create envelops? (size nState)");
            configFile.addItem(envStateFileName, "Post-processing: name of envelop files (size nState)");
        }
        configFile.writeToFile(workspace, this.getConfigFileName());
    }

    @Override
    public String toString() {
        String str = String.format("PredictionConfig '%s' (%b, %b, %d):\n",
                this.name,
                this.propagateParametricUncertainty,
                this.printProgress,
                this.nPriorReplicates);
        str += " Inputs: \n";
        for (PredictionInput i : this.inputs) {
            str += i.toString() + "\n";
        }
        str += " Outputs: \n";
        for (PredictionOutput o : this.outputs) {
            str += o.toString();
        }
        return str;
    }
}
package controleur;

import java.nio.file.Path;

import Utils.Defaults;
import Utils.FileReadWrite;
import moteur.Envelop;
import moteur.Spaghetti;

public class BaMresult {
    // this would need the following
    // - a workspace (it could read the configuration from the config files)
    // - the prediction master object (to know where to look for regarding the
    // prediction results)

    // TODO:
    // - an object (yet to be created) that would contain the whole configuration of
    // BaM
    // - including predictions, et eveything else
    // - excluding data (both input calibration data, prediction input data)

    private final String maxpostHeader = "LogPost";

    private PredictionMaster predictionMaster;
    private String workspace;

    private int logPostColumnIndex;
    private int maxpostIndex;
    private Double[][] mcmc;
    private String[] mcmcHeaders;
    // private Double[][][][] spags;
    // private Double[][][][] envelops;

    // it should have methods to retrieve a specific envelop or matrix
    //

    BaMresult(String workspace, PredictionMaster predictionMaster) throws Exception {
        this.workspace = workspace;
        this.predictionMaster = predictionMaster;

        readMcmc();
    }

    // FIXME: looks like this might be an important utility function
    // whose scope goes beyond this class...
    // Should there be a matrix class which would be:
    // - memory efficient or fast
    // - with quick access to rows and columns and specific values
    // - optional headers for columns

    static private Double[] getMatrixColumn(Double[][] matrix, int columnIndex) {
        int nRow = matrix.length;
        Double[] column = new Double[nRow];
        for (int k = 0; k < nRow; k++) {
            column[k] = matrix[k][columnIndex];
        }
        return column;
    }

    static public Double[][] transposeMatrix(Double[][] matrix) {
        Double[][] tMatrix = new Double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                tMatrix[j][i] = matrix[i][j];
            }
        }
        return tMatrix;
    }

    static public Double[][] swapColumns(Double[][] matrix, int i, int j) {
        Double[] column = matrix[i];
        matrix[i] = matrix[j];
        matrix[j] = column;
        return matrix;
    }

    private int getColumnIndex(String columnName) {
        for (int k = 0; k < mcmcHeaders.length; k++) {
            if (mcmcHeaders[k].equals(columnName)) {
                return k;
            }
        }
        return -1;
    }

    private String getFullFilePath(String fileName) {
        return Path.of(workspace, fileName).toString();
    }

    private void readMcmc() throws Exception {

        // read MCMC cooked matrix:
        // FIXME: should be part of the configuration object (see above)
        String cookedMcmcFilePath = getFullFilePath(Defaults.results_CookedMCMC);
        mcmc = FileReadWrite.readMatrix(cookedMcmcFilePath, "\\s+", 1);

        // retrieve MCMC headers:
        mcmcHeaders = FileReadWrite.readHeaders(cookedMcmcFilePath, "\\s+", 0);

        // retrieve the index of the column containing logpost values:
        logPostColumnIndex = getColumnIndex(maxpostHeader);

        // retrieving the index of the MaxPost:
        maxpostIndex = 0;
        Double maxLogPost = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < mcmc.length; k++) {
            if (mcmc[k][logPostColumnIndex] > maxLogPost) {
                maxLogPost = mcmc[k][logPostColumnIndex];
                maxpostIndex = k;
            }
        }

    }

    public Double[] getBaRatinRatingCurveInput(String predictionName) throws Exception {
        Prediction prediction = predictionMaster.getPrediction(predictionName);

        InputVarConfig inputVarConfig = prediction.getInputVariables()[0];
        Double[][] stageMatrix = FileReadWrite.readMatrix(inputVarConfig.getFilePath(), " ", 0);
        return BaMresult.transposeMatrix(stageMatrix)[0];
    }

    public Spaghetti getBaRatinRatingCurveSpaghetti(String predictionName, Double[] stageVector)
            throws Exception {
        Prediction prediction = predictionMaster.getPrediction(predictionName);

        OutputVarConfig outputVarConfig = prediction.getOutputVariables()[0];
        Double[][] spaghettiMatrix = FileReadWrite.readMatrix(getFullFilePath(outputVarConfig.getSpagFilePath()),
                "\\s+", 0);
        spaghettiMatrix = BaMresult.transposeMatrix(spaghettiMatrix);

        Spaghetti spaghetti = new Spaghetti();
        spaghetti.setX(stageVector);
        spaghetti.setY(spaghettiMatrix);
        spaghetti.setNx(spaghettiMatrix[0].length);
        spaghetti.setNspag(spaghettiMatrix.length);
        return spaghetti;
    }

    public Envelop getBaRatinRatingCurveEnvelop(String predictionName, Double[] stageVector, Double[] maxpostVector)
            throws Exception {
        Prediction prediction = predictionMaster.getPrediction(predictionName);

        OutputVarConfig outputVarConfig = prediction.getOutputVariables()[0];
        Double[][] envelopMatrix = FileReadWrite.readMatrix(getFullFilePath(outputVarConfig.getEnvFilePath()), "\\s+",
                1);
        envelopMatrix = BaMresult.transposeMatrix(envelopMatrix);

        Envelop envelop = new Envelop();
        // column of env file: Q_Median, Q_q2.5, Q_q97.5, Q_q16, Q_q84, Q_Mea, Q_Stdev
        envelop.setMedian(envelopMatrix[0]);
        envelop.setQlow(envelopMatrix[1]);
        envelop.setQhigh(envelopMatrix[2]);
        envelop.setNx(envelopMatrix[0].length); // number of obs I guess...
        envelop.setX(stageVector);
        envelop.setMaxpost(maxpostVector);
        return envelop;
    }

    // public Double[][] getPredictionOutput(String predictionName, String
    // variableName, boolean getEnv) throws Exception {
    // Prediction prediction = predictionMaster.getPrediction(predictionName);
    // if (prediction == null) {
    // System.err.println(String.format("Prediction with name '%s' not found.",
    // predictionName));
    // return null;
    // }
    // OutputVarConfig[] outVarConfigs = prediction.getOutputVariables();
    // OutputVarConfig outVarConfig = null;
    // for (OutputVarConfig ovc : outVarConfigs) {
    // if (ovc.getName() == variableName) {
    // outVarConfig = ovc;
    // break;
    // }
    // }
    // if (outVarConfig == null) {
    // System.err.println(String.format("Prediction '%s' does not contain an output
    // variable named '%s'.",
    // predictionName, variableName));
    // return null;
    // }
    // String filePath = getEnv ? outVarConfig.getEnvFilePath() :
    // outVarConfig.getSpagFilePath();
    // if (filePath == null) {
    // System.err.println(
    // String.format("In prediction '%s', output variable '%s' does not have a file
    // specified for its %s.",
    // predictionName, variableName, getEnv ? "envelops" : "spaghettis"));
    // return null;
    // }
    // Double[][] data = FileReadWrite.readMatrix(filePath, "\\s+", getEnv ? 1 : 0);
    // return data;
    // }

    // public Double[][] getPredictionInput(String predictionName, String
    // variableName) {
    // Prediction prediction = predictionMaster.getPrediction(predictionName);
    // if (prediction == null) {
    // System.err.println(String.format("Prediction with name '%s' not found.",
    // predictionName));
    // return null;
    // }
    // InputVarConfig[] inVarConfigs = prediction.getInputVariables();
    // InputVarConfig inVarConfig = null;
    // for (InputVarConfig ivc : inVarConfigs) {
    // if (ivc.getName() == variableName) {
    // outVarConfig = ovc;
    // break;
    // }
    // }
    // if (outVarConfig == null) {
    // System.err.println(String.format("Prediction '%s' does not contain an output
    // variable named '%s'.",
    // predictionName, variableName));
    // return null;
    // }
    // }

    public Double[][] readMcmcSummary() throws Exception {
        return FileReadWrite.readMatrix(getFullFilePath(Defaults.results_SummaryMCMC), "\\s+", 1, 1, true, false);
    }

    public Double[][] getMcmc() {
        return mcmc;
    }

    // public Double[][] getTransposedMcmc() {

    // return tMcmc;
    // }

    public int getMaxpostIndex() {
        return maxpostIndex;
    }

    public Double[] getMaxPostParameters() {
        return mcmc[maxpostIndex];
    }

    public Double[] getParameterMcmcSamples(String parameterName) {
        int columnIndex = getColumnIndex(maxpostHeader);
        return columnIndex == -1 ? null : getMatrixColumn(mcmc, columnIndex);
    }
}

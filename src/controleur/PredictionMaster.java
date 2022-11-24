package controleur;

import java.io.IOException;

import Utils.Defaults;

public class PredictionMaster {
    private Prediction[] predictions = new Prediction[0];

    PredictionMaster() {

    }

    public boolean hasPrediction(String predictionName) {
        for (Prediction pred : predictions) {
            if (pred.getName() == predictionName) {
                return true;
            }
        }
        return false;
    }

    public void addPrediction(Prediction prediction) {

        if (hasPrediction(prediction.getName())) {
            System.err.println("Error: predictions must have unique names!");
            return;
        }

        int n = predictions.length;
        Prediction[] newPredictions = new Prediction[n + 1];
        for (int k = 0; k < n; k++) {
            newPredictions[k] = predictions[k];
        }
        newPredictions[n] = prediction;
        predictions = newPredictions;
    }

    public void removePrediction(String predictionName) {
        if (!hasPrediction(predictionName)) {
            System.err
                    .println(String.format("Error: prediction '%s' not found! It cannot be removed.", predictionName));
            return;
        }

        int n = predictions.length;
        Prediction[] newPredictions = new Prediction[n - 1];
        int i = 0;
        for (int k = 0; k < n; k++) {
            if (predictions[k].getName() != predictionName) {
                newPredictions[i] = predictions[k];
                i++;
            }
        }
        predictions = newPredictions;
    }

    public Prediction getPrediction(String predictionName) {
        if (!hasPrediction(predictionName)) {
            System.err
                    .println(String.format("Error: prediction '%s' not found! ", predictionName));
            return null;
        }
        int n = predictions.length;
        for (int k = 0; k < n; k++) {
            if (predictions[k].getName() == predictionName) {
                return predictions[k];
            }
        }
        return null;
    }

    public void writeConfigFile(String workspace) throws IOException {
        int nPredictions = predictions.length;
        BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configPredMaster);
        configFile.addItem(nPredictions, "Number of prediction experiments");
        for (int k = 0; k < nPredictions; k++) {
            predictions[k].writeConfigFile(workspace);
            configFile.addItem(predictions[k].getConfigFileName(),
                    "Config file for experiments - an many lines as the number above", true);
        }
        configFile.writeToFile();
    }

}

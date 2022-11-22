package controleur;

// import java.io.BufferedWriter;
// import java.io.File;
// import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import moteur.BonnifaitMatrix;
import moteur.ConfigHydrau;
// import moteur.Gauging;
import moteur.GaugingSet;
import moteur.HydrauControl;
import commons.Distribution;
import commons.Parameter;

import moteur.Hydrograph;
import moteur.InputVariable;
import moteur.Limnigraph;
import moteur.MCMCoptions;
import moteur.OutputVariable;
import moteur.Prediction;
import moteur.PredictionInputVariable;
import moteur.RatingCurve;
import moteur.RemnantError;
import moteur.RunOptions;
import Utils.Defaults;
import Utils.FileReadWrite;

public class ConfigControl {

	private static ConfigControl instance;

	public static synchronized ConfigControl getInstance() {
		if (instance == null) {
			instance = new ConfigControl();
		}
		return instance;
	}

	public ConfigControl() {
	}

	private static Parameter processControlParameter(Parameter p, int controlNumber) {
		p = new Parameter(p);
		p.setName(String.format("%s_%d", p.getName(), controlNumber));
		return p;
	}

	public void write_engine(RunOptions runOptions, GaugingSet gaugings, ConfigHydrau hydrau,
			RemnantError remnant, MCMCoptions mcmc, RatingCurve rc,
			Limnigraph limni, Hydrograph hydro) throws IOException {

		// master Config_BaRatin ------------------------------------
		writeConfigBaRatin();

		// RunOptions -----------------------------------------------
		writeConfigRunOptions(
				runOptions.doMCMC(),
				runOptions.doMcmcSummary(),
				runOptions.doResidualDiag(),
				runOptions.doPrediction());

		// Model ----------------------------------------------------
		ArrayList<HydrauControl> controls = hydrau.getControls();
		Parameter[] parameters = new Parameter[controls.size() * 3];
		for (int k = 0; k < controls.size(); k++) {
			parameters[k * 3 + 0] = processControlParameter(controls.get(k).getK(), k + 1);
			parameters[k * 3 + 1] = processControlParameter(controls.get(k).getA(), k + 1);
			parameters[k * 3 + 2] = processControlParameter(controls.get(k).getC(), k + 1);
		}
		writeConfigModel("BaRatin", 1, 1, parameters);

		// Model setup / Xtra / Control matrix ----------------------
		writeConfigControlMatrix(hydrau.getMatrix());

		// Calibration data -----------------------------------------
		writeConfigGaugingsData(gaugings);

		// Remnant sigma --------------------------------------------
		writeConfigRemnantSigma(remnant);

		// MCMC options ---------------------------------------------
		writeConfigMcmc(mcmc);

		// Cooking, summary and residuals ---------------------------
		// FIXME: naming conventions!
		writeConfigCooking(Defaults.results_CookedMCMC, 0.5, 100);
		writeConfigSummary(Defaults.results_SummaryMCMC);
		writeConfigResiduals(Defaults.results_SummaryGaugings);

		// Prediction -----------------------------------------------
		if (runOptions.doPrediction()) {
			if (limni != null && hydro != null) {
				writeConfigH2Qproppagation(limni, hydro);
			} else {
				System.err.println("If doPrediction is true, limni and hydro should be set!");
			}
		}

	}

	private void writeConfigBaRatin() throws IOException {
		BaMconfigFile configFile = new BaMconfigFile(Defaults.exeDir, Defaults.exeConfigFileName);
		configFile.addItem(Defaults.workspacePath + "\\", "workspace", true);
		configFile.addItem(Defaults.configRunOptions, "Config file: run options", true);
		configFile.addItem(Defaults.configModel, "Config file: model", true);
		configFile.addItem(Defaults.configSetup, "Config file: control matrix", true);
		configFile.addItem(Defaults.configData, "Config file: data", true);
		configFile.addItem(Defaults.configRemnantSigma,
				"Config file: remnant sigma (as many files as there are output variables separated by commas)", true);
		configFile.addItem(Defaults.configMcmc, "Config file: MCMC", true);
		configFile.addItem(Defaults.configCooking, "Config file: cooking of MCMC samples", true);
		configFile.addItem(Defaults.configSummary, "Config file: summary of MCMC samples", true);
		configFile.addItem(Defaults.configResiduals, "Config file: residual diagnostics", true);
		configFile.addItem(Defaults.configPredMaster, "Config file: prediction experiments", true);
		configFile.writeToFile();
	}

	private void writeConfigRunOptions(boolean doMcmc, boolean doMcmcSummary, boolean doResidualDiag,
			boolean doPrediction) throws IOException {
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configRunOptions);
		configFile.addItem(doMcmc, "Do MCMC?");
		configFile.addItem(doMcmcSummary, "Do MCMC summary?");
		configFile.addItem(doResidualDiag, "Do Residual diagnostics?");
		configFile.addItem(doPrediction, "Do Predictions?");
		configFile.writeToFile();
	}

	private static BaMconfigFile addParameterItems(BaMconfigFile bamConfigFile, Parameter parameter) {
		bamConfigFile.addItem(parameter.getName(), "Parameter Name", true);
		bamConfigFile.addItem(parameter.getValue(), "Initial guess");
		Distribution distribution = parameter.getPrior();
		bamConfigFile.addItem(distribution.getName(), "Prior distribution", true);
		bamConfigFile.addItem(distribution.getParval(), "Prior parameters");
		return bamConfigFile;
	}

	private void writeConfigModel(String modelId, int nX, int nY, Parameter[] parameters) throws IOException {
		int nPar = parameters.length;
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configModel);
		configFile.addItem(modelId, "Model ID", true);
		configFile.addItem(nX, "nX: number of input variables");
		configFile.addItem(nY, "nY: number of input variables");
		configFile.addItem(nPar, "nPar: number of parameters theta");
		for (int k = 0; k < nPar; k++) {
			configFile = addParameterItems(configFile, parameters[k]);
		}
		configFile.writeToFile();
	}

	// FIXME: this should be named writeConfigSetup to be general.
	// However, it maybe more relevant, for now to have a specifc function for
	// BaRatin.
	// Maybe each model type should have it's own function.
	// QUESTION: each row is a control, each column a stage range?
	// looks like not!
	private void writeConfigControlMatrix(BonnifaitMatrix bonnifaitMatrix) throws IOException {
		ArrayList<ArrayList<Boolean>> controlMatrix = bonnifaitMatrix.getMatrix();
		int n = controlMatrix.size();
		String[][] strControlMatrix = new String[n][n];

		for (int i = 0; i < n; i++) { // for each control
			ArrayList<Boolean> controlLine = controlMatrix.get(i);
			for (int j = 0; j < n; j++) { // for each stage range
				strControlMatrix[j][i] = controlLine.get(j) ? "1" : "0";
			}
		}
		String[] lines = new String[controlMatrix.size()];
		for (int i = 0; i < n; i++) {
			lines[i] = String.join(" ", strControlMatrix[i]);
		}
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configSetup);
		for (int k = 0; k < lines.length; k++) {
			configFile.addItem(lines[k]);
		}
		configFile.writeToFile();
	}

	// FIXME: Should handle multiple input/output variables
	// FIXME: e.g. should be named "private void writeConfigCalibrationData() {}"
	private void writeConfigGaugingsData(GaugingSet gaugingSet) throws IOException {
		Double[][] gaugingMatrix = gaugingSet.toMatrix(); // [column][rows] !
		int nGaugings = 0;
		for (int k = 0; k < gaugingMatrix[10].length; k++) {
			// nGaugings = nGaugings + (gaugingMatrix[k][10] == 1.0 ? 1 : 0);
			nGaugings = nGaugings + (int) (double) gaugingMatrix[10][k];
		}
		Double[][] calibrationData = new Double[nGaugings][4];
		int currentIndex = 0;
		for (int k = 0; k < gaugingMatrix[0].length; k++) {
			if (gaugingMatrix[10][k] == 1.0) {
				calibrationData[currentIndex][0] = gaugingMatrix[6][k]; // H
				calibrationData[currentIndex][1] = gaugingMatrix[7][k] / 2 * gaugingMatrix[5][k] / 100; // uH / 2 * H /
																										// 100??
				calibrationData[currentIndex][2] = gaugingMatrix[8][k]; // Q
				calibrationData[currentIndex][3] = gaugingMatrix[9][k] / 2 * gaugingMatrix[8][k] / 100; // uQ / 2 * Q /
																										// 100
				currentIndex++;
			}
		}
		String[] headers = { "H", "uH", "Q", "uQ" };
		String dataFilePath = Path.of(Defaults.workspacePath, Defaults.dataGaugings).toString();
		FileReadWrite.writeMatrix(dataFilePath, calibrationData,
				headers, "\t");

		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configData);
		configFile.addItem(dataFilePath, "Absolute path to data file", true);
		configFile.addItem(1, "number of header lines");
		configFile.addItem(nGaugings, "Nobs, number of rows in data file (excluding header lines)");
		configFile.addItem(4, "number of columns in the data file");
		configFile.addItem(1, "columns for X (observed inputs) in data file - comma-separated if several");
		configFile.addItem(0,
				"columns for Xu (random uncertainty in X, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
		configFile.addItem(0,
				"columns for Xb (systematic uncertainty in X, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
		configFile.addItem(0,
				"columns for Xb_indx (index of systematic errors in X - use 0 for a no-error assumption)");
		configFile.addItem(3, "columns for Y (observed outputs) in data file - comma-separated if several");
		configFile.addItem(4,
				"columns for Yu (uncertainty in Y, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
		configFile.addItem(0,
				"columns for Yb (systematic uncertainty in Y, EXPRESSED AS A STANDARD DEVIATION - use 0 for a no-error assumption)");
		configFile.addItem(0,
				"columns for Yb_indx (index of systematic errors in Y - use 0 for a no-error assumption)");
		configFile.writeToFile();

	}

	// FIXME: should handle multiple output variables
	private void writeConfigRemnantSigma(RemnantError remnantSigma) throws IOException {
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configRemnantSigma);
		configFile.addItem(remnantSigma.getFunction(), "Function f used in sdev=f(Qrc)", true);
		int nPar = remnantSigma.getNpar();
		configFile.addItem(nPar, "Number of parameters gamma for f");
		Parameter[] parameters = remnantSigma.getPar();
		for (int k = 0; k < nPar; k++) {
			configFile = addParameterItems(configFile, parameters[k]);
		}
		configFile.writeToFile();
	}

	private void writeConfigMcmc(MCMCoptions mcmcOptions) throws IOException {
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configMcmc);
		configFile.addItem(mcmcOptions.getFilename(), "File name", true);
		configFile.addItem(mcmcOptions.getnAdapt(), "NAdapt");
		configFile.addItem(mcmcOptions.getnCycles(), "Ncycles");
		configFile.addItem(mcmcOptions.getMinMR(), "MinMoveRate");
		configFile.addItem(mcmcOptions.getMaxMR(), "MaxMoveRate");
		configFile.addItem(mcmcOptions.getDownMult(), "DownMult");
		configFile.addItem(mcmcOptions.getUpMult(), "UpMult");
		// FIXME: the following section is unclear to me and seems undefined in the
		// MCMCoptions class
		configFile.addItem(0, "Mode for setting the initial Std of the jump distribution");
		configFile.addItem("**** DEFINITION OF INITIAL JUMP STD **** ", "Cosmetics");
		configFile.addItem(0.1, "MultFactor in default mode (ignored in manual mode)");
		Double[] rcMultFactors = { 0.1, 0.1, 0.1 };
		Double[] remnantMultFactors = { 0.1, 0.1 };
		configFile.addItem(rcMultFactors, "RC MultFactor in manual mode (ignored in auto mode)");
		configFile.addItem(remnantMultFactors, "Remnant MultFactor in manual mode (ignored in auto mode)");
		configFile.writeToFile();
	}

	private void writeConfigCooking(String resultFileName, Double burnFactor, int nSlim) throws IOException {
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configCooking);
		configFile.addItem(resultFileName, "File name", true);
		configFile.addItem(burnFactor, "BurnFactor");
		configFile.addItem(nSlim, "Nslim");
		configFile.writeToFile();
	}

	private void writeConfigSummary(String resultFileName) throws IOException {
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configSummary);
		configFile.addItem(resultFileName, "File name", true);
		configFile.writeToFile();
	}

	private void writeConfigResiduals(String resultFileName) throws IOException {
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configResiduals);
		configFile.addItem(resultFileName, "File name", true);
		configFile.writeToFile();
	}

	// FIXME: should have a proper object representing a prediction
	// FIXME: should handle mutliple inputs/outputs
	private void writeConfigH2Qproppagation(Limnigraph limnigraph, Hydrograph hydrograph) throws IOException {

		InputVariable inputVariable = new InputVariable(limnigraph);
		PredictionInputVariable[] errFreeInputVar = {
				new PredictionInputVariable("errorFree", inputVariable, 100, false,
						false) };
		PredictionInputVariable[] withErrInputVar = {
				new PredictionInputVariable("withError", inputVariable, 100, true,
						true) };

		writePredictionInputVariable(errFreeInputVar);
		writePredictionInputVariable(withErrInputVar);

		// FIXME: may need to be called PredictionOutputVariable for consistency with
		// PredictionInputVariable?
		OutputVariable[] errFreeOutputVar = {
				new OutputVariable("WithRemnantError",
						true, true, true)
		};
		OutputVariable[] withErrOutputVar = {
				new OutputVariable("WithoutRemnantError",
						true, true, false)
		};

		Prediction H11C00 = new Prediction("H11C00", withErrInputVar, errFreeOutputVar, false);
		Prediction H00C11 = new Prediction("H00C11", errFreeInputVar, withErrOutputVar, false);
		Prediction H11C11 = new Prediction("H11C11", withErrInputVar, withErrOutputVar, true);

		String[] predictionNames = new String[3];
		predictionNames[0] = writeConfigPrediction(H11C00);
		predictionNames[1] = writeConfigPrediction(H00C11);
		predictionNames[2] = writeConfigPrediction(H11C11);

		writePredictionMaster(predictionNames);
	}

	private String[] writePredictionInputVariable(PredictionInputVariable[] predictionInputVariable)
			throws IOException {

		int n = predictionInputVariable.length;
		String[] filePaths = new String[n];

		for (int k = 0; k < n; k++) {
			Double[][] data = predictionInputVariable[k].getData();

			String fileName = String.format("%s%s%s", Defaults.dataLimnigraphPrefix,
					predictionInputVariable[k].getName(), ".txt");
			filePaths[k] = Path.of(Defaults.workspacePath, fileName).toString();
			FileReadWrite.writeMatrix(filePaths[k], data);
		}
		return filePaths;
	}

	private String writeConfigPrediction(Prediction prediction) throws IOException {
		String predictionName = prediction.getName();

		PredictionInputVariable[] predInVar = prediction.getPredictionInputVariables();
		OutputVariable[] predOutVar = prediction.getOutputVariables();

		int nInput = predInVar.length;
		int nOutput = predOutVar.length;

		int nObs = predInVar[0].getNobs();

		String[] inputFilePaths = new String[nInput];
		int[] nSpag = new int[nInput];
		for (int k = 0; k < nInput; k++) {
			Double[][] data = predInVar[k].getData();
			if (predInVar[k].getNobs() != nObs) {
				System.err.println("Number of observations must be equal accross all input variables!");
			}
			nSpag[k] = data[0].length;
			String fileName = String.format("%s%s%s", Defaults.dataLimnigraphPrefix, predInVar[k].getName(), ".txt");
			inputFilePaths[k] = Path.of(Defaults.workspacePath, fileName).toString();
			// FileReadWrite.writeMatrix(inputFilePaths[k], data);
		}

		String[] spagOutputFilenames = new String[nOutput];
		String[] envOutputFilenames = new String[nOutput];
		boolean[] propagateRemnantErrors = new boolean[nOutput];
		boolean[] transposeSpagMatrix = new boolean[nOutput];
		boolean[] createEnvelopFiles = new boolean[nOutput];
		for (int k = 0; k < nOutput; k++) {
			String outName = String.format("%s_%s", predictionName, predOutVar[k].getName());
			spagOutputFilenames[k] = String.format("%s%s%S", Defaults.resultsSpagPrefix, outName, ".spag");
			envOutputFilenames[k] = String.format("%s%s%S", Defaults.resultsEnvPrefix, outName, ".env");
			propagateRemnantErrors[k] = predOutVar[k].shouldPropagateRemnantUncertainty();
			transposeSpagMatrix[k] = predOutVar[k].shouldTranspose();
			createEnvelopFiles[k] = predOutVar[k].shouldCreateEnvelopFile();
		}

		String configFileName = String.format("%s%s%s", Defaults.configPredSuffix, predictionName, ".txt");
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, configFileName);
		configFile.addItem(inputFilePaths, "Files containing spaghettis for each input variable (size nX)", true);
		configFile.addItem(nObs, "Nobs, number of observations per spaghetti (common to all files!)");
		configFile.addItem(nSpag, "Nspag, number of spaghettis for each input variable (size nX)");
		configFile.addItem(prediction.includeParametricUncertainty(), "Propagate parametric uncertainty?");
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

		return configFileName;
	}

	private void writePredictionMaster(String[] predictionFileNames) throws IOException {
		int nPredictions = predictionFileNames.length;
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configPredMaster);
		configFile.addItem(nPredictions, "Number of prediction experiments");
		for (int k = 0; k < nPredictions; k++) {
			configFile.addItem(predictionFileNames[k],
					"Config file for experiments - an many lines as the number above", true);
		}
		configFile.writeToFile();
	}

}

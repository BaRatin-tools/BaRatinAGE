package controleur;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import moteur.BonnifaitMatrix;
import moteur.ConfigHydrau;
import moteur.GaugingSet;
import moteur.HydrauControl;
import commons.Constants;
import commons.Distribution;
import commons.Parameter;

import moteur.Hydrograph;
import moteur.InputVariable;
import moteur.Limnigraph;
import moteur.MCMCoptions;
import moteur.PostRatingCurveOptions;
import moteur.PriorRatingCurveOptions;
import moteur.RatingCurve;
import moteur.RemnantError;
import moteur.RunOptions;
import Utils.Defaults;
import Utils.FileReadWrite;

public class ConfigControl {

	static private Parameter processControlParameter(Parameter p, int controlNumber) {
		p = new Parameter(p);
		p.setName(String.format("%s_%d", p.getName(), controlNumber));
		return p;
	}

	static public PredictionMaster write_engine(RunOptions runOptions, GaugingSet gaugings, ConfigHydrau hydrau,
			RemnantError remnant, MCMCoptions mcmc, RatingCurve rc,
			Limnigraph limni, Hydrograph hydro) throws IOException {

		/**
		 * Note: here everything can be null exept:
		 * - runOptions
		 * - hydrau
		 * - remnant (has a default one built in any case)
		 * - mcmc (provided by the BaRatinAGE config object, created from a text file)
		 */
		if (runOptions == null) {
			System.err.println("Error: RunOptions is null! Aborting.");
			return null;
		}
		if (hydrau == null) {
			System.err.println("Error: ConfigHydrau is null! Aborting.");
			return null;
		}
		if (remnant == null) {
			System.err.println("Error: RemnantError is null! Aborting.");
			return null;
		}
		if (mcmc == null) {
			System.err.println("Error: MCMCoptions is null! Aborting.");
			return null;
		}

		// master Config_BaRatin ------------------------------------
		writeConfigBaRatin();

		// RunOptions -----------------------------------------------
		// FIXME: have it receive a runOption object as argument?
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
		if (gaugings != null) {
			writeConfigGaugingsData(gaugings);
		} else {
			System.out.println("No gaugings specified...");
		}

		// Remnant sigma --------------------------------------------
		writeConfigRemnantSigma(remnant);

		// MCMC options ---------------------------------------------
		writeConfigMcmc(mcmc);

		// Cooking, summary and residuals ---------------------------
		// FIXME: naming conventions!
		writeConfigCooking(Defaults.results_CookedMCMC, 0.5, 10);
		writeConfigSummary(Defaults.results_SummaryMCMC);
		writeConfigResiduals(Defaults.results_SummaryGaugings);

		// Predictions ----------------------------------------------
		if (runOptions.doPrediction()) {
			PredictionMaster predictionMaster = new PredictionMaster();
			if (rc == null && limni == null && hydro == null) {
				System.out.println("Probably a prior experiment...");
				if (!runOptions.doMCMC() && !runOptions.doMcmcSummary() && !runOptions.doResidualDiag()
						&& runOptions.doPrediction()) {
					System.out.println("Certainly a prior experiment...");
					predictionMaster = writeCongigPriorRC(hydrau, predictionMaster);
				}
			}
			if (rc != null) {
				predictionMaster = writeConfigRC(rc, predictionMaster);
			} else {
				System.err.println("No RC prediction...");
			}
			if (limni != null && hydro != null) {
				predictionMaster = writeConfigH2Qproppagation(limni, hydro, predictionMaster);
			} else {
				System.err.println("No H2Q prediction...");
			}

			predictionMaster.writeConfigFile(Defaults.workspacePath);
			return predictionMaster;
		} else {
			return null;
		}

	}

	static private void writeConfigBaRatin() throws IOException {
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

	static private void writeConfigRunOptions(boolean doMcmc, boolean doMcmcSummary, boolean doResidualDiag,
			boolean doPrediction) throws IOException {
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configRunOptions);
		configFile.addItem(doMcmc, "Do MCMC?");
		configFile.addItem(doMcmcSummary, "Do MCMC summary?");
		configFile.addItem(doResidualDiag, "Do Residual diagnostics?");
		configFile.addItem(doPrediction, "Do Predictions?");
		configFile.writeToFile();
	}

	static private BaMconfigFile addParameterItems(BaMconfigFile bamConfigFile, Parameter parameter) {
		bamConfigFile.addItem(parameter.getName(), "Parameter Name", true);
		bamConfigFile.addItem(parameter.getValue(), "Initial guess");
		Distribution distribution = parameter.getPrior();
		bamConfigFile.addItem(distribution.getName(), "Prior distribution", true);
		bamConfigFile.addItem(distribution.getParval(), "Prior parameters");
		return bamConfigFile;
	}

	private static void writeConfigModel(String modelId, int nX, int nY, Parameter[] parameters) throws IOException {
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
	// BaRatin. Maybe each model type should have it's own function.
	static private void writeConfigControlMatrix(BonnifaitMatrix bonnifaitMatrix) throws IOException {
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
	static private void writeConfigGaugingsData(GaugingSet gaugingSet) throws IOException {
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
				calibrationData[currentIndex][1] = gaugingMatrix[7][k] / 2 * gaugingMatrix[5][k] / 100; // uH/2*H/100??
				calibrationData[currentIndex][2] = gaugingMatrix[8][k]; // Q
				calibrationData[currentIndex][3] = gaugingMatrix[9][k] / 2 * gaugingMatrix[8][k] / 100; // uQ/2*Q/100??
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
	static private void writeConfigRemnantSigma(RemnantError remnantSigma) throws IOException {
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

	static private void writeConfigMcmc(MCMCoptions mcmcOptions) throws IOException {
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

	static private void writeConfigCooking(String resultFileName, Double burnFactor, int nSlim) throws IOException {
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configCooking);
		configFile.addItem(resultFileName, "File name", true);
		configFile.addItem(burnFactor, "BurnFactor");
		configFile.addItem(nSlim, "Nslim");
		configFile.writeToFile();
	}

	static private void writeConfigSummary(String resultFileName) throws IOException {
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configSummary);
		configFile.addItem(resultFileName, "File name", true);
		configFile.writeToFile();
	}

	static private void writeConfigResiduals(String resultFileName) throws IOException {
		BaMconfigFile configFile = new BaMconfigFile(Defaults.workspacePath, Defaults.configResiduals);
		configFile.addItem(resultFileName, "File name", true);
		configFile.writeToFile();
	}

	static private InputVariable createStageGridInputVariable(Double from, Double to, Double step) {
		int nStep = (int) ((to - from) / step);
		Double[] stageValues = new Double[nStep];
		for (int k = 0; k < nStep; k++) {
			stageValues[k] = from + step * k;
		}
		return new InputVariable("RatingCurveStageGrid", stageValues);
	}

	static private InputVariable createStageGridInputVariable(Double from, Double to, int nStep) {
		Double step = (to - from) / (double) nStep;
		return createStageGridInputVariable(from, to, step);
	}

	static private PredictionMaster writeCongigPriorRC(ConfigHydrau hydraulicConfig, PredictionMaster predictionMaster)
			throws IOException {

		PriorRatingCurveOptions priorRatingCurveOptions = hydraulicConfig.getPriorRCoptions();

		int nStep = priorRatingCurveOptions.getnStep();
		Double hMin = priorRatingCurveOptions.gethMin();
		Double hMax = priorRatingCurveOptions.gethMax();

		InputVariable hGridInputVariable = createStageGridInputVariable(hMin, hMax, nStep);

		String hGridFilePath = Path.of(Defaults.workspacePath, Defaults.dataRatingCurveStageGrid).toString();
		InputVarConfig hGridInVarConf = hGridInputVariable.writeToFile(hGridFilePath, false, false);

		OutputVarConfig QoutputWithoutRemnantErr = new OutputVarConfig("Q_noRemnErr",
				true, true, false);

		Prediction C10 = new Prediction("C10", new InputVarConfig[] { hGridInVarConf },
				new OutputVarConfig[] { QoutputWithoutRemnantErr }, true);

		predictionMaster.addPrediction(C10);

		return predictionMaster;

	}

	static private PredictionMaster writeConfigRC(RatingCurve ratingCurve, PredictionMaster predictionMaster)
			throws IOException {
		PostRatingCurveOptions ratingCurveOptions = ratingCurve.getPostRCoptions();

		int nStep = ratingCurveOptions.getnStep();
		Double hStep = ratingCurveOptions.gethStep();
		Double hMin = ratingCurveOptions.gethMin();
		Double hMax = ratingCurveOptions.gethMax();
		if (hStep == Constants.D_MISSING || hMin == Constants.D_MISSING || hMax == Constants.D_MISSING) {
			System.err.println("Error: rating curve grid should be specified");
			return null;
		}

		InputVariable hGridInputVariable = createStageGridInputVariable(hMin, hMax, nStep);

		String hGridFilePath = Path.of(Defaults.workspacePath, Defaults.dataRatingCurveStageGrid).toString();
		InputVarConfig hGridInVarConf = hGridInputVariable.writeToFile(hGridFilePath, false, false);

		OutputVarConfig QoutputWithRemnantErr = new OutputVarConfig("Q_remnErr",
				true, true, true);
		OutputVarConfig QoutputWithoutRemnantErr = new OutputVarConfig("Q_noRemnErr",
				true, true, false);

		Prediction C10 = new Prediction("C10", new InputVarConfig[] { hGridInVarConf },
				new OutputVarConfig[] { QoutputWithoutRemnantErr }, true);
		Prediction C11 = new Prediction("C11", new InputVarConfig[] { hGridInVarConf },
				new OutputVarConfig[] { QoutputWithRemnantErr }, true);

		predictionMaster.addPrediction(C10);
		predictionMaster.addPrediction(C11);

		return predictionMaster;
	};

	// FIXME: should handle mutliple inputs/outputs
	static private PredictionMaster writeConfigH2Qproppagation(Limnigraph limnigraph, Hydrograph hydrograph,
			PredictionMaster predictionMaster) throws IOException {

		InputVariable inputVariable = new InputVariable(limnigraph, 100);

		String h00filePath = Path
				.of(Defaults.workspacePath, String.format("%s%s", Defaults.dataLimnigraphPrefix, "H00.txt")).toString();
		String h11filePath = Path
				.of(Defaults.workspacePath, String.format("%s%s", Defaults.dataLimnigraphPrefix, "H11.txt")).toString();

		InputVarConfig h00InVarConfig = inputVariable.writeToFile(h00filePath, false, false);
		InputVarConfig h11InVarConfig = inputVariable.writeToFile(h11filePath, true, true);

		// FIXME: may need to be called PredictionOutputVariable for consistency with
		// PredictionInputVariable?
		OutputVarConfig errFreeOutputVar = new OutputVarConfig("WithRemnantError", true, true, true);
		OutputVarConfig withErrOutputVar = new OutputVarConfig("WithRemnantError", true, true, false);

		Prediction H00C00 = new Prediction("H00C00", new InputVarConfig[] { h00InVarConfig },
				new OutputVarConfig[] { errFreeOutputVar }, false);
		Prediction H11C00 = new Prediction("H11C00", new InputVarConfig[] { h11InVarConfig },
				new OutputVarConfig[] { errFreeOutputVar }, false);
		Prediction H11C10 = new Prediction("H11C10", new InputVarConfig[] { h11InVarConfig },
				new OutputVarConfig[] { errFreeOutputVar }, true);
		Prediction H11C11 = new Prediction("H11C11", new InputVarConfig[] { h11InVarConfig },
				new OutputVarConfig[] { withErrOutputVar }, true);

		predictionMaster.addPrediction(H00C00);
		predictionMaster.addPrediction(H11C00);
		predictionMaster.addPrediction(H11C10);
		predictionMaster.addPrediction(H11C11);

		return predictionMaster;
	}
}

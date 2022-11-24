package controleur;

import java.awt.Cursor;
import java.io.BufferedReader;
// import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
// import java.io.OutputStream;
// import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import commons.DirectoryUtils;
import commons.Frame_YesNoQuestion;
// import commons.Observation;
import commons.ReadWrite;
// import commons.TimeSerie;
import moteur.ConfigHydrau;
import moteur.Envelop;
import moteur.GaugingSet;
import moteur.Hydrograph;
import moteur.Limnigraph;
import moteur.MCMCoptions;
import moteur.RatingCurve;
import moteur.RemnantError;
import moteur.RunOptions;
import moteur.Spaghetti;
import moteur.Station;
// import vue.AllDonePanel;
import vue.ConfigHydrauPanel;
import vue.ExceptionPanel;
import vue.HydrographPanel;
import vue.MainFrame;
import vue.RatingCurvePanel;
import Utils.Config;
import Utils.Defaults;
import Utils.Dico;
// import Utils.FileReadWrite;

public class ExeControl {

	private static ExeControl instance;

	public static synchronized ExeControl getInstance() {
		if (instance == null) {
			instance = new ExeControl();
		}
		return instance;
	}

	// locals
	private Config config = Config.getInstance();
	private Dico dico = Dico.getInstance(config.getLanguage());
	// private ConfigControl configController = new ConfigControl();
	// private ResultControl resultController = new ResultControl();
	private Station station = Station.getInstance();
	private Process exeProc = null;

	private ExeControl() {
	}

	private void engine(RunOptions runOptions, GaugingSet gaugings, ConfigHydrau hydrau,
			RemnantError remnant, MCMCoptions mcmc, RatingCurve rc, Limnigraph limni, Hydrograph hydro)
			throws IOException, Exception, InterruptedException, FileNotFoundException {

		// Write config files and
		new File(Defaults.workspacePath).mkdir();
		PredictionMaster predictionMaster = ConfigControl.write_engine(runOptions, gaugings, hydrau, remnant, mcmc,
				rc, limni, hydro);

		// make a copy in recycle folder
		DirectoryUtils.deleteDirContent(new File(Defaults.recycleDir));
		DirectoryUtils.copyFilesInDir(new File(Defaults.workspacePath), new File(Defaults.recycleDir));

		// // Pilot executable
		String[] exeCommand = { Defaults.exeCommand };
		File exeDirectory = new File(Defaults.home, "exe");
		try {
			exeProc = Runtime.getRuntime().exec(exeCommand, null, exeDirectory);
		} catch (IOException e) {
			System.err.println(e);
			throw new IOException(e);
		}

		InputStream inputStream = exeProc.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferReader = new BufferedReader(inputStreamReader);
		ArrayList<String> consoleLines = new ArrayList<String>();
		String currentLine = null;
		while ((currentLine = bufferReader.readLine()) != null) {
			System.out.println(String.format("\"%s\"", currentLine));
			consoleLines.add(currentLine);
		}
		System.out.flush();
		int exitcode = exeProc.waitFor();
		if (exitcode != 0) {
			String errorMessage = "Unknown Error Message";
			for (int k = 0; k < consoleLines.size(); k++) {
				if (consoleLines.get(k).contains("FATAL ERROR has occured")) {
					if (k < consoleLines.size() - 2) {
						errorMessage = consoleLines.get(k + 1);
						break;
					}
				}
			}
			// ConsoleLines.forEach(null);q
			throw new InterruptedException(errorMessage);
		}

		// // ##################################

		BaMresult bamResult = new BaMresult(Defaults.workspacePath,
				predictionMaster);

		// Read MCMC
		Double[][] mcmcSamples = bamResult.getMcmc();
		mcmcSamples = BaMresult.transposeMatrix(mcmcSamples);
		mcmcSamples = BaMresult.swapColumns(mcmcSamples, 0, 1);
		Double[] columnK1 = mcmcSamples[0];
		mcmcSamples[0] = mcmcSamples[1];
		mcmcSamples[1] = columnK1;
		rc.setMcmc(mcmcSamples);
		rc.setMcmc_cooked(mcmcSamples);
		Double[][] mcmcSummary = bamResult.readMcmcSummary();
		mcmcSummary = BaMresult.transposeMatrix(mcmcSummary);
		mcmcSummary = BaMresult.swapColumns(mcmcSummary, 0, 1);
		rc.setMcmc_summary(mcmcSummary);

		// Read envelops
		// FIXME: needs refactoring/encapsulations

		// predictionMaster
		// predictionName
		// predictionName

		Double[] stageVector = bamResult.getBaRatinRatingCurveInput("C10");

		Spaghetti spaghettiParam = bamResult.getBaRatinRatingCurveSpaghetti("C10", stageVector);
		Envelop envelopParam = bamResult.getBaRatinRatingCurveEnvelop("C10", stageVector,
				spaghettiParam.getY()[bamResult.getMaxpostIndex()]);

		rc.setEnv_param(envelopParam);
		rc.setSpag_param(spaghettiParam);

		Spaghetti spaghettiTotal = bamResult.getBaRatinRatingCurveSpaghetti("C11", stageVector);
		Envelop envelopTotal = bamResult.getBaRatinRatingCurveEnvelop("C11", stageVector,
				spaghettiTotal.getY()[bamResult.getMaxpostIndex()]);

		rc.setEnv_total(envelopTotal);
		rc.setSpag_total(spaghettiTotal);

		// // read result files and update panels
		// // prior RC
		// // if (options[0] == true) {
		// // Envelop env = resultController.readEnvelop(
		// // new File(Defaults.workspacePath, Defaults.results_PriorRC_env +
		// // "_010.txt").getAbsolutePath());
		// // hydrau.setPriorEnv(env);
		// // Spaghetti spag = resultController.readSpaghetti(
		// // new File(Defaults.workspacePath, Defaults.results_PriorRC_spag +
		// // "_010.txt").getAbsolutePath());
		// // hydrau.setPriorSpag(spag);
		// // }
		// // raw MCMC
		// // if (options[1] == true) {

		// // String rcEnvFilePath = new File(Defaults.workspacePath,
		// // Defaults.results_Qt_env +"_111.txt")

		// // h2Q propagation
		// if (options[3] == true) {
		// Envelop env = resultController.readEnvelop(
		// new File(Defaults.workspacePath, Defaults.results_Qt_env +
		// "_111.txt").getAbsolutePath());
		// env.setX(toYear(limni));
		// hydro.setEnv_total(env);
		// env = resultController.readEnvelop(
		// new File(Defaults.workspacePath, Defaults.results_Qt_env +
		// "_100.txt").getAbsolutePath());
		// env.setX(toYear(limni));
		// hydro.setEnv_h(env);
		// env = resultController.readEnvelop(
		// new File(Defaults.workspacePath, Defaults.results_Qt_env +
		// "_110.txt").getAbsolutePath());
		// env.setX(toYear(limni));
		// hydro.setEnv_hparam(env);
		// hydro.setObservations(new ArrayList<Observation>());
		// for (int i = 0; i < limni.getObservations().size(); i++) {
		// hydro.addObservation(
		// new Observation(env.getMaxpost()[i],
		// limni.getObservations().get(i).getObsDate(), null));
		// }
		// // TODO: read spag, but this could take several gigas in memory, need to
		// // think
		// // about it
		// if (config.isSaveHydroSpag()) {
		// Spaghetti spag = resultController.readSpaghetti(
		// new File(Defaults.workspacePath, Defaults.results_Qt_spag +
		// "_111.txt").getAbsolutePath());
		// spag.setX(toYear(limni));
		// hydro.setSpag_total(spag);
		// spag = resultController.readSpaghetti(
		// new File(Defaults.workspacePath, Defaults.results_Qt_spag +
		// "_110.txt").getAbsolutePath());
		// spag.setX(toYear(limni));
		// hydro.setSpag_hparam(spag);
		// spag = resultController.readSpaghetti(
		// new File(Defaults.workspacePath, Defaults.results_Qt_spag +
		// "_100.txt").getAbsolutePath());
		// spag.setX(toYear(limni));
		// hydro.setSpag_h(spag);
		// }
		// }
		// // Clean up
		DirectoryUtils.deleteDirContent(new File(Defaults.workspacePath));
		// if (options[1]) {
		// new AllDonePanel(null,
		// dico.entry("AllDone") + System.getProperty("line.separator") +
		// dico.entry("CheckPar"));
		// } else {
		// new AllDonePanel(null, dico.entry("AllDone"));
		// }

		// ##################################
	}

	public void run(RunOptions runOptions, String id_gaugings, String id_hydrau, Integer indx_remnant, String id_mcmc,
			String id_rc) {
		GaugingSet gaugings = null;
		ConfigHydrau hydrau = null;
		RemnantError remnant = new RemnantError();
		MCMCoptions mcmc = config.getMcmc();
		RatingCurve rc = null;
		if (id_gaugings != null) {
			if (station.getGauging() != null) {
				if (station.getGauging().getSize() > 0) {
					gaugings = station.getGauging(id_gaugings);
				}
			}
		}
		if (id_hydrau != null) {
			if (station.getConfig() != null) {
				if (station.getConfig().getSize() > 0) {
					hydrau = station.getHydrauConfig(id_hydrau);
				}
			}
		}
		if (indx_remnant != null) {
			if (station.getRemnant() != null) {
				if (station.getRemnant().getSize() > 0) {
					remnant = station.getRemnantAt(indx_remnant);
				}
			}
		}
		if (id_rc != null) {
			if (station.getRc() != null) {
				if (station.getRc().getSize() > 0) {
					rc = station.getRatingCurve(id_rc);
				}
			}
		}
		try {
			MainFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			engine(runOptions, gaugings, hydrau, remnant, mcmc, rc, null, null);
			// update panels
			if (id_hydrau != null) {
				new ConfigHydrauPanel(id_hydrau, true);
			}
			if (id_rc != null) {
				new RatingCurvePanel(id_rc, true);
			}
		} catch (FileNotFoundException e3) {
			System.err.println(e3);
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("FileNotFound"));
		} catch (IOException e3) {
			System.err.println(e3);
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("ConfigWriteProblem"));
		} catch (InterruptedException e3) {
			System.err.println(e3);
			String message = String.format("%s!\n%s: %s", dico.entry("RunProblem"), dico.entry("ExeAborted"),
					e3.getMessage());
			new ExceptionPanel(MainFrame.getInstance(), message);
		} catch (Exception e3) {
			System.err.println(e3);
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("UnidentifiedProblem"));
		} finally {
			MainFrame.getInstance().setCursor(Cursor.getDefaultCursor());
		}
	}

	public void propagate(String id_hydro) {
		// retrieve hydrograph, limni and RC
		try {
			MCMCoptions mcmc = config.getMcmc();
			Hydrograph hydro = station.getHydrograph(id_hydro);
			Limnigraph limni = station.getLimnigraph(hydro.getLimni_id());
			RatingCurve rc = station.getRatingCurve(hydro.getRc_id());
			ConfigHydrau hydrau = station.getHydrauConfig(rc.getHydrau_id());
			GaugingSet gaugings = station.getGauging(rc.getGauging_id());
			RemnantError remnant = station.getRemnant(rc.getError_id());
			MainFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			// Ask confirmation to user
			int nt = limni.getObservations().size();
			int nspag = (int) (mcmc.getnAdapt() * mcmc.getnCycles() * mcmc.getBurn() / mcmc.getnSlim());
			double roughSize = nt * nspag * 16 * 3 / 1000000; // 16-char representation times 3 propagation experiments
			if (roughSize > 10) {
				String sizeTxt;
				if (roughSize >= 1000) { // express in GB
					sizeTxt = ((int) roughSize / 1000) + "GB";
				} else { // express in MB
					sizeTxt = (int) roughSize + "MB";
				}
				String mess = String.format("<html>%s<br>%s:<span style=\"font-weight: bold\">%s</span><br>%s</html>",
						dico.entry("propagationWarning"),
						dico.entry("estimatedFileSize"),
						sizeTxt,
						dico.entry("ConfirmContinue"));
				int ok = new Frame_YesNoQuestion().ask(MainFrame.getInstance(),
						mess,
						dico.entry("Warning"),
						Defaults.iconWarning, dico.entry("Yes"), dico.entry("No"));
				if (ok == JOptionPane.NO_OPTION) {
					return;
				}
			}
			// Rewrite MCMC file
			int ncol = rc.getMcmc().length;
			String[] head = new String[ncol];
			for (int i = 0; i < ncol; i++) {
				head[i] = "dummy";
			}
			ReadWrite.write(rc.getMcmc(), head, new File(Defaults.workspacePath, mcmc.getFilename()).getAbsolutePath(),
					Defaults.resultSep);
			engine(new RunOptions(false, false, false, true), gaugings, hydrau, remnant, mcmc, rc, limni, hydro);
			// update panels
			if (id_hydro != null) {
				new HydrographPanel(id_hydro, true);
			}
		} catch (FileNotFoundException e3) {
			System.err.println(e3);
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("FileNotFound"));
		} catch (IOException e3) {
			System.err.println(e3);
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("ConfigWriteProblem"));
		} catch (InterruptedException e3) {
			System.err.println(e3);
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("RunProblem"));
		} catch (Exception e3) {
			System.err.println(e3);
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("UnidentifiedProblem"));
		} finally {
			MainFrame.getInstance().setCursor(Cursor.getDefaultCursor());
		}
	}

	// private Double[] toYear(TimeSerie z) {
	// int n = z.length();
	// Double[] x = new Double[n];
	// for (int i = 0; i < n; i++) {
	// x[i] = z.getObservations().get(i).getObsDate().toYear();
	// }
	// return (x);
	// }

	public Process getExeProc() {
		return exeProc;
	}

	public void setExeProc(Process exeProc) {
		this.exeProc = exeProc;
	}

}

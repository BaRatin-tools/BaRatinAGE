package controleur;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import commons.DirectoryUtils;
import commons.Frame_YesNoQuestion;
import commons.Observation;
import commons.ReadWrite;
import commons.TimeSerie;
import moteur.ConfigHydrau;
import moteur.Envelop;
import moteur.GaugingSet;
import moteur.Hydrograph;
import moteur.Limnigraph;
import moteur.MCMCoptions;
import moteur.RatingCurve;
import moteur.RemnantError;
import moteur.Spaghetti;
import moteur.Station;
import vue.AllDonePanel;
import vue.ConfigHydrauPanel;
import vue.ExceptionPanel;
import vue.HydrographPanel;
import vue.MainFrame;
import vue.RatingCurvePanel;
import Utils.Config;
import Utils.Defaults;
import Utils.Dico;

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
	private ConfigControl configController = new ConfigControl();
	private ResultControl resultController = new ResultControl();
	private Station station = Station.getInstance();
	private Process exeProc = null;

	private ExeControl() {
	}

	private void engine(boolean[] options, GaugingSet gaugings, ConfigHydrau hydrau,
			RemnantError remnant, MCMCoptions mcmc, RatingCurve rc, Limnigraph limni, Hydrograph hydro)
			throws IOException, Exception, InterruptedException, FileNotFoundException {
		// Write config files and make a copy in recycle folder
		new File(Defaults.tempWorkspace).mkdir();
		configController.write_engine(options, gaugings, hydrau, remnant, mcmc, rc, limni, hydro);
		DirectoryUtils.deleteDirContent(new File(Defaults.recycleDir));
		DirectoryUtils.copyFilesInDir(new File(Defaults.tempWorkspace), new File(Defaults.recycleDir));
		// Pilot executable
		exeProc = Runtime.getRuntime().exec(new String[] { Defaults.exeFile.trim() }, null,
				new File(Defaults.exeDir.trim()));
		InputStream exeOut = exeProc.getInputStream();
		BufferedReader is = new BufferedReader(new InputStreamReader(exeOut));
		String line = "";
		String foo = null;
		while ((foo = is.readLine()) != null) {
			System.out.println(foo);
			line = foo;
		}
		System.out.flush();
		int exitcode = exeProc.waitFor();
		if (exitcode != 0) {
			throw new InterruptedException(dico.entry("ExeAborted") +
					System.lineSeparator() +
					dico.entry("Error") + ":" + exeProc.exitValue() +
					System.lineSeparator() +
					dico.entry("Message") + ":" + line);
		}
		// read result files and update panels
		// prior RC
		if (options[0] == true) {
			Envelop env = resultController.readEnvelop(
					new File(Defaults.tempWorkspace, Defaults.results_PriorRC_env + "_010.txt").getAbsolutePath());
			hydrau.setPriorEnv(env);
			Spaghetti spag = resultController.readSpaghetti(
					new File(Defaults.tempWorkspace, Defaults.results_PriorRC_spag + "_010.txt").getAbsolutePath());
			hydrau.setPriorSpag(spag);
		}
		// raw MCMC
		if (options[1] == true) {
			Double[][] y = ReadWrite.read(new File(Defaults.tempWorkspace, mcmc.getFilename()).getAbsolutePath(),
					Defaults.resultSep, 1);
			rc.setMcmc(y);
		}
		// Post-processing and posterior RC
		if (options[2] == true) {
			// Cooked & summary MCMC
			Double[][] y = ReadWrite.read(
					new File(Defaults.tempWorkspace, Defaults.results_CookedMCMC).getAbsolutePath(), Defaults.resultSep,
					1);
			rc.setMcmc_cooked(y);
			y = ReadWrite.read(new File(Defaults.tempWorkspace, Defaults.results_SummaryMCMC).getAbsolutePath(),
					Defaults.resultSep, 1, 1);
			rc.setMcmc_summary(y);
			// summary HQ
			y = ReadWrite.read(new File(Defaults.tempWorkspace, Defaults.results_SummaryGaugings).getAbsolutePath(),
					Defaults.resultSep, 1);
			rc.setHQ(y);
			// Envelops and Spaghettis
			Envelop env = resultController.readEnvelop(
					new File(Defaults.tempWorkspace, Defaults.results_PostRC_env + "_011.txt").getAbsolutePath());
			rc.setEnv_total(env);
			env = resultController.readEnvelop(
					new File(Defaults.tempWorkspace, Defaults.results_PostRC_env + "_010.txt").getAbsolutePath());
			rc.setEnv_param(env);
			Spaghetti spag = resultController.readSpaghetti(
					new File(Defaults.tempWorkspace, Defaults.results_PostRC_spag + "_011.txt").getAbsolutePath());
			rc.setSpag_total(spag);
			spag = resultController.readSpaghetti(
					new File(Defaults.tempWorkspace, Defaults.results_PostRC_spag + "_010.txt").getAbsolutePath());
			rc.setSpag_param(spag);
		}
		// h2Q propagation
		if (options[3] == true) {
			Envelop env = resultController.readEnvelop(
					new File(Defaults.tempWorkspace, Defaults.results_Qt_env + "_111.txt").getAbsolutePath());
			env.setX(toYear(limni));
			hydro.setEnv_total(env);
			env = resultController.readEnvelop(
					new File(Defaults.tempWorkspace, Defaults.results_Qt_env + "_100.txt").getAbsolutePath());
			env.setX(toYear(limni));
			hydro.setEnv_h(env);
			env = resultController.readEnvelop(
					new File(Defaults.tempWorkspace, Defaults.results_Qt_env + "_110.txt").getAbsolutePath());
			env.setX(toYear(limni));
			hydro.setEnv_hparam(env);
			hydro.setObservations(new ArrayList<Observation>());
			for (int i = 0; i < limni.getObservations().size(); i++) {
				hydro.addObservation(
						new Observation(env.getMaxpost()[i], limni.getObservations().get(i).getObsDate(), null));
			}
			// TODO: read spag, but this could take several gigas in memory, need to think
			// about it
			if (config.isSaveHydroSpag()) {
				Spaghetti spag = resultController.readSpaghetti(
						new File(Defaults.tempWorkspace, Defaults.results_Qt_spag + "_111.txt").getAbsolutePath());
				spag.setX(toYear(limni));
				hydro.setSpag_total(spag);
				spag = resultController.readSpaghetti(
						new File(Defaults.tempWorkspace, Defaults.results_Qt_spag + "_110.txt").getAbsolutePath());
				spag.setX(toYear(limni));
				hydro.setSpag_hparam(spag);
				spag = resultController.readSpaghetti(
						new File(Defaults.tempWorkspace, Defaults.results_Qt_spag + "_100.txt").getAbsolutePath());
				spag.setX(toYear(limni));
				hydro.setSpag_h(spag);
			}
		}
		// Clean up
		DirectoryUtils.deleteDirContent(new File(Defaults.tempWorkspace));
		if (options[1]) {
			new AllDonePanel(null,
					dico.entry("AllDone") + System.getProperty("line.separator") + dico.entry("CheckPar"));
		} else {
			new AllDonePanel(null, dico.entry("AllDone"));
		}
	}

	public void run(boolean[] options, String id_gaugings, String id_hydrau, Integer indx_remnant, String id_mcmc,
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
			engine(options, gaugings, hydrau, remnant, mcmc, rc, null, null);
			// update panels
			if (id_hydrau != null) {
				new ConfigHydrauPanel(id_hydrau, true);
			}
			if (id_rc != null) {
				new RatingCurvePanel(id_rc, true);
			}
		} catch (FileNotFoundException e3) {
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("FileNotFound"));
		} catch (IOException e3) {
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("ConfigWriteProblem"));
		} catch (InterruptedException e3) {
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("RunProblem"));
		} catch (Exception e3) {
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
			ReadWrite.write(rc.getMcmc(), head, new File(Defaults.tempWorkspace, mcmc.getFilename()).getAbsolutePath(),
					Defaults.resultSep);
			engine(new boolean[] { false, false, false, true }, gaugings, hydrau, remnant, mcmc, rc, limni, hydro);
			// update panels
			if (id_hydro != null) {
				new HydrographPanel(id_hydro, true);
			}
		} catch (FileNotFoundException e3) {
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("FileNotFound"));
		} catch (IOException e3) {
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("ConfigWriteProblem"));
		} catch (InterruptedException e3) {
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("RunProblem"));
		} catch (Exception e3) {
			new ExceptionPanel(MainFrame.getInstance(), dico.entry("UnidentifiedProblem"));
		} finally {
			MainFrame.getInstance().setCursor(Cursor.getDefaultCursor());
		}
	}

	private Double[] toYear(TimeSerie z) {
		int n = z.length();
		Double[] x = new Double[n];
		for (int i = 0; i < n; i++) {
			x[i] = z.getObservations().get(i).getObsDate().toYear();
		}
		return (x);
	}

	public Process getExeProc() {
		return exeProc;
	}

	public void setExeProc(Process exeProc) {
		this.exeProc = exeProc;
	}

}

package controleur;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import moteur.ConfigHydrau;
import moteur.Gauging;
import moteur.GaugingSet;
import moteur.Hydrograph;
import moteur.Limnigraph;
import moteur.MCMCoptions;
import moteur.RatingCurve;
import moteur.RemnantError;
import Utils.Defaults;

public class ConfigControl {

	private static ConfigControl instance;

	public static synchronized ConfigControl getInstance(){
		if (instance == null){
			instance = new ConfigControl();
		}
		return instance;
	}

	public ConfigControl() {
	}

	public void write_engine(boolean[] options,GaugingSet gaugings,ConfigHydrau hydrau,
			RemnantError remnant,MCMCoptions mcmc,RatingCurve rc,
			Limnigraph limni,Hydrograph hydro) throws IOException{
		// master Config_BaRatin
		writeConfigBaRatin();
		// RunOptions
		writeRunOptions(options);
		// Data (config + XY.BAD)
		writeData(gaugings);
		// Rating curve (RC + matrix + prior RC)
		writeRatingCurve(hydrau);
		// Remnant Sigma
		writeRemantSigma(remnant);
		// MCMC
		writeMCMC(mcmc);
		// Post-Process
		writePostProcessing(rc);
		// Propagation
		writeH2Q(limni,hydro);
	}

	private void writeConfigBaRatin() throws IOException{
		File f=new File(Defaults.exeConfigFile);
		if (!f.exists()) {f.createNewFile();}
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("'"+Defaults.tempWorkspace.trim()+"'");
		bw.close();
	}

	private void writeRunOptions(boolean[] options) throws IOException{
		File f=new File(Defaults.config_RunOptions);
		if (!f.exists()) {f.createNewFile();}
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i=0;i<options.length;i++){
			if(options[i]) {bw.write(".true.");} else {bw.write(".false.");}
			bw.newLine();
		}
		bw.close();
	}

	private void writeData(GaugingSet gaugings) throws IOException{
		int nobs,ntot=-1;
		if(gaugings==null){
			nobs=0;ntot=0;
		}
		else {
			ntot=gaugings.getGaugings().size();
			nobs=0;
			for(int i=0;i<ntot;i++){
				if(gaugings.getGaugings().get(i).getActive()){nobs=nobs+1;}
			}
		}		
		// Config file
		File f=new File(Defaults.config_Data);
		if (!f.exists()) {f.createNewFile();}
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("'"+Defaults.config_XYBAD.trim()+"'");bw.newLine();
		bw.write("1");bw.newLine();
		bw.write(Integer.toString(nobs));bw.newLine();
		bw.write("4");bw.newLine();
		bw.write("1");bw.newLine();
		bw.write("2");bw.newLine();
		bw.write("3");bw.newLine();
		bw.write("4");bw.newLine();
		bw.close();		
		// XY.BAD file
		f=new File(Defaults.config_XYBAD);
		if (!f.exists()) {f.createNewFile();}
		fw = new FileWriter(f.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		bw.write("H[m] sigmaH[m] Q[m3/s] sigmaQ[m3/s]");bw.newLine();
		Gauging g=new Gauging();
		for (int i=0;i<ntot;i++){
			g=gaugings.getGaugings().get(i);
			if(g.getActive()){
				bw.write(Double.toString(g.getH()) + Defaults.txtSep +
						Double.toString(0.5*g.getuH()) + Defaults.txtSep +
						Double.toString(g.getQ()) + Defaults.txtSep +
						Double.toString(0.5*g.getuQ()*g.getQ()*0.01));
				bw.newLine();
			}
		}
		bw.close();
	}

	private void writeRatingCurve(ConfigHydrau hydrau) throws IOException{
		// RatingCurve
		File f=new File(Defaults.config_RatingCurve);
		if (!f.exists()) {f.createNewFile();}
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("RC_General");bw.newLine();
		bw.write(Integer.toString(3*hydrau.getControls().size()));bw.newLine();
		bw.write(hydrau.toString());
		bw.close();	
		// control matrix
		f=new File(Defaults.config_ControlMatrix);
		if (!f.exists()) {f.createNewFile();}
		fw = new FileWriter(f.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		bw.write(hydrau.getMatrix().toString());
		bw.close();
		// prior RC
		f=new File(Defaults.config_PriorRC);
		if (!f.exists()) {f.createNewFile();}
		fw = new FileWriter(f.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		bw.write(hydrau.getPriorRCoptions().toString());bw.newLine();
		bw.write(".true.");bw.newLine();
		bw.write(Defaults.results_PriorRC_spag);bw.newLine();
		bw.write(".true.");bw.newLine();
		bw.write(Defaults.results_PriorRC_env);bw.newLine();		
		bw.close();
	}

	private void writeRemantSigma(RemnantError remnant) throws IOException{
		File f=new File(Defaults.config_RemnantSigma);
		if (!f.exists()) {f.createNewFile();}
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(remnant.toString());
		bw.close();	
	}

	private void writeMCMC(MCMCoptions mcmc) throws IOException{
		File f=new File(Defaults.config_MCMC);
		if (!f.exists()) {f.createNewFile();}
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(mcmc.toString_mini());bw.newLine();
		bw.write("0");bw.newLine();
		bw.write("cosmetics");bw.newLine();
		bw.write("0.1");bw.newLine();
		bw.write("notused");bw.newLine();
		bw.write("notused");bw.newLine();
		bw.write("cosmetics");bw.newLine();
		bw.write(".true.");bw.newLine();
		bw.write("'"+mcmc.getFilename().trim()+"'");bw.newLine();
		bw.close();	
	}

	private void writePostProcessing(RatingCurve rc) throws IOException{
		if(rc==null){return;}
		File f=new File(Defaults.config_PostProcessing);
		if (!f.exists()) {f.createNewFile();}
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("**** Cooked MCMC runs ****");bw.newLine();	
		bw.write(".true.");bw.newLine();
		bw.write(Defaults.results_CookedMCMC);bw.newLine();
		bw.write("**** Summary of MCMC runs ****");bw.newLine();	
		bw.write(".true.");bw.newLine();
		bw.write(Defaults.results_SummaryMCMC);bw.newLine();
		bw.write("**** Summary of H-Q gauging data ****");bw.newLine();	
		bw.write(".true.");bw.newLine();
		bw.write(Defaults.results_SummaryGaugings);bw.newLine();
		bw.write("**** Rating Curve ****");bw.newLine();	
		bw.write(rc.getPostRCoptions().toString());bw.newLine();	
		bw.write(".true.");bw.newLine();
		bw.write(Defaults.results_PostRC_spag);bw.newLine();	
		bw.write(".true.");bw.newLine();
		bw.write(Defaults.results_PostRC_env);bw.newLine();	
		bw.write("2");bw.newLine();
		bw.write("1,0");bw.newLine();
		bw.write("1,1");bw.newLine();
		bw.close();
	}
	
	private void writeH2Q(Limnigraph limni,Hydrograph hydro) throws IOException{
		if(hydro==null){return;}
		// H2Q
		File f=new File(Defaults.config_H2Q);
		if (!f.exists()) {f.createNewFile();}
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("'"+Defaults.config_HHBAD.trim()+"'");bw.newLine();	
		bw.write(".true.");bw.newLine();
		bw.write(Defaults.results_Qt_spag);bw.newLine();
		bw.write(".true.");bw.newLine();
		bw.write(Defaults.results_Qt_env);bw.newLine();
		bw.write("3");bw.newLine();
		bw.write("1,0,0");bw.newLine();
		bw.write("1,1,0");bw.newLine();
		bw.write("1,1,1");bw.newLine();
		bw.close();
		// HH.BAD
		f=new File(Defaults.config_HHBAD);
		if (!f.exists()) {f.createNewFile();}
		fw = new FileWriter(f.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		bw.write("H  Hstd  Hbias_indx  Hbias");bw.newLine();
		for (int i=0;i<limni.length();i++){
				bw.write(Double.toString(limni.getObservations().get(i).getValue()) + Defaults.txtSep +
						Double.toString(0.5*limni.getuH()[i]) + Defaults.txtSep +
						Double.toString(limni.getbHindx()[i]) + Defaults.txtSep +
						Double.toString(0.5*limni.getbH()[i]));
				bw.newLine();
		}
		bw.close();
	}

}

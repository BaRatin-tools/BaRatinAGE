package serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import Utils.Defaults;
import commons.ReadWrite;
import moteur.Envelop;
import moteur.PostRatingCurveOptions;
import moteur.RatingCurve;
import moteur.Spaghetti;

public class RatingCurve_DAO extends RatingCurve implements DAO {

	private String folder;
	// Constants
	private static final String FILE_PROPERTIES="Properties.txt";
	private static final String FILE_POSTERIOR="PosteriorOptions.txt";
	private static final String FILE_ENV_TOTAL="EnvelopTotal.txt";
	private static final String FILE_ENV_PARAM="EnvelopParam.txt";
	private static final String FILE_SPAG_TOTAL="SpagTotal.txt";
	private static final String FILE_SPAG_PARAM="SpagParam.txt";
	private static final String FILE_MCMC="MCMC.txt";
	private static final String FILE_MCMC_COOKED="MCMCcooked.txt";
	private static final String FILE_MCMC_SUMMARY="MCMCsummary.txt";
	private static final String FILE_HQ="HQ.txt";

	public RatingCurve_DAO(RatingCurve rc,String folder){
		super(rc);
		this.folder=folder;
	}

	@Override
	public void create() throws FileNotFoundException, IOException, Exception {
		File f;
		FileWriter fw;
		BufferedWriter bw;
		///////////////////////////////////////////////////////////////////////
		// properties
		f=new File(this.folder.trim(),FILE_PROPERTIES);
		if (!f.exists()) {f.createNewFile();}
		fw = new FileWriter(f.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		DAOtools.safeWrite(bw,this.getName());		
		DAOtools.safeWrite(bw,this.getDescription());
		DAOtools.safeWrite(bw,this.getHydrau_id());
		DAOtools.safeWrite(bw,this.getGauging_id());
		DAOtools.safeWrite(bw,this.getError_id());
		bw.close();
		///////////////////////////////////////////////////////////////////////
		// Posterior Options
		f=new File(this.folder.trim(),FILE_POSTERIOR);
		if (!f.exists()) {f.createNewFile();}
		fw = new FileWriter(f.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		if(this.getPostRCoptions()==null){
			bw.newLine();bw.newLine();bw.newLine();
			}
		else {
			DAOtools.safeWrite(bw,this.getPostRCoptions().gethMin());
			DAOtools.safeWrite(bw,this.getPostRCoptions().gethMax());
			DAOtools.safeWrite(bw,this.getPostRCoptions().gethStep());
			DAOtools.safeWrite(bw,this.getPostRCoptions().getnStep());
			}
		bw.close();
		///////////////////////////////////////////////////////////////////////
		// Total & Parametric Envelops
		Envelop env = this.getEnv_total();
		if(env!=null){
			ReadWrite.write(env.toMatrix(),null,new File(this.folder.trim(),FILE_ENV_TOTAL).getAbsolutePath(),Defaults.barSep);
		}
		env = this.getEnv_param();
		if(env!=null){
			ReadWrite.write(env.toMatrix(),null,new File(this.folder.trim(),FILE_ENV_PARAM).getAbsolutePath(),Defaults.barSep);
		}
		///////////////////////////////////////////////////////////////////////
		// Total & Parametric spaghetti
		Spaghetti spag = this.getSpag_total();
		if(spag!=null){
			ReadWrite.write(spag.toMatrix(),null,new File(this.folder.trim(),FILE_SPAG_TOTAL).getAbsolutePath(),Defaults.barSep);
		}
		spag = this.getSpag_param();
		if(spag!=null){
			ReadWrite.write(spag.toMatrix(),null,new File(this.folder.trim(),FILE_SPAG_PARAM).getAbsolutePath(),Defaults.barSep);
		}
		///////////////////////////////////////////////////////////////////////
		// MCMC
		if(this.getMcmc()!=null){
			ReadWrite.write(this.getMcmc(),null,new File(this.folder.trim(),FILE_MCMC).getAbsolutePath(),Defaults.barSep);
		}
		if(this.getMcmc_cooked()!=null){
			ReadWrite.write(this.getMcmc_cooked(),null,new File(this.folder.trim(),FILE_MCMC_COOKED).getAbsolutePath(),Defaults.barSep);
		}
		if(this.getMcmc_summary()!=null){
			ReadWrite.write(this.getMcmc_summary(),null,new File(this.folder.trim(),FILE_MCMC_SUMMARY).getAbsolutePath(),Defaults.barSep);
		}
		///////////////////////////////////////////////////////////////////////
		// HQ
		if(this.getHQ()!=null){
			ReadWrite.write(this.getHQ(),null,new File(this.folder.trim(),FILE_HQ).getAbsolutePath(),Defaults.barSep);
		}
	}

	@Override
	public void read() throws FileNotFoundException, IOException, Exception {
		File f;
		Scanner sc;
		///////////////////////////////////////////////////////////////////////
		//properties
		f=new File(this.folder.trim(),FILE_PROPERTIES);
		sc = new Scanner(f);
		this.setName(sc.nextLine());
		this.setDescription(sc.nextLine());
		this.setHydrau_id(sc.nextLine());
		this.setGauging_id(sc.nextLine());
		this.setError_id(sc.nextLine());
		sc.close();	
		///////////////////////////////////////////////////////////////////////
		// Posterior Options
		f=new File(this.folder.trim(),FILE_POSTERIOR);
		sc = new Scanner(f);
		PostRatingCurveOptions post = new PostRatingCurveOptions();
		post.sethMin(DAOtools.safeRead_d(sc.nextLine()));
		post.sethMax(DAOtools.safeRead_d(sc.nextLine()));
		post.sethStep(DAOtools.safeRead_d(sc.nextLine()));
		post.setnStep(DAOtools.safeRead_i(sc.nextLine()));
		sc.close();
		this.setPostRCoptions(post);
		///////////////////////////////////////////////////////////////////////
		// Total & Parametric Envelops
		f=new File(this.folder.trim(),FILE_ENV_TOTAL);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
			this.setEnv_total(new Envelop(w));
		}
		f=new File(this.folder.trim(),FILE_ENV_PARAM);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
			this.setEnv_param(new Envelop(w));
		}
		///////////////////////////////////////////////////////////////////////
		// Total & Parametric spaghetti
		f=new File(this.folder.trim(),FILE_SPAG_TOTAL);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
			this.setSpag_total(new Spaghetti(w));
		}		
		f=new File(this.folder.trim(),FILE_SPAG_PARAM);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
			this.setSpag_param(new Spaghetti(w));
		}		
		///////////////////////////////////////////////////////////////////////
		// MCMC
		f=new File(this.folder.trim(),FILE_MCMC);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
			this.setMcmc(w);
		}		
		f=new File(this.folder.trim(),FILE_MCMC_COOKED);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
			this.setMcmc_cooked(w);
		}		
		f=new File(this.folder.trim(),FILE_MCMC_SUMMARY);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
			this.setMcmc_summary(w);
		}		
		///////////////////////////////////////////////////////////////////////
		// HQ
		f=new File(this.folder.trim(),FILE_HQ);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
			this.setHQ(w);
		}		


		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
		
	}

}

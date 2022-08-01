package serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import Utils.Config;
import Utils.Defaults;
import commons.Observation;
import commons.ReadWrite;
import moteur.Envelop;
import moteur.Hydrograph;
import moteur.Spaghetti;

public class Hydrograph_DAO extends Hydrograph implements DAO{
	private String folder;
	// Constants
	private static final String FILE_PROPERTIES="Properties.txt";
	private static final String FILE_ENV_TOTAL="EnvelopTotal.txt";
	private static final String FILE_ENV_HPARAM="EnvelopHParam.txt";
	private static final String FILE_ENV_H="EnvelopH.txt";
	private static final String FILE_SPAG_TOTAL="SpagTotal.txt";
	private static final String FILE_SPAG_HPARAM="SpagHParam.txt";
	private static final String FILE_SPAG_H="SpagH.txt";
	private static final String FILE_HYDRO="Hydrograph.txt";
	private Config config=Config.getInstance();

	public Hydrograph_DAO(Hydrograph h,String folder){
		super(h);
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
		DAOtools.safeWrite(bw,this.getType());
		DAOtools.safeWrite(bw,this.getLimni_id());
		DAOtools.safeWrite(bw,this.getRc_id());
		bw.close();
		///////////////////////////////////////////////////////////////////////
		// observations
		List<Observation> hydro = this.getObservations();
		if(hydro!=null){
			ReadWrite.write(this.toMatrix(),null,new File(this.folder.trim(),FILE_HYDRO).getAbsolutePath(),Defaults.barSep);
		}
		///////////////////////////////////////////////////////////////////////
		// Envelops
		Envelop env = this.getEnv_total();
		if(env!=null){
			ReadWrite.write(env.toMatrix(),null,new File(this.folder.trim(),FILE_ENV_TOTAL).getAbsolutePath(),Defaults.barSep);
		}
		env = this.getEnv_hparam();
		if(env!=null){
			ReadWrite.write(env.toMatrix(),null,new File(this.folder.trim(),FILE_ENV_HPARAM).getAbsolutePath(),Defaults.barSep);
		}
		env = this.getEnv_h();
		if(env!=null){
			ReadWrite.write(env.toMatrix(),null,new File(this.folder.trim(),FILE_ENV_H).getAbsolutePath(),Defaults.barSep);
		}
		///////////////////////////////////////////////////////////////////////
		// Spaghetti
		if(config.isSaveHydroSpag()){
			Spaghetti spag = this.getSpag_total();
			if(spag!=null){
				ReadWrite.write(spag.toMatrix(),null,new File(this.folder.trim(),FILE_SPAG_TOTAL).getAbsolutePath(),Defaults.barSep);
			}
			spag = this.getSpag_hparam();
			if(spag!=null){
				ReadWrite.write(spag.toMatrix(),null,new File(this.folder.trim(),FILE_SPAG_HPARAM).getAbsolutePath(),Defaults.barSep);
			}
			spag = this.getSpag_h();
			if(spag!=null){
				ReadWrite.write(spag.toMatrix(),null,new File(this.folder.trim(),FILE_SPAG_H).getAbsolutePath(),Defaults.barSep);
			}
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
		this.setDescription(sc.nextLine());
		this.setLimni_id(sc.nextLine());
		this.setRc_id(sc.nextLine());
		sc.close();	
		///////////////////////////////////////////////////////////////////////
		// Envelops
		f=new File(this.folder.trim(),FILE_ENV_TOTAL);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
			this.setEnv_total(new Envelop(w));
		}
		f=new File(this.folder.trim(),FILE_ENV_HPARAM);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
			this.setEnv_hparam(new Envelop(w));
		}
		f=new File(this.folder.trim(),FILE_ENV_H);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
			this.setEnv_h(new Envelop(w));
		}
		///////////////////////////////////////////////////////////////////////
		// Spaghetti
		if(config.isSaveHydroSpag()){
			f=new File(this.folder.trim(),FILE_SPAG_TOTAL);
			if(f.exists()){
				Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
				this.setSpag_total(new Spaghetti(w));
			}	
			f=new File(this.folder.trim(),FILE_SPAG_HPARAM);
			if(f.exists()){
				Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
				this.setSpag_hparam(new Spaghetti(w));
			}	
			f=new File(this.folder.trim(),FILE_SPAG_H);
			if(f.exists()){
				Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
				this.setSpag_h(new Spaghetti(w));
			}	
		}
		///////////////////////////////////////////////////////////////////////
		// Observations	
		f=new File(this.folder.trim(),FILE_HYDRO);
		if(f.exists()){
			Double[][] w=ReadWrite.read(f.getAbsolutePath(),Defaults.barSep,0);
			if(w.length>0){this.fromMatrix(w);}
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

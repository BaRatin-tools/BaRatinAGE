package serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import commons.Distribution;
import commons.Parameter;
import commons.ReadWrite;
import Utils.Defaults;
import moteur.BonnifaitMatrix;
import moteur.ConfigHydrau;
import moteur.Envelop;
import moteur.HydrauControl;
import moteur.PriorRatingCurveOptions;
import moteur.Spaghetti;

public class HydrauConfig_DAO extends ConfigHydrau implements DAO {

	private String folder;
	// Constants
	private static final String FILE_PROPERTIES="Properties.txt";
	private static final String FILE_MATRIX="BonnifaitMatrix.txt";
	private static final String FILE_CONTROL="Control.txt";
	private static final String FILE_PRIOR="PriorOptions.txt";
	private static final String FILE_PRIOR_ENV="PriorEnvelop.txt";
	private static final String FILE_PRIOR_SPAG="PriorSpaghetti.txt";

	public HydrauConfig_DAO(ConfigHydrau h,String folder){
		super(h);
		this.folder=folder;
	}

	@Override
	public void create() throws FileNotFoundException,IOException,Exception {
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
		if(this.getControls()==null){DAOtools.safeWrite(bw,0);} else {DAOtools.safeWrite(bw,this.getControls().size());}		
		bw.close();
		///////////////////////////////////////////////////////////////////////
		// Bonnifait Matrix
		BonnifaitMatrix bonnifait = this.getMatrix();
		if(bonnifait!=null){
			ArrayList<ArrayList<Boolean>> m = this.getMatrix().getMatrix();
			// transform m into double matrix
			Double[][] md=new Double[m.size()][m.size()];			
			if(m!=null){
				for(int i=0;i<m.size();i++){
					for(int j=0;j<=i;j++){
						if(m.get(j).get(i)) {md[i][j]=0.0;md[j][i]=1.0;}
						else{md[j][i]=0.0;md[i][j]=0.0;}
					}				
				}
			}
			ReadWrite.write(md,null,new File(this.folder.trim(),FILE_MATRIX).getAbsolutePath(),Defaults.barSep);
		}
		///////////////////////////////////////////////////////////////////////
		// Controls
		ArrayList<HydrauControl> control = this.getControls();
		if(control!=null){
			for(int i=0;i<control.size();i++){
				f=new File(this.folder.trim(),Integer.toString(i)+"_"+FILE_CONTROL);
				if (!f.exists()) {f.createNewFile();}
				fw = new FileWriter(f.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				DAOtools.safeWrite(bw,control.get(i).getDescription());	
				DAOtools.safeWrite(bw,control.get(i).getType());	
				bw.write(control.get(i).toString_kac());bw.newLine();
				Parameter[] specifix = control.get(i).getSpecifix();
				if(specifix!=null){
					bw.write(Integer.toString(specifix.length));bw.newLine();
					for(int j=0;j<specifix.length;j++){
						bw.write(specifix[j].toString());bw.newLine();
					}
				}
				else{
					bw.write("0");bw.newLine();
				}
				bw.close();
			}
		}
		///////////////////////////////////////////////////////////////////////
		// Prior RC option
		f=new File(this.folder.trim(),FILE_PRIOR);
		if (!f.exists()) {f.createNewFile();}
		fw = new FileWriter(f.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		if(this.getPriorRCoptions()==null){
			bw.newLine();bw.newLine();bw.newLine();bw.newLine();
		}
		else {
			DAOtools.safeWrite(bw,this.getPriorRCoptions().getnSim());
			DAOtools.safeWrite(bw,this.getPriorRCoptions().gethMin());
			DAOtools.safeWrite(bw,this.getPriorRCoptions().gethMax());
			DAOtools.safeWrite(bw,this.getPriorRCoptions().gethStep());
			DAOtools.safeWrite(bw,this.getPriorRCoptions().getnStep());
		}
		bw.close();
		///////////////////////////////////////////////////////////////////////
		// Prior Envelop
		Envelop env = this.getPriorEnv();
		if(env!=null){
			ReadWrite.write(env.toMatrix(),null,new File(this.folder.trim(),FILE_PRIOR_ENV).getAbsolutePath(),Defaults.barSep);
		}
		///////////////////////////////////////////////////////////////////////
		// Prior spag
		Spaghetti spag = this.getPriorSpag();
		if(spag!=null){
			ReadWrite.write(spag.toMatrix(),null,new File(this.folder.trim(),FILE_PRIOR_SPAG).getAbsolutePath(),Defaults.barSep);
		}
	}

	@Override
	public void read() throws IOException, Exception {
		File f;
		Scanner sc;
		///////////////////////////////////////////////////////////////////////
		//properties
		f=new File(this.folder.trim(),FILE_PROPERTIES);
		sc = new Scanner(f);
		this.setName(sc.nextLine());
		this.setDescription(sc.nextLine());
		int ncontrol=DAOtools.safeRead_i(sc.nextLine());
		sc.close();
		if(ncontrol>0) {
			///////////////////////////////////////////////	////////////////////////
			// Bonnifait Matrix
			f=new File(this.folder.trim(),FILE_MATRIX);
			Double[][] md=ReadWrite.read(f.getAbsolutePath(),Defaults.barSep,0);
			ArrayList<ArrayList<Boolean>> mat = new ArrayList<ArrayList<Boolean>>();
			ArrayList<Boolean> foo;
			for(int j=0;j<ncontrol;j++){
				foo=new ArrayList<Boolean>();
				for(int i=0;i<ncontrol;i++){foo.add(md[j][i]==1.0);}
				mat.add(foo);
			}
			this.setMatrix(new BonnifaitMatrix(mat));
			///////////////////////////////////////////////////////////////////////
			// Controls
			ArrayList<HydrauControl> controlList = new ArrayList<HydrauControl>();
			HydrauControl control;
			for(int i=0;i<ncontrol;i++){
				control=new HydrauControl();
				f=new File(this.folder.trim(),Integer.toString(i)+"_"+FILE_CONTROL);
				sc = new Scanner(f);
				control.setDescription(sc.nextLine());
				String type=sc.nextLine(); if(!type.equals("")){control.setType(Integer.valueOf(type));}
				// par K, A, C
				control.setK(readParBlock(sc,"K"));
				control.setA(readParBlock(sc,"A"));
				control.setC(readParBlock(sc,"C"));
				int nspec=Integer.valueOf(sc.nextLine());
				if(nspec>0){
					Parameter [] specifix=new Parameter[nspec];
					for(int j=0;j<nspec;j++){
						specifix[j]=readParBlock(sc,Integer.toString(j));
					}
					control.setSpecifix(specifix);
				}
				sc.close();
				controlList.add(control);
			}
			this.setControls(controlList);
			///////////////////////////////////////////////////////////////////////
			// Prior Envelop
			f=new File(this.folder.trim(),FILE_PRIOR_ENV);
			if(f.exists()){
				Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
				this.setPriorEnv(new Envelop(w));
			}
			///////////////////////////////////////////////////////////////////////
			// Prior Spag
			f=new File(this.folder.trim(),FILE_PRIOR_SPAG);
			if(f.exists()){
				Double[][] w=ReadWrite.read(f.getAbsolutePath(), Defaults.barSep, 0);
				this.setPriorSpag(new Spaghetti(w));
			}			
		}
		///////////////////////////////////////////////////////////////////////
		// Prior RC option
		f=new File(this.folder.trim(),FILE_PRIOR);
		if(ncontrol>0) {
			sc = new Scanner(f);
			PriorRatingCurveOptions prior = new PriorRatingCurveOptions();
			prior.setnSim(DAOtools.safeRead_i(sc.nextLine()));
			prior.sethMin(DAOtools.safeRead_d(sc.nextLine()));
			prior.sethMax(DAOtools.safeRead_d(sc.nextLine()));
			prior.sethStep(DAOtools.safeRead_d(sc.nextLine()));
			prior.setnStep(DAOtools.safeRead_i(sc.nextLine()));
			sc.close();
			this.setPriorRCoptions(prior);
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

	private Parameter readParBlock(Scanner sc,String name){
		Parameter par=new Parameter();
		sc.nextLine(); // name
		sc.nextLine(); // initval
		String dist=sc.nextLine();//dist
		String parline=sc.nextLine();//par
		String [] foo=parline.split(",+");
		par.setName(name);
		par.setPrior(null);
		if(!dist.equals("Gaussian")){return(par);}
		if(foo.length!=2){return(par);}
		if(foo[0].equals("")){return(par);}
		if(foo[1].equals("")){return(par);}
		Double mean=Double.parseDouble(foo[0]);
		Double sd=Double.parseDouble(foo[1]);		
		par.setPrior(new Distribution("Gaussian",2,new String[] {"mean","standard_deviation"},new Double[]{mean,sd}));
		par.setValue(mean);
		return(par);
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

}

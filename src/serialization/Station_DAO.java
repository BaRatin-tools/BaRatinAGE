package serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import commons.DirectoryUtils;
import commons.textFileReader;
import moteur.ConfigHydrau;
import moteur.Dataset;
import moteur.GaugingSet;
import moteur.Hydrograph;
import moteur.Limnigraph;
import moteur.RatingCurve;
import moteur.Station;

public class Station_DAO implements DAO {

	private String folder;
	// Constants
	private Station station=Station.getInstance();
	private static final String FILE_PROPERTIES="properties.txt";
	private static final String FOLDER_HYDRAU="HydraulicConfiguration";
	private static final String FOLDER_GAUGING="GaugingSet";
	private static final String FOLDER_LIMNI="Limnigraph";
	private static final String FOLDER_RC="RatingCurve";
	private static final String FOLDER_HYDRO="Hydrograph";

	public Station_DAO(String folder){
		this.folder=folder;
	}

	@Override
	public void create() throws FileNotFoundException, IOException, Exception {
		File f;
		FileWriter fw;
		BufferedWriter bw;
		// properties
		File temp = new File(this.folder.trim());
		temp.mkdirs();
		DirectoryUtils.deleteDirContent(temp);
		f=new File(this.folder.trim(),FILE_PROPERTIES);
		if (!f.exists()) {f.createNewFile();}
		fw = new FileWriter(f.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		writeSize(bw,station.getConfig());
		writeSize(bw,station.getGauging());
		writeSize(bw,station.getLimni());
		writeSize(bw,station.getRc());
		writeSize(bw,station.getHydrograph());
		bw.close();
		// Hydraulic configurations
		Dataset<ConfigHydrau> hydrau = station.getConfig();
		HydrauConfig_DAO h;
		if(hydrau!=null){
			for(int i=0;i<hydrau.getSize();i++){
				f=new File(new File(this.folder.trim(),FOLDER_HYDRAU).getAbsolutePath(),Integer.toString(i));
				f.mkdirs();
				h=new HydrauConfig_DAO(hydrau.getItemAt(i),f.getAbsolutePath());
				h.create();
			}
		}
		// Gauging sets
		Dataset<GaugingSet> gauging = station.getGauging();
		GaugingSet_DAO g;
		if(gauging!=null){
			for(int i=0;i<gauging.getSize();i++){
				f=new File(new File(this.folder.trim(),FOLDER_GAUGING).getAbsolutePath(),Integer.toString(i));
				f.mkdirs();
				g=new GaugingSet_DAO(gauging.getItemAt(i),f.getAbsolutePath());
				g.create();
			}
		}
		// Limnigraphs
		Dataset<Limnigraph> limni = station.getLimni();
		Limnigraph_DAO l;
		if(limni!=null){
			for(int i=0;i<limni.getSize();i++){
				f=new File(new File(this.folder.trim(),FOLDER_LIMNI).getAbsolutePath(),Integer.toString(i));
				f.mkdirs();
				l=new Limnigraph_DAO(limni.getItemAt(i),f.getAbsolutePath());
				l.create();
			}
		}	
		// Rating curves
		Dataset<RatingCurve> rating = station.getRc();
		RatingCurve_DAO rc;
		if(rating!=null){
			for(int i=0;i<rating.getSize();i++){
				f=new File(new File(this.folder.trim(),FOLDER_RC).getAbsolutePath(),Integer.toString(i));
				f.mkdirs();
				rc=new RatingCurve_DAO(rating.getItemAt(i),f.getAbsolutePath());
				rc.create();
			}
		}	
		// Hydrographs		
		Dataset<Hydrograph> hydro = station.getHydrograph();
		Hydrograph_DAO hy;
		if(hydro!=null){
			for(int i=0;i<hydro.getSize();i++){
				f=new File(new File(this.folder.trim(),FOLDER_HYDRO).getAbsolutePath(),Integer.toString(i));
				f.mkdirs();
				hy=new Hydrograph_DAO(hydro.getItemAt(i),f.getAbsolutePath());
				hy.create();
			}
		}	
	}

	@Override
	public void read() throws IOException, Exception {
		// properties
		File f;
		Scanner sc;
		//properties
		f=new File(this.folder.trim(),FILE_PROPERTIES);
//		textFileReader.getFileInfo(f);
		sc = textFileReader.createScanner(f);
//		sc = new Scanner(f);
		int nHydrau=Integer.valueOf(sc.nextLine());
		int nGauging=Integer.valueOf(sc.nextLine());
		int nLimni=Integer.valueOf(sc.nextLine());
		int nRC=Integer.valueOf(sc.nextLine());
		int nHydro=Integer.valueOf(sc.nextLine());
		sc.close();
		// Hydraulic configurations
		Dataset<ConfigHydrau> hydrau = new Dataset<ConfigHydrau>();
		HydrauConfig_DAO h;
		for(int i=0;i<nHydrau;i++){
			f=new File(new File(this.folder.trim(),FOLDER_HYDRAU).getAbsolutePath(),Integer.toString(i));
			h=new HydrauConfig_DAO(new ConfigHydrau(),f.getAbsolutePath());
			h.read();
			hydrau.add((ConfigHydrau) h);
		}
		// Gauging sets
		Dataset<GaugingSet> gauging = station.getGauging();
		GaugingSet_DAO g;
		for(int i=0;i<nGauging;i++){
			f=new File(new File(this.folder.trim(),FOLDER_GAUGING).getAbsolutePath(),Integer.toString(i));
			g=new GaugingSet_DAO(new GaugingSet(),f.getAbsolutePath());
			g.read();
			gauging.add((GaugingSet)g);
		}
		// Limnigraphs
		Dataset<Limnigraph> limni = station.getLimni();
		Limnigraph_DAO l;
		for(int i=0;i<nLimni;i++){
			f=new File(new File(this.folder.trim(),FOLDER_LIMNI).getAbsolutePath(),Integer.toString(i));
			l=new Limnigraph_DAO(new Limnigraph(),f.getAbsolutePath());
			l.read();
			limni.add((Limnigraph)l);
		}
		// Rating curves
		Dataset<RatingCurve> rating = station.getRc();
		RatingCurve_DAO rc;
		for(int i=0;i<nRC;i++){
			f=new File(new File(this.folder.trim(),FOLDER_RC).getAbsolutePath(),Integer.toString(i));
			rc=new RatingCurve_DAO(new RatingCurve(),f.getAbsolutePath());
			rc.read();
			rating.add((RatingCurve)rc);
		}
		// Hydrographs
		Dataset<Hydrograph> hydro = station.getHydrograph();
		Hydrograph_DAO hy;
		for(int i=0;i<nHydro;i++){
			f=new File(new File(this.folder.trim(),FOLDER_HYDRO).getAbsolutePath(),Integer.toString(i));
			hy=new Hydrograph_DAO(new Hydrograph(),f.getAbsolutePath());
			hy.read();
			hydro.add((Hydrograph)hy);
		}
		// update station
		station.clear();
		station.setConfig(hydrau);
		station.setGauging(gauging);
		station.setLimni(limni);
		station.setRc(rating);
		station.setHydrograph(hydro);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub

	}

	private void writeSize(BufferedWriter bw,Dataset<?> d) throws IOException{
		if(d!=null){
			bw.write(Integer.toString(d.getSize()));bw.newLine();
		}
		else{
			bw.write(Integer.toString(0));bw.newLine();		
		}
	}
}

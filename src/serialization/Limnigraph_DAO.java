package serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import Utils.Defaults;
import commons.Observation;
import commons.ReadWrite;
import commons.textFileReader;
import moteur.Limnigraph;

public class Limnigraph_DAO extends Limnigraph implements DAO {

	private String folder;
	// Constants
	private static final String FILE_PROPERTIES="Properties.txt";
	private static final String FILE_LIMNI="Limnigraph.txt";

	public Limnigraph_DAO(Limnigraph l,String folder){
		super(l);
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
		DAOtools.safeWrite(bw,this.getFilePath());
		DAOtools.safeWrite(bw,this.getType());
		bw.close();
		///////////////////////////////////////////////////////////////////////
		// Time series
		List<Observation> limni = this.getObservations();
		if(limni!=null){
			ReadWrite.write(this.toMatrix(),null,new File(this.folder.trim(),FILE_LIMNI).getAbsolutePath(),Defaults.barSep);
		}
	}

	@Override
	public void read() throws FileNotFoundException, IOException, Exception {
		File f;
		Scanner sc;
		///////////////////////////////////////////////////////////////////////
		//properties
		f=new File(this.folder.trim(),FILE_PROPERTIES);
		textFileReader.getFileInfo(f);
//		sc = new Scanner(f);
		sc = textFileReader.createScanner(f);
		this.setName(sc.nextLine());
		this.setDescription(sc.nextLine());
		this.setFilePath(sc.nextLine());
		this.setType(sc.nextLine());
		sc.close();
		///////////////////////////////////////////////////////////////////////
		// Time series	
		f=new File(this.folder.trim(),FILE_LIMNI);
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

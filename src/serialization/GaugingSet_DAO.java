package serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import Utils.Defaults;
import commons.ReadWrite;
import moteur.Gauging;
import moteur.GaugingSet;


public class GaugingSet_DAO extends GaugingSet implements DAO{

	private String folder;
	// Constants
	private static final String FILE_PROPERTIES="Properties.txt";
	private static final String FILE_GAUGINGS="Gaugings.txt";

	public GaugingSet_DAO(GaugingSet g,String folder){
		super(g);
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
		bw.close();
		///////////////////////////////////////////////////////////////////////
		// Gaugings
		ArrayList<Gauging> gauging = this.getGaugings();
		if(gauging!=null){
			ReadWrite.write(this.toMatrix(),null,new File(this.folder.trim(),FILE_GAUGINGS).getAbsolutePath(),Defaults.barSep);
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
		this.setFilePath(sc.nextLine());
		sc.close();
		///////////////////////////////////////////////////////////////////////
		// Gaugings	
		f=new File(this.folder.trim(),FILE_GAUGINGS);
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

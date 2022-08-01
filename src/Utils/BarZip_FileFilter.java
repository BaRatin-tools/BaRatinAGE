package Utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import controleur.Control;

public class BarZip_FileFilter extends FileFilter{
	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());

	public BarZip_FileFilter(){super();}
	
	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {return true;}
		if(f.getName().endsWith(Control.BAR_EXT)) {return(true);}
		else {return false;}
	}

	@Override
	public String getDescription() {
		return dico.entry("BaRatinAGEFiles");
	}

}

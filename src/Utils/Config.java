package Utils;

import java.awt.Font;

import moteur.MCMCoptions;
import commons.Constants;
import commons.ReadWrite;

/**
 * Configuration of the application (can be modified by the user)
 * @author Sylvain Vigneau - Benjamin Renard, Irstea Lyon *
 */
public class Config {
	
	private static Config instance;
	private String language;
	private MCMCoptions mcmc;
	private String defaultDir;
	private boolean useManning=false;
	private boolean saveHydroSpag=false;
	
	// Appearance - not yet interfaced
	private Boolean expertMode = true;
	private String lookAndFeel="OS"; // Available: "OS" (OS-specific) "Metal" "Nimbus" "CDE/Motif" "Windows" "Windows Classic"
	private Font fontXplorerLbl=new Font("Tahoma",Font.BOLD,24);
	private Font fontMenu=new Font("Tahoma",Font.BOLD,16);
	private Font fontMenuItem=new Font("Tahoma",Font.PLAIN,16);
	private Font fontTxt=new Font("Tahoma",Font.PLAIN,14);
	private Font fontBigTxt=new Font("Tahoma",Font.PLAIN,16);
	private Font fontLbl=new Font("Tahoma",Font.BOLD,14);
	private Font fontBigLbl=new Font("Tahoma",Font.BOLD,18);
	private Font fontTabs=new Font("Tahoma",Font.PLAIN,14);
	private Font fontTree=new Font("Tahoma",Font.PLAIN,16);

	
	public static synchronized Config getInstance(){
	    if (instance == null){
            instance = new Config();
        }
        return instance;
    }

	private Config() {
		// read language
		try {
			String[] foo=ReadWrite.read(Defaults.options_lang);
			this.language=foo[0];
		} catch (Exception e) {
			// use defaults
			this.language="en";
		}
		// read mcmc options
		try {
			Double[][] foo=ReadWrite.read(Defaults.options_mcmc, Defaults.barSep,0);
			this.setMcmc(new MCMCoptions(foo[0][0].intValue(),foo[0][1].intValue(),foo[0][2],foo[0][3].intValue(),foo[0][4],foo[0][5],foo[0][6],foo[0][7]));
		} catch (Exception e) {
			// use defaults
			this.setMcmc(new MCMCoptions());
		}
		// read preferences
		try {
			String[] foo=ReadWrite.read(Defaults.options_preferences);
			this.setUseManning(foo[0].equals("true"));
		} catch (Exception e) {
			// use defaults
			this.setUseManning(false);
		}
		// read directories
		try {
			String[] foo=ReadWrite.read(Defaults.options_directory);
			if(foo[0].trim().equals(Constants.S_EMPTY)) {this.setDefaultDir(Defaults.home);}
			else{this.setDefaultDir(foo[0]);}
		} catch (Exception e) {
			// use defaults
			this.setDefaultDir(Defaults.home);
		}
		// read preferences
		try {
			String[] foo=ReadWrite.read(Defaults.options_save);
			this.setSaveHydroSpag(foo[0].equals("true"));
		} catch (Exception e) {
			// use defaults
			this.setSaveHydroSpag(false);
		}
	}

	/**
	 * @return the expertMode
	 */
	public Boolean isExpertMode() {
		return expertMode;
	}

	/**
	 * @param expertMode the expertMode to set
	 */
	public void setExpertMode(Boolean expertMode) {
		this.expertMode = expertMode;
	}
	
	public String getLookAndFeel() {
		return lookAndFeel;
	}

	public void setLookAndFeel(String lookAndFeel) {
		this.lookAndFeel = lookAndFeel;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Font getFontMenu() {
		return fontMenu;
	}

	public void setFontMenu(Font fontMenu) {
		this.fontMenu = fontMenu;
	}

	public Font getFontMenuItem() {
		return fontMenuItem;
	}

	public void setFontMenuItem(Font fontMenuItem) {
		this.fontMenuItem = fontMenuItem;
	}

	public Font getFontTabs() {
		return fontTabs;
	}

	public void setFontTabs(Font fontTabs) {
		this.fontTabs = fontTabs;
	}

	public Font getFontCatLbl() {
		return fontXplorerLbl;
	}

	public void setFontCatLbl(Font fontCatLbl) {
		this.fontXplorerLbl = fontCatLbl;
	}

	public Font getFontTxt() {
		return fontTxt;
	}

	public void setFontTxt(Font fontTxt) {
		this.fontTxt = fontTxt;
	}

	public Font getFontLbl() {
		return fontLbl;
	}

	public void setFontLbl(Font fontLbl) {
		this.fontLbl = fontLbl;
	}

	public Font getFontTree() {
		return fontTree;
	}

	public void setFontTree(Font fontTree) {
		this.fontTree = fontTree;
	}

	public Font getFontBigLbl() {
		return fontBigLbl;
	}

	public void setFontBigLbl(Font fontBigLbl) {
		this.fontBigLbl = fontBigLbl;
	}

	public Font getFontBigTxt() {
		return fontBigTxt;
	}

	public void setFontBigTxt(Font fontBigTxt) {
		this.fontBigTxt = fontBigTxt;
	}

	public boolean isSaveHydroSpag() {
		return saveHydroSpag;
	}

	public void setSaveHydroSpag(boolean saveHydroSpag) {
		this.saveHydroSpag = saveHydroSpag;
	}

	public Font getFontXplorerLbl() {
		return fontXplorerLbl;
	}

	public void setFontXplorerLbl(Font fontXplorerLbl) {
		this.fontXplorerLbl = fontXplorerLbl;
	}

	public Boolean getExpertMode() {
		return expertMode;
	}

	public MCMCoptions getMcmc() {
		return mcmc;
	}

	public void setMcmc(MCMCoptions mcmc) {
		this.mcmc = mcmc;
	}

	public boolean isUseManning() {
		return useManning;
	}

	public void setUseManning(boolean useManning) {
		this.useManning = useManning;
	}

	public String getDefaultDir() {
		return defaultDir;
	}

	public void setDefaultDir(String defaultDir) {
		this.defaultDir = defaultDir;
	}

}

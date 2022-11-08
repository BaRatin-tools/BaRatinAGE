package Utils;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
	
	// Help directories and files
	private String helpDir;
	private String helpFile;
	private String helpImageDir;
	private String legend_HydrauConfig;
	private String legend_Gauging;
	private String legend_RC;
	private String legend_Limni;
	private String legend_Hydro;

	public static synchronized Config getInstance(){
	    if (instance == null){
            instance = new Config();
        }
        return instance;
    }

	private Config() {
		// read language and assign help directories accordingly
		try {
			String[] foo=ReadWrite.read(Defaults.options_lang);
			this.language=foo[0];
		} catch (Exception e) {
			// use defaults
			this.language=Defaults.lang_def;
		}
		
		// set font
		// import custom font
		try {
			Font[] fonts = {
					Font.createFont(Font.TRUETYPE_FONT,
							new File(Defaults.fontDir, "OpenSans/OpenSans-VariableFont_wdth,wght.ttf")),
					 Font.createFont(Font.TRUETYPE_FONT,
							 new File(Defaults.fontDir, "Hahmlet/Hahmlet-VariableFont_wght.ttf")),
			};			
			for (Font f: fonts) {
				System.out.println("");
		
					GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
					ge.registerFont(f);
	
			}
		} catch (FontFormatException | IOException e1) {
			e1.printStackTrace();
		}

		String fontName = "OpenSans";
		// handle special case for korean (not in OpenSans font)
		if ("ko".equals(this.getLanguage())) {
			fontName = "Hahmlet";
		} 
		this.setFontXplorerLbl(new Font(fontName,Font.BOLD,24));
		this.setFontMenu(new Font(fontName,Font.BOLD,16));
		this.setFontMenuItem(new Font(fontName,Font.PLAIN,16));
		this.setFontTxt(new Font(fontName,Font.PLAIN,14));
		this.setFontBigTxt(new Font(fontName,Font.PLAIN,16));
		this.setFontLbl(new Font(fontName,Font.BOLD,14));
		this.setFontBigLbl(new Font(fontName,Font.BOLD,18));
		this.setFontTabs(new Font(fontName,Font.PLAIN,14));
		this.setFontTree(new Font(fontName,Font.PLAIN,16));

		// setup help
		this.helpDir=new File(Defaults.helpDir,this.language).getAbsolutePath();
		// Check helpDir exists - otherwise fallback on default
		if(!Files.exists(Paths.get(this.helpDir))) {
			this.helpDir=new File(Defaults.helpDir,Defaults.lang_def).getAbsolutePath();
		}
		this.setHelpFile(new File(this.helpDir,Defaults.helpFile).getAbsolutePath());
		this.helpImageDir=new File(this.helpDir,Defaults.helpImageDir).getAbsolutePath();
		this.setLegend_HydrauConfig(new File(this.helpImageDir,Defaults.legend_HydrauConfig).getAbsolutePath());
		this.setLegend_Gauging(new File(this.helpImageDir,Defaults.legend_Gauging).getAbsolutePath());
		this.setLegend_RC(new File(this.helpImageDir,Defaults.legend_RC).getAbsolutePath());
		this.setLegend_Limni(new File(this.helpImageDir,Defaults.legend_Limni).getAbsolutePath());
		this.setLegend_Hydro(new File(this.helpImageDir,Defaults.legend_Hydro).getAbsolutePath());

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

	public String getHelpDir() {
		return helpDir;
	}

	public void setHelpDir(String helpDir) {
		this.helpDir = helpDir;
	}

	public String getHelpFile() {
		return helpFile;
	}

	public void setHelpFile(String helpFile) {
		this.helpFile = helpFile;
	}

	public String getHelpImageDir() {
		return helpImageDir;
	}

	public void setHelpImageDir(String helpImageDir) {
		this.helpImageDir = helpImageDir;
	}

	public String getLegend_HydrauConfig() {
		return legend_HydrauConfig;
	}

	public void setLegend_HydrauConfig(String legend_HydrauConfig) {
		this.legend_HydrauConfig = legend_HydrauConfig;
	}

	public String getLegend_Gauging() {
		return legend_Gauging;
	}

	public void setLegend_Gauging(String legend_Gauging) {
		this.legend_Gauging = legend_Gauging;
	}

	public String getLegend_RC() {
		return legend_RC;
	}

	public void setLegend_RC(String legend_RC) {
		this.legend_RC = legend_RC;
	}

	public String getLegend_Limni() {
		return legend_Limni;
	}

	public void setLegend_Limni(String legend_Limni) {
		this.legend_Limni = legend_Limni;
	}

	public String getLegend_Hydro() {
		return legend_Hydro;
	}

	public void setLegend_Hydro(String legend_Hydro) {
		this.legend_Hydro = legend_Hydro;
	}

}

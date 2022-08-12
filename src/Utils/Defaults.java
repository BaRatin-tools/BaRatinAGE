package Utils;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.ScrollPaneConstants;

/**
 * Defaults properties of the aplication (cannot be modified by the user)
 * @author Sylvain Vigneau - Benjamin Renard, Irstea Lyon *
 */
public class Defaults {
	
	// -------------------
	// GRAPHICAL ELEMENTS
	// -------------------
	// main frame properties
	public static final String appName="BaRatinAGE";
	public static final Dimension appSize=new Dimension(1400,700);
	public static final Dimension popupSize=new Dimension(400,150);
	public static final Dimension aboutSize=new Dimension(800,250);
	public static final Dimension popupSize_Wide=new Dimension(600,150);
	public static final Dimension priorAssistantSize=appSize; //new Dimension(600,740);
	public static final Dimension JAssistantSize=new Dimension(600,300);
	public static final Dimension MAssistantSize=new Dimension(600,200);
	public static final Dimension kickPlotSize=new Dimension(1400,700);
	public static final Dimension mcmcOptionsSize=new Dimension(600,300);
	public static final Dimension baremeInfoSize=new Dimension(500,300);
	// icons
	public static final String iconGUI= "resources/BaRatinAGE_icon.png";
	public static final String iconHydraulic= "resources/Hydraulic_icon.png";
	public static final String iconGauging= "resources/Gauging_icon.png";
	public static final String iconError= "resources/Error_icon.png";
	public static final String iconRC= "resources/RC_icon.png";
	public static final String iconLimni= "resources/Limni_icon.png";
	public static final String iconHydro= "resources/Hydro_icon.png";
	public static final String iconYlog= "resources/Ylog_icon.png";
	public static final String iconPriorAssistant= "resources/PriorAssistant_icon.png";
	public static final String iconPropagate= "resources/Propagate_icon.png";
	public static final String iconOpen= "resources/Open.png";
	public static final String iconSave= "resources/Save.png";
	public static final String iconNew= "resources/New.png";
	public static final String iconBrowse= "resources/Browse.png";
	public static final String iconHelp= "resources/Buoy.png";
	public static final String iconApply= "resources/Apply.png";
	public static final String iconDelete= "resources/Delete.png";
	public static final String iconCancel= "resources/Cancel.png";
	public static final String iconRun= "resources/Run.png";
	public static final String iconKickPlot= "resources/KickPlot.png";
	public static final String iconLegend= "resources/Legend_icon.png";
	public static final String iconMorePlots= "resources/MorePlots.png";
	public static final String iconWarning= "resources/Warning.png";
	public static final String iconSW= "resources/Arrow_SouthWest.png";
	public static final String iconSE= "resources/Arrow_SouthEast.png";
	// scrolling
	public static final int scrollV=ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
	public static final int scrollH=ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
	// Appearance
	public static final Color bkgColor=Color.WHITE;
	public static final Color xplorerLblColor=Color.BLACK;
	public static final Color lblColor=Color.BLACK;
	public static final Color txtColor=Color.BLACK;
	public static final Color activeEnabledColor=new Color(0.0f,1.0f,0.0f);
	public static final Color activeDisabledColor=new Color(0.5f,1.0f,0.5f);
	public static final Color passiveEnabledColor=new Color(0.5f,0.5f,0.5f);
	public static final Color passiveDisabledColor=new Color(0.8f,0.8f,0.8f);
	// Graphics
	public static final Double plot_pointSize=8d;
	public static final Color plot_bkgColor=Color.WHITE;
	public static final Color plot_gridColor=Color.GRAY;
	public static final Color plot_Color=Color.BLACK;
	public static final Color plot_lineColor=Color.BLACK;
	public static final Color plot_paramColor=new Color(1.0f,0.5f,0.5f);
	public static final Color plot_stageColor=Color.YELLOW;
	public static final Color plot_totalColor=new Color(0.5f,0.5f,1.0f);
	public static final Color plot_kColor_light=Color.GREEN;
	public static final Color plot_kColor=new Color(0.0f,0.8f,0.0f);
	public static final Color plot_priorColor=new Color(0.0f,0.0f,0.9f);
	public static final Color plot_priorColor_light=new Color(0.5f,0.5f,1.0f);
	public static final Color plot_postColor=new Color(0.9f,0.0f,0.0f);
	public static final Color plot_postColor_light=new Color(1.0f,0.6f,0.6f);
	
	// -------------------
	// FILES & DIRECTORIES
	// -------------------
	public static final String os = System.getProperty("os.name").toLowerCase();  
	public static final String home = System.getProperty("user.dir");
	public static final String txtSep=" ";
	public static final String resultSep=" ";
	public static final String barSep=";";
	public static final String menuSep="   ";
	// Directories for executable
	public static final String exeDir=new File(home,"exe").getAbsolutePath();
	public static final String exeName="BaRatin";
	private static String exeFullName=os.startsWith("windows") ? exeName+".exe" : exeName;
	public static final String exeFile=new File(exeDir,exeFullName).getAbsolutePath();
	public static final String exeConfigFile=new File(exeDir,"Config_BaRatin.txt").getAbsolutePath();
	public static final  String tempWorkspace=new File(exeDir,"workspace").getAbsolutePath();
	public static final  String recycleDir=new File(home,"recycle").getAbsolutePath();
	// Directories for help
	public static final String helpDir_fr= new File(home,"aide").getAbsolutePath();
	public static final String helpDir_en= new File(home,"help").getAbsolutePath();
	public static final String helpFile_fr=new File(helpDir_fr,"index.html").getAbsolutePath();
	public static final String helpFile_en=new File(helpDir_en,"index.html").getAbsolutePath();
	public static final String helpImageDir_fr= new File(helpDir_fr,"img").getAbsolutePath();
	public static final String helpImageDir_en= new File(helpDir_en,"img").getAbsolutePath();
	public static final String legend_HydrauConfig_fr= new File(helpImageDir_fr,"Legend_HydrauConfig.png").getAbsolutePath();
	public static final String legend_HydrauConfig_en= new File(helpImageDir_en,"Legend_HydrauConfig.png").getAbsolutePath();
	public static final String legend_Gauging_fr= new File(helpImageDir_fr,"Legend_Gaugings.png").getAbsolutePath();
	public static final String legend_Gauging_en= new File(helpImageDir_en,"Legend_Gaugings.png").getAbsolutePath();
	public static final String legend_RC_fr= new File(helpImageDir_fr,"Legend_RC.png").getAbsolutePath();
	public static final String legend_RC_en= new File(helpImageDir_en,"Legend_RC.png").getAbsolutePath();
	public static final String legend_Limni_fr= new File(helpImageDir_fr,"Legend_Limni.png").getAbsolutePath();
	public static final String legend_Limni_en= new File(helpImageDir_en,"Legend_Limni.png").getAbsolutePath();
	public static final String legend_Hydro_fr= new File(helpImageDir_fr,"Legend_Hydro.png").getAbsolutePath();
	public static final String legend_Hydro_en= new File(helpImageDir_en,"Legend_Hydro.png").getAbsolutePath();
	// Directories for options
	public static final String optionsDir= new File(home,"options").getAbsolutePath();
	public static final String options_lang= new File(optionsDir,"lang.txt").getAbsolutePath();
	public static final String options_mcmc= new File(optionsDir,"mcmc.txt").getAbsolutePath();
	public static final String options_preferences= new File(optionsDir,"preferences.txt").getAbsolutePath();
	public static final String options_save= new File(optionsDir,"save.txt").getAbsolutePath();
	public static final String options_directory= new File(optionsDir,"directory.txt").getAbsolutePath();
	// Configuration files
	public static final String config_RunOptions=new File(tempWorkspace,"Config_RunOptions.txt").getAbsolutePath();
	public static final String config_Data=new File(tempWorkspace,"Config_Data.txt").getAbsolutePath();
	public static final String config_XYBAD=new File(tempWorkspace,"XY.BAD").getAbsolutePath();
	public static final String config_HHBAD=new File(tempWorkspace,"HH.BAD").getAbsolutePath();
	public static final String config_RatingCurve=new File(tempWorkspace,"Config_RatingCurve.txt").getAbsolutePath();
	public static final String config_ControlMatrix=new File(tempWorkspace,"Config_ControlMatrix.txt").getAbsolutePath();
	public static final String config_RemnantSigma=new File(tempWorkspace,"Config_RemnantSigma.txt").getAbsolutePath();
	public static final String config_MCMC=new File(tempWorkspace,"Config_MCMC.txt").getAbsolutePath();
	public static final String config_PriorRC=new File(tempWorkspace,"Config_PriorRC.txt").getAbsolutePath();
	public static final String config_PostProcessing=new File(tempWorkspace,"Config_PostProcessing.txt").getAbsolutePath();
	public static final String config_H2Q=new File(tempWorkspace,"Config_H2QPropagation.txt").getAbsolutePath();
	// Result files
	public static final String results_PriorRC_env="Results_RC_Prior_Envelop";
	public static final String results_PriorRC_spag="Results_RC_Prior_Spaghetti";
	public static final String results_PostRC_env="Results_RC_Post_Envelop";
	public static final String results_PostRC_spag="Results_RC_Post_Spaghetti";
	public static final String results_Qt_env="Results_Qt_Envelop";
	public static final String results_Qt_spag="Results_Qt_Spaghetti";
	public static final String results_CookedMCMC="Results_MCMCcooked.txt";
	public static final String results_SummaryMCMC="Results_MCMCsummary.txt";
	public static final String results_SummaryGaugings="Results_HQ.txt";
	// Import/Export
	public static final  String tempExport=new File(home,"temp").getAbsolutePath();
	// Dictionary
	public static final String dicoDir = new File(home,"lang").getAbsolutePath();
	public static final String dicoFile=new File(dicoDir,"dico.txt").getAbsolutePath();

	public Defaults() {
	}

}

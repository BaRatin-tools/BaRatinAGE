package vue;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import commons.Constants;
import commons.Custom_FileChooser;
import commons.Custom_MenuItem;
import commons.Frame_GetName;
import commons.Frame_SelectItem;
import commons.Frame_YesNoQuestion;
import commons.GridBag_Button;
import commons.GridBag_Layout;
import commons.GridBag_SplitPanel;
import commons.ReadWrite;
import controleur.Control;
import controleur.ExeControl;
import moteur.Station;
import Utils.Config;
import Utils.Defaults;
import Utils.Dico;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ActionListener{

	private static MainFrame instance;
	private MenuBar menubar;
	private ToolBar toolbar;
	private TreesPanel trees = new TreesPanel();
	private JTabbedPane tabs = new JTabbedPane();
	private ConfigHydrauPanel hydraulic=new ConfigHydrauPanel("",false);
	private GaugingPanel gauging=new GaugingPanel("",false);
	private RemnantErrorPanel remnant=new RemnantErrorPanel();
	private RatingCurvePanel rc=new RatingCurvePanel("",false);
	private LimniPanel limni=new LimniPanel("",false);
	private HydrographPanel hydrograph=new HydrographPanel("",false);
	private GridBag_SplitPanel split;
	private String barFile;
	private String lastBarDir;
	private String lastDataDir;

	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	private Control controller=Control.getInstance();
	// constants
	public static final int HYDRAULIC_INDX=0;
	public static final int GAUGING_INDX=1;
	public static final int REMNANT_INDX=2;
	public static final int LIMNI_INDX=3;
	public static final int RC_INDX=4;
	public static final int HYDROGRAPH_INDX=5;

	public static synchronized MainFrame getInstance(){
		if (instance==null) 
		{instance = new MainFrame();}
		return instance;
	}

	private MainFrame(String name,Dimension size) throws HeadlessException {
		//-------------------------------------------------------------
		// Main frame
		//-------------------------------------------------------------
		super(name);
		this.setSize(size);
		this.setLocationRelativeTo(null);
		GridBag_Layout.SetGrid(this.getContentPane(),new int[] {0, 0}, new int[] {0}, new double[] {0.0, 1.0},new double[] {1.0});
		this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
		//this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {close();}
		});

		//-------------------------------------------------------------
		// icons
		//-------------------------------------------------------------
		ImageIcon iconGUI=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconGUI));
		ImageIcon iconHydraulic=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconHydraulic));
		ImageIcon iconGauging=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconGauging));
		ImageIcon iconError=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconError));
		ImageIcon iconRC=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconRC));
		ImageIcon iconLimni=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconLimni));
		ImageIcon iconHydro=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconHydro));
		this.setIconImage(iconGUI.getImage());

		//-------------------------------------------------------------
		// Menus
		//-------------------------------------------------------------
		menubar = new MenuBar(this,this,dico);
		
		//-------------------------------------------------------------
		//KeyAccelerator
		//-------------------------------------------------------------
		// file
		menubar.getMenu(0).getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
		menubar.getMenu(0).getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		menubar.getMenu(0).getItem(3).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
		menubar.getMenu(0).getItem(4).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
		// Add for each object
		menubar.getMenu(1).getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK));
		menubar.getMenu(2).getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, KeyEvent.CTRL_MASK));
		menubar.getMenu(3).getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
		menubar.getMenu(4).getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		menubar.getMenu(5).getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
		// help
		menubar.getMenu(7).getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK));

		//-------------------------------------------------------------
		// Tool bar
		//-------------------------------------------------------------
		toolbar = new ToolBar(this.getContentPane(),this,dico);

		//-------------------------------------------------------------
		// Double panel
		//-------------------------------------------------------------
		split=new GridBag_SplitPanel(this.getContentPane(),JSplitPane.HORIZONTAL_SPLIT,0.2,0,1,1,1);

		//-------------------------------------------------------------
		// Left component: trees panel
		//-------------------------------------------------------------
		split.setLeftComponent(trees);

		//-------------------------------------------------------------
		// Right component: thematic tabs
		//-------------------------------------------------------------
		tabs.setFont(config.getFontTabs());
		split.setRightComponent(tabs);
		tabs.addTab(dico.entry("HydrauConf"),iconHydraulic, hydraulic,dico.entry("HydrauConf"));
		tabs.addTab(dico.entry("Gaugings"),iconGauging, gauging,dico.entry("Gaugings"));
		tabs.addTab(dico.entry("RemnantError"),iconError, remnant,dico.entry("RemnantError"));
		tabs.addTab(dico.entry("Limnigraph"),iconLimni, limni,dico.entry("Limnigraph"));
		tabs.addTab(dico.entry("RatingCurve"),iconRC, rc,dico.entry("RatingCurve"));
		tabs.addTab(dico.entry("Hydrograph"),iconHydro, hydrograph,dico.entry("Hydrograph"));
		//-------------------------------------------------------------
		// Initialize last opened directories
		//-------------------------------------------------------------
		this.setLastBarDir(config.getDefaultDir());
		this.setLastDataDir(config.getDefaultDir());
		//-------------------------------------------------------------
		// Finito!
		//-------------------------------------------------------------
		this.setVisible(true);
	}

	private MainFrame() throws HeadlessException {
		this(Defaults.appName, Defaults.appSize);
	}

	private MainFrame(GraphicsConfiguration arg0) {
		super(arg0);
	}

	private MainFrame(String arg0, GraphicsConfiguration arg1) {
		super(Defaults.appName, arg1);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		// Determine class of the event source
		String id = null;
		String eclass = event.getSource().getClass().getName();
		// Event from a menu
		if(eclass.equals(menubar.getMenu(0).getItem(0).getClass().getName())){
			Custom_MenuItem obj = (Custom_MenuItem) event.getSource();
			id=obj.getID();
		}
		// Event from a button
		if(eclass.equals(toolbar.getButton(0).getClass().getName())){
			GridBag_Button obj = (GridBag_Button) event.getSource();
			id=obj.getID();
		}
		// Take action!
		if(id.equals("Quit")){close();}
		else if(id.equals("AddConfig")){addConfig(true,null,null);}
		else if(id.equals("DeleteConfig")){deleteConfig(controller.getHydrauList(),true,null,null);}
		else if(id.equals("RenameConfig")){renameConfig(controller.getHydrauList(),true,null,null);}
		else if(id.equals("DuplicateConfig")){duplicateConfig(controller.getHydrauList(),true,null,null);}
		else if(id.equals("AddGauging")){addGaugingSet(true,null,null);}
		else if(id.equals("DeleteGauging")){deleteGaugingSet(controller.getGaugingList(),true,null,null);}
		else if(id.equals("RenameGauging")){renameGaugingSet(controller.getGaugingList(),true,null,null);}
		else if(id.equals("DuplicateGauging")){duplicateGaugingSet(controller.getGaugingList(),true,null,null);}
		else if(id.equals("AddLimni")){addLimni(true,null,null);}
		else if(id.equals("DeleteLimni")){deleteLimni(controller.getLimniList(),true,null,null);}
		else if(id.equals("RenameLimni")){renameLimni(controller.getLimniList(),true,null,null);}
		else if(id.equals("DuplicateLimni")){duplicateLimni(controller.getLimniList(),true,null,null);}
		else if(id.equals("AddRC")){addRatingCurve(true,null,null);}
		else if(id.equals("DeleteRC")){deleteRatingCurve(controller.getRCList(),true,null,null);}
		else if(id.equals("RenameRC")){renameRatingCurve(controller.getRCList(),true,null,null);}
		else if(id.equals("DuplicateRC")){duplicateRatingCurve(controller.getRCList(),true,null,null);}
		else if(id.equals("AddHydro")){addHydrograph(true,null,null);}
		else if(id.equals("DeleteHydro")){deleteHydrograph(controller.getHydrographList(),true,null,null);}
		else if(id.equals("RenameHydro")){renameHydrograph(controller.getHydrographList(),true,null,null);}
		else if(id.equals("DuplicateHydro")){duplicateHydrograph(controller.getHydrographList(),true,null,null);}
		else if(id.equals("Help")){openHelp();}
		else if(id.equals("Open")){controller.open();}
		else if(id.equals("New")){newStation();}
		else if(id.equals("Save")){
			if(this.barFile!=null){controller.save(false);} else {controller.save(true);}
		}
		else if(id.equals("SaveAs")){controller.save(true);}
		else if(id.equals("ExportRC")){exportRatingCurve(controller.getRCList(),true,null,null);}
		else if(id.equals("ExportRCeq")){exportRatingCurveEquation(controller.getRCList(),true,null,null);}
		else if(id.equals("ExportMCMC")){exportMCMC(controller.getRCList(),true,null,null);}
		else if(id.equals("ExportHydro")){exportHydrograph(controller.getHydrographList(),true,null,null);}
		else if(id.equals("About")){new Frame_About();}
		else if(id.equals("Language")){setLanguage();}
		else if(id.equals("Preferences")){setPreferences();}
		else if(id.equals("DefaultDirectory")){setDefaultDirectory();}
		else if(id.equals("SaveOptions")){setSaveOptions();}
		else if(id.equals("MCMCoptions")){setMCMCOptions();}
	}

	/////////////////
	// HYDRAU CONFIG
	/////////////////
	public void deleteConfig(String[] itemList,boolean decorated,Integer x,Integer y){
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("HydrauConf"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			// remove from station (through controller)
			controller.deleteHydrauConfig(nam);
		}
	}

	public void addConfig(boolean decorated,Integer x,Integer y){
		Frame_GetName f=Popup_GetName(decorated,x,y,Constants.S_EMPTY);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			// Add to station (through controller)
			controller.addHydrauConfig(nam);
		}
	}

	public void renameConfig(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("HydrauConf"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			controller.renameHydrauConfig(nam);
		}
	}

	public void duplicateConfig(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f1=Popup_SelectItem(itemList,dico.entry("HydrauConf"),decorated,x,y);
		String original=f1.getName();
		if(!original.equals(Constants.S_EMPTY)){
			controller.duplicateHydrauConfig(original);
		}
	}

	/////////////////
	// GAUGINGS
	/////////////////
	public void deleteGaugingSet(String[] itemList,boolean decorated,Integer x,Integer y){
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("Gaugings"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			// remove from station (through controller)
			controller.deleteGaugingSet(nam);
		}
	}

	public void addGaugingSet(boolean decorated,Integer x,Integer y){
		Frame_GetName f=Popup_GetName(decorated,x,y,Constants.S_EMPTY);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			// Add to station (through controller)
			controller.addGaugingSet(nam);
		}
	}

	public void renameGaugingSet(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("Gaugings"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			controller.renameGaugingSet(nam);
		}
	}

	public void duplicateGaugingSet(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f1=Popup_SelectItem(itemList,dico.entry("Gaugings"),decorated,x,y);
		String original=f1.getName();
		if(!original.equals(Constants.S_EMPTY)){
			controller.duplicateGauging(original);
		}
	}

	/////////////////
	// RATING CURVE
	/////////////////
	public void deleteRatingCurve(String[] itemList,boolean decorated,Integer x,Integer y){
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("RatingCurve"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			// remove from station (through controller)
			controller.deleteRatingCurve(nam);
		}
	}

	public void addRatingCurve(boolean decorated,Integer x,Integer y){
		Frame_GetName f=Popup_GetName(decorated,x,y,Constants.S_EMPTY);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			// Add to station (through controller)
			controller.addRatingCurve(nam);
		}
	}

	public void renameRatingCurve(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("RatingCurve"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			controller.renameRatingCurve(nam);
		}
	}

	public void exportRatingCurve(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("RatingCurve"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			controller.exportRatingCurve(nam);
		}
	}

	public void exportRatingCurveEquation(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("RatingCurve"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			controller.exportRatingCurveEquation(nam);
		}
	}

	public void exportMCMC(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("RatingCurve"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			controller.exportMCMC(nam);
		}
	}
	
	public void duplicateRatingCurve(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f1=Popup_SelectItem(itemList,dico.entry("RatingCurve"),decorated,x,y);
		String original=f1.getName();
		if(!original.equals(Constants.S_EMPTY)){
			controller.duplicateRatingCurve(original);
		}
	}

	/////////////////
	// LIMNI
	/////////////////
	public void deleteLimni(String[] itemList,boolean decorated,Integer x,Integer y){
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("Limnigraph"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			// remove from station (through controller)
			controller.deleteLimni(nam);
		}
	}

	public void addLimni(boolean decorated,Integer x,Integer y){
		Frame_GetName f=Popup_GetName(decorated,x,y,Constants.S_EMPTY);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			// Add to station (through controller)
			controller.addLimni(nam);
		}
	}

	public void renameLimni(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("Limnigraph"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			controller.renameLimni(nam);
		}
	}

	public void duplicateLimni(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f1=Popup_SelectItem(itemList,dico.entry("Limnigraph"),decorated,x,y);
		String original=f1.getName();
		if(!original.equals(Constants.S_EMPTY)){
			controller.duplicateLimni(original);
		}
	}

	/////////////////
	// HYDROGRAPH
	/////////////////
	public void deleteHydrograph(String[] itemList,boolean decorated,Integer x,Integer y){
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("Hydrograph"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			// remove from station (through controller)
			controller.deleteHydrograph(nam);
		}
	}

	public void addHydrograph(boolean decorated,Integer x,Integer y){
		Frame_GetName f=Popup_GetName(decorated,x,y,Constants.S_EMPTY);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			// Add to station (through controller)
			controller.addHydrograph(nam);
		}
	}

	public void renameHydrograph(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("Hydrograph"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			controller.renameHydrograph(nam);
		}
	}

	public void exportHydrograph(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f=Popup_SelectItem(itemList,dico.entry("Hydrograph"),decorated,x,y);
		String nam=f.getName();
		if(!nam.equals(Constants.S_EMPTY)){
			controller.exportHydrograph(nam);
		}
	}

	public void duplicateHydrograph(String[] itemList,boolean decorated,Integer x,Integer y){
		// select item
		Frame_SelectItem f1=Popup_SelectItem(itemList,dico.entry("Hydrograph"),decorated,x,y);
		String original=f1.getName();
		if(!original.equals(Constants.S_EMPTY)){
			controller.duplicateHydrograph(original);
		}
	}

	/////////////////
	// MISC.
	/////////////////

	public void setLanguage(){
		// select language
		Frame_SelectItem select=Popup_SelectItem(dico.getAvailable(),dico.entry("Language"),true,null,null);
		String lang=select.getName();
		if(!lang.equals(Constants.S_EMPTY)){
			try {
				ReadWrite.write(new String[]{lang}, Defaults.options_lang);
				new InformationPanel(this,dico.entry("SetLanguageInfo"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setPreferences(){
		// NOTE BR, 18/08/2022: Manning option has been deactivated since it wasn't handled properly, 
		//                      and handling it properly would require major changes.
		// select coefficient
		Frame_SelectItem select=Popup_SelectItem(new String[] {"Strickler"},dico.entry("RoughnessCoefficient"),true,null,null);
		//Frame_SelectItem select=Popup_SelectItem(new String[] {"Strickler","Manning"},dico.entry("RoughnessCoefficient"),true,null,null);
		String coeff=select.getName();
		if(!coeff.equals(Constants.S_EMPTY)){
			try {
				ReadWrite.write(new String[]{coeff}, Defaults.options_preferences);
				config.setUseManning(coeff.equals("Manning"));
				new InformationPanel(this,dico.entry("ChangeSaved"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setDefaultDirectory(){
		// select directory
		Custom_FileChooser chooser = new Custom_FileChooser(config.getDefaultDir(),"d");
		if(chooser.getFilepath()!=Constants.S_EMPTY){
			try {
				ReadWrite.write(new String[]{chooser.getFilepath()}, Defaults.options_directory);
				config.setDefaultDir(chooser.getFilepath());
				new InformationPanel(this,dico.entry("ChangeSaved"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setSaveOptions(){
		// save spags?
		Frame_SelectItem select=Popup_SelectItem(new String[] {dico.entry("No"),dico.entry("Yes")},dico.entry("SaveHydroSpag"),true,null,null);
		String save=select.getName();
		if(!save.equals(Constants.S_EMPTY)){
			try {
				String foo;
				if(save.equals(dico.entry("Yes"))) {foo="true";} else {foo="false";}
				if(foo.equalsIgnoreCase("true")) {
					int ok=new Frame_YesNoQuestion().ask(this,
							dico.entry("saveSpagWarning")+System.getProperty("line.separator")+dico.entry("ConfirmContinue"),
							dico.entry("Warning"),
							Defaults.iconWarning,dico.entry("Yes"),dico.entry("No"));
					if(ok==JOptionPane.NO_OPTION) {foo="false";}
				}				
				ReadWrite.write(new String[]{foo}, Defaults.options_save);
				config.setSaveHydroSpag(foo.equalsIgnoreCase("true"));				
				new InformationPanel(this,dico.entry("ChangeSaved"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setMCMCOptions(){
		Frame_MCMCoptions f = new Frame_MCMCoptions(this, config.getMcmc());
		if(f.getMcmc()!=null){
			try {
				ReadWrite.write(new String[]{f.getMcmc().toString_mini()}, Defaults.options_mcmc);
				config.setMcmc(f.getMcmc());
				new InformationPanel(this,dico.entry("ChangeSaved"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Frame_GetName Popup_GetName(boolean decorated,Integer x,Integer y, String init){
		Frame_GetName f = new Frame_GetName(this,
				dico.entry("TypeName"),decorated,dico.entry("Name"),
				dico.entry("Apply"),dico.entry("Cancel"),
				Defaults.iconApply,Defaults.iconCancel,
				config.getFontTxt(),config.getFontLbl(),
				Defaults.bkgColor,Defaults.txtColor,Defaults.lblColor,
				Defaults.popupSize,
				x,y,dico.entry("TypeName"),init);
		return(f);
	}

	private Frame_SelectItem Popup_SelectItem(String[] itemList,String label,boolean decorated,Integer x,Integer y){
		Frame_SelectItem f = new Frame_SelectItem(this,
				dico.entry("SelectItem"),
				itemList,
				decorated,
				label,
				dico.entry("Apply"),dico.entry("Cancel"),
				Defaults.iconApply,Defaults.iconCancel,
				config.getFontTxt(),config.getFontLbl(),
				Defaults.bkgColor,Defaults.txtColor,Defaults.lblColor,
				Defaults.popupSize,
				x,y,dico.entry("SelectItem"));
		return(f);
	}

	private void openHelp(){
		String help=config.getHelpFile();
		File htmlFile = new File(help);
		try {
			Desktop.getDesktop().browse(htmlFile.toURI());
		} catch (IOException ex) {
			//TODO
			ex.printStackTrace();
		}

	}

	private void close(){
		int ok=new Frame_YesNoQuestion().ask(this,
				dico.entry("CloseWarning"),
				dico.entry("Warning"),
				Defaults.iconWarning,dico.entry("Yes"),dico.entry("No"));
		if(ok==JOptionPane.YES_OPTION){
			// Kill executable if needed
			ExeControl exeController = ExeControl.getInstance();
			Process p = exeController.getExeProc();
			if(p!=null) {p.destroy();}
			// Kill Java VM
			System.exit(0);
			}
	}
	
	private void newStation(){
		int ok;
		if(!Station.getInstance().isEmpty()) {
			ok=new Frame_YesNoQuestion().ask(this,
					dico.entry("NewWarning")+System.getProperty("line.separator")+dico.entry("ConfirmContinue"),
					dico.entry("Warning"),
					Defaults.iconWarning,dico.entry("Yes"),dico.entry("No"));
		} else {
			ok=JOptionPane.YES_OPTION;
		}
		if(ok==JOptionPane.YES_OPTION){controller.newStation();}
		this.barFile=null;
	}


	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public MenuBar getMenubar() {
		return menubar;
	}

	public void setMenubar(MenuBar menubar) {
		this.menubar = menubar;
	}

	public ToolBar getToolbar() {
		return toolbar;
	}

	public void setToolbar(ToolBar toolbar) {
		this.toolbar = toolbar;
	}

	public TreesPanel getTrees() {
		return trees;
	}

	public void setTrees(TreesPanel trees) {
		this.trees = trees;
	}

	public JTabbedPane getTabs() {
		return tabs;
	}

	public void setTabs(JTabbedPane tabs) {
		this.tabs = tabs;
	}

	public ConfigHydrauPanel getHydraulic() {
		return hydraulic;
	}

	public void setHydraulic(ConfigHydrauPanel hydraulic) {
		this.hydraulic = hydraulic;
	}

	public GaugingPanel getGauging() {
		return gauging;
	}

	public void setGauging(GaugingPanel gauging) {
		this.gauging = gauging;
	}

	public RemnantErrorPanel getRemnant() {
		return remnant;
	}

	public void setRemnant(RemnantErrorPanel remnant) {
		this.remnant = remnant;
	}

	public RatingCurvePanel getRc() {
		return rc;
	}

	public void setRc(RatingCurvePanel rc) {
		this.rc = rc;
	}

	public LimniPanel getLimni() {
		return limni;
	}

	public void setLimni(LimniPanel limni) {
		this.limni = limni;
	}

	public HydrographPanel getHydrograph() {
		return hydrograph;
	}

	public void setHydrograph(HydrographPanel hydrogram) {
		this.hydrograph = hydrogram;
	}

	public GridBag_SplitPanel getSplit() {
		return split;
	}

	public void setSplit(GridBag_SplitPanel split) {
		this.split = split;
	}

	public String getBarFile() {
		return barFile;
	}

	public void setBarFile(String barFile) {
		this.barFile = barFile;
	}

	public String getLastBarDir() {
		return lastBarDir;
	}

	public void setLastBarDir(String lastBarDir) {
		this.lastBarDir = lastBarDir;
	}

	public String getLastDataDir() {
		return lastDataDir;
	}

	public void setLastDataDir(String lastDataDir) {
		this.lastDataDir = lastDataDir;
	}

}

package controleur;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartPanel;

import commons.Constants;
import commons.Custom_TableModel;
import commons.DirectoryUtils;
import commons.Distribution;
import commons.Frame_GetName;
import commons.Frame_YesNoQuestion;
import commons.GridBag_CheckBox;
import commons.GridBag_Layout;
import commons.GridBag_Panel;
import commons.GridBag_TextField_Titled;
import commons.Observation;
import commons.Parameter;
import commons.ReadWrite;
import Utils.BarZip_FileFilter;
import Utils.Config;
import Utils.Defaults;
import Utils.Dico;
import serialization.Station_DAO;
import vue.ConfigHydrauPanel;
import vue.ControlPanel;
import vue.ExceptionPanel;
import vue.Frame_BaremeInfo;
import vue.Frame_Plot;
import vue.Frame_PriorAssistant;
import vue.GaugingPanel;
import vue.HydrographPanel;
import vue.InformationPanel;
import vue.ItemPanel;
import vue.LimniPanel;
import vue.MainFrame;
import vue.RatingCurvePanel;
import vue.RemnantErrorPanel;
import vue.TreesPanel;
import moteur.BonnifaitMatrix;
import moteur.ConfigHydrau;
import moteur.Dataset;
import moteur.Envelop;
import moteur.Gauging;
import moteur.GaugingSet;
import moteur.HydrauControl;
import moteur.Hydrograph;
import moteur.Limnigraph;
import moteur.PostRatingCurveOptions;
import moteur.PriorRatingCurveOptions;
import moteur.RatingCurve;
import moteur.RemnantError;
import moteur.Station;

/**
 * Main controller, making the connection between the view and the model
 * @author Ben Renard & Sylvain Vigneau, Irstea Lyon
 */
public class Control {

	private static Control instance;

	public static synchronized Control getInstance(){
		if (instance == null){
			instance = new Control();
		}
		return instance;
	}

	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	private Station station=Station.getInstance();

	// constants
	public static final String BAR_EXT="bar.zip";
	private static final FileNameExtensionFilter[] filter_exportRC={
		new FileNameExtensionFilter("csv",new String[] {"CSV", "csv"}),
		new FileNameExtensionFilter("Bareme",new String[] {"dat"})};


	private Control() {
	}

	//-----------------------------------
	// General tools

	public void popupFormatError(){
		new ExceptionPanel(MainFrame.getInstance(),dico.entry("FormatErrorMessage"));
	}

	public void popupImportError(){
		new ExceptionPanel(MainFrame.getInstance(),dico.entry("ImportErrorMessage"));
	}
	
	public void popupKError(){
		new ExceptionPanel(MainFrame.getInstance(),dico.entry("KErrorMessage"));
	}

	public double safeParse_d(String s){
		double d;
		if(s.trim().equals("")){
			return Constants.D_MISSING;
		}
		try{
			d=Double.parseDouble(s);
			return d;
		}
		catch (Exception e){
			new ExceptionPanel(MainFrame.getInstance(),dico.entry("FormatErrorMessage"));
			return Constants.D_UNFEAS;			
		}
	}

	public Double safeParse_D(String s){
		Double d;
		if(s.trim().equals("")){
			return null;
		}
		try{
			d=Double.parseDouble(s);
			return d;
		}
		catch (Exception e){
			new ExceptionPanel(MainFrame.getInstance(),dico.entry("FormatErrorMessage"));
			return Constants.D_UNFEAS;			
		}
	}

	public int safeParse_i(String s){
		int i;
		if(s.trim().equals("")){
			return Constants.I_MISSING;
		}
		try{
			i=Integer.parseInt(s);
			return i;
		}
		catch (Exception e){
			new ExceptionPanel(MainFrame.getInstance(),dico.entry("FormatErrorMessage"));
			return Constants.I_UNFEAS;			
		}
	}

	private void kickPlot(ChartPanel chart){
		if(chart!=null){
			Frame_Plot fr = new Frame_Plot();
			GridBag_Layout.putIntoGrid(chart,fr.getPan(),0,0,1,1,true,true);
		}
	}

	public void kickPlot(JPanel chart){
		if(chart!=null){
			Frame_Plot fr = new Frame_Plot();
			GridBag_Layout.putIntoGrid(chart,fr.getPan(),0,0,1,1,true,true);
		}
	}

	private void redrawTrees(){
		MainFrame mf = MainFrame.getInstance();
		mf.setTrees(new TreesPanel());
		TreesPanel trees = mf.getTrees();
		// Datasets
		if(station.getConfig()!=null){
			for(int i=0;i<station.getConfig().getSize();i++){
				trees.getTree_cat().getDstModel().addConfigNode(station.getConfigAt(i).getName());
			}
		}
		if(station.getGauging()!=null){
			for(int i=0;i<station.getGauging().getSize();i++){
				trees.getTree_cat().getDstModel().addGaugingNode(station.getGaugingAt(i).getName());
			}
		}
		if(station.getLimni()!=null){
			for(int i=0;i<station.getLimni().getSize();i++){
				trees.getTree_cat().getDstModel().addLimniNode(station.getLimnigraphAt(i).getName());
			}
		}
		// RC
		if(station.getRc()!=null){
			for(int i=0;i<station.getRc().getSize();i++){
				trees.getTree_RC().getRctModel().addNode(station.getRatingCurveAt(i).getName());
			}
		}
		// Hydro
		if(station.getHydrograph()!=null){
			for(int i=0;i<station.getHydrograph().getSize();i++){
				trees.getTree_HYD().getHydModel().addNode(station.getHydrographAt(i).getName());
			}
		}
		expandAllNodes(trees.getTree_cat());
		mf.getSplit().setLeftComponent(trees);
	}

	private void expandAllNodes(JTree tree){
		for(int i=0;i<tree.getRowCount();++i){
			tree.expandRow(i);
		}
	}

	public void save(boolean askFile){
		File f=null;
		// File selection by user
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new BarZip_FileFilter());
		fc.setDialogTitle(dico.entry("Save"));
		fc.setAcceptAllFileFilterUsed(false);
		fc.setCurrentDirectory(new File(MainFrame.getInstance().getLastBarDir()));
		String barFile;
		if(askFile){
			int returnVal = fc.showOpenDialog(null);
			if(returnVal!=JFileChooser.APPROVE_OPTION){return;}
			barFile=fc.getSelectedFile().getAbsolutePath();
			MainFrame.getInstance().setLastBarDir(fc.getSelectedFile().getAbsoluteFile().getParent());
		}
		else {
			barFile=MainFrame.getInstance().getBarFile();
		}
		String extension=barFile.substring(barFile.length()-8, barFile.length());
		if(!extension.equals("."+BAR_EXT)) {barFile=barFile+"."+BAR_EXT;}
		MainFrame.getInstance().setBarFile(barFile);
		MainFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try{
			//Write data in temp folder
			try {new Station_DAO(Defaults.tempExport).create();}
			/*
			catch (FileNotFoundException e) {}
			catch (IOException e) {}
			 */
			catch (Exception e) {new ExceptionPanel(null,dico.entry("SaveProblem"));}
			// Zip temp folder
			List<File> fileList = new ArrayList<File>();
			f=new File(Defaults.tempExport);
			try {DirectoryUtils.getAllFiles(f, fileList);}
			catch (IOException e) {new ExceptionPanel(null,dico.entry("SaveProblem"));}
			File zip=new File(barFile);
			try {DirectoryUtils.writeZipFile(f, fileList,zip);} 
			/*
			catch (FileNotFoundException e) {}
			 */
			catch (IOException e) {new ExceptionPanel(null,dico.entry("SaveProblem"));}
		}
		finally{
			// cleanup
			if(f!=null){if(f.exists()){DirectoryUtils.deleteDir(f);}}
			MainFrame.getInstance().setCursor(Cursor.getDefaultCursor());
		}
	}

	public void open() {
		File f=null;
		// Ask confirmation if current workspace is not empty
		if (!Station.getInstance().isEmpty()) {
			int ok=new Frame_YesNoQuestion().ask(null,
					String.format("<html>%s<br>%s</html>", dico.entry("OpenWarning"), dico.entry("ConfirmContinue")),
					dico.entry("Open"),Defaults.iconWarning,dico.entry("Yes"),dico.entry("No"));
			if(ok==JOptionPane.NO_OPTION) {return;}
		}
		// Select bar file
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new BarZip_FileFilter());
		fc.setDialogTitle(dico.entry("Open"));
		fc.setCurrentDirectory(new File(MainFrame.getInstance().getLastBarDir()));
		String barFile;
		int returnVal = fc.showOpenDialog(null);
		if(returnVal!=JFileChooser.APPROVE_OPTION){return;}
		barFile=fc.getSelectedFile().getAbsolutePath();
		MainFrame.getInstance().setBarFile(barFile);
		MainFrame.getInstance().setLastBarDir(fc.getSelectedFile().getAbsoluteFile().getParent());
		MainFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try{
			// Unzip it
			f=new File(Defaults.tempExport);
			if(f.exists()){DirectoryUtils.deleteDir(f);}
			f.mkdirs();
			ZipFile zip;
			try {
				zip = new ZipFile(barFile);
				DirectoryUtils.Unzip(zip, f);
			}
			catch (IOException e) {new ExceptionPanel(null,dico.entry("OpenProblem"));}
			// populate station
			newStation();
			try {new Station_DAO(Defaults.tempExport).read();}
			catch (Exception e) {new ExceptionPanel(null,dico.entry("OpenProblem"));}
		}
		finally{
			// cleanup
			if(f!=null){if(f.exists()){DirectoryUtils.deleteDir(f);}}
			redrawTrees();
			MainFrame.getInstance().setCursor(Cursor.getDefaultCursor());
		}
	}

	public void newStation(){
		station=Station.getInstance();
		MainFrame mf = MainFrame.getInstance();
		station.setConfig(new Dataset<ConfigHydrau>());
		station.setGauging(new Dataset<GaugingSet>());
		station.setLimni(new Dataset<Limnigraph>());
		station.setRc(new Dataset<RatingCurve>());
		station.setHydrograph(new Dataset<Hydrograph>());		
		for(int i=0;i<=MainFrame.HYDROGRAPH_INDX;i++){refresh(i,"");}
		mf.getTabs().setSelectedIndex(0);
	}

	public void showLegend(ItemPanel panel){
		String file=null;
		// Hydraulic configuration
		if(panel.getClass().getName().equals("vue.ConfigHydrauPanel")){
			file=config.getLegend_HydrauConfig();
		}
		// Gaugings
		if(panel.getClass().getName().equals("vue.GaugingPanel")){
			file=config.getLegend_Gauging();
		}
		// RC
		if(panel.getClass().getName().equals("vue.RatingCurvePanel")){
			file=config.getLegend_RC();
		}
		// Limni
		if(panel.getClass().getName().equals("vue.LimniPanel")){
			file=config.getLegend_Limni();
		}
		// Hydro
		if(panel.getClass().getName().equals("vue.HydrographPanel")){
			file=config.getLegend_Hydro();
		}		
		File htmlFile = new File(file);
		try {
			Desktop.getDesktop().browse(htmlFile.toURI());
		} catch (IOException ex) {
			//TODO
			ex.printStackTrace();
		}

	}

	public int h2n(double hmin,double hmax, double hstep){
		ArrayList<Double> g = generateGrid(hmin,hmax,hstep);
		if(g==null){return(0);} else {return(g.size());}
	}

	public double n2h(double hmin,double hmax, int nstep){
		if(hmax<hmin){return Constants.D_UNFEAS;}
		if(nstep<=1){return Constants.D_UNFEAS;}
		return((hmax-hmin)/(nstep-1));
	}

	private ArrayList<Double> generateGrid(double hmin,double hmax, double hstep){
		if(hmax<hmin){return null;}
		if(hstep<=0.0){return null;}
		ArrayList<Double> g = new ArrayList<Double>();
		Double current=hmin;
		while(current<=hmax){g.add(current);current=current+hstep;}
		return g;	
	}

	public void fillNHstep(GridBag_TextField_Titled hmin,GridBag_TextField_Titled hmax,
			GridBag_TextField_Titled hstep,GridBag_TextField_Titled nstep,String nORh){
		double min,max,step;
		int n;
		min=safeParse_d(hmin.getText());
		max=safeParse_d(hmax.getText());
		if(nORh.equals("n")){
			step=safeParse_d(hstep.getText());
			n=h2n(min,max,step);
			nstep.setText(Integer.toString(n));
		} else {
			n=safeParse_i(nstep.getText());
			step=n2h(min,max,n);
			hstep.setText(Double.toString(step));	
		}
	}

	private void refresh(int indx,String name){
		MainFrame mf = MainFrame.getInstance();
		Object o=null;
		if(indx==MainFrame.HYDRAULIC_INDX){
			if(name.equals("")){o=new ConfigHydrauPanel("",false);}
			else {o=new ConfigHydrauPanel(name,true);}
		}
		else if(indx==MainFrame.GAUGING_INDX){
			if(name.equals("")){o=new GaugingPanel("",false);}
			else {o=new GaugingPanel(name,true);}
		}
		else if(indx==MainFrame.LIMNI_INDX){
			if(name.equals("")){o=new LimniPanel("",false);}
			else {o=new LimniPanel(name,true);}
		}
		else if(indx==MainFrame.REMNANT_INDX){
			if(name.equals("")){o=new RemnantErrorPanel();}
			else {o=new RemnantErrorPanel();}
		}
		else if(indx==MainFrame.RC_INDX){
			if(name.equals("")){o=new RatingCurvePanel("",false);}
			else {o=new RatingCurvePanel(name,true);}
		}
		else if(indx==MainFrame.HYDROGRAPH_INDX){
			if(name.equals("")){o=new HydrographPanel("",false);}
			else {o=new HydrographPanel(name,true);}
		}
		mf.getTabs().setComponentAt(indx,(Component) o);
		mf.getTabs().setSelectedIndex(indx);
		mf.getTabs().revalidate();
		redrawTrees();		
	}

	//-----------------------------------
	// Hydraulic config

	public int getHydrauConfigNcontrol(String id) {
		return station.getHydrauConfigNcontrol(id);
	}

	public void addHydrauConfig(String name){
		try {
			station.addConfig(name);
		} catch (Exception e) {
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(e.getMessage()));
		}
		refresh(MainFrame.HYDRAULIC_INDX,name);
	}

	public void deleteHydrauConfig(String name){
		MainFrame main=MainFrame.getInstance();
		// Check with user that it's ok to remove this and all dependent objects
		int check=new Frame_YesNoQuestion().ask(main,dico.entry("DeleteHydrauWarning"), dico.entry("Warning"),
				Defaults.iconWarning,dico.entry("Yes"),dico.entry("No"));
		if(check==JOptionPane.NO_OPTION) {return;}
		ArrayList<String> rc2delete=new ArrayList<String>();
		ArrayList<String> hydro2delete=new ArrayList<String>();
		// Look for all RC using this config
		if(station.getRc()!=null){
			int n=station.getRc().getSize();
			for(int i=0;i<n;i++){
				RatingCurve rc=station.getRatingCurveAt(i);
				if(rc.getHydrau_id()!=null){
					if(rc.getHydrau_id().equals(name)){
						rc2delete.add(rc.getName());
						// Look for all hydrographs using the rc to be deleted
						if(station.getHydrograph()!=null){
							int m=station.getHydrograph().getSize();
							for(int j=0;j<m;j++){
								Hydrograph hydro=station.getHydrographAt(j);
								if(hydro.getRc_id()!=null){
									if(hydro.getRc_id().equals(rc.getName())){
										hydro2delete.add(hydro.getName());
									}
								}
							}
						}
					}
				}
			}
		}
		// Update trees, Perform deletions, redraw blank panels
		for(int i=0;i<hydro2delete.size();i++){
			main.getTabs().setComponentAt(MainFrame.HYDROGRAPH_INDX, new HydrographPanel("", false));
			station.deleteHydrograph(hydro2delete.get(i));

		}
		for(int i=0;i<rc2delete.size();i++){
			main.getTabs().setComponentAt(MainFrame.RC_INDX, new RatingCurvePanel("", false));
			station.deleteRatingCurve(rc2delete.get(i));
		}
		station.deleteConfig(name);
		refresh(MainFrame.HYDRAULIC_INDX,"");
	}

	public void renameHydrauConfig(String name){
		MainFrame main=MainFrame.getInstance();
		// Ask new name
		Frame_GetName f=main.Popup_GetName(true,null,null,name+"_");
		String newname=f.getName();
		if(newname.equals(Constants.S_EMPTY)){return;}		
		// check that newname isn't used
		if(station.getConfig().isNameAlreadyUsed(newname)){
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(Dataset.IS_USED));
			return;
		}
		ArrayList<String> rename=new ArrayList<String>();
		// Look for all RC using this config
		if(station.getRc()!=null){
			int n=station.getRc().getSize();
			for(int i=0;i<n;i++){
				RatingCurve rc=station.getRatingCurveAt(i);
				if(rc.getHydrau_id()!=null){
					if(rc.getHydrau_id().equals(name)){
						rename.add(rc.getName());
					}
				}
			}
		}
		// Update trees, Perform renaming, redraw panels
		for(int i=0;i<rename.size();i++){
			station.getRatingCurve(rename.get(i)).setHydrau_id(newname);
			main.getTabs().setComponentAt(MainFrame.RC_INDX, new RatingCurvePanel("", false));
		}
		station.getHydrauConfig(name).setName(newname);
		refresh(MainFrame.HYDRAULIC_INDX,newname);
	}

	public void fillHydrauConfigPanel(String id,ConfigHydrauPanel panel){
		ConfigHydrau hydrau = station.getHydrauConfig(id);
		panel.getId().setText(hydrau.getName());
		panel.getDescription().setText(hydrau.getDescription());
		int nc=0;		
		if(hydrau.getControls()==null){nc=0;}
		else{nc=hydrau.getControls().size();}
		panel.getNcontrol().setSelectedIndex(nc);
		// controls
		for(int i=0;i<nc;i++){
			HydrauControl cont = hydrau.getControls().get(i);
			// description
			panel.getControls()[i].getDescription().setText(cont.getDescription());
			// type
			panel.getControls()[i].setManualSelection(false);
			panel.getControls()[i].getType().setSelectedIndex(cont.getType());
			panel.getControls()[i].setManualSelection(true);
			// K-A-C parameters
			writeParInPanel(cont.getA().getPrior(),panel.getControls()[i].getA(),panel.getControls()[i].getApom());
			writeParInPanel(cont.getK().getPrior(),panel.getControls()[i].getK(),panel.getControls()[i].getKpom());
			writeParInPanel(cont.getC().getPrior(),panel.getControls()[i].getC(),panel.getControls()[i].getCpom());
		}
		// set the matrix
		for(int i=0;i<nc;i++){
			for(int j=i;j<nc;j++){
				panel.getMatrix()[i][j].setSelected(hydrau.getMatrix().getMatrix().get(i).get(j));
			}
		}
		// set editable checkboxes
		setCheckboxEnabled(panel.getMatrix());
		// Prior RC options
		PriorRatingCurveOptions rco = hydrau.getPriorRCoptions();
		if(rco!=null){
			panel.getNsim().setText(Integer.toString(hydrau.getPriorRCoptions().getnSim()));
			if(rco.gethMin()!=null){panel.getHmin().setText(Double.toString(rco.gethMin()));}
			if(rco.gethMax()!=null){panel.getHmax().setText(Double.toString(rco.gethMax()));}
			if(rco.gethStep()!=null){panel.getHstep().setText(Double.toString(rco.gethStep()));}
			panel.getNstep().setText(Integer.toString(hydrau.getPriorRCoptions().getnStep()));
		}
		// plot envelops
		if(hydrau.getPriorEnv()!=null){
			ChartPanel chart = plotHydrau(panel);
			GridBag_Panel pan = panel.getPan_graph();
			if(chart!=null){
				pan.removeAll();
				GridBag_Layout.putIntoGrid(chart,pan,0,0,1,1,true,true);
			}
		}
		// select good tab
		panel.revalidate();
		MainFrame.getInstance().getTabs().setComponentAt(MainFrame.HYDRAULIC_INDX, panel);
	}

	public void setCheckboxEnabled(GridBag_CheckBox[][] matrix){
		int nc=matrix.length;
		// create a Bonnifait Matrix from checkbox matrix
		BonnifaitMatrix lolo=new BonnifaitMatrix();
		ArrayList<Boolean> column=new ArrayList<Boolean> ();
		for(int i=0;i<nc;i++){
			// reinitialize column
			column=new ArrayList<Boolean> ();
			for(int j=0;j<i;j++){column.add(false);}
			for(int j=i;j<nc;j++){column.add(matrix[i][j].isSelected());}
			lolo.AddColumn(column);
		}
		// now try feasability for all cells
		Boolean b;
		for(int i=0;i<nc;i++){
			for(int j=i;j<nc;j++){
				// reverse current cell
				b=Boolean.valueOf(lolo.getMatrix().get(i).get(j));
				lolo.getMatrix().get(i).set(j,!b);
				// check whether it's feasible and enable cell accordingly
				matrix[i][j].setEnabled(lolo.isValid());
				matrix[i][j].revalidate();
				// make nice color
				if(matrix[i][j].isSelected()){
					if(matrix[i][j].isEnabled()){matrix[i][j].setBackground(Defaults.activeEnabledColor);}
					else{matrix[i][j].setBackground(Defaults.activeDisabledColor);}
				}
				else {
					if(matrix[i][j].isEnabled()){matrix[i][j].setBackground(Defaults.passiveEnabledColor);}
					else{matrix[i][j].setBackground(Defaults.passiveDisabledColor);}
				}
				matrix[i][j].revalidate();
				// revert current cell
				lolo.getMatrix().get(i).set(j,b);
			}
		}
	}

	private void writeParInPanel(Distribution prior, GridBag_TextField_Titled val, GridBag_TextField_Titled pom){
		if(prior==null) {return;}
		if(prior.getParval()[0]!=null) {val.setText(Double.toString(prior.getParval()[0]));}
		if(prior.getParval()[1]!=null) {pom.setText(Double.toString(2*prior.getParval()[1]));}
	}

	public void updateHydrauConfig(ConfigHydrauPanel panel){
		Parameter A,C,K;
		String id = panel.getId().getText();
		String description = panel.getDescription().getText();
		ControlPanel[] controls = panel.getControls();
		int nc=Integer.parseInt(panel.getNcontrol().getSelectedItem().toString());
		GridBag_CheckBox[][] matrix = panel.getMatrix();
		int indx=station.getHydrauConfigIndex(id);
		ConfigHydrau hydrau = new ConfigHydrau(station.getConfigAt(indx));
		hydrau.setName(id);
		hydrau.setDescription(description);
		if(nc>0){
			// controls			
			ArrayList<HydrauControl> conts = hydrau.getControls();
			if(conts==null){
				conts=new ArrayList<HydrauControl>();
				for (int i=0;i<nc;i++){conts.add(new HydrauControl());}
				hydrau.setControls(conts);
			}
			if(conts.size()>nc){
				for (int i=nc;i<conts.size();i++){conts.remove(i);}
				hydrau.setControls(conts);				
			}
			for (int i=0;i<nc;i++){
				HydrauControl cont;
				if(i>=conts.size()){
					cont=new HydrauControl();
					conts.add(cont);
				}
				else {cont=conts.get(i);}
				// description
				cont.setDescription(controls[i].getDescription().getText());
				// type
				cont.setType(controls[i].getType().getSelectedIndex());
				// K-A-C parameters
				A=readParInPanel("A"+(i+1),controls[i].getA(), controls[i].getApom());
				K=readParInPanel("K"+(i+1),controls[i].getK(), controls[i].getKpom());
				C=readParInPanel("C"+(i+1),controls[i].getC(), controls[i].getCpom());
				cont.setA(A);cont.setK(K);cont.setC(C);
				// create hydrau-control
			}
			hydrau.setControls(conts);

			// matrix
			BonnifaitMatrix mat = new BonnifaitMatrix();
			ArrayList<Boolean> col= new ArrayList<Boolean>();
			for(int i=0;i<nc;i++){
				col= new ArrayList<Boolean>();
				for(int j=0;j<i;j++){
					col.add(false);
				}
				for(int j=i;j<nc;j++){
					col.add(matrix[i][j].isSelected());
				}
				mat.AddColumn(col);
			}
			hydrau.setMatrix(mat);
		}
		station.setConfigAt(indx,hydrau);
	}

	public void resetControl(ControlPanel panel){
		ConfigHydrauPanel hp=panel.getHydrau();
		String id = hp.getId().getText();
		int i=hp.getControlPanel().getSelectedIndex();
		int indx=station.getHydrauConfigIndex(id);
		ConfigHydrau hydrau = station.getConfigAt(indx);
		HydrauControl cont = new HydrauControl();
		cont.setType(panel.getType().getSelectedIndex());
		hydrau.setControlAt(i,cont);		
		station.setConfigAt(indx,hydrau);
	}

	public void updateHydrauConfig_specifix(Frame_PriorAssistant panel){
		ConfigHydrauPanel hp=panel.getHydrau();
		String id = hp.getId().getText();
		int i=hp.getControlPanel().getSelectedIndex();
		int indx=station.getHydrauConfigIndex(id);
		ConfigHydrau hydrau = station.getConfigAt(indx);
		HydrauControl cont=hydrau.getControls().get(i);

		// read parameters in panel
		int n=panel.getUncertainties().length;
		Parameter[] specifix=new Parameter[n];
		for(int j=0;j<n;j++){
			specifix[j]=readParInPanel(Integer.toString(j),
					panel.getUncertainties()[j][0], panel.getUncertainties()[j][1]);
		}
		// update hydraulic config
		cont.setSpecifix(specifix);
		hydrau.setControlAt(i, cont);
		station.setConfigAt(indx,hydrau);
	}

	public void fillPriorAssistant(Frame_PriorAssistant panel){
		ConfigHydrauPanel hp=panel.getHydrau();
		String id = hp.getId().getText();
		int i=hp.getControlPanel().getSelectedIndex();
		int indx=station.getHydrauConfigIndex(id);
		ConfigHydrau hydrau = station.getConfigAt(indx);
		HydrauControl cont=hydrau.getControls().get(i);
		// read parameters in panel
		int n=panel.getUncertainties().length;
		for(int j=0;j<n;j++){
			if(cont.getSpecifix()!=null){	
				writeParInPanel(cont.getSpecifix()[j].getPrior(),
						panel.getUncertainties()[j][0],panel.getUncertainties()[j][1]);
			}
		}
	}

	private Parameter readParInPanel(String name,GridBag_TextField_Titled val, GridBag_TextField_Titled pom){
		String m0,sd0;
		Double m = null,sd = null;
		Distribution prior = new Distribution("Gaussian",2,new String[] {"mean","standard_deviation"},new Double[] {0.d,1.d});
		Parameter par=new Parameter();
		m0=val.getText();
		if(m0.equals(Constants.S_EMPTY)) {m=null;}
		else {m=safeParse_d(m0);}
		sd0=pom.getText();
		if(sd0.equals(Constants.S_EMPTY)) {sd=null;}
		else{sd=0.5*safeParse_d(sd0);}
		prior.setParval(new Double[] {m,sd});
		par.setPrior(prior);
		par.setValue(m);
		par.setName(name);
		return par;
	}

	public ChartPanel plotHydrau(ConfigHydrauPanel panel){
		// get current hydrau config
		int indx=station.getHydrauConfigIndex(panel.getId().getText());
		ConfigHydrau hydrau = station.getConfigAt(indx);
		boolean ylog=panel.getButt_ylog().isSelected();
		if(hydrau.getPriorEnv()==null){return null;}
		ChartPanel chart =null;
		if(hydrau.getPriorEnv().getNx()>0){
			chart = hydrau.plot(dico.entry("PriorRC")+" - "+hydrau.getName(),dico.entry("Hunit"),dico.entry("Qunit"),ylog);
		}
		return chart;
	}

	public void kickPlot_hydrau(ConfigHydrauPanel panel){
		ChartPanel chart = plotHydrau(panel);
		kickPlot(chart);
	}

	public String[] getHydrauList(){
		Dataset<ConfigHydrau> object = station.getConfig();
		return object.getStringList();
	}

	public int getHydrauConfigIndex(String name){
		return station.getHydrauConfigIndex(name);		
	}

	public void duplicateHydrauConfig(String original){
		ConfigHydrau old = station.getHydrauConfig(original);
		ConfigHydrau nu = new ConfigHydrau(old);
		MainFrame main = MainFrame.getInstance();
		Frame_GetName f=main.Popup_GetName(true,null,null,original+"_");
		String copy=f.getName();
		if(copy.equals(Constants.S_EMPTY)){return;}		
		// check that newname isn't used
		if(station.getConfig().isNameAlreadyUsed(copy)){
			new ExceptionPanel(main,dico.entry(Dataset.IS_USED));
			return;
		}
		nu.setName(copy);
		try {
			station.addConfig(copy);
		} catch (Exception e) {
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(e.getMessage()));
		}
		int indx=station.getHydrauConfigIndex(copy);
		station.setConfigAt(indx, nu);
		refresh(MainFrame.HYDRAULIC_INDX,copy);
	}	
	
	public boolean checkActivationStages(ConfigHydrauPanel panel){
		// get current hydrau config
		int indx=station.getHydrauConfigIndex(panel.getId().getText());
		ConfigHydrau hydrau = station.getConfigAt(indx);		
		return hydrau.checkActivationStages();
	}
	
	//-----------------------------------
	// RC options

	public boolean setPriorRatingCurveOptions(ConfigHydrauPanel panel){
		boolean ok=true;
		int nsim,nstep;
		Double hmin,hmax,hstep;
		nsim=safeParse_i(panel.getNsim().getText());
		ok=(nsim!=Constants.I_UNFEAS);if(!ok){return ok;}
		hmin=safeParse_D(panel.getHmin().getText());
		if(hmin!=null){ok=(hmin!=Constants.D_UNFEAS);if(!ok){return ok;}}
		hmax=safeParse_D(panel.getHmax().getText());
		if(hmax!=null){ok=(hmax!=Constants.D_UNFEAS);if(!ok){return ok;}}
		hstep=safeParse_D(panel.getHstep().getText());
		if(hstep!=null){ok=(hstep!=Constants.D_UNFEAS);if(!ok){return ok;}}
		nstep=safeParse_i(panel.getNstep().getText());
		ok=(nstep!=Constants.I_UNFEAS);if(!ok){return ok;}
		ConfigHydrau hydrau = station.getHydrauConfig(panel.getId().getText());
		hydrau.setPriorRCoptions(new PriorRatingCurveOptions(nsim,hmin,hmax,hstep,nstep));
		ok=(hmin!=null&hmax!=null&hstep!=null);
		return ok;
	}

	public boolean setPostRatingCurveOptions(RatingCurvePanel panel){
		boolean ok=true;
		Double hmin,hmax,hstep;
		int nstep;
		hmin=safeParse_D(panel.getHmin().getText());
		if(hmin!=null){ok=(hmin!=Constants.D_UNFEAS);if(!ok){return ok;}}
		hmax=safeParse_D(panel.getHmax().getText());
		if(hmax!=null){ok=(hmax!=Constants.D_UNFEAS);if(!ok){return ok;}}
		hstep=safeParse_D(panel.getHstep().getText());
		if(hstep!=null){ok=(hstep!=Constants.D_UNFEAS);if(!ok){return ok;}}
		nstep=safeParse_i(panel.getNstep().getText());
		ok=(nstep!=Constants.I_UNFEAS);if(!ok){return ok;}
		RatingCurve rc = station.getRatingCurve(panel.getId().getText());
		rc.setPostRCoptions(new PostRatingCurveOptions(hmin,hmax,hstep,nstep));
		ok=(hmin!=null&hmax!=null&hstep!=null);
		return ok;
	}

	//-----------------------------------
	// Gaugings

	public void addGaugingSet(String name){
		try {
			station.addGaugingSet(name);
		} catch (Exception e) {
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(e.getMessage()));
		}
		refresh(MainFrame.GAUGING_INDX,name);
	}

	public void deleteGaugingSet(String name){
		MainFrame main=MainFrame.getInstance();
		// Check with user that it's ok to remove this and all dependent objects
		int check=new Frame_YesNoQuestion().ask(main,dico.entry("DeleteGaugingsWarning"), dico.entry("Warning"),
				Defaults.iconWarning,dico.entry("Yes"),dico.entry("No"));
		if(check==JOptionPane.NO_OPTION) {return;}
		ArrayList<String> rc2delete=new ArrayList<String>();
		ArrayList<String> hydro2delete=new ArrayList<String>();
		// Look for all RC using this gauging set
		if(station.getRc()!=null){
			int n=station.getRc().getSize();
			for(int i=0;i<n;i++){
				RatingCurve rc=station.getRatingCurveAt(i);
				if(rc.getGauging_id()!=null){
					if(rc.getGauging_id().equals(name)){
						rc2delete.add(rc.getName());
						// Look for all hydrographs using the rc to be deleted
						if(station.getHydrograph()!=null){
							int m=station.getHydrograph().getSize();
							for(int j=0;j<m;j++){
								Hydrograph hydro=station.getHydrographAt(j);
								if(hydro.getRc_id()!=null){
									if(hydro.getRc_id().equals(rc.getName())){
										hydro2delete.add(hydro.getName());
									}
								}
							}
						}
					}
				}
			}
		}
		// Update trees, Perform deletions, redraw blank panels
		for(int i=0;i<hydro2delete.size();i++){
			main.getTabs().setComponentAt(MainFrame.HYDROGRAPH_INDX, new HydrographPanel("", false));
			station.deleteHydrograph(hydro2delete.get(i));

		}
		for(int i=0;i<rc2delete.size();i++){
			main.getTabs().setComponentAt(MainFrame.RC_INDX, new RatingCurvePanel("", false));
			station.deleteRatingCurve(rc2delete.get(i));
		}
		station.deleteGaugingSet(name);
		refresh(MainFrame.GAUGING_INDX,"");
	}

	public void renameGaugingSet(String name){
		MainFrame main=MainFrame.getInstance();
		// Ask new name
		Frame_GetName f=main.Popup_GetName(true,null,null,name+"_");
		String newname=f.getName();
		if(newname.equals(Constants.S_EMPTY)){return;}		
		// check that newname isn't used
		if(station.getGauging().isNameAlreadyUsed(newname)){
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(Dataset.IS_USED));
			return;
		}
		ArrayList<String> rename=new ArrayList<String>();
		// Look for all RC using these gaugings
		if(station.getRc()!=null){
			int n=station.getRc().getSize();
			for(int i=0;i<n;i++){
				RatingCurve rc=station.getRatingCurveAt(i);
				if(rc.getGauging_id()!=null){
					if(rc.getGauging_id().equals(name)){
						rename.add(rc.getName());
					}
				}
			}
		}
		// Update trees, Perform renaming, redraw panels
		for(int i=0;i<rename.size();i++){
			station.getRatingCurve(rename.get(i)).setGauging_id(newname);
			main.getTabs().setComponentAt(MainFrame.RC_INDX, new RatingCurvePanel("", false));
		}
		station.getGauging(name).setName(newname);
		refresh(MainFrame.GAUGING_INDX,newname);
	}

	public void fillGaugingPanel(String id,GaugingPanel panel){
		// Basic info
		GaugingSet gauging = station.getGauging(id);
		panel.getId().setText(gauging.getName());
		panel.getDescription().setText(gauging.getDescription());
		String file=new File(gauging.getFilePath()).getName();
		if(file!=null){panel.getFile().setText(file);}
		// Gauging table
		fillGaugingTable(gauging,panel);
		// Graph
		if(!gauging.getGaugings().isEmpty()){
			ChartPanel chart = plotGauging(panel);
			GridBag_Panel pan = panel.getPan_graph();
			if(chart!=null){
				pan.removeAll();
				GridBag_Layout.putIntoGrid(chart,pan,0,0,1,1,true,true);
			}
		}
		// select good tab
		panel.revalidate();
		MainFrame.getInstance().getTabs().setComponentAt(MainFrame.GAUGING_INDX, panel);
	}

	public void updateGaugingSet(GaugingPanel panel){
		// retrieve basic info
		String id = panel.getId().getText();
		String description = panel.getDescription().getText();
		String file = panel.getFile().getText();
		// retrieve active gaugings in table
		Object[] active = panel.getTM().getColumnAt(panel.getTM().getColumnCount()-1);
		// update object
		int indx=station.getGaugingIndex(id);
		GaugingSet gauging = new GaugingSet(station.getGaugingAt(indx));
		gauging.setName(id);
		gauging.setDescription(description);
		gauging.setFilePath(file);
		for (int i=0;i<gauging.getGaugings().size();i++){
			gauging.getGaugings().get(i).setActive((Boolean) active[i]);
		}
		station.setGaugingAt(indx,gauging);
	}

	public void importGaugingSet(GaugingPanel panel,String ext){
		String id = panel.getId().getText();
		int indx=station.getGaugingIndex(id);
		GaugingSet gauging = station.getGaugingAt(indx);
		try {
			gauging.read(ext);
			fillGaugingPanel(id,panel);
		} catch (FileNotFoundException e) {
			popupImportError();
		} catch (Exception e) {
			popupImportError();
		}
	}

	private void fillGaugingTable(GaugingSet gauging, GaugingPanel panel){
		// reset table model
		Custom_TableModel TM = panel.getTM();
		TM.reset();
		// fill TM with content of gauging object
		for(int i=0;i<gauging.getGaugings().size();i++){
			Gauging g = gauging.getGaugings().get(i);
			TM.addRow(new Object[] {g.getH(),g.getuH(),g.getQ(),g.getuQ(),g.getActive()});
		}
		panel.getTable().setModel(TM);TM.fireTableDataChanged();
	}

	public ChartPanel plotGauging(GaugingPanel panel){
		// get current gauging set
		GaugingSet gauging = station.getGauging(panel.getId().getText());
		if(gauging.getGaugings()==null){return null;}
		// get graph type & ylog
		boolean ylog=panel.getButt_ylog().isSelected();
		// do plot
		ChartPanel chart =null;
		if(!gauging.getGaugings().isEmpty()){
			chart = gauging.plot(dico.entry("Gaugings")+" - "+gauging.getName(),dico.entry("Hunit"),dico.entry("Qunit"),ylog);
		}
		return chart;
	}

	public void kickPlot_gauging(GaugingPanel panel){
		ChartPanel chart = plotGauging(panel);
		kickPlot(chart);
	}

	public String[] getGaugingList(){
		Dataset<GaugingSet> object = station.getGauging();
		return object.getStringList();
	}

	public void duplicateGauging(String original){
		GaugingSet old = station.getGauging(original);
		GaugingSet nu = new GaugingSet(old);
		MainFrame main = MainFrame.getInstance();
		Frame_GetName f=main.Popup_GetName(true,null,null,original+"_");
		String copy=f.getName();
		if(copy.equals(Constants.S_EMPTY)){return;}		
		// check that newname isn't used
		if(station.getGauging().isNameAlreadyUsed(copy)){
			new ExceptionPanel(main,dico.entry(Dataset.IS_USED));
			return;
		}
		nu.setName(copy);
		try {
			station.addGaugingSet(copy);
		} catch (Exception e) {
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(e.getMessage()));
		}
		int indx=station.getGaugingIndex(copy);
		station.setGaugingAt(indx, nu);
		refresh(MainFrame.GAUGING_INDX,copy);
	}	

	//-----------------------------------
	// Remnant errors

	public void addError(){

	}

	public String[] getErrorList(){
		Dataset<RemnantError> object = station.getRemnant();
		return object.getStringList();
	}

	//-----------------------------------
	// Limni

	public void addLimni(String name){
		try {
			station.addLimnigraph(name);
		} catch (Exception e) {
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(e.getMessage()));
		}
		refresh(MainFrame.LIMNI_INDX,name);
	}

	public void deleteLimni(String name){
		MainFrame main=MainFrame.getInstance();
		// Check with user that it's ok to remove this and all dependent objects
		int check=new Frame_YesNoQuestion().ask(main,dico.entry("DeleteLimniWarning"), dico.entry("Warning"),
				Defaults.iconWarning,dico.entry("Yes"),dico.entry("No"));
		if(check==JOptionPane.NO_OPTION) {return;}
		ArrayList<String> hydro2delete=new ArrayList<String>();
		// Look for all hydrographs to be deleted
		if(station.getHydrograph()!=null){
			int m=station.getHydrograph().getSize();
			for(int j=0;j<m;j++){
				Hydrograph hydro=station.getHydrographAt(j);
				if(hydro.getLimni_id()!=null){
					if(hydro.getLimni_id().equals(name)){
						hydro2delete.add(hydro.getName());
					}
				}
			}
		}
		// Update trees, Perform deletions, redraw blank panels
		for(int i=0;i<hydro2delete.size();i++){
			main.getTabs().setComponentAt(MainFrame.HYDROGRAPH_INDX, new HydrographPanel("", false));
			station.deleteHydrograph(hydro2delete.get(i));
		}
		station.deleteLimnigraph(name);
		refresh(MainFrame.LIMNI_INDX,"");
	}

	public void renameLimni(String name){
		MainFrame main=MainFrame.getInstance();
		// Ask new name
		Frame_GetName f=main.Popup_GetName(true,null,null,name+"_");
		String newname=f.getName();
		if(newname.equals(Constants.S_EMPTY)){return;}		
		// check that newname isn't used
		if(station.getLimni().isNameAlreadyUsed(newname)){
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(Dataset.IS_USED));
			return;
		}
		ArrayList<String> rename=new ArrayList<String>();
		// Look for all hydrographs using this limni
		if(station.getHydrograph()!=null){
			int n=station.getHydrograph().getSize();
			for(int i=0;i<n;i++){
				Hydrograph hydro=station.getHydrographAt(i);
				if(hydro.getLimni_id()!=null){
					if(hydro.getLimni_id().equals(name)){
						rename.add(hydro.getName());
					}
				}
			}
		}
		// Update trees, Perform renaming, redraw panels
		for(int i=0;i<rename.size();i++){
			station.getHydrograph(rename.get(i)).setLimni_id(newname);
			main.getTabs().setComponentAt(MainFrame.HYDROGRAPH_INDX, new HydrographPanel("", false));
		}
		station.getLimnigraph(name).setName(newname);
		refresh(MainFrame.LIMNI_INDX,newname);
	}

	public void fillLimniPanel(String id,LimniPanel panel){
		// Basic info
		Limnigraph limni = station.getLimnigraph(id);
		panel.getId().setText(limni.getName());
		panel.getDescription().setText(limni.getDescription());
		String file=new File(limni.getFilePath()).getName();
		if(file!=null) {panel.getFile().setText(file);}
		// Limnigraph table
		fillLimniTable(limni,panel);
		// Graph
		if(limni.getObservations()!=null){
			if(!limni.getObservations().isEmpty()){
				ChartPanel chart = plotLimni(panel);
				GridBag_Panel pan = panel.getPan_graph();
				if(chart!=null){
					pan.removeAll();
					GridBag_Layout.putIntoGrid(chart,pan,0,0,1,1,true,true);
				}
			}
		}
		// select good tab
		panel.revalidate();
		MainFrame.getInstance().getTabs().setComponentAt(MainFrame.LIMNI_INDX, panel);
	}

	public void updateLimnigraph(LimniPanel panel){
		// retrieve basic info
		String id = panel.getId().getText();
		String description = panel.getDescription().getText();
		String file = panel.getFile().getText();
		// update object
		int indx=station.getLimnigraphIndex(id);
		Limnigraph limni = new Limnigraph(station.getLimnigraphAt(indx));
		limni.setName(id);
		limni.setDescription(description);
		limni.setFilePath(file);
		station.setLimnigraphAt(indx,limni);
	}

	public void importLimnigraph(LimniPanel panel){
		String id = panel.getId().getText();
		int indx=station.getLimnigraphIndex(id);
		Limnigraph limni = station.getLimnigraphAt(indx);
		try {
			limni.read();
			fillLimniPanel(id,panel);
		} catch (FileNotFoundException e) {
			popupImportError();
		} catch (Exception e) {
			popupImportError();
		}
	}

	private void fillLimniTable(Limnigraph limni, LimniPanel panel){
		if(limni.getObservations()==null) {return;}
		// reset table model
		Custom_TableModel TM = panel.getTM();
		TM.reset();
		// fill TM with content of gauging object
		for(int i=0;i<limni.getObservations().size();i++){
			Observation obs = limni.getObservations().get(i);
			TM.addRow(new Object[] {obs.getObsDate().toString(),obs.getValue(),limni.getuH()[i],limni.getbHindx()[i],limni.getbH()[i]});
		}
		panel.getTable().setModel(TM);TM.fireTableDataChanged();
	}

	public ChartPanel plotLimni(LimniPanel panel){
		// get current limni
		Limnigraph limni = station.getLimnigraph(panel.getId().getText());
		if(limni.getObservations()==null){return null;}
		// get graph type & ylog
		boolean ylog=panel.getButt_ylog().isSelected();
		// do plot
		ChartPanel chart =null;
		if(!limni.getObservations().isEmpty()){
			chart = limni.plot(dico.entry("Limnigraph") +" - "+limni.getName(), dico.entry("TimeInYears"),dico.entry("Hunit"),ylog);
		}
		return chart;
	}

	public void kickPlot_limni(LimniPanel panel){
		ChartPanel chart = plotLimni(panel);
		kickPlot(chart);
	}

	public String[] getLimniList(){
		Dataset<Limnigraph> object = station.getLimni();
		return object.getStringList();
	}

	public void duplicateLimni(String original){
		Limnigraph old = station.getLimnigraph(original);
		Limnigraph nu = new Limnigraph(old);
		MainFrame main = MainFrame.getInstance();
		Frame_GetName f=main.Popup_GetName(true,null,null,original+"_");
		String copy=f.getName();
		if(copy.equals(Constants.S_EMPTY)){return;}		
		// check that newname isn't used
		if(station.getLimni().isNameAlreadyUsed(copy)){
			new ExceptionPanel(main,dico.entry(Dataset.IS_USED));
			return;
		}
		nu.setName(copy);
		try {
			station.addLimnigraph(copy);
		} catch (Exception e) {
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(e.getMessage()));
		}
		int indx=station.getLimnigraphIndex(copy);
		station.setLimnigraphAt(indx, nu);
		refresh(MainFrame.LIMNI_INDX,copy);
	}	

	//-----------------------------------
	// Rating Curve

	public void addRatingCurve(String name){
		try {
			station.addRatingCurve(name);
		} catch (Exception e) {
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(e.getMessage()));
		}
		refresh(MainFrame.RC_INDX,name);
	}

	public void deleteRatingCurve(String name){
		MainFrame main=MainFrame.getInstance();
		// Check with user that it's ok to remove this and all dependent objects
		int check=new Frame_YesNoQuestion().ask(main,dico.entry("DeleteRCWarning"), dico.entry("Warning"),
				Defaults.iconWarning,dico.entry("Yes"),dico.entry("No"));
		if(check==JOptionPane.NO_OPTION) {return;}
		ArrayList<String> hydro2delete=new ArrayList<String>();
		// Look for all hydrographs to be deleted
		if(station.getHydrograph()!=null){
			int m=station.getHydrograph().getSize();
			for(int j=0;j<m;j++){
				Hydrograph hydro=station.getHydrographAt(j);
				if(hydro.getRc_id()!=null){
					if(hydro.getRc_id().equals(name)){
						hydro2delete.add(hydro.getName());
					}
				}
			}
		}
		// Update trees, Perform deletions, redraw blank panels
		for(int i=0;i<hydro2delete.size();i++){
			main.getTabs().setComponentAt(MainFrame.HYDROGRAPH_INDX, new HydrographPanel("", false));
			station.deleteHydrograph(hydro2delete.get(i));
		}
		station.deleteRatingCurve(name);
		refresh(MainFrame.RC_INDX,"");
	}

	public void renameRatingCurve(String name){
		MainFrame main=MainFrame.getInstance();
		// Ask new name
		Frame_GetName f=main.Popup_GetName(true,null,null,name+"_");
		String newname=f.getName();
		if(newname.equals(Constants.S_EMPTY)){return;}		
		// check that newname isn't used
		if(station.getRc().isNameAlreadyUsed(newname)){
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(Dataset.IS_USED));
			return;
		}
		ArrayList<String> rename=new ArrayList<String>();
		// Look for all hydrographs using this RC
		if(station.getHydrograph()!=null){
			int n=station.getHydrograph().getSize();
			for(int i=0;i<n;i++){
				Hydrograph hydro=station.getHydrographAt(i);
				if(hydro.getRc_id()!=null){
					if(hydro.getRc_id().equals(name)){
						rename.add(hydro.getName());
					}
				}
			}
		}
		// Update trees, Perform renaming, redraw panels
		for(int i=0;i<rename.size();i++){
			station.getHydrograph(rename.get(i)).setRc_id(newname);
			main.getTabs().setComponentAt(MainFrame.HYDROGRAPH_INDX, new HydrographPanel("", false));
		}
		station.getRatingCurve(name).setName(newname);
		refresh(MainFrame.RC_INDX,newname);
	}

	public void fillRatingCurvePanel(String id,RatingCurvePanel panel){
		// Basic info
		RatingCurve rc = station.getRatingCurve(id);
		panel.getId().setText(rc.getName());
		panel.getDescription().setText(rc.getDescription());
		if(rc.getHydrau_id()!=null){panel.getCombo_hydrau().setSelectedIndex(station.getHydrauConfigIndex(rc.getHydrau_id()));}
		if(rc.getGauging_id()!=null){panel.getCombo_gauging().setSelectedIndex(station.getGaugingIndex(rc.getGauging_id()));}
		if(rc.getError_id()!=null){panel.getCombo_error().setSelectedIndex(station.getRemnantIndex(rc.getError_id()));}
		// Graph
		if(rc.getEnv_total()!=null){
			ChartPanel chart = plotRatingCurve(panel);
			GridBag_Panel pan = panel.getPan_graph();
			if(chart!=null){
				pan.removeAll();
				GridBag_Layout.putIntoGrid(chart,pan,0,0,1,1,true,true);
			}
		}
		// Post RC options
		PostRatingCurveOptions rco = rc.getPostRCoptions();
		if(rco!=null){
			if(rco.gethMin()!=null){panel.getHmin().setText(Double.toString(rco.gethMin()));}
			if(rco.gethMax()!=null){panel.getHmax().setText(Double.toString(rco.gethMax()));}
			if(rco.gethStep()!=null){panel.getHstep().setText(Double.toString(rco.gethStep()));}
			panel.getNstep().setText(Integer.toString(rco.getnStep()));
		}
		// select good tab
		panel.revalidate();
		MainFrame.getInstance().getTabs().setComponentAt(MainFrame.RC_INDX, panel);
	}

	public void updateRatingCurve(RatingCurvePanel panel){
		// retrieve basic info
		String id = panel.getId().getText();
		String description = panel.getDescription().getText();
		int indx=station.getRatingCurveIndex(id);
		// update object
		RatingCurve rc = new RatingCurve(station.getRatingCurveAt(indx)); //new RatingCurve(); //
		rc.setName(id);
		rc.setDescription(description);
		if(panel.getCombo_hydrau().getSelectedIndex()>=0){
			if(station.getConfig().getSize()>0){
				rc.setHydrau_id((String) panel.getCombo_hydrau().getSelectedItem());
			}
		}
		if(panel.getCombo_gauging().getSelectedIndex()>=0){
			if(station.getGauging().getSize()>0){
				rc.setGauging_id((String) panel.getCombo_gauging().getSelectedItem());
			}
		}
		if(panel.getCombo_error().getSelectedIndex()>=0){
			if(station.getRemnant().getSize()>0){
				RemnantError error = station.getRemnantAt(panel.getCombo_error().getSelectedIndex());
				rc.setError_id(error.getName());
			}
		}
		station.setRatingCurveAt(indx,rc);
	}

	public ChartPanel plotRatingCurve(RatingCurvePanel panel){
		// get current RC
		int indx=station.getRatingCurveIndex(panel.getId().getText());
		RatingCurve rc = station.getRatingCurveAt(indx);
		boolean ylog=panel.getButt_ylog().isSelected();
		if(rc.getEnv_total()==null){return null;}
		ChartPanel chart =null;
		if(rc.getEnv_total().getNx()>0){
			chart = rc.plot(dico.entry("PostRC") +" - "+rc.getName(),dico.entry("Hunit"),dico.entry("Qunit"),ylog);
		}
		return chart;
	}

	public void kickPlot_RatingCurve(RatingCurvePanel panel){
		ChartPanel chart = plotRatingCurve(panel);
		kickPlot(chart);
	}

	public String[] getRCList(){
		Dataset<RatingCurve> object = station.getRc();
		return object.getStringList();
	}

	public void exportRatingCurve(String name){
		// ask file
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle(dico.entry("Save"));
		fc.setCurrentDirectory(new File(MainFrame.getInstance().getLastDataDir()));
		fc.setAcceptAllFileFilterUsed(false);
		for(int i=0;i<filter_exportRC.length;i++){fc.addChoosableFileFilter(filter_exportRC[i]);}
		String file,file2;
		int returnVal = fc.showOpenDialog(null);
		if(returnVal!=JFileChooser.APPROVE_OPTION){return;}
		file=fc.getSelectedFile().getAbsolutePath();
		MainFrame.getInstance().setLastDataDir(fc.getSelectedFile().getAbsoluteFile().getParent());
		// Retrieve RC and save it
		RatingCurve rc=station.getRatingCurve(name);
		FileFilter foo = fc.getFileFilter();
		String ext = ((FileNameExtensionFilter) foo).getExtensions()[0];
		String fext=file.substring(file.length()-3, file.length());
		if(fext.equalsIgnoreCase(ext)){file2=file;} else {file2=file+"."+ext.toLowerCase();}
		try{
			if(ext.equalsIgnoreCase("csv")){rc.export_csv(file2);}
			else if(ext.equalsIgnoreCase("dat")){
				if(rc.getEnv_param().getX().length>RatingCurve.BAREME_NMAX){new InformationPanel(null,dico.entry("BaremeNmaxInfo"));}
				Frame_BaremeInfo bi = new Frame_BaremeInfo(MainFrame.getInstance());
				rc.export_bareme(file2,bi.getCode(),bi.getName(),bi.getStart(),bi.getEnd());
				}
		}
		/*
		catch (FileNotFoundException e) {}
		catch (IOException e) {}
		 */
		catch (Exception e) {new ExceptionPanel(null,dico.entry("SaveProblem"));}
	};

	public void exportRatingCurveEquation(String name){
		// ask file
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle(dico.entry("Save"));
		fc.setCurrentDirectory(new File(MainFrame.getInstance().getLastDataDir()));
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter("eq",new String[] {"eq", "EQ"}));
		int returnVal = fc.showOpenDialog(null);
		if(returnVal!=JFileChooser.APPROVE_OPTION){return;}
		String file = fc.getSelectedFile().getAbsolutePath();
		MainFrame.getInstance().setLastDataDir(fc.getSelectedFile().getAbsoluteFile().getParent());
		String fext=file.substring(file.length()-2, file.length());
		if(!fext.equalsIgnoreCase("eq")){file=file+".eq";}
		// Retrieve RC and export its equation
		RatingCurve rc=station.getRatingCurve(name);
		try{rc.export_equation(file);}
		catch (Exception e) {new ExceptionPanel(null,dico.entry("SaveProblem"));}
	};
	
	public void exportMCMC(String name){
		// ask file
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle(dico.entry("Save"));
		fc.setCurrentDirectory(new File(MainFrame.getInstance().getLastDataDir()));
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter("csv",new String[] {"csv", "CSV"}));
		int returnVal = fc.showOpenDialog(null);
		if(returnVal!=JFileChooser.APPROVE_OPTION){return;}
		String file = fc.getSelectedFile().getAbsolutePath();
		MainFrame.getInstance().setLastDataDir(fc.getSelectedFile().getAbsoluteFile().getParent());
		String fext=file.substring(file.length()-3, file.length());
		if(!fext.equalsIgnoreCase("csv")){file=file+".csv";}
		// Retrieve RC and export its equation
		RatingCurve rc=station.getRatingCurve(name);
		try{rc.export_mcmc(file);}
		catch (Exception e) {new ExceptionPanel(null,dico.entry("SaveProblem"));}
	};
	
	public void duplicateRatingCurve(String original){
		RatingCurve old = station.getRatingCurve(original);
		RatingCurve nu = new RatingCurve(old);
		MainFrame main = MainFrame.getInstance();
		Frame_GetName f=main.Popup_GetName(true,null,null,original+"_");
		String copy=f.getName();
		if(copy.equals(Constants.S_EMPTY)){return;}		
		// check that newname isn't used
		if(station.getRc().isNameAlreadyUsed(copy)){
			new ExceptionPanel(main,dico.entry(Dataset.IS_USED));
			return;
		}
		nu.setName(copy);
		try {
			station.addRatingCurve(copy);
		} catch (Exception e) {
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(e.getMessage()));
		}
		int indx=station.getRatingCurveIndex(copy);
		station.setRatingCurveAt(indx, nu);
		refresh(MainFrame.RC_INDX,copy);
	}	

	//-----------------------------------
	// Hydrograph

	public void addHydrograph(String name){
		try {
			station.addHydrograph(name);
		} catch (Exception e) {
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(e.getMessage()));
		}
		refresh(MainFrame.HYDROGRAPH_INDX,name);
	}

	public void deleteHydrograph(String name){
		MainFrame main=MainFrame.getInstance();
		// Check with user that it's ok to remove this and all dependent objects
		int check=new Frame_YesNoQuestion().ask(main,dico.entry("DeleteHydroWarning"), dico.entry("Warning"),
				Defaults.iconWarning,dico.entry("Yes"),dico.entry("No"));
		if(check==JOptionPane.NO_OPTION) {return;}
		station.deleteHydrograph(name);
		refresh(MainFrame.HYDROGRAPH_INDX,"");
	}

	public void renameHydrograph(String name){
		MainFrame main=MainFrame.getInstance();
		// Ask new name
		Frame_GetName f=main.Popup_GetName(true,null,null,name+"_");
		String newname=f.getName();
		if(newname.equals(Constants.S_EMPTY)){return;}		
		// check that newname isn't used
		if(station.getHydrograph().isNameAlreadyUsed(newname)){
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(Dataset.IS_USED));
			return;
		}
		station.getHydrograph(name).setName(newname);
		refresh(MainFrame.HYDROGRAPH_INDX,newname);
	}

	public void fillHydrographPanel(String id,HydrographPanel panel){
		// Basic info
		Hydrograph hyd = station.getHydrograph(id);
		panel.getId().setText(hyd.getName());
		panel.getDescription().setText(hyd.getDescription());
		if(hyd.getLimni_id()!=null){panel.getCombo_limni().setSelectedIndex(station.getLimnigraphIndex(hyd.getLimni_id()));}
		if(hyd.getRc_id()!=null){panel.getCombo_rc().setSelectedIndex(station.getRatingCurveIndex(hyd.getRc_id()));}
		// Graph
		if(hyd.getEnv_total()!=null){
			ChartPanel chart = plotHydrograph(panel);
			GridBag_Panel pan = panel.getPan_graph();
			if(chart!=null){
				pan.removeAll();
				GridBag_Layout.putIntoGrid(chart,pan,0,0,1,1,true,true);
			}
		}
		// select good tab
		panel.revalidate();
		MainFrame.getInstance().getTabs().setComponentAt(MainFrame.HYDROGRAPH_INDX, panel);
	}

	public void updateHydrograph(HydrographPanel panel){
		// retrieve basic info
		String id = panel.getId().getText();
		String description = panel.getDescription().getText();
		int indx=station.getHydrographIndex(id);
		// update object
		Hydrograph hyd = new Hydrograph(station.getHydrographAt(indx)); 
		hyd.setName(id);
		hyd.setDescription(description);
		if(panel.getCombo_limni().getSelectedIndex()>=0){
			if(station.getLimni().getSize()>0){
				hyd.setLimni_id((String) panel.getCombo_limni().getSelectedItem());
			}
		}
		if(panel.getCombo_rc().getSelectedIndex()>=0){
			if(station.getRc().getSize()>0){
				hyd.setRc_id((String) panel.getCombo_rc().getSelectedItem());
			}
		}
		station.setHydrographAt(indx,hyd);
	}

	public ChartPanel plotHydrograph(HydrographPanel panel){
		// get current hydrograph
		int indx=station.getHydrographIndex(panel.getId().getText());
		Hydrograph hyd = station.getHydrographAt(indx);
		boolean ylog=panel.getButt_ylog().isSelected();
		if(hyd.getObservations()==null){return null;}
		ChartPanel chart =null;
		if(hyd.getObservations().size()>0){
			chart = hyd.plot(dico.entry("Hydrograph")+" - "+hyd.getName(), dico.entry("TimeInYears"), dico.entry("Qunit"),ylog);
		}
		return chart;
	}

	public void kickPlot_Hydrograph(HydrographPanel panel){
		ChartPanel chart = plotHydrograph(panel);
		kickPlot(chart);
	}

	public String[] getHydrographList(){
		Dataset<Hydrograph> object = station.getHydrograph();
		return object.getStringList();
	}

	public void exportHydrograph(String name){
		// ask file
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle(dico.entry("Save"));
		fc.setCurrentDirectory(new File(MainFrame.getInstance().getLastDataDir()));
		String file;
		int returnVal = fc.showOpenDialog(null);
		if(returnVal!=JFileChooser.APPROVE_OPTION){return;}
		file=fc.getSelectedFile().getAbsolutePath();
		MainFrame.getInstance().setLastDataDir(fc.getSelectedFile().getAbsoluteFile().getParent());
		// Retrieve RC and save it
		Hydrograph h=station.getHydrograph(name);
		Envelop env=h.getEnv_h();
		Double[][] foo=station.getLimnigraph(h.getLimni_id()).toMatrix();
		Double[][] w=new Double[13][env.getNx()];
		w[0]=foo[0];
		w[1]=foo[1];
		w[2]=foo[2];
		w[3]=foo[3];
		w[4]=foo[4];
		w[5]=foo[5];		
		w[6]=env.getMaxpost();
		w[7]=env.getQlow();
		w[8]=env.getQhigh();
		env=h.getEnv_hparam();
		w[9]=env.getQlow();
		w[10]=env.getQhigh();
		env=h.getEnv_total();
		w[11]=env.getQlow();
		w[12]=env.getQhigh();
		try {
			ReadWrite.write(w,
					new String[] {"Year","Month","Day","Hour","Min","Sec","Qmaxpost[m3/s]","Qlow_stage[m3/s]","Qhigh_stage[m3/s]",
					"Qlow_stage+param[m3/s]","Qhigh_stage+param[m3/s]","Qlow_total[m3/s]","Qhigh_total[m3/s]"},
					file,Defaults.csvSep);
		}
		/*
		catch (FileNotFoundException e) {}
		catch (IOException e) {}
		 */
		catch (Exception e) {new ExceptionPanel(null,dico.entry("SaveProblem"));}
	};

	public void duplicateHydrograph(String original){
		Hydrograph old = station.getHydrograph(original);
		Hydrograph nu = new Hydrograph(old);
		MainFrame main = MainFrame.getInstance();
		Frame_GetName f=main.Popup_GetName(true,null,null,original+"_");
		String copy=f.getName();
		if(copy.equals(Constants.S_EMPTY)){return;}		
		// check that newname isn't used
		if(station.getHydrograph().isNameAlreadyUsed(copy)){
			new ExceptionPanel(main,dico.entry(Dataset.IS_USED));
			return;
		}
		nu.setName(copy);
		try {
			station.addHydrograph(copy);
		} catch (Exception e) {
			new ExceptionPanel(MainFrame.getInstance(),dico.entry(e.getMessage()));
		}
		int indx=station.getHydrographIndex(copy);
		station.setHydrographAt(indx, nu);
		refresh(MainFrame.HYDROGRAPH_INDX,copy);
	}	

}

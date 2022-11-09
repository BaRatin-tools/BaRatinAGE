package vue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;

import commons.Constants;
import commons.Frame_SelectItem;
import commons.Frame_YesNoQuestion;
import commons.GridBag_Button;
import commons.GridBag_CheckBox;
import commons.GridBag_ComboBox_Titled;
import commons.GridBag_Label;
import commons.GridBag_Layout;
import commons.GridBag_Panel;
import commons.GridBag_SplitPanel;
import commons.GridBag_TextField_Titled;
import commons.GridBag_Text_Titled;
import commons.GridBag_ToggleButton;
import controleur.Control;
import controleur.ExeControl;
import controleur.PlotControl;
import Utils.Config;
import Utils.Defaults;
import Utils.Dico;

/**
 * Panel for handling Hydraulic configurations
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class ConfigHydrauPanel extends ItemPanel implements ActionListener {

	private GridBag_Text_Titled id;
	private GridBag_TextField_Titled description;
	private GridBag_ComboBox_Titled ncontrol;
	private JPanel matrixPanel;
	private JTabbedPane controlPanel;
	private GridBag_Button butt_apply;
	private GridBag_CheckBox[][] matrix;
	private ControlPanel[] controls;
	private GridBag_Button butt_run;
	private GridBag_Button butt_kickout;
	private GridBag_ToggleButton butt_ylog; 
	private GridBag_Button butt_legend;
	private GridBag_Button butt_moreplots;
	private GridBag_Panel pan_graph;
	private GridBag_TextField_Titled nsim;
	private GridBag_TextField_Titled hmin;
	private GridBag_TextField_Titled hmax;
	private GridBag_TextField_Titled hstep;
	private GridBag_TextField_Titled nstep;
	private GridBag_Button butt_h2n;
	private GridBag_Button butt_n2h;

	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	private Control controller=Control.getInstance();
	private ExeControl exeController=ExeControl.getInstance();
	private PlotControl plotController=PlotControl.getInstance();
	private int currentNC=0;
	
	// constants
	public static final String[] nControlCombo=new String[] {"0","1","2","3","4","5","6","7","8","9","10"};
	public static final int nsim_def=1000;
	public static final int nstep_def=101;
	private static final String[] morePlotsList=new String[] {"Spaghetti"};
	private static final int Spag_indx=0;
	
	public ConfigHydrauPanel(String hID,boolean enabled) {
		super(new int[] {0,0,0,0}, new int[] {0,0}, new double[] {0.0,0.0,1.0,0.0},new double[] {1.0,1.0},
				new int[] {0}, new int[] {0}, new double[] {1.0},new double[] {1.0});
		drawInfoPanel(hID,enabled);
		drawGraphPanel(hID,enabled);
		if(!hID.equals("")){
			// fill in content with ConfigHydrau object, through controller
			controller.fillHydrauConfigPanel(hID,this);
		}
		ncontrol.addActionListener(listener_ncontrol);
	}

	private void drawInfoPanel(String hID,boolean enabled){
		// basic description
		id=new GridBag_Text_Titled(this.getInfoPanel(),Constants.S_EMPTY,dico.entry("Name"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,0,1,1);
		ncontrol=new GridBag_ComboBox_Titled(this.getInfoPanel(),nControlCombo,dico.entry("Ncontrol"),
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				1,0,1,1,true,false,dico.entry("Ncontrol"));
		description=new GridBag_TextField_Titled(this.getInfoPanel(),Constants.S_EMPTY,dico.entry("Description"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,1,3,1,dico.entry("Description"));
		ncontrol.setEnabled(enabled);description.setEnabled(enabled);

		// split pannel
		GridBag_SplitPanel split=new GridBag_SplitPanel(this.getInfoPanel(),JSplitPane.VERTICAL_SPLIT,0.5,0,2,2,1);
		JScrollPane scroll1=new JScrollPane();
		scroll1.setVerticalScrollBarPolicy(Defaults.scrollV);
		scroll1.setHorizontalScrollBarPolicy(Defaults.scrollH);
		matrixPanel=new JPanel();matrixPanel.setBackground(Defaults.bkgColor);
		scroll1.setViewportView(matrixPanel);
		split.setLeftComponent(scroll1);
		JScrollPane scroll2=new JScrollPane();
		scroll2.setVerticalScrollBarPolicy(Defaults.scrollV);
		scroll2.setHorizontalScrollBarPolicy(Defaults.scrollH);
		controlPanel = new JTabbedPane();
		controlPanel.setFont(config.getFontTabs());
		scroll2.setViewportView(controlPanel);
		split.setRightComponent(scroll2);
		// buttons
		butt_apply=new GridBag_Button(this.getInfoPanel(),this,"butt_apply",dico.entry("Apply"),
				Defaults.iconApply,0,3,2,1,false,true,"");
		butt_apply.setEnabled(enabled);
		// Extra actions only if hydrocontrol already exists
		if(!hID.equals("")){
			int nc=controller.getHydrauConfigNcontrol(hID);
			currentNC=nc;
			// Bonnifait Matrix
			matrix = drawMatrixPanel(nc);
			// Controls
			controls = drawControlPanel(nc);		
		}
		this.revalidate();
	}

	private void drawGraphPanel(String hID,boolean enabled){
		// Split Panel
		GridBag_SplitPanel split= new GridBag_SplitPanel(this.getGraphPanel(),JSplitPane.VERTICAL_SPLIT,0.D,0,0,1,1);
		// top panel
		JPanel topPanel=new JPanel();
		GridBag_Layout.SetGrid(topPanel,new int[] {0,0,0},new int[] {0,0,0,0,0,0,0},new double[] {0.0,0.0,0.0},new double[] {1.0,1.0,1.0,1.0,1.0,1.0,1.0});
		topPanel.setBackground(Defaults.bkgColor);
		split.setLeftComponent(topPanel);
		// bottom graph panel
		JPanel bottomPanel=new JPanel();
		GridBag_Layout.SetGrid(bottomPanel,new int[] {0,0},new int[] {0,0,0,0},new double[] {0.0,1.0},new double[] {1.0,1.0,1.0,1.0});
		bottomPanel.setBackground(Defaults.bkgColor);
		split.setRightComponent(bottomPanel);
		//------------------
		// top panel: compute prior RC
		new GridBag_Label(topPanel,dico.entry("PriorRC"),config.getFontBigLbl(),Defaults.txtColor,SwingConstants.CENTER,0,0,7,1,true,false);
		nsim=new GridBag_TextField_Titled(topPanel,Integer.toString(nsim_def),"Nsim",
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				0,1,1,2,dico.entry("NMCsim_long"));
		nsim.setEnabled(enabled);
		hmin=new GridBag_TextField_Titled(topPanel,"","Hmin",
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				1,1,1,2,dico.entry("Hmin_long"));
		hmin.setEnabled(enabled);
		hmax=new GridBag_TextField_Titled(topPanel,"","Hmax",
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				2,1,1,2,dico.entry("Hmax_long"));
		hmax.setEnabled(enabled);
		hstep=new GridBag_TextField_Titled(topPanel,"","Hstep",
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				5,1,1,2,dico.entry("Hstep_long"));
		hstep.setEnabled(enabled);
		butt_h2n=new GridBag_Button(topPanel,this,"butt_h2n","<-",
				null,4,2,1,1,true,false,dico.entry("h2n"));
		butt_h2n.setEnabled(enabled);
		butt_n2h=new GridBag_Button(topPanel,this,"butt_h2n","->",
				null,4,1,1,1,true,false,dico.entry("n2h"));
		butt_n2h.setEnabled(enabled);
		nstep=new GridBag_TextField_Titled(topPanel,Integer.toString(nstep_def),"Nstep",
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				3,1,1,2,dico.entry("Nstep_long"));
		nstep.setEnabled(enabled);
		butt_run=new GridBag_Button(topPanel,this,"butt_run",dico.entry("Run"),
				Defaults.iconRun,6,1,1,2,true,true,dico.entry("Run"));
		butt_run.setEnabled(enabled);
		
		//------------------		
		// bottom panel: graph			
		butt_ylog=new GridBag_ToggleButton(bottomPanel,this,"ylog",dico.entry("Ylog_on"),dico.entry("Ylog_off"),false,
				Defaults.iconYlog,0,0,1,1,true,true,dico.entry("ApplyYlog"));
		butt_ylog.setEnabled(enabled);
		butt_kickout=new GridBag_Button(bottomPanel,this,"butt_kickout",dico.entry("KickPlot"),
				Defaults.iconKickPlot,1,0,1,1,true,true,dico.entry("PlotInExternalWindow"));
		butt_kickout.setEnabled(enabled);
		butt_legend=new GridBag_Button(bottomPanel,this,"butt_legend",dico.entry("Legend"),
				Defaults.iconLegend,2,0,1,1,true,true,dico.entry("ShowLegend"));
		butt_legend.setEnabled(enabled);
		butt_moreplots=new GridBag_Button(bottomPanel,this,"butt_moreplots",dico.entry("MorePlot"),
				Defaults.iconMorePlots,3,0,1,1,true,true,dico.entry("MorePlotTip"));
		butt_moreplots.setEnabled(enabled);
		pan_graph=new GridBag_Panel(bottomPanel,0,1,4,1);
		pan_graph.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(pan_graph,new int[] {0},new int[] {0},new double[] {1.0},new double[] {1.0});
	}

	public ControlPanel[] drawControlPanel(int ncontrol){
		controlPanel.removeAll();
		ControlPanel[] controls= new ControlPanel[ncontrol];
		for(int i=0;i<ncontrol;i++){
			controls[i]=new ControlPanel(this);
			controlPanel.addTab(dico.entry("Control")+" "+(i+1),controls[i]);
		}
		controlPanel.revalidate();
		return controls;
	}

	public GridBag_CheckBox[][] drawMatrixPanel(int ncontrol){
		matrixPanel.removeAll();
		int nrow=ncontrol+2;int ncol=ncontrol+2;
		int[] xx=new int[ncol];
		int[] yy=new int[nrow];
		Arrays.fill(xx,0);
		Arrays.fill(yy,0);
		double[] wx=new double[ncol];
		double[] wy=new double[nrow];
		Arrays.fill(wx,1.0);
		Arrays.fill(wy,1.0);
		GridBag_Layout.SetGrid(matrixPanel,yy,xx,wy,wx);
		// draw column headers
		for(int i=0;i<ncontrol;i++){
			new GridBag_Label(matrixPanel,dico.entry("Control")+" "+(i+1)+" ",config.getFontTxt(),Defaults.txtColor,SwingConstants.CENTER,i+1,0,1,1,true,true);
		}
		// draw row headers
		for(int i=0;i<ncontrol;i++){
			String txt=dico.entry("Segment")+" "+(i+1)+" ";
			if(ncontrol>1 & i==0) {txt=txt+"("+dico.entry("lowest")+")";}
			if(ncontrol>1 & i==ncontrol-1) {txt=txt+"("+dico.entry("highest")+")";}
			new GridBag_Label(matrixPanel,txt,config.getFontTxt(),Defaults.txtColor,SwingConstants.CENTER,0,i+1,1,1,true,true);
		}
		GridBag_CheckBox[][] matrix = new GridBag_CheckBox[ncontrol][ncontrol];
		// draw buttons
		for(int i=0;i<ncontrol;i++){//column
			for(int j=i;j<ncontrol;j++){//row
				matrix[i][j]=new GridBag_CheckBox(matrixPanel,this,(i+","+j),i+1,j+1,1,1,true,true,"");
				matrix[i][j].setBackground(Defaults.bkgColor);
				if(i==j){matrix[i][j].setSelected(true);}
			}
		}
		// check feasability
		controller.setCheckboxEnabled(matrix);
		// refresh
		matrixPanel.revalidate();
		return matrix;
	}

	public void updateHydrau(){
		controller.updateHydrauConfig(this);
		controller.setPriorRatingCurveOptions(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(butt_apply)){
			updateHydrau();
		}
		else if(e.getSource().equals(butt_run)){
			boolean ok = controller.setPriorRatingCurveOptions(this);
			if(!ok){controller.popupFormatError();return;}
			controller.updateHydrauConfig(this);
			ok = controller.checkActivationStages(this);
			if(!ok){controller.popupKError();return;}
			exeController.run(new boolean[] {true,false,false,false}, // RunOptions
					null, // gaugings
					this.id.getText().trim(), // hydrau-config
					null, // remnant error model
					null, // MCMC
					null // Rating Curve
					);
		}
		else if(e.getSource().equals(butt_kickout)){
			controller.kickPlot_hydrau(this);
		}
		else if(e.getSource().equals(butt_ylog)){
			this.getPan_graph().removeAll();
			ChartPanel chart = controller.plotHydrau(this);
			if(chart!=null){
				GridBag_Layout.putIntoGrid(chart,this.getPan_graph(),0,0,1,1,true,true);
				this.getPan_graph().revalidate();
			}
		}
		else if(e.getSource().equals(butt_moreplots)){
			Frame_SelectItem f = new Frame_SelectItem(null,
					dico.entry("SelectItem"),dico.entry(morePlotsList),true,dico.entry("SelectPlot"),
					dico.entry("Apply"),dico.entry("Cancel"),
					Defaults.iconApply,Defaults.iconCancel,
					config.getFontTxt(),config.getFontLbl(),
					Defaults.bkgColor,Defaults.txtColor,Defaults.lblColor,
					Defaults.popupSize_Wide,
					null,null,dico.entry("SelectItem"));
			int indx=f.getIndx();
			if(indx>=0){
				JPanel chart = null;
				if(indx==Spag_indx){chart = plotController.priorSpag(this);}
				if(chart!=null){controller.kickPlot(chart);} 
			}
		}
		else if(e.getSource().equals(butt_legend)){controller.showLegend(this);}
		else if(e.getSource().equals(butt_h2n)){controller.fillNHstep(hmin,hmax,hstep,nstep,"n");}
		else if(e.getSource().equals(butt_n2h)){controller.fillNHstep(hmin,hmax,hstep,nstep,"h");}
		// if source is none of the above, it's the matrix, so update feasability
		controller.setCheckboxEnabled(matrix);matrixPanel.revalidate();this.revalidate();
	}

	ActionListener listener_ncontrol = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			int k=Integer.parseInt((String) ncontrol.getSelectedItem());
			if(currentNC>0) {
				String mess = String.format("<html>%s<br>%s</html>", dico.entry("nControlWarning"), dico.entry("ConfirmContinue"));
				int ok=new Frame_YesNoQuestion().ask(MainFrame.getInstance(),mess,
							dico.entry("Warning"),
							Defaults.iconWarning,dico.entry("Yes"),dico.entry("No"));
				if(ok==JOptionPane.NO_OPTION) {
					ncontrol.setSelectedIndex(currentNC);
					return;
				}
			}
			matrix=drawMatrixPanel(k);
			controls=drawControlPanel(k);
			currentNC=k;
		};
	};
	
	ItemListener listener_graph = new ItemListener() {
		public void itemStateChanged(ItemEvent itemEvent) {
			updateHydrau();
		}
	};

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public GridBag_TextField_Titled getDescription() {
		return description;
	}

	public void setDescription(GridBag_TextField_Titled description) {
		this.description = description;
	}

	public GridBag_Text_Titled getId() {
		return id;
	}

	public void setId(GridBag_Text_Titled id) {
		this.id = id;
	}

	public GridBag_ComboBox_Titled getNcontrol() {
		return ncontrol;
	}

	public void setNcontrol(GridBag_ComboBox_Titled ncontrol) {
		this.ncontrol = ncontrol;
	}

	public JPanel getMatrixPanel() {
		return matrixPanel;
	}

	public void setMatrixPanel(JPanel matrixPanel) {
		this.matrixPanel = matrixPanel;
	}

	public JTabbedPane getControlPanel() {
		return controlPanel;
	}

	public void setControlPanel(JTabbedPane controlPanel) {
		this.controlPanel = controlPanel;
	}

	public GridBag_CheckBox[][] getMatrix() {
		return matrix;
	}

	public void setMatrix(GridBag_CheckBox[][] matrix) {
		this.matrix = matrix;
	}

	public ControlPanel[] getControls() {
		return controls;
	}

	public void setControls(ControlPanel[] controls) {
		this.controls = controls;
	}

	public GridBag_Button getButt_apply() {
		return butt_apply;
	}

	public void setButt_apply(GridBag_Button butt_apply) {
		this.butt_apply = butt_apply;
	}

	public GridBag_Button getButt_run() {
		return butt_run;
	}

	public void setButt_run(GridBag_Button butt_run) {
		this.butt_run = butt_run;
	}

	public GridBag_Panel getPan_graph() {
		return pan_graph;
	}

	public void setPan_graph(GridBag_Panel pan_graph) {
		this.pan_graph = pan_graph;
	}

	public GridBag_TextField_Titled getNsim() {
		return nsim;
	}

	public void setNsim(GridBag_TextField_Titled nsim) {
		this.nsim = nsim;
	}

	public GridBag_TextField_Titled getHmin() {
		return hmin;
	}

	public void setHmin(GridBag_TextField_Titled hmin) {
		this.hmin = hmin;
	}

	public GridBag_TextField_Titled getHmax() {
		return hmax;
	}

	public void setHmax(GridBag_TextField_Titled hmax) {
		this.hmax = hmax;
	}

	public GridBag_TextField_Titled getHstep() {
		return hstep;
	}

	public void setHstep(GridBag_TextField_Titled hstep) {
		this.hstep = hstep;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public Dico getDico() {
		return dico;
	}

	public void setDico(Dico dico) {
		this.dico = dico;
	}

	public Control getController() {
		return controller;
	}

	public void setController(Control controller) {
		this.controller = controller;
	}

	public ExeControl getExeController() {
		return exeController;
	}

	public void setExeController(ExeControl exeController) {
		this.exeController = exeController;
	}

	public static String[] getNcontrolcombo() {
		return nControlCombo;
	}

	public static int getNsimDef() {
		return nsim_def;
	}

	public GridBag_Button getButt_kickout() {
		return butt_kickout;
	}

	public void setButt_kickout(GridBag_Button butt_kickout) {
		this.butt_kickout = butt_kickout;
	}

	public GridBag_ToggleButton getButt_ylog() {
		return butt_ylog;
	}

	public void setButt_ylog(GridBag_ToggleButton butt_ylog) {
		this.butt_ylog = butt_ylog;
	}

	public GridBag_Button getButt_legend() {
		return butt_legend;
	}

	public void setButt_legend(GridBag_Button butt_legend) {
		this.butt_legend = butt_legend;
	}

	public GridBag_TextField_Titled getNstep() {
		return nstep;
	}

	public void setNstep(GridBag_TextField_Titled nstep) {
		this.nstep = nstep;
	}

	
	public GridBag_Button getButt_h2n() {
		return butt_h2n;
	}
	

	public void setButt_h2n(GridBag_Button butt_h2n) {
		this.butt_h2n = butt_h2n;
	}
	

	public GridBag_Button getButt_n2h() {
		return butt_n2h;
	}
	

	public void setButt_n2h(GridBag_Button butt_n2h) {
		this.butt_n2h = butt_n2h;
	}
	
}

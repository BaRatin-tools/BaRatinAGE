package vue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;

import Utils.Config;
import Utils.Defaults;
import Utils.Dico;
import commons.Constants;
import commons.Frame_SelectItem;
import commons.GridBag_Button;
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

/**
 * Panel for handling rating curves
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class RatingCurvePanel extends ItemPanel implements ActionListener {

	private GridBag_Text_Titled id;
	private GridBag_TextField_Titled description;
	private GridBag_ComboBox_Titled combo_hydrau;
	private GridBag_ComboBox_Titled combo_gauging;
	private GridBag_ComboBox_Titled combo_error;
	private GridBag_Button butt_apply;
	private GridBag_Button butt_run;
	private GridBag_Button butt_kickout;
	private GridBag_Button butt_moreplots;
	private GridBag_Button butt_legend;
	private GridBag_ToggleButton butt_ylog; 
	private GridBag_Panel pan_graph;
	private GridBag_TextField_Titled hmin;
	private GridBag_TextField_Titled hmax;
	private GridBag_TextField_Titled hstep;
	private GridBag_TextField_Titled nstep;
	private GridBag_Button butt_h2n;
	private GridBag_Button butt_n2h;

	// constants
	private static final String[] morePlotsList=new String[] {"PriorVsPost_Par","PriorVsPost_ParTable","PriorVsPost_RC","MCMCtrace","Spaghetti","Remarks"};
	private static final int PVP_Par_indx=0;
	private static final int PVP_ParTable_indx=1;
	private static final int PVP_RC_indx=2;
	private static final int MCMC_indx=3;
	private static final int Spag_indx=4;
	private static final int Remarks_indx=5;
	public static final int nstep_def=101;

	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	private Control controller=Control.getInstance();
	private ExeControl exeController=ExeControl.getInstance();
	private PlotControl plotController=PlotControl.getInstance();
	
	
	/**
	 * Full constructor
	 * @param rcID, ID of the rating curve
	 * @param enabled, are components enabled?
	 */
	public RatingCurvePanel(String rcID,boolean enabled) {
		super(new int[] {0,0,0,0,0,0,0}, new int[] {0}, new double[] {0.0,0.0,0.0,0.0,0.0,1.0,0.0},new double[] {1.0},
				new int[] {0}, new int[] {0}, new double[] {1.0},new double[] {1.0});
		drawInfoPanel(enabled);
		drawGraphPanel(enabled);
		if(!rcID.equals("")){
			// fill in content with Rating Curve object, through controller
			controller.fillRatingCurvePanel(rcID,this);
		}
	}
	
	private void drawInfoPanel(boolean enabled){
		id=new GridBag_Text_Titled(this.getInfoPanel(),Constants.S_BLANK,dico.entry("Name"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,0,1,1);
		description=new GridBag_TextField_Titled(this.getInfoPanel(),Constants.S_BLANK,dico.entry("Description"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,1,1,1,dico.entry("Description"));
		description.setEnabled(enabled);
		combo_hydrau=new GridBag_ComboBox_Titled(this.getInfoPanel(),controller.getHydrauList(),dico.entry("HydrauConf"),
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				0,2,1,1,true,true,dico.entry("HydrauConf"));
		combo_hydrau.setEnabled(enabled);
		combo_gauging=new GridBag_ComboBox_Titled(this.getInfoPanel(),controller.getGaugingList(),dico.entry("Gaugings"),
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				0,3,1,1,true,true,dico.entry("Gaugings"));
		combo_gauging.setEnabled(enabled);
		combo_error=new GridBag_ComboBox_Titled(this.getInfoPanel(),dico.entry(controller.getErrorList()),dico.entry("RemnantError"),
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				0,4,1,1,true,true,dico.entry("RemnantError"));
		combo_error.setEnabled(enabled);
		// buttons
		butt_apply=new GridBag_Button(this.getInfoPanel(),this,"butt_apply",dico.entry("Apply"),
				Defaults.iconApply,0,6,1,1,false,false,"");
		butt_apply.setEnabled(enabled);

	}
	
	private void drawGraphPanel(boolean enabled){
		// Split Panel
		GridBag_SplitPanel split= new GridBag_SplitPanel(this.getGraphPanel(),JSplitPane.VERTICAL_SPLIT,0.D,0,0,1,1);
		// top panel
		JPanel topPanel=new JPanel();
		GridBag_Layout.SetGrid(topPanel,new int[] {0,0,0},new int[] {0,0,0,0,0,0},new double[] {0.0,0.0,0.0},new double[] {1.0,1.0,1.0,1.0,1.0,1.0});
		topPanel.setBackground(Defaults.bkgColor);
		split.setLeftComponent(topPanel);
		// bottom graph panel
		JPanel bottomPanel=new JPanel();
		GridBag_Layout.SetGrid(bottomPanel,new int[] {0,0},new int[] {0,0,0,0},new double[] {0.0,1.0},new double[] {1.0,1.0,1.0,1.0});
		bottomPanel.setBackground(Defaults.bkgColor);
		split.setRightComponent(bottomPanel);
		//------------------
		// top panel: compute RC
		new GridBag_Label(topPanel,dico.entry("PostRC"),config.getFontBigLbl(),Defaults.txtColor,SwingConstants.CENTER,0,0,6,1,true,false);
		hmin=new GridBag_TextField_Titled(topPanel,"","Hmin",
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				0,1,1,2,dico.entry("Hmin_long"));
		hmin.setEnabled(enabled);
		hmax=new GridBag_TextField_Titled(topPanel,"","Hmax",
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				1,1,1,2,dico.entry("Hmax_long"));
		hmax.setEnabled(enabled);
		hstep=new GridBag_TextField_Titled(topPanel,"","Hstep",
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				4,1,1,2,dico.entry("Hstep_long"));
		hstep.setEnabled(enabled);
		butt_h2n=new GridBag_Button(topPanel,this,"butt_h2n","<-",
				null,3,2,1,1,true,false,dico.entry("h2n"));
		butt_h2n.setEnabled(enabled);
		butt_n2h=new GridBag_Button(topPanel,this,"butt_h2n","->",
				null,3,1,1,1,true,false,dico.entry("n2h"));
		butt_n2h.setEnabled(enabled);
		nstep=new GridBag_TextField_Titled(topPanel,Integer.toString(nstep_def),"Nstep",
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				2,1,1,2,dico.entry("Nstep_long"));
		nstep.setEnabled(enabled);
		butt_run=new GridBag_Button(topPanel,this,"butt_run",dico.entry("Run"),
				Defaults.iconRun,5,1,1,2,true,true,dico.entry("Run"));
		butt_run.setEnabled(enabled);		

		//------------------		
		// bottom: graph panel
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

	private void updateRC(){
		controller.updateRatingCurve(this);
		controller.setPostRatingCurveOptions(this);
		controller.fillRatingCurvePanel(this.id.getText().trim(), this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(butt_apply)){
			updateRC();
		}
		else if(e.getSource().equals(butt_run)){
			boolean ok = controller.setPostRatingCurveOptions(this);
			if(!ok){controller.popupFormatError();return;}
			updateRC();
			exeController.run(new boolean[] {false,true,true,false}, // RunOptions
					this.combo_gauging.getSelectedItem().toString(), // gaugings
					this.combo_hydrau.getSelectedItem().toString(), // hydrau-config
					this.combo_error.getSelectedIndex(), // remnant error model
					null, // MCMC
					this.getId().getText() // Rating curve
					);
		}
		else if(e.getSource().equals(butt_kickout)){
			controller.kickPlot_RatingCurve(this);
		}
		else if(e.getSource().equals(butt_ylog)){
			this.getPan_graph().removeAll();
			ChartPanel chart = controller.plotRatingCurve(this);
			if(chart!=null){
				GridBag_Layout.putIntoGrid(chart,this.getPan_graph(),0,0,1,1,true,true);
				this.getPan_graph().revalidate();
			}
		}
		else if(e.getSource().equals(butt_moreplots)){
			// JPanel chart = plotController.remarks(this);
			// controller.kickPlot(chart);
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
				if(indx==PVP_Par_indx){chart = plotController.priorVsPost_par(this);}
				else if(indx==PVP_ParTable_indx){chart = plotController.priorVsPost_parTable(this);}
				else if(indx==PVP_RC_indx){chart = plotController.priorVsPost_RC(this);}
				else if(indx==MCMC_indx){chart = plotController.MCMCtraces(this);}
				else if(indx==Spag_indx){chart = plotController.postSpag(this);}
				else if(indx==Remarks_indx){chart = plotController.remarks(this);}
				if(chart!=null){controller.kickPlot(chart);} 
			}
		}
		else if(e.getSource().equals(butt_legend)){controller.showLegend(this);}
		else if(e.getSource().equals(butt_h2n)){controller.fillNHstep(hmin,hmax,hstep,nstep,"n");}
		else if(e.getSource().equals(butt_n2h)){controller.fillNHstep(hmin,hmax,hstep,nstep,"h");}
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public GridBag_Text_Titled getId() {
		return id;
	}

	public void setId(GridBag_Text_Titled id) {
		this.id = id;
	}

	public GridBag_TextField_Titled getDescription() {
		return description;
	}

	public void setDescription(GridBag_TextField_Titled description) {
		this.description = description;
	}

	public GridBag_ComboBox_Titled getCombo_hydrau() {
		return combo_hydrau;
	}

	public void setCombo_hydrau(GridBag_ComboBox_Titled combo_hydrau) {
		this.combo_hydrau = combo_hydrau;
	}

	public GridBag_ComboBox_Titled getCombo_gauging() {
		return combo_gauging;
	}

	public void setCombo_gauging(GridBag_ComboBox_Titled combo_gauging) {
		this.combo_gauging = combo_gauging;
	}

	public GridBag_ComboBox_Titled getCombo_error() {
		return combo_error;
	}

	public void setCombo_error(GridBag_ComboBox_Titled combo_error) {
		this.combo_error = combo_error;
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
	
	public GridBag_Panel getPan_graph() {
		return pan_graph;
	}
	
	public void setPan_graph(GridBag_Panel pan_graph) {
		this.pan_graph = pan_graph;
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

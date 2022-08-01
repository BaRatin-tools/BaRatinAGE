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
 * Panel for handling hydrographs
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class HydrographPanel extends ItemPanel implements ActionListener {

	private GridBag_Text_Titled id;
	private GridBag_TextField_Titled description;
	private GridBag_ComboBox_Titled combo_limni;
	private GridBag_ComboBox_Titled combo_rc;
	private GridBag_Button butt_apply;
	private GridBag_Button butt_run;
	private GridBag_Button butt_kickout;
	private GridBag_Button butt_moreplots;
	private GridBag_Button butt_legend;
	private GridBag_ToggleButton butt_ylog; 
	private GridBag_Panel pan_graph;

	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	private Control controller=Control.getInstance();
	private ExeControl exeController=ExeControl.getInstance();
	private PlotControl plotController=PlotControl.getInstance();
	
	// constants
	private static final String[] morePlotsList=new String[] {"Spaghetti"};
	private static final int Spag_indx=0;
	
	/**
	 * Full constructor
	 * @param hID, ID of the hydrograph
	 * @param enabled, are components enabled?
	 */	
	public HydrographPanel(String hID,boolean enabled) {
		super(new int[] {0,0,0,0,0,0}, new int[] {0}, new double[] {0.0,0.0,0.0,0.0,1.0,0.0},new double[] {1.0},
				new int[] {0}, new int[] {0}, new double[] {1.0},new double[] {1.0});
		drawInfoPanel(enabled);
		drawGraphPanel(enabled);
		if(!hID.equals("")){
			// fill in content with Hydrograph object, through controller
			controller.fillHydrographPanel(hID,this);
		}
	}
	
	private void drawInfoPanel(boolean enabled){
		id=new GridBag_Text_Titled(this.getInfoPanel(),Constants.S_BLANK,dico.entry("Name"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,0,1,1);
		description=new GridBag_TextField_Titled(this.getInfoPanel(),Constants.S_BLANK,dico.entry("Description"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,1,1,1,dico.entry("Description"));
		description.setEnabled(enabled);
		combo_limni=new GridBag_ComboBox_Titled(this.getInfoPanel(),controller.getLimniList(),dico.entry("Limnigraph"),
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				0,2,1,1,true,true,dico.entry("HydrauConf"));
		combo_limni.setEnabled(enabled);
		combo_rc=new GridBag_ComboBox_Titled(this.getInfoPanel(),controller.getRCList(),dico.entry("RatingCurve"),
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				0,3,1,1,true,true,dico.entry("Gaugings"));
		combo_rc.setEnabled(enabled);
		// buttons
		butt_apply=new GridBag_Button(this.getInfoPanel(),this,"butt_apply",dico.entry("Apply"),
				Defaults.iconApply,0,5,1,1,false,false,"");
		butt_apply.setEnabled(enabled);

	}

	private void drawGraphPanel(boolean enabled){
		// Split Panel
		GridBag_SplitPanel split= new GridBag_SplitPanel(this.getGraphPanel(),JSplitPane.VERTICAL_SPLIT,0.D,0,0,1,1);
		// top panel
		JPanel topPanel=new JPanel();
		GridBag_Layout.SetGrid(topPanel,new int[] {0,0},new int[] {0,0,0,0},new double[] {0.0,0.0},new double[] {1.0,1.0,1.0,1.0});
		topPanel.setBackground(Defaults.bkgColor);
		split.setLeftComponent(topPanel);
		// bottom graph panel
		JPanel bottomPanel=new JPanel();
		GridBag_Layout.SetGrid(bottomPanel,new int[] {0,0},new int[] {0,0,0,0},new double[] {0.0,1.0},new double[] {1.0,1.0,1.0,1.0});
		bottomPanel.setBackground(Defaults.bkgColor);
		split.setRightComponent(bottomPanel);
		//------------------
		// top panel: propagate
		new GridBag_Label(topPanel,dico.entry("Hydrograph"),config.getFontBigLbl(),Defaults.txtColor,SwingConstants.CENTER,0,0,4,1,true,false);
		butt_run=new GridBag_Button(topPanel,this,"butt_run",dico.entry("Run"),
				Defaults.iconRun,0,1,4,1,false,true,dico.entry("Run"));
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
		if(!config.isSaveHydroSpag()) butt_moreplots.setEnabled(false);
		pan_graph=new GridBag_Panel(bottomPanel,0,1,4,1);
		pan_graph.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(pan_graph,new int[] {0},new int[] {0},new double[] {1.0},new double[] {1.0});
	}

	private void updateHydro(){
		controller.updateHydrograph(this);
		controller.fillHydrographPanel(this.id.getText().trim(), this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(butt_apply)){updateHydro();}
		else if(e.getSource().equals(butt_run)){
			updateHydro();
			exeController.propagate(this.id.getText());
			}
		else if(e.getSource().equals(butt_kickout)){controller.kickPlot_Hydrograph(this);}
		else if(e.getSource().equals(butt_ylog)){
			this.pan_graph.removeAll();
			ChartPanel chart = controller.plotHydrograph(this);
			if(chart!=null){
				GridBag_Layout.putIntoGrid(chart,this.pan_graph,0,0,1,1,true,true);
				this.pan_graph.revalidate();
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
				if(indx==Spag_indx){chart = plotController.hydroSpag(this);}
				if(chart!=null){controller.kickPlot(chart);} 
			}
		}
		else if(e.getSource().equals(butt_legend)){controller.showLegend(this);}
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

	public GridBag_ComboBox_Titled getCombo_limni() {
		return combo_limni;
	}

	public void setCombo_limni(GridBag_ComboBox_Titled combo_limni) {
		this.combo_limni = combo_limni;
	}

	public GridBag_ComboBox_Titled getCombo_rc() {
		return combo_rc;
	}

	public void setCombo_rc(GridBag_ComboBox_Titled combo_rc) {
		this.combo_rc = combo_rc;
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

	public GridBag_Button getButt_moreplots() {
		return butt_moreplots;
	}

	public void setButt_moreplots(GridBag_Button butt_moreplots) {
		this.butt_moreplots = butt_moreplots;
	}

	public GridBag_Button getButt_legend() {
		return butt_legend;
	}

	public void setButt_legend(GridBag_Button butt_legend) {
		this.butt_legend = butt_legend;
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
}

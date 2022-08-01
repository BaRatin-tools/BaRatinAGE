package vue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTable;

import Utils.Config;
import Utils.Defaults;
import Utils.Dico;
import commons.Constants;
import commons.Custom_FileChooser;
import commons.Custom_TableModel;
import commons.GridBag_Button;
import commons.GridBag_Layout;
import commons.GridBag_Panel;
import commons.GridBag_Scroll;
import commons.GridBag_TextField_Titled;
import commons.GridBag_Text_Titled;
import commons.GridBag_ToggleButton;
import controleur.Control;

/**
 * Panel for handling limnigraphs
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class LimniPanel extends ItemPanel implements ActionListener{

	private GridBag_Text_Titled id;
	private GridBag_Text_Titled file;
	private GridBag_TextField_Titled description;
	private GridBag_Button butt_browse;
	private GridBag_Button butt_apply;
	private JTable table;
	private Custom_TableModel TM;
	private GridBag_ToggleButton butt_ylog; 
	private GridBag_Button butt_kickout;
	private GridBag_Button butt_legend;
	private GridBag_Button butt_moreplots;
	private GridBag_Panel pan_graph;

	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	private Control controller=Control.getInstance();

	public LimniPanel(String lID,boolean enabled) {
		super(new int[] {0,0,0,0,0}, new int[] {0,0}, new double[] {0.0,0.0,0.0,1.0,0.0},new double[] {1.0,1.0},
				new int[] {0,0}, new int[] {0,0,0,0}, new double[] {0.0,1.0},new double[] {1.0,1.0,1.0,1.0});
		drawInfoPanel(enabled);
		drawGraphPanel(enabled);
		if(!lID.equals("")){
			// fill in content with GaugingSet object, through controller
			controller.fillLimniPanel(lID,this);
		}
	}

	private void drawInfoPanel(boolean enabled){
		id=new GridBag_Text_Titled(this.getInfoPanel(),Constants.S_BLANK,dico.entry("Name"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,0,2,1);
		description=new GridBag_TextField_Titled(this.getInfoPanel(),Constants.S_BLANK,dico.entry("Description"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,1,2,1,dico.entry("Description"));
		file=new GridBag_Text_Titled(this.getInfoPanel(),Constants.S_BLANK,dico.entry("File"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,2,1,1);
		butt_browse=new GridBag_Button(this.getInfoPanel(),this,"butt_browse",dico.entry("Browse"),
				Defaults.iconBrowse,1,2,1,1,false,true,"");
		description.setEnabled(enabled);file.setEnabled(enabled);butt_browse.setEnabled(enabled);
		GridBag_Scroll scroll=new GridBag_Scroll(this.getInfoPanel(),0,3,2,1);
		TM = new Custom_TableModel(new String[] {dico.entry("Date"),dico.entry("H"),dico.entry("uH"),dico.entry("bHindx"),dico.entry("bH")},
				new Boolean[] {false,false,false,false,false});
		table=new JTable(TM);
		scroll.setViewportView(table);
		butt_apply=new GridBag_Button(this.getInfoPanel(),this,"butt_apply",dico.entry("Apply"),
				Defaults.iconApply,0,4,2,1,false,true,"");
		butt_apply.setEnabled(enabled);
	}

	private void drawGraphPanel(boolean enabled){
		//------------------
		// 1st row: tunings
		// draw what?
		butt_ylog=new GridBag_ToggleButton(this.getGraphPanel(),this,"ylog",dico.entry("Ylog_on"),dico.entry("Ylog_off"),false,
				Defaults.iconYlog,0,0,1,1,true,true,dico.entry("ApplyYlog"));
		butt_ylog.setEnabled(enabled);
		butt_kickout=new GridBag_Button(this.getGraphPanel(),this,"butt_kickout",dico.entry("KickPlot"),
				Defaults.iconKickPlot,1,0,1,1,true,true,dico.entry("PlotInExternalWindow"));
		butt_kickout.setEnabled(enabled);
		butt_legend=new GridBag_Button(this.getGraphPanel(),this,"butt_legend",dico.entry("Legend"),
				Defaults.iconLegend,2,0,1,1,true,true,dico.entry("ShowLegend"));
		butt_legend.setEnabled(enabled);
		butt_moreplots=new GridBag_Button(this.getGraphPanel(),this,"butt_moreplots",dico.entry("MorePlot"),
				Defaults.iconMorePlots,3,0,1,1,true,true,dico.entry("MorePlotTip"));
		//butt_moreplots.setEnabled(enabled);
		butt_moreplots.setEnabled(false);
		//------------------		
		// bottom: graph panel
		pan_graph=new GridBag_Panel(this.getGraphPanel(),0,1,4,1);
		pan_graph.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(pan_graph,new int[] {0},new int[] {0},new double[] {1.0},new double[] {1.0});
	}

	private void updateLimni(){
		controller.updateLimnigraph(this);
		controller.fillLimniPanel(this.id.getText().trim(), this);
	}

	private void importLimni(){
		// browse and fill textfield
		Custom_FileChooser chooser = new Custom_FileChooser(config.getDefaultDir());
		if(chooser.getFilepath()!=Constants.S_EMPTY){
			file.setText(chooser.getFilepath());
			updateLimni();
			// import limni
			controller.importLimnigraph(this);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(butt_apply)){updateLimni();}		
		else if(e.getSource().equals(butt_browse)){importLimni();}		
		else if(e.getSource().equals(butt_kickout)){controller.kickPlot_limni(this);}		
		else if(e.getSource().equals(butt_ylog)){controller.fillLimniPanel(this.id.getText().trim(), this);}		
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
	public GridBag_Text_Titled getFile() {
		return file;
	}
	public void setFile(GridBag_Text_Titled file) {
		this.file = file;
	}
	public GridBag_TextField_Titled getDescription() {
		return description;
	}
	public void setDescription(GridBag_TextField_Titled description) {
		this.description = description;
	}
	public GridBag_Button getButt_browse() {
		return butt_browse;
	}
	public void setButt_browse(GridBag_Button butt_browse) {
		this.butt_browse = butt_browse;
	}
	public GridBag_Button getButt_apply() {
		return butt_apply;
	}
	public void setButt_apply(GridBag_Button butt_apply) {
		this.butt_apply = butt_apply;
	}
	public JTable getTable() {
		return table;
	}
	public void setTable(JTable table) {
		this.table = table;
	}
	public Custom_TableModel getTM() {
		return TM;
	}
	public void setTM(Custom_TableModel tM) {
		TM = tM;
	}
	public GridBag_ToggleButton getButt_ylog() {
		return butt_ylog;
	}
	public void setButt_ylog(GridBag_ToggleButton butt_ylog) {
		this.butt_ylog = butt_ylog;
	}
	public GridBag_Button getButt_kickout() {
		return butt_kickout;
	}
	public void setButt_kickout(GridBag_Button butt_kickout) {
		this.butt_kickout = butt_kickout;
	}
	public GridBag_Button getButt_legend() {
		return butt_legend;
	}
	public void setButt_legend(GridBag_Button butt_legend) {
		this.butt_legend = butt_legend;
	}
	public GridBag_Button getButt_moreplots() {
		return butt_moreplots;
	}
	public void setButt_moreplots(GridBag_Button butt_moreplots) {
		this.butt_moreplots = butt_moreplots;
	}
	public GridBag_Panel getPan_graph() {
		return pan_graph;
	}
	public void setPan_graph(GridBag_Panel pan_graph) {
		this.pan_graph = pan_graph;
	}

}

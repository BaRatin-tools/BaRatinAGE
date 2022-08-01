package vue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JPanel;

import Utils.Config;
import Utils.Defaults;
import Utils.Dico;
import commons.Constants;
import commons.GridBag_Button;
import commons.GridBag_ComboBox_Titled;
import commons.GridBag_Layout;
import commons.GridBag_TextField_Titled;
import controleur.Control;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel implements ActionListener {

	private ConfigHydrauPanel hydrau;
	private GridBag_TextField_Titled description;
	private GridBag_ComboBox_Titled type;
	private GridBag_TextField_Titled k;
	private GridBag_TextField_Titled kpom;
	private GridBag_TextField_Titled a;
	private GridBag_TextField_Titled apom;
	private GridBag_TextField_Titled c;
	private GridBag_TextField_Titled cpom;
	private GridBag_Button butt_priorHelp;
	private boolean manualSelection=true; // ugly... used to know whether the "type" combobox has been changed by hand or programmatically.
	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	private Control controller=Control.getInstance();

	public ControlPanel(ConfigHydrauPanel hydrau){
		super();
		this.hydrau=hydrau;
		this.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(this, new int[] {0,0,0,0,0,0}, new int[] {0,0}, new double[] {0.0,0.0,0.0,0.0,0.0,1.0}, new double[] {0.5,0.5});
		description=new GridBag_TextField_Titled(this,Constants.S_EMPTY,dico.entry("Description"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,0,2,1,dico.entry("Description"));
		String[] typelist=Combo_ControlType.getStringList();
		type=new GridBag_ComboBox_Titled(this,dico.entry(typelist),dico.entry("Type"),
				config.getFontTxt(),config.getFontLbl(),Defaults.txtColor,Defaults.lblColor,
				0,1,1,1,true,false,dico.entry("Type"));
		type.setSelectedIndex(Combo_ControlType.UNKNOWN_INDX);
		type.addItemListener(listener_type);
		butt_priorHelp=new GridBag_Button(this,this,"butt_priorHelp",dico.entry("PriorAssistant"),
				Defaults.iconPriorAssistant,1,1,1,1,false,true,dico.entry("OpenPriorAssistant"));
		butt_priorHelp.setEnabled(false);
		k=new GridBag_TextField_Titled(this,Constants.S_EMPTY,dico.entry("kpar"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,2,1,1,dico.entry("kpar"));
		kpom=new GridBag_TextField_Titled(this,Constants.S_EMPTY,dico.entry("+/-"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,1,2,1,1,dico.entry("+/-_long"));
		a=new GridBag_TextField_Titled(this,Constants.S_EMPTY,dico.entry("apar"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,3,1,1,dico.entry("apar"));
		apom=new GridBag_TextField_Titled(this,Constants.S_EMPTY,dico.entry("+/-"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,1,3,1,1,dico.entry("+/-_long"));
		c=new GridBag_TextField_Titled(this,Constants.S_EMPTY,dico.entry("cpar"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,4,1,1,dico.entry("cpar"));
		cpom=new GridBag_TextField_Titled(this,Constants.S_EMPTY,dico.entry("+/-"),config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,1,4,1,1,dico.entry("+/-_long"));

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(butt_priorHelp)){
			this.hydrau.updateHydrau();
			new Frame_PriorAssistant(MainFrame.getInstance(),type.getSelectedIndex(),this,this.hydrau); 
		};
	}
	
	ItemListener listener_type = new ItemListener() {
		public void itemStateChanged(ItemEvent itemEvent) {
			if (type.getSelectedIndex()==Combo_ControlType.UNKNOWN_INDX){
				butt_priorHelp.setEnabled(false);
			}
			else{
				butt_priorHelp.setEnabled(true);
				}
			if(manualSelection){reset();}
		}
	};

	private void reset(){
		hydrau.updateHydrau();
		controller.resetControl(this);
	}
	
	public GridBag_TextField_Titled getDescription() {
		return description;
	}

	public void setDescription(GridBag_TextField_Titled description) {
		this.description = description;
	}

	public GridBag_ComboBox_Titled getType() {
		return type;
	}

	public void setType(GridBag_ComboBox_Titled type) {
		this.type = type;
	}

	public GridBag_TextField_Titled getK() {
		return k;
	}

	public void setK(GridBag_TextField_Titled k) {
		this.k = k;
	}

	public GridBag_TextField_Titled getKpom() {
		return kpom;
	}

	public void setKpom(GridBag_TextField_Titled kpom) {
		this.kpom = kpom;
	}

	public GridBag_TextField_Titled getA() {
		return a;
	}

	public void setA(GridBag_TextField_Titled a) {
		this.a = a;
	}

	public GridBag_TextField_Titled getApom() {
		return apom;
	}

	public void setApom(GridBag_TextField_Titled apom) {
		this.apom = apom;
	}

	public GridBag_TextField_Titled getC() {
		return c;
	}

	public void setC(GridBag_TextField_Titled c) {
		this.c = c;
	}

	public GridBag_TextField_Titled getCpom() {
		return cpom;
	}

	public void setCpom(GridBag_TextField_Titled cpom) {
		this.cpom = cpom;
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

	public ConfigHydrauPanel getHydrau() {
		return hydrau;
	}

	public void setHydrau(ConfigHydrauPanel hydrau) {
		this.hydrau = hydrau;
	}

	/**
	 * @return the manualSelection
	 */
	public boolean isManualSelection() {
		return manualSelection;
	}

	/**
	 * @param manualSelection the manualSelection to set
	 */
	public void setManualSelection(boolean manualSelection) {
		this.manualSelection = manualSelection;
	}

}

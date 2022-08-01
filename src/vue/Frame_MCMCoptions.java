package vue;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JPanel;

import Utils.Config;
import Utils.Defaults;
import Utils.Dico;
import moteur.MCMCoptions;
import commons.Constants;
import commons.GridBag_Button;
import commons.GridBag_Layout;
import commons.GridBag_TextField_Titled;
import controleur.Control;

/**
 * A dialog box to change MCMC options
 * @author Ben Renard, Irstea Lyon
 *
 */
@SuppressWarnings("serial")
public class Frame_MCMCoptions extends JDialog implements ActionListener{

	private MCMCoptions mcmc=null;
	private GridBag_Button butt_apply;
	private GridBag_Button butt_cancel;
	private GridBag_TextField_Titled nAdapt;
	private GridBag_TextField_Titled nCycles;
	private GridBag_TextField_Titled burn;
	private GridBag_TextField_Titled nSlim;
	private GridBag_TextField_Titled minMR;
	private GridBag_TextField_Titled maxMR;
	private GridBag_TextField_Titled downMult;
	private GridBag_TextField_Titled upMult;
	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());
	private Control controller=Control.getInstance();

	public Frame_MCMCoptions(Frame parent,MCMCoptions mcmc){
		super(parent,true);
		this.setTitle(dico.entry("MCMCoptions"));
		this.setMinimumSize(Defaults.mcmcOptionsSize);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setLocationRelativeTo(parent);
		JPanel pan =new JPanel();
		GridBag_Layout.SetGrid(pan, new int[] {0,0,0,0,0}, new int[] {0,0}, new double[] {0.0,0.0,0.0,0.0,0.0},new double[] {1.0,1.0});
		pan.setBackground(Defaults.bkgColor);
		int k=0;
		nAdapt=new GridBag_TextField_Titled(pan,
				Integer.toString(mcmc.getnAdapt()),dico.entry("nAdapt"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				0,k,1,1,dico.entry("Explanation_nAdapt"));
		nCycles=new GridBag_TextField_Titled(pan,
				Integer.toString(mcmc.getnCycles()),dico.entry("nCycles"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				1,k,1,1,dico.entry("Explanation_nCycles"));
		k=k+1;
		minMR=new GridBag_TextField_Titled(pan,
				Double.toString(mcmc.getMinMR()),dico.entry("minMR"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				0,k,1,1,dico.entry("Explanation_minMR"));
		maxMR=new GridBag_TextField_Titled(pan,
				Double.toString(mcmc.getMaxMR()),dico.entry("maxMR"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				1,k,1,1,dico.entry("Explanation_maxMR"));
		k=k+1;
		downMult=new GridBag_TextField_Titled(pan,
				Double.toString(mcmc.getDownMult()),dico.entry("downMult"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				0,k,1,1,dico.entry("Explanation_downMult"));
		upMult=new GridBag_TextField_Titled(pan,
				Double.toString(mcmc.getUpMult()),dico.entry("upMult"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				1,k,1,1,dico.entry("Explanation_upMult"));
		k=k+1;
		burn=new GridBag_TextField_Titled(pan,
				Double.toString(mcmc.getBurn()),dico.entry("burn"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				0,k,1,1,dico.entry("Explanation_burn"));
		nSlim=new GridBag_TextField_Titled(pan,
				Integer.toString(mcmc.getnSlim()),dico.entry("nSlim"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,
				1,k,1,1,dico.entry("Explanation_nSlim"));
		k=k+1;
		butt_apply=new GridBag_Button(pan,this,"foo",dico.entry("Apply"),Defaults.iconApply,0,k,1,1,false,false,"");
		butt_cancel=new GridBag_Button(pan,this,"foo",dico.entry("Cancel"),Defaults.iconCancel,1,k,1,1,false,false,"");
		this.setContentPane(pan);
		this.setVisible(true);		
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource().equals(butt_cancel)){
			this.dispose();
		}
		if (ae.getSource().equals(butt_apply)){
			int nAdapt,nCycles,nSlim;
			double minMR,maxMR,downMult,upMult,burn; 
			nAdapt=controller.safeParse_i(this.nAdapt.getText());
			if(nAdapt==Constants.I_MISSING | nAdapt==Constants.I_UNFEAS ) {return;}
			nCycles=controller.safeParse_i(this.nCycles.getText());
			if(nCycles==Constants.I_MISSING | nCycles==Constants.I_UNFEAS ) {return;}
			nSlim=controller.safeParse_i(this.nSlim.getText());
			if(nSlim==Constants.I_MISSING | nSlim==Constants.I_UNFEAS ) {return;}
			minMR=controller.safeParse_d(this.minMR.getText());
			if(minMR==Constants.D_MISSING | minMR==Constants.D_UNFEAS ) {return;}
			maxMR=controller.safeParse_d(this.maxMR.getText());
			if(maxMR==Constants.D_MISSING | maxMR==Constants.D_UNFEAS ) {return;}
			downMult=controller.safeParse_d(this.downMult.getText());
			if(downMult==Constants.D_MISSING | downMult==Constants.D_UNFEAS ) {return;}
			upMult=controller.safeParse_d(this.upMult.getText());
			if(upMult==Constants.D_MISSING | upMult==Constants.D_UNFEAS ) {return;}
			burn=controller.safeParse_d(this.burn.getText());
			if(burn==Constants.D_MISSING | burn==Constants.D_UNFEAS ) {return;}
			this.setMcmc(new MCMCoptions(nAdapt,nCycles,burn,nSlim,minMR,maxMR,downMult,upMult));
			this.dispose();
		}		
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public MCMCoptions getMcmc() {
		return mcmc;
	}

	public void setMcmc(MCMCoptions mcmc) {
		this.mcmc = mcmc;
	}

	public GridBag_TextField_Titled getnAdapt() {
		return nAdapt;
	}

	public void setnAdapt(GridBag_TextField_Titled nAdapt) {
		this.nAdapt = nAdapt;
	}

	public GridBag_TextField_Titled getnCycles() {
		return nCycles;
	}

	public void setnCycles(GridBag_TextField_Titled nCycles) {
		this.nCycles = nCycles;
	}

	public GridBag_TextField_Titled getBurn() {
		return burn;
	}

	public void setBurn(GridBag_TextField_Titled burn) {
		this.burn = burn;
	}

	public GridBag_TextField_Titled getnSlim() {
		return nSlim;
	}

	public void setnSlim(GridBag_TextField_Titled nSlim) {
		this.nSlim = nSlim;
	}

	public GridBag_TextField_Titled getMinMR() {
		return minMR;
	}

	public void setMinMR(GridBag_TextField_Titled minMR) {
		this.minMR = minMR;
	}

	public GridBag_TextField_Titled getMaxMR() {
		return maxMR;
	}

	public void setMaxMR(GridBag_TextField_Titled maxMR) {
		this.maxMR = maxMR;
	}

	public GridBag_TextField_Titled getDownMult() {
		return downMult;
	}

	public void setDownMult(GridBag_TextField_Titled downMult) {
		this.downMult = downMult;
	}

	public GridBag_TextField_Titled getUpMult() {
		return upMult;
	}

	public void setUpMult(GridBag_TextField_Titled upMult) {
		this.upMult = upMult;
	}
}

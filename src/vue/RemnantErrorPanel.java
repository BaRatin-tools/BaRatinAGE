package vue;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingConstants;

import Utils.Config;
import Utils.Defaults;
import Utils.Dico;
import commons.GridBag_Button;
import commons.GridBag_Label;

/**
 * Panel for handling remnant errors
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class RemnantErrorPanel extends ItemPanel implements ActionListener  {
	
	private GridBag_Label lbl;
	private GridBag_Button butt;
	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());

	public RemnantErrorPanel() {
		super(new int[] {0,0}, new int[] {0}, new double[] {1.0,1.0},new double[] {1.0},
				new int[] {0}, new int[] {0}, new double[] {1.0},new double[] {1.0});
		this.setResizeWeight(1.0);
		drawInfoPanel();
	}
	
	private void drawInfoPanel(){
		 lbl=new GridBag_Label(this.getInfoPanel(),dico.entry("RemnantErrorText"),config.getFontTxt(),Defaults.txtColor,SwingConstants.CENTER,0,0,1,1,true,true);
		 butt=new GridBag_Button(this.getInfoPanel(),this,"help","",Defaults.iconHelp,0,1,1,1,false,false,dico.entry("Help"));
		 lbl.setVerticalAlignment(SwingConstants.BOTTOM);
		 butt.setVerticalAlignment(SwingConstants.TOP);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource()==butt){
			String help=new File(config.getHelpDir(),"RemnantError.html").getAbsolutePath();
			File htmlFile = new File(help);
			try {
				Desktop.getDesktop().browse(htmlFile.toURI());
			} catch (IOException ex) {
				//TODO
				ex.printStackTrace();
			}
		}		
	}	


	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the lbl
	 */
	public GridBag_Label getLbl() {
		return lbl;
	}

	/**
	 * @param lbl the lbl to set
	 */
	public void setLbl(GridBag_Label lbl) {
		this.lbl = lbl;
	}

	/**
	 * @return the butt
	 */
	public GridBag_Button getButt() {
		return butt;
	}

	/**
	 * @param butt the butt to set
	 */
	public void setButt(GridBag_Button butt) {
		this.butt = butt;
	}

}

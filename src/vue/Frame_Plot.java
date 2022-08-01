package vue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import Utils.Defaults;
import commons.GridBag_Layout;

@SuppressWarnings("serial")
public class Frame_Plot extends JFrame{
	
	private JPanel pan;
	
	public Frame_Plot(){
		super();
		this.setTitle("");
		this.setVisible(true);
		ImageIcon icon=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconKickPlot));
		this.setIconImage(icon.getImage());
		this.setSize(Defaults.kickPlotSize);
		pan=new JPanel();
		GridBag_Layout.SetGrid(pan,new int[] {0}, new int[] {0}, new double[] {1.0}, new double[] {1.0});
		pan.setBackground(Defaults.bkgColor);
		this.add(pan);
	}

	/**
	 * @return the pan
	 */
	public JPanel getPan() {
		return pan;
	}

	/**
	 * @param pan the pan to set
	 */
	public void setPan(JPanel pan) {
		this.pan = pan;
	}

}

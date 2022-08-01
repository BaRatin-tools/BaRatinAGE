package vue;

import java.awt.GridBagConstraints;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import commons.GridBag_Layout;

import Utils.Config;
import Utils.Defaults;
import Utils.Dico;


@SuppressWarnings("serial")
public class Frame_About extends JFrame{
	
	// locals
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());


	public Frame_About(){
		super();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		ImageIcon icon=new ImageIcon(getClass().getClassLoader().getResource(Defaults.iconGUI));
		this.setIconImage(icon.getImage());
		this.setTitle(Defaults.appName);
		this.setSize(Defaults.aboutSize);
		JPanel panel = new JPanel();panel.setBackground(Defaults.bkgColor);
		this.getContentPane().add(panel);
		GridBag_Layout.SetGrid(panel,new int[] {0}, new int[] {0},new double[] {1.0},new double[] {1.0});
		// text
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;gbc.gridy = 0;
		gbc.gridwidth=1;gbc.gridheight=1;
		gbc.fill = GridBagConstraints.BOTH;
		JTextArea txt=new JTextArea();
		txt.setText(
				System.getProperty("line.separator")+
				dico.entry("AboutText_1")+
				System.getProperty("line.separator")+
				dico.entry("AboutText_2")+
				System.getProperty("line.separator")+
				System.getProperty("line.separator")+
				dico.entry("AboutText_3")+
				System.getProperty("line.separator")+
				dico.entry("AboutText_4")
				);
		txt.setFont(config.getFontBigTxt());
		txt.setEditable(false);
		txt.setAlignmentY(CENTER_ALIGNMENT);
		panel.add(txt,gbc);
		this.setVisible(true);
	}
}

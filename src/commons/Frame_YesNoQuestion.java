package commons;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import Utils.Config;

@SuppressWarnings("serial")
public class Frame_YesNoQuestion extends JOptionPane {

	private Config config=Config.getInstance();
	
	public Frame_YesNoQuestion(){
		super();
//		this.setFont(config.getFontTxt());
	}
	
	public int ask(Component parent, String question, String title,String icon,String yesTxt, String noTxt){
		JLabel label = new JLabel(question);
		label.setFont(config.getFontTxt());
		return JOptionPane.showOptionDialog(parent,label,title,JOptionPane.YES_NO_OPTION,JOptionPane.ERROR_MESSAGE,
				new ImageIcon(getClass().getClassLoader().getResource(icon)),new String[] {yesTxt,noTxt},yesTxt);		
	}

}

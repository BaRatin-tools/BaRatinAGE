package commons;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class Frame_YesNoQuestion extends JOptionPane {

	public Frame_YesNoQuestion(){
		super();
	}
	
	public int ask(Component parent, String question, String title,String icon,String yesTxt, String noTxt){
		return JOptionPane.showOptionDialog(parent,question,title,JOptionPane.YES_NO_OPTION,JOptionPane.ERROR_MESSAGE,
				new ImageIcon(getClass().getClassLoader().getResource(icon)),new String[] {yesTxt,noTxt},yesTxt);		
	}

}

package vue;

import java.awt.Component;

import javax.swing.JOptionPane;

import Utils.Config;
import Utils.Dico;

public class InformationPanel {

	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());

	public InformationPanel(Component parent, String text){
		super();
		JOptionPane.showMessageDialog(parent,text,dico.entry("Information"),JOptionPane.INFORMATION_MESSAGE);
	}

}

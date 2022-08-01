package vue;

import java.awt.Component;

import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class AllDonePanel extends JOptionPane {

	public AllDonePanel(Component parent, String text){
		super();
		JOptionPane.showMessageDialog(parent,text,"",JOptionPane.INFORMATION_MESSAGE);
	}

}

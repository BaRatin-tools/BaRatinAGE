package vue;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import Utils.Config;
import Utils.Dico;


@SuppressWarnings("serial")
public class ExceptionPanel extends JOptionPane {
	
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());

	public ExceptionPanel(Component parent, String text){
		super();
		JOptionPane.showMessageDialog(parent,text,dico.entry("Error"),JOptionPane.ERROR_MESSAGE);
	}

	public ExceptionPanel() {
		// TODO Auto-generated constructor stub
	}

	public ExceptionPanel(Object arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public ExceptionPanel(Object arg0, int arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public ExceptionPanel(Object arg0, int arg1, int arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public ExceptionPanel(Object arg0, int arg1, int arg2, Icon arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

	public ExceptionPanel(Object arg0, int arg1, int arg2, Icon arg3,
			Object[] arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
		// TODO Auto-generated constructor stub
	}

	public ExceptionPanel(Object arg0, int arg1, int arg2, Icon arg3,
			Object[] arg4, Object arg5) {
		super(arg0, arg1, arg2, arg3, arg4, arg5);
		// TODO Auto-generated constructor stub
	}

}

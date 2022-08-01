package commons;

import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

/**
 * Customized menu item
 * @author Ben Renard, Irstea Lyon
 *
 */
@SuppressWarnings("serial")
public class Custom_MenuItem extends JMenuItem{

	private String ID;

	/**
	 * Full Constructor
	 * @param ID the id of the menu item
	 * @param txt the text of the menu item
	 * @param font the font of the menu item
	 * @param listener the listener of the menu item
	 */
	public Custom_MenuItem(String ID,String txt,Font font,ActionListener listener) {
		super(txt);
		this.setID(ID);
		this.addActionListener(listener);
		this.setFont(font);
	}
	
	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the ID
	 */
	public String getID() {
		return ID;
	}

	/**
	 * @param iD the id to set
	 */
	public void setID(String iD) {
		ID = iD;
	}

}

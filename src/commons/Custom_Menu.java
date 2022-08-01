package commons;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenu;

/**
 * Customized Menu, containing a list of customized menu items
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class Custom_Menu extends JMenu{

	private ArrayList<Custom_MenuItem> item= new ArrayList<Custom_MenuItem>();

	/**
	 * Full constructor
	 * @param text text displayed for the menu
	 * @param font font of the text
	 */
	public Custom_Menu(String text,Font font){
		super(text);
		this.setFont(font);
	}
	
	/**
	 * Default constructor
	 */
	public Custom_Menu(){
		super(Constants.S_BLANK);
	}

	/**
	 * Add (custom) menu items
	 * @param ID list of ids for each item
	 * @param txt list of texts for each item
	 * @param font common font for all items
	 * @param listener common listener for all menu items
	 */
	public void AddItem(String[] ID,String[] txt,Font font,ActionListener listener) {
		int n=ID.length;
		for(int i=0;i<n;i++){
			Custom_MenuItem it=new Custom_MenuItem(ID[i],txt[i],font,listener);
			this.getItem().add(it);
			this.add(it);
		}
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the list of menu items
	 */
	public ArrayList<Custom_MenuItem> getItem() {
		return item;
	}

	/**
	 * @param i the position of the desired menu item (starting from zero)
	 * @return the ith menu item
	 */
	public Custom_MenuItem getItem(int i) {
		return item.get(i);
	}

	/**
	 * @param item the list of menu items to set
	 */
	public void setItem(ArrayList<Custom_MenuItem> item) {
		this.item = item;
	}
}

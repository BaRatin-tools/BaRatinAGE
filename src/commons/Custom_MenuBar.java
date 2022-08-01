package commons;

import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

/**
 * Customized menu bar, containing a list of customized menus
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class Custom_MenuBar extends JMenuBar{

	private ArrayList<Custom_Menu> menu= new ArrayList<Custom_Menu>();
	
	/**
	 * Full constructor
	 * @param container the container where the menu bar belongs
	 * @param text list of menus' text
	 * @param font common font for all menus
	 * @param addSep add a vertical bar to separate items?
	 */
	public Custom_MenuBar(JFrame container,String[] text,Font font,String sep){
		super();
		// create the menubar
		this.Create(text,font,sep);
		// Add the menubar in container
		this.AddToContainer(container);
	}

	/**
	 * create the menu bar
	 * @param text list of menus' text
	 * @param font common font for all menus
	 */
	private void Create(String[] text,Font font,String sep){
		int n=text.length;
		for(int i=0;i<n;i++){
			// create menu
			Custom_Menu men=new Custom_Menu(text[i]+sep,font);
			this.add(men);
			this.menu.add(men);
		}
	}
	
	/**
	 * Add the menu bar to the container
	 * @param container the container where the menu bar belongs
	 */
	private void AddToContainer(JFrame container){
		container.setJMenuBar(this);
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the list of menus
	 */
	public ArrayList<Custom_Menu> getMenu() {
		return menu;
	}

	/**
	 * @param i the position of the menu to retrieve
	 * @return the ith menu
	 */
	public Custom_Menu getMenu(int i) {
		return menu.get(i);
	}

	/**
	 * @param menu the list of custom menus to set
	 */
	public void setMenu(ArrayList<Custom_Menu> menu) {
		this.menu = menu;
	}
}

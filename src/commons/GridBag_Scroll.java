package commons;

import java.awt.Container;
import java.awt.GridBagConstraints;

import javax.swing.JScrollPane;

/**
 * A derived scrollpane type
 * <p> Assumes that the component lies in a container (typically a Jpanel) whose layout is set to GridBagLayout.
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class GridBag_Scroll extends JScrollPane {

	/**
	 * Full constructor
	 * @param container The container where the component is added
	 * @param x x positioning of the component in the GridBagLayout of the container
	 * @param y y positioning of the component in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the component lies
	 * @param ny Number of vertical cells on which the component lies
	 */

	public GridBag_Scroll(Container container,
			int x, 
			int y, 
			int nx, 
			int ny){
		super();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;gbc.gridy = y;
		gbc.gridwidth=nx;gbc.gridheight=ny;
		gbc.fill = GridBagConstraints.BOTH;
		container.add(this,gbc);
	}
}

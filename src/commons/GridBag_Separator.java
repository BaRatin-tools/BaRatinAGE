package commons;

import java.awt.GridBagConstraints;

import javax.swing.JComponent;
import javax.swing.JSeparator;

/**
 * A derived separator type
 * <p> Assumes that the component lies in a container (typically a Jpanel) whose layout is set to GridBagLayout.
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class GridBag_Separator extends JSeparator{
	
	/**
	 * @param container The container where the component is added
	 * @param x x positioning of the component in the GridBagLayout of the container
	 * @param y y positioning of the component in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the component lies
	 * @param xExpand Expand label horizontally on all its cells?
	 */
	public GridBag_Separator(
			JComponent container,
			int x, 
			int y, 
			int nx, 
			boolean xExpand) {
		super();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = nx;
		gbc.gridx = x;gbc.gridy = y;
		if(xExpand){gbc.fill = GridBagConstraints.HORIZONTAL;}
		container.add(this, gbc);
	}
}

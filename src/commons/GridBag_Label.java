package commons;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;

/**
 * A derived Jlabel type
 * <p> Assumes that the component lies in a container (typically a Jpanel) whose layout is set to GridBagLayout.
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class GridBag_Label extends JLabel {
	
	/**
	 * @param container The container where the label is added
	 * @param text Text of the label
	 * @param font font of the label
	 * @param color color of the label
	 * @param alignment alignment of the label
	 * @param x x positioning of the label in the GridBagLayout of the container
	 * @param y y positioning of the label in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the label lies
	 * @param ny Number of vertical cells on which the label lies
	 * @param xExpand Expand label horizontally on all its cells?
	 * @param yExpand Expand label vertically on all its cells?
	 */
	public GridBag_Label(
			Container container,
			String text,
			Font font,
			Color color,
			int alignment,
			int x, 
			int y, 
			int nx, 
			int ny,
			boolean xExpand,
			boolean yExpand) {
		super();
		this.setText(text);
		this.setFont(font);
		this.setForeground(color);
		this.setHorizontalAlignment(alignment);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;gbc.gridy = y;
		gbc.gridwidth=nx;gbc.gridheight=ny;
		if(xExpand && yExpand) {gbc.fill = GridBagConstraints.BOTH;}
		else if(xExpand){gbc.fill = GridBagConstraints.HORIZONTAL;}
		else if(yExpand){gbc.fill = GridBagConstraints.VERTICAL;}
		container.add(this, gbc);
	}
}

package commons;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 * A derived combobox type
 * <p> Assumes that the component lies in a container (typically a Jpanel) whose layout is set to GridBagLayout.
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class GridBag_ComboBox extends JComboBox<String>{

	/**
	 * Full constructor
	 * @param container The container where the component is added
	 * @param item items of the box
	 * @param font font of the items of the box
	 * @param x x positioning of the component in the GridBagLayout of the container
	 * @param y y positioning of the component in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the component lies
	 * @param ny Number of vertical cells on which the component lies
	 * @param xExpand Expand component horizontally on all its cells?
	 * @param yExpand Expand component vertically on all its cells?
	 * @param tip Tip text
	 */
	public GridBag_ComboBox(JComponent container,
			String[] item,
			Font itemFont,
			Color itemColor,
			int x, 
			int y, 
			int nx, 
			int ny,
			boolean xExpand,
			boolean yExpand,
			String tip) {
		super();
		this.setModel(new DefaultComboBoxModel<String>(item));
		this.setFont(itemFont);
		this.setForeground(itemColor);
		this.setSize(0,0);
		this.setAlignmentY(CENTER_ALIGNMENT);
		this.setAlignmentX(CENTER_ALIGNMENT);
		if(tip != ""){this.setToolTipText(tip);}		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;gbc.gridy = y;
		gbc.gridwidth=nx;gbc.gridheight=ny;
		if(xExpand && yExpand) {gbc.fill = GridBagConstraints.BOTH;}
		else if(xExpand){gbc.fill = GridBagConstraints.HORIZONTAL;}
		else if(yExpand){gbc.fill = GridBagConstraints.VERTICAL;}
		container.add(this, gbc);
	}
}

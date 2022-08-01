package commons;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * A derived combobox type, with a title
 * <p> Assumes that the component lies in a container (typically a Jpanel) whose layout is set to GridBagLayout.
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class GridBag_ComboBox_Titled extends GridBag_ComboBox {
	
	/**
	 * Full constructor
	 * @param container The container where the component is added
	 * @param item list of items in the component
	 * @param title the title of the component
	 * @param itemFont the font of the items
	 * @param titleFont the font of the title
	 * @param itemColor the color of the items
	 * @param titleColor the color of the title
	 * @param x x positioning of the component in the GridBagLayout of the container
	 * @param y y positioning of the component in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the component lies
	 * @param ny Number of vertical cells on which the component lies
	 * @param xExpand Expand component horizontally on all its cells?
	 * @param yExpand Expand component vertically on all its cells?
	 * @param tip Tip text
	 */
	public GridBag_ComboBox_Titled(JComponent container,
			String[] item,
			String title,
			Font itemFont,
			Font titleFont,
			Color itemColor,
			Color titleColor,
			int x, 
			int y, 
			int nx, 
			int ny,
			boolean xExpand,
			boolean yExpand,
			String tip){
		super(container,item,itemFont,itemColor,x,y,nx,ny,xExpand,yExpand,tip);
		TitledBorder bord = BorderFactory.createTitledBorder(null, title,
				SwingConstants.CENTER, SwingConstants.LEFT);
		bord.setTitleColor(titleColor);
		bord.setTitleFont(titleFont);
		this.setBorder(bord);
	}

}

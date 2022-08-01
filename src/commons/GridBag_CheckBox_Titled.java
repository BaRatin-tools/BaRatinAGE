package commons;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * A derived checkbox type, with a title.
 * <p> Assumes that the component lies in a container (typically a Jpanel) whose layout is set to GridBagLayout.
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class GridBag_CheckBox_Titled extends GridBag_CheckBox{
	
	private String ID;

	/**
	 * Full constructor
	 * @param container The container where the component is added
	 * @param listener The listener of the component 
	 * @param ID Identifier of the component
	 * @param title the title of the component
	 * @param titleFont the font of the title
	 * @param titleColor the color of the title
	 * @param x x positioning of the component in the GridBagLayout of the container
	 * @param y y positioning of the component in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the component lies
	 * @param ny Number of vertical cells on which the component lies
	 * @param xExpand Expand component horizontally on all its cells?
	 * @param yExpand Expand component vertically on all its cells?
	 * @param tip Tip text
	 */
	public GridBag_CheckBox_Titled(JComponent container,
			ActionListener listener,
			String ID, 
			String title,
			Font titleFont,
			Color titleColor,
			int x, 
			int y, 
			int nx, 
			int ny,
			boolean xExpand,
			boolean yExpand,
			String tip){
		super(container,listener,ID,x,y,nx,ny,xExpand,yExpand,tip);
		TitledBorder bord = BorderFactory.createTitledBorder(null, title,
				SwingConstants.CENTER, SwingConstants.LEFT);
		bord.setTitleColor(titleColor);
		bord.setTitleFont(titleFont);
		this.setBorder(bord);

	}


	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	
}

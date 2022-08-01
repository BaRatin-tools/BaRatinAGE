package commons;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 * A derived checkbox type.
 * <p> Assumes that the component lies in a container (typically a Jpanel) whose layout is set to GridBagLayout.
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class GridBag_CheckBox extends JCheckBox{
	
	private String ID;

	/**
	 * Full constructor
	 * @param container The container where the component is added
	 * @param listener The listener of the component 
	 * @param ID Identifier of the component
	 * @param x x positioning of the component in the GridBagLayout of the container
	 * @param y y positioning of the component in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the component lies
	 * @param ny Number of vertical cells on which the component lies
	 * @param xExpand Expand component horizontally on all its cells?
	 * @param yExpand Expand component vertically on all its cells?
	 * @param tip Tip text
	 */
	public GridBag_CheckBox(JComponent container,
			ActionListener listener,
			String ID, 
			int x, 
			int y, 
			int nx, 
			int ny,
			boolean xExpand,
			boolean yExpand,
			String tip){
		super();
		this.addActionListener(listener);
		this.setID(ID);
		if(tip != ""){this.setToolTipText(tip);}		
		this.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;gbc.gridy = y;
		gbc.gridwidth=nx;gbc.gridheight=ny;
		if(xExpand && yExpand) {gbc.fill = GridBagConstraints.BOTH;}
		else if(xExpand){gbc.fill = GridBagConstraints.HORIZONTAL;}
		else if(yExpand){gbc.fill = GridBagConstraints.VERTICAL;}
		container.add(this, gbc);
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

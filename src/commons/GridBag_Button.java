package commons;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * GridBag_Button, a derived JButton type.
 * <p> Assumes that the component lies in a container (typically a Jpanel) whose layout is set to GridBagLayout.
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class GridBag_Button extends JButton{
	
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
	public GridBag_Button(Container container,
			ActionListener listener,
			String ID, 
			int x, 
			int y, 
			int nx, 
			int ny,
			boolean xExpand,
			boolean yExpand,
			String tip) {
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
		else{gbc.fill = GridBagConstraints.NONE;}
		container.add(this, gbc);
	}
	
	/**
	 * Constructor with a text and an icon 
	 * @param container The container where the Button is added
	 * @param listener The listener of the button 
	 * @param ID Identifier of the button
	 * @param txt text of the button
	 * @param icon icon of the button (path to resource file)
	 * @param x x positioning of the button in the GridBagLayout of the container
	 * @param y y positioning of the button in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the button lies
	 * @param ny Number of vertical cells on which the button lies
	 * @param xExpand Expand button horizontally on all its cells?
	 * @param yExpand Expand button vertically on all its cells?
	 * @param tip Tip text
	 */
	public GridBag_Button(Container container,
			ActionListener listener,
			String ID,
			String txt,
			String icon,
			int x, 
			int y, 
			int nx, 
			int ny,
			boolean xExpand,
			boolean yExpand,
			String tip) {
		this(container,listener,ID,x,y,nx,ny,xExpand,yExpand,tip);
		this.setText(txt);
		if(icon!=null){this.setIcon(new ImageIcon(getClass().getClassLoader().getResource(icon)));}
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

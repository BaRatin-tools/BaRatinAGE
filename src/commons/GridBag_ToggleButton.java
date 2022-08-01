package commons;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * A derived toggle button type.
 * <p> Assumes that the component lies in a container (typically a Jpanel) whose layout is set to GridBagLayout.
 * @author Ben Renard, Irstea Lyon 
 */
@SuppressWarnings("serial")
public class GridBag_ToggleButton extends JToggleButton implements ItemListener{

	private String ID;
	private String txt_on;
	private String txt_off;

	/**
	 * Full constructor
	 * @param container The container where the component is added
	 * @param listener a listener
	 * @param ID id of the component
	 * @param txt_on text when the button is selected
	 * @param txt_off text when the button is not selected
	 * @param isOn is button selected at instantiation?
	 * @param icon icon in the button
	 * @param x x positioning of the component in the GridBagLayout of the container
	 * @param y y positioning of the component in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the component lies
	 * @param ny Number of vertical cells on which the component lies
	 * @param tip tip message
	 */
	public GridBag_ToggleButton(Container container,
			ActionListener listener,
			String ID,
			String txt_on,
			String txt_off,
			boolean isOn,
			String icon,
			int x, 
			int y, 
			int nx, 
			int ny,
			boolean xExpand,
			boolean yExpand,
			String tip){
		super();
		this.txt_on=txt_on;
		this.txt_off=txt_off;
		this.addActionListener(listener);
		this.addItemListener(this);
		this.setID(ID);
		if(tip != ""){this.setToolTipText(tip);}
		if(isOn){this.setText(txt_on);} else {this.setText(txt_off);} ;
		this.setSelected(isOn);
		this.setIcon(new ImageIcon(getClass().getClassLoader().getResource(icon)));
		this.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;gbc.gridy = y;
		gbc.gridwidth=nx;gbc.gridheight=ny;
		if(xExpand && yExpand) {gbc.fill = GridBagConstraints.BOTH;}
		else if(xExpand){gbc.fill = GridBagConstraints.HORIZONTAL;}
		else if(yExpand){gbc.fill = GridBagConstraints.VERTICAL;}
		container.add(this, gbc);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getStateChange() == ItemEvent.SELECTED){
			this.setText(this.txt_on);
		} 
		else{
			this.setText(this.txt_off);
		}		
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the iD
	 */
	public String getID() {
		return ID;
	}

	/**
	 * @param iD the iD to set
	 */
	public void setID(String iD) {
		ID = iD;
	}

	public String getTxt_on() {
		return txt_on;
	}

	public void setTxt_on(String txt_on) {
		this.txt_on = txt_on;
	}

	public String getTxt_off() {
		return txt_off;
	}

	public void setTxt_off(String txt_off) {
		this.txt_off = txt_off;
	}

}

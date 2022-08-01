package commons;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * MyText_Titled, a derived JTextField type.
 * <p> The editable text box has a title on the border.
 * <p> Assumes that the text box lies in a container (typically a Jpanel) whose layout is set to GridBagLayout.
 * @author Ben Renard, Irstea Lyon 
 */

// TODO: overload constructor (simplify)

@SuppressWarnings("serial")
public class GridBag_TextField_Titled extends JTextField{
	
	/**
	 * @param container The container where the component is added
	 * @param text Text in the component
	 * @param title title of the component
	 * @param textFont Font of the text
	 * @param titleFont Font of the title
	 * @param textColor Color of the text 
	 * @param titleColor Color of the title
	 * @param x x positioning of the component in the GridBagLayout of the container
	 * @param y y positioning of the component in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the component lies
	 * @param ny Number of vertical cells on which the component lies
	 * @param tip tip message
	 */
	public GridBag_TextField_Titled(
			JComponent container,
			String text,
			String title,
			Font textFont,
			Font titleFont,
			Color textColor,
			Color titleColor,
			int x, 
			int y, 
			int nx,
			int ny,
			String tip) {
		super();
		// set the textfield
		this.setFont(textFont);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setText(text);
		this.setForeground(textColor);
		this.setToolTipText(tip);
		this.setBorder(BorderFactory.createTitledBorder(null, title, CENTER, LEFT,titleFont,titleColor));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;gbc.gridy = y;
		gbc.gridwidth=nx;gbc.gridheight=ny;
		gbc.fill = GridBagConstraints.BOTH;
		container.add(this, gbc);
	}

}

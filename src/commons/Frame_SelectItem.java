package commons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * A dialog box to ask the user to select an item in a list
 * @author Sylvain Vigneau & Ben Renard, Irstea Lyon
 *
 */
@SuppressWarnings("serial")
public class Frame_SelectItem extends JDialog implements ActionListener{  

	private String name=Constants.S_EMPTY;
	private int indx=-1;
	private GridBag_ComboBox_Titled txt_name;
	private GridBag_Button butt_apply;
	private GridBag_Button butt_cancel;

	/**
	 * Full Constructor
	 * @param parent parent frame
	 * @param itemList the list of items
	 * @param title title
	 * @param decorated is decorated?
	 * @param labelName label above the typing area
	 * @param apply apply string
	 * @param cancel cancel string
	 * @param applyIcon apply icon
	 * @param cancelIcon cancel icon
	 * @param textFont font of the typed text
	 * @param labelFont font of the label
	 * @param bkgColor background color
	 * @param textColor color of the typed text
	 * @param labelColor color of the label
	 * @param dim dimension of the dialog
	 * @param xPos x positioning of the dialog
	 * @param yPos y positioning of the dialog
	 * @param tip tips
	 */
	public Frame_SelectItem(Frame parent,
			String title, //the frame's title
			String[] itemList,
			Boolean decorated, 
			String labelName, String apply, String cancel,  //all the strings of the frame to allow a traduction 
			String applyIcon,String cancelIcon,
			Font textFont,Font labelFont,
			Color bkgColor,Color textColor,Color labelColor,
			Dimension dim,
			Integer xPos, Integer yPos,
			String tip){
		super(parent,true);
		this.setTitle(title);
		this.setMinimumSize(dim);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setUndecorated(!decorated);
		if(xPos == null || yPos == null){  //set the location to the center of the component
			this.setLocationRelativeTo(parent);
		}
		else{  //set the location on a specific place, generally use to place the frame where the user have clicked
			Integer posX = (int)parent.getLocationOnScreen().getX() + xPos;
			Integer posY = (int)parent.getLocationOnScreen().getY() + yPos;
			this.setLocation(posX, posY);
		}

		JPanel pan =new JPanel();
		pan.setBackground(bkgColor);
		GridBag_Layout.SetGrid(pan, new int[] {0,0}, new int[] {0,0}, new double[] {1.,1.}, new double[] {1.,1.});
		txt_name=new GridBag_ComboBox_Titled(pan,itemList,labelName,textFont,labelFont,
				textColor,labelColor,0,0,2,1,true,false,tip);
		butt_apply=new GridBag_Button(pan,this,"foo",apply,applyIcon,0,1,1,1,false,false,"");
		butt_cancel=new GridBag_Button(pan,this,"foo",cancel,cancelIcon,1,1,1,1,false,false,"");
		this.getRootPane().setDefaultButton(butt_apply);
		this.setContentPane(pan);
		this.setVisible(true);
		//TODO: default constructor with less parameters
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource().equals(butt_cancel)){
			this.dispose();
		}
		if (ae.getSource().equals(butt_apply)){
			this.setName((String)this.txt_name.getSelectedItem());
			this.setIndx(this.txt_name.getSelectedIndex());
			this.dispose();
		}
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndx() {
		return indx;
	}

	public void setIndx(int indx) {
		this.indx = indx;
	}

}

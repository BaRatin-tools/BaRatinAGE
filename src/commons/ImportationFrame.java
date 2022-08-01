package commons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;


/**************************************************************************************************************************************************
 * To be able to use the file and name provided by user, you could use the code below
 * 
 * file and name are fields of the class who contains this part of code 
  
	final ImportationFrame impFrame = new ImportationFrame(TONS OF PARAMS)

	impFrame.addWindowListener(new WindowAdapter(){
		public void windowClosed(WindowEvent windowEvent){
			file = impFrame.getFile();
			name = impFrame.getAssociatedName();
		} 
	});
	this.doWhatYouWant();
**************************************************************************************************************************************************/

/**
 * A frame to ask the user for a file and the associated name
 * @author Sylvain Vigneau, Irstea Lyon
 */
@SuppressWarnings("serial")
public class ImportationFrame extends JFrame implements ActionListener{  

	private JFileChooser chooser;
	private FileNameExtensionFilter filter;

	private String file;
	private String associatedName;

	private JButton buttonBrowse;
	private JButton buttonValidate;
	private JButton buttonCancel;

	private JLabel nameLabel;
	private JLabel fileLabel;

	private JTextField fileTextField;
	private JTextField nameTextField;

	private JPanel panel;

	/**
	 * Full constructor
	 * @param title frame's title
	 * @param decorated is frame decorated?
	 * @param description description for the file chooser
	 * @param extensions filter list for the file chooser
	 * @param labelFile label for the "file" text box
	 * @param labelName label for the "name" text box
	 * @param browse label for the "browse" button
	 * @param validate label for the "validate" button
	 * @param cancel label for the "cancel" button
	 * @param xPos x-position of the frame
	 * @param yPos y-position of the frame
	 * @param relative position relavtive to which component?
	 */
	public ImportationFrame(String title, //the frame's title
			Boolean decorated, 
			String description, String[] extensions,  //parameters for the FileChooser
			String labelFile, String labelName, String browse, String validate, String cancel,  //all the strings of the frame to allow a traduction 
			Integer xPos, Integer yPos, Component relative  //args to position the frame. Possible null value for xPos and yPos
			) {

		super(title);

		this.setMinimumSize(new Dimension(300,200));
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);  //disable the close option of the frame
		this.setUndecorated(!decorated);

		if(xPos == null || yPos == null){  //set the location to the center of the component
			this.setLocationRelativeTo(relative);
		}
		else{  //set the location on a specific place, generally use to place the frame where the user have clicked
			Integer posX = (int)relative.getLocationOnScreen().getX() + xPos;
			Integer posY = (int)relative.getLocationOnScreen().getY() + yPos;
			this.setLocation(posX, posY);
		}

		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		filter = new FileNameExtensionFilter (description, extensions);
		chooser.setFileFilter(filter);

		panel = new JPanel();
		if (!decorated){
			panel.setBorder(BorderFactory.createLineBorder(Color.black));
		}

		buttonBrowse = new JButton(browse);
		buttonBrowse.addActionListener(this);
		buttonValidate = new JButton(validate);
		buttonValidate.addActionListener(this);
		buttonCancel = new JButton(cancel);
		buttonCancel.addActionListener(this);

		nameLabel = new JLabel(labelName);
		fileLabel = new JLabel(labelFile);

		nameTextField = new JTextField();
		nameTextField.setColumns(10);
		fileTextField = new JTextField();
		fileTextField.setColumns(10);


		panel.add(nameLabel, BorderLayout.LINE_START);
		panel.add(nameTextField, BorderLayout.NORTH);
		panel.add(fileLabel, BorderLayout.LINE_START);
		panel.add(fileTextField, BorderLayout.EAST);
		panel.add(buttonBrowse, BorderLayout.SOUTH);
		panel.add(buttonValidate, BorderLayout.SOUTH);
		panel.add(buttonCancel, BorderLayout.SOUTH);

		this.add(panel);

		this.setVisible(true);
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource().equals(buttonBrowse)){
			if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				this.setFile(chooser.getSelectedFile().getAbsolutePath());
				this.fileTextField.setText(file);
			}
		}
		if (ae.getSource().equals(buttonCancel)){
			this.dispose();
		}
		if (ae.getSource().equals(buttonValidate)){
			this.setAssociatedName(this.getNameTextField().getText());
			this.dispose();
		}
	}

	/**
	 * @return the associatedNameFile
	 */
	public String getAssociatedName() {
		return associatedName;
	}

	/**
	 * @param associatedNameFile the associatedNameFile to set
	 */
	public void setAssociatedName(String associatedNameFile) {
		this.associatedName = associatedNameFile;
	}

	/**
	 * @return the buttonBrowse
	 */
	public JButton getButtonBrowse() {
		return buttonBrowse;
	}

	/**
	 * @param buttonBrowse the buttonBrowse to set
	 */
	public void setButtonBrowse(JButton buttonBrowse) {
		this.buttonBrowse = buttonBrowse;
	}

	/**
	 * @return the validate
	 */
	public JButton getButtonValidate() {
		return buttonValidate;
	}

	/**
	 * @param validate the validate to set
	 */
	public void setButtonValidate(JButton validate) {
		this.buttonValidate = validate;
	}

	/**
	 * @return the nameTextField
	 */
	public JTextField getNameTextField() {
		return nameTextField;
	}

	/**
	 * @param nameTextField the nameTextField to set
	 */
	public void setNameTextField(JTextField nameTextField) {
		this.nameTextField = nameTextField;
	}

	/**
	 * @return the fileTextField
	 */
	public JTextField getFileTextField() {
		return fileTextField;
	}

	/**
	 * @param fileTextField the fileTextField to set
	 */
	public void setFileTextField(JTextField fileTextField) {
		this.fileTextField = fileTextField;
	}

	/**
	 * @return the panel
	 */
	public JPanel getPanel() {
		return panel;
	}

	/**
	 * @param panel the panel to set
	 */
	public void setPanel(JPanel panel) {
		this.panel = panel;
	}

	/**
	 * @return the nameLabel
	 */
	public JLabel getNameLabel() {
		return nameLabel;
	}

	/**
	 * @param nameLabel the nameLabel to set
	 */
	public void setNameLabel(JLabel nameLabel) {
		this.nameLabel = nameLabel;
	}

	/**
	 * @return the fileLabel
	 */
	public JLabel getFileLabel() {
		return fileLabel;
	}

	/**
	 * @param fileLabel the fileLabel to set
	 */
	public void setFileLabel(JLabel fileLabel) {
		this.fileLabel = fileLabel;
	}

	/**
	 * @return the buttonCancel
	 */
	public JButton getButtonCancel() {
		return buttonCancel;
	}

	/**
	 * @param buttonCancel the buttonCancel to set
	 */
	public void setButtonCancel(JButton buttonCancel) {
		this.buttonCancel = buttonCancel;
	}

}

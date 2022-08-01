package commons;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Customized file chooser
 * @author Ben Renard, Irstea Lyon *
 */
@SuppressWarnings("serial")
public class Custom_FileChooser extends JFileChooser{

	private String filepath=Constants.S_EMPTY;

	/**
	 * Full constructor - will open a dialog box and retrieve full path to selected file/dir
	 * @param currentDir the directory where the dialog starts from
	 * @param selectOption "f"->files only, "d"->dir only, "b"->both
	 * @param filter
	 * @return the customized file chooser
	 */
	public Custom_FileChooser(String currentDir, String selectOption, FileFilter filter[]){
		super();
		this.setCurrentDirectory(new File(currentDir));
		if(filter!=null) {
			this.setAcceptAllFileFilterUsed(false);
			for(int i=0;i<filter.length;i++){this.addChoosableFileFilter(filter[i]);}
			}
		if(selectOption.equals("f")){this.setFileSelectionMode(JFileChooser.FILES_ONLY);}
		else if (selectOption.equals("d")){this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);}
		int dialog = this.showOpenDialog(this);
		if(dialog==JFileChooser.APPROVE_OPTION){
			setFilepath(this.getSelectedFile().getAbsolutePath());
		}
	}
	
	/**
	 * Reduced constructor - only files, no filter
	 * @param currentDir the directory where the dialog starts from
	 * @return the customized file chooser
	 */
	public Custom_FileChooser(String currentDir){
		this(currentDir,"f",null);
	}

	/**
	 * Reduced constructor - no filter
	 * @param currentDir the directory where the dialog starts from
	 * @return the customized file chooser
	 */
	public Custom_FileChooser(String currentDir,String selectOption){
		this(currentDir,selectOption,null);
	}

	
	/**
	 * Reduced constructor - only files filter
	 * @param currentDir the directory where the dialog starts from
	 * @param filter
	 * @return the customized file chooser
	 */
	public Custom_FileChooser(String currentDir, FileFilter filter[]){
		this(currentDir,"f",filter);
	}


	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the filepath
	 */
	public String getFilepath() {
		return filepath;
	}

	/**
	 * @param filepath the filepath to set
	 */
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
}

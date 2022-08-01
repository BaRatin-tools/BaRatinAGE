package commons;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;

/**
 * Wrappers and shortcuts for handling gridbag layouts
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class GridBag_Layout extends GridBagLayout{
	
	/**
	 * Constructor
	 * @param sizeRow Table of length nrow giving the size (in pixels) of each row of the grid. 
	 * @param sizeCol Table of length ncol giving the size (in pixels) of each column of the grid. 
	 * @param weightRow Table of length nrow giving the weight (between 0 and 1) of each row of the grid.
	 * @param weightCol  Table of length ncol giving the weight (between 0 and 1) of each column of the grid.
	 */	
	public GridBag_Layout(int[] sizeRow,
			int[] sizeCol,
			double[] weightRow,
			double[] weightCol) {
		super();
		this.rowHeights = sizeRow;
		this.rowWeights = weightRow;
		this.columnWidths = sizeCol;
		this.columnWeights = weightCol;
	}
	
	/**
	 * Set a Grid to a container
	 * @param container The container receiving the layout.
	 * @param sizeRow Table of length nrow giving the size (in pixels) of each row of the grid. 
	 * @param sizeCol Table of length ncol giving the size (in pixels) of each column of the grid. 
	 * @param weightRow Table of length nrow giving the weight (between 0 and 1) of each row of the grid.
	 * @param weightCol  Table of length ncol giving the weight (between 0 and 1) of each column of the grid.
	 */	
	public static void SetGrid(Container container,
			int[] sizeRow,
			int[] sizeCol,
			double[] weightRow,
			double[] weightCol) {
		GridBag_Layout gb=new GridBag_Layout(sizeRow,sizeCol,weightRow,weightCol);
		container.setLayout(gb);
	}
	
	/**
	 * Put a component into a container (having a pre-existing grid)
	 * @param component The component to put into the grid.
	 * @param container The container receiving the component.
	 * @param x x positioning of the label in the GridBagLayout of the container
	 * @param y y positioning of the label in the GridBagLayout of the container
	 * @param nx Number of horizontal cells on which the label lies
	 * @param ny Number of vertical cells on which the label lies
	 * @param xExpand Expand label horizontally on all its cells?
	 * @param yExpand Expand label vertically on all its cells?
	 */	
	public static void putIntoGrid(JComponent component,
			Container container,
			int x, 
			int y, 
			int nx, 
			int ny,
			boolean xExpand,
			boolean yExpand) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;gbc.gridy = y;
		gbc.gridwidth=nx;gbc.gridheight=ny;
		if(xExpand && yExpand) {gbc.fill = GridBagConstraints.BOTH;}
		else if(xExpand){gbc.fill = GridBagConstraints.HORIZONTAL;}
		else if(yExpand){gbc.fill = GridBagConstraints.VERTICAL;}
		container.add(component,gbc);
	}

}

package commons;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * Customized table model
 * @author Ben Renard, Irstea Lyon
 */
@SuppressWarnings("serial")
public class Custom_TableModel extends AbstractTableModel {

	private String[] columnNames=null;
	private ArrayList<Object[]> data = new ArrayList<Object[]>();
	private Boolean[] columnEditable=null;

	/**
	 * Full constructor
	 * @param columnNames name of the columns of the table
	 * @param columnEditable are columns editable?
	 */
	public Custom_TableModel(String[] columnNames, Boolean[] columnEditable){
		super();
		this.columnNames=columnNames;
		this.columnEditable=columnEditable;
	}

	/**
	 * Partial constructor (by default all columns are non-editable)
	 * @param columnNames name of the columns of the table
	 */
	public Custom_TableModel(String[] columnNames){
		super();
		this.columnNames=columnNames;
		this.columnEditable=new Boolean[columnNames.length];		
		for(int i=0;i<columnNames.length;i++){
			this.columnEditable[i]=false;
		}
	}

	/**
	 * @return the number of columns
	 */
	public int getColumnCount() {return columnNames.length;}

	/**
	 * @return the number of rows
	 */
	public int getRowCount() {return data.size();}

	/**
	 * @param col the position of the column whose name is sought (starting from zero)
	 * @return the column name
	 */
	public String getColumnName(int col) {return columnNames[col];}

	/**
	 * retrieve the object from the row/column position
	 * @param row
	 * @param col
	 * @return the object at position (row;col) 
	 */
	public Object getValueAt(int row, int col) {return data.get(row)[col];}

	/**
	 * retrieve the list of objects from the column position
	 * @param col
	 * @return the object list at column col 
	 */
	public Object[] getColumnAt(int col) {
		int n=this.getRowCount();
		if(n<1){return null;}
		Object[] z = new Object[n];
		for(int i=0;i<n;i++){z[i]=data.get(i)[col];}
		return z;
		}
	
	/**
	 * @param col the position of the column whose class is sought (starting from zero)
	 * @return the column class
	 */
	public Class<?> getColumnClass(int c) {return getValueAt(0, c).getClass();}

	/**
	 * Set the value at position (row;col)
	 * @param value the value to set
	 * @param row
	 * @param col
	 */
	public void setValueAt(Object value, int row, int col) {data.get(row)[col] = value;fireTableCellUpdated(row, col);}

	/**
	 * Add a whole row of values
	 * @param o the list of values to add
	 */
	public void addRow(Object[] o) {data.add(o);fireTableDataChanged();}

	/**
	 * Clear table
	 */
	public void reset(){data.clear();fireTableDataChanged();}

	/**
	 * Editable cells
	 * @param row
	 * @param col
	 * @return is cell editable?
	 */
	public boolean isCellEditable(int row, int col) {return this.columnEditable[col];}

}

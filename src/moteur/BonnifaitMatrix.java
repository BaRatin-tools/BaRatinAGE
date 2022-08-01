package moteur;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import Utils.Defaults;

public class BonnifaitMatrix {
	//Bonnifait control matrix implemented as a list of lists [each sublist being a column]

	private ArrayList<ArrayList<Boolean>> matrix = new ArrayList<ArrayList<Boolean>>();  //ne pas utiliser le type primitif boolean mais bien le Wrapper Boolean

	/**
	 * empty constructor
	 */
	public BonnifaitMatrix() {
	}

	/**
	 * full constructor
	 * @param matrix
	 */
	public BonnifaitMatrix(ArrayList<ArrayList<Boolean>> matrix) {
		this.matrix = matrix;
	}

	/**
	 * copy constructor
	 * @param x copied object
	 */
	public BonnifaitMatrix(BonnifaitMatrix x){
		if(x==null) {return;}
		if(x.getMatrix()!=null){
			this.matrix=new ArrayList<ArrayList<Boolean>>();
			for(int i=0;i<x.getMatrix().size();i++){
				ArrayList<Boolean> foo=new ArrayList<Boolean>();
				for(int j=0;j<x.getMatrix().get(i).size();j++){
					if(x.getMatrix().get(i).get(j)!=null) foo.add(new Boolean(x.getMatrix().get(i).get(j)));
				}
				this.matrix.add(foo);
			}
		}
	}

	public BonnifaitMatrix(List<Boolean> firstColumn) {
		this.matrix.add((ArrayList<Boolean>) firstColumn);
	}

	public Dimension getDimension(){
		return new Dimension(matrix.size(), matrix.get(1).size());
	}

	public ArrayList<Boolean> getLine(Integer index){   //génère une liste de booléen qui est la ligne de numéro index, compté en partant de 0
		if (this.matrix == null || index >= this.matrix.size())
			return null;
		else {
			ArrayList<Boolean> tmp = new ArrayList<Boolean>();
			for (ArrayList<Boolean> column : this.matrix){
				tmp.add(column.get(index));
			}
			return tmp;
		}
	}

	public void AddColumn(List<Boolean> column) {
		this.matrix.add((ArrayList<Boolean>) column);
	}

	public Boolean isValid(){
		int n=this.matrix.size();
		// Check the whole diagonal is set to true
		for (int i=0;i<n;i++){
			if(!this.matrix.get(i).get(i)){return false;}
		}
		// Check that for each column there is no true after a false below the diagonal
		for (int i=0;i<n;i++){
			ArrayList<Boolean> column = this.matrix.get(i);
			for(int j=i+1;j<n;j++){
				if(column.get(j)&(!column.get(j-1))){return false;}
			}
		}
		// Made it to here, it's all right!
		return true;
	}

	@Override
	public String toString(){
		String out="";
		for (int i=0;i<this.matrix.size();i++){
			for(int j=0;j<this.matrix.size();j++){
				if(this.matrix.get(j).get(i)){out=out+"1";} else {out=out+"0";}
				out=out+Defaults.txtSep;
			}
			out=out+System.getProperty("line.separator");
		}
		return out;}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the matrix
	 */
	public ArrayList<ArrayList<Boolean>> getMatrix() {
		return matrix;
	}

	/**
	 * @param matrix the matrix to set
	 */
	public void setMatrix(ArrayList<ArrayList<Boolean>> matrix) {
		this.matrix = matrix;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((matrix == null) ? 0 : matrix.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BonnifaitMatrix)) {
			return false;
		}
		BonnifaitMatrix other = (BonnifaitMatrix) obj;
		if (matrix == null) {
			if (other.matrix != null) {
				return false;
			}
		} else if (!matrix.equals(other.matrix)) {
			return false;
		}
		return true;
	}

}

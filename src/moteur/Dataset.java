package moteur;

import java.util.Vector;

public class Dataset<E extends Item> {
	// generic class of dataset (<=> catalogue), use an instance of Item to parameter
	private String name;
	private Vector<E> dataset = new Vector<E>();
	// Constants
	public static final String IS_USED="NameIsAlreadyUsed";

	/**
	 * empty constructor
	 */
	public Dataset(){}

	/**
	 * Copy constructor
	 * @param x copied object
	 */
	public Dataset(Dataset<E> x){
		if(x==null){return;}
		if(x.getName()!=null){
			this.name=new String(x.getName());
		}
		if(x.getDataset()!=null){	
			this.dataset=new Vector<E>(x.getDataset());
		}
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getName() {
		return this.name;
	}

	public void setName(String nom) {
		this.name = nom;
	}

	public Vector<E> getDataset() {
		return dataset;
	}

	public void setDataset(Vector<E> dataset) {
		this.dataset = dataset;
	}

	public E getItemAt(int index) {
		return dataset.get(index);
	}

	public void setItemAt(int indx,E item) {
		dataset.set(indx, item);
	}

	@SuppressWarnings("unchecked")
	public void insertAt(Item item, int index) throws Exception{
		// TODO: define own exception class
		if(isNameAlreadyUsed(item.getName())) {throw new Exception();}
		this.dataset.insertElementAt((E) item, index);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataset == null) ? 0 : dataset.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Dataset)) {
			return false;
		}
		Dataset other = (Dataset) obj;
		if (dataset == null) {
			if (other.dataset != null) {
				return false;
			}
		} else if (!dataset.equals(other.dataset)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public Item getItemByName(String nameTest){
		for(Item i : this.dataset){
			if(i.getName().equals(nameTest)){
				return i;
			}
		}
		return null;
	}

	public int getIndxByName(String nameTest){
		int k=-1;
		for(Item i : this.dataset){
			k=k+1;
			if(i.getName().equals(nameTest)){
				return k;
			}
		}
		return k;
	}

	public Boolean isNameAlreadyUsed(String nameTest){
		for(Item i : dataset){
			if(i.getName().equals(nameTest)){
				return true;
			}
		}
		return false;
	}

	public void add(E e) throws Exception {
		// TODO: define own exception class
		if(isNameAlreadyUsed(((Item)e).getName())) {
			Exception ex=new Exception(IS_USED);
			throw ex;}
		else{this.dataset.add(e);}
	}

	public void delete(String nameTest) {
		int indx=this.getIndxByName(nameTest);
		this.dataset.remove(indx);
	}

	public int getSize(){
		return this.dataset.size();
	}

	public String[] getStringList(){
		Vector<E> object = this.dataset;
		int n=object.size();
		if(n<1) {return new String[] {""};}
		String[] list = new String[n];
		for(int i=0;i<n;i++){list[i]=object.get(i).getName();}
		return list;
	}

}

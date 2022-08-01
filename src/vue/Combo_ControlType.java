package vue;

import commons.Constants;

public class Combo_ControlType {

	private int indx;
	private String txt;
	// constants
	public static final int UNKNOWN_INDX=7;
	
	public Combo_ControlType(){
		this.indx=-1;
		this.txt=Constants.S_EMPTY;
	}

	public Combo_ControlType(int indx, String txt){
		this.indx=indx;
		this.txt=txt;
	}
	
	public static Combo_ControlType[] getList(){
		Combo_ControlType[] list={
				new Combo_ControlType(0,"RectangularSill"),
				new Combo_ControlType(1,"TriangularSill"),
				new Combo_ControlType(2,"ParabolicSill"),
				new Combo_ControlType(3,"Orifice"),
				new Combo_ControlType(4,"RectangularChannel"),
				new Combo_ControlType(5,"TriangularChannel"),
				new Combo_ControlType(6,"ParabolicChannel"),
				new Combo_ControlType(UNKNOWN_INDX,"Unknown"),
				};
		return list;
		}
	
	public static String[] getStringList(){
		Combo_ControlType[] list=getList();
		int n=list.length;
		String[] slist=new String[n];
		for(int i=0;i<n;i++){slist[i]=list[i].getTxt();}
		return slist;
		}

	
	public int getIndx() {
		return indx;
	}

	public void setIndx(int indx) {
		this.indx = indx;
	}

	public String getTxt() {
		return txt;
	}

	public void setTxt(String txt) {
		this.txt = txt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + indx;
		result = prime * result + ((txt == null) ? 0 : txt.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Combo_ControlType other = (Combo_ControlType) obj;
		if (indx != other.indx)
			return false;
		if (txt == null) {
			if (other.txt != null)
				return false;
		} else if (!txt.equals(other.txt))
			return false;
		return true;
	}
}

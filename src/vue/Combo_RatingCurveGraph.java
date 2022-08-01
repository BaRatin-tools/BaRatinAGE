package vue;

/**
 * Handling of plot options for rating curves
 * @author Ben Renard, Irstea Lyon
 */

public class Combo_RatingCurveGraph {

	private int indx;
	private String txt;
	
	/**
	 * Full Constructor
	 * @param indx
	 * @param txt
	 */
	public Combo_RatingCurveGraph(int indx, String txt){
		this.indx=indx;
		this.txt=txt;
	}

	/**
	 * Default Constructor
	 */
	public Combo_RatingCurveGraph(){
		Combo_HydrauGraph[] list=getList();
		this.indx=list[0].getIndx();
		this.txt=list[0].getTxt();
	}

	/**
	 * List of available options
	 * @return the list 
	 */
	public static Combo_HydrauGraph[] getList(){
		Combo_HydrauGraph[] list={
				new Combo_HydrauGraph(0,"PostRC_env"),
				new Combo_HydrauGraph(1,"PostRC_spag"),
				};
		return list;
		}
	
	/**
	 * List of available options displayed as text only
	 * @return the list
	 */
	public static String[] getStringList(){
		Combo_HydrauGraph[] list=getList();
		int n=list.length;
		String[] slist=new String[n];
		for(int i=0;i<n;i++){slist[i]=list[i].getTxt();}
		return slist;
		}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

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

}

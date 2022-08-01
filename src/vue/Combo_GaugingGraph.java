package vue;

/**
 * Handling of plot options for gaugings
 * @author Ben Renard, Irstea Lyon
 */
public class Combo_GaugingGraph {

	private int indx;
	private String txt;
	
	/**
	 * Full Constructor
	 * @param indx
	 * @param txt
	 */
	public Combo_GaugingGraph(int indx, String txt){
		this.indx=indx;
		this.txt=txt;
	}

	/**
	 * Default Constructor
	 */
	public Combo_GaugingGraph(){
		Combo_GaugingGraph[] list=getList();
		this.indx=list[0].getIndx();
		this.txt=list[0].getTxt();
	}

	/**
	 * List of available options
	 * @return the list 
	 */
	public static Combo_GaugingGraph[] getList(){
		Combo_GaugingGraph[] list={
				new Combo_GaugingGraph(0,"Gaugings")
				};
		return list;
		}
	
	/**
	 * List of available options displayed as text only
	 * @return the list
	 */
	public static String[] getStringList(){
		Combo_GaugingGraph[] list=getList();
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

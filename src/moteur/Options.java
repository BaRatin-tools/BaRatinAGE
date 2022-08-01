package moteur;

/**
 * handling of BaRatinAGE options
 * @author Ben Renard, Irstea Lyon
 */
public class Options {
	
	private String lang;
	// TODO: add MCMC options + fonts, look-and-feel etc.
	
	/**
	 * Full constructor
	 * @param lang language
	 */
	public Options(String lang){
		this.lang=lang;
	}

	/**
	 * Default constructor
	 */
	public Options(){
		this("fr");
	}

	/**
	 * copy constructor
	 * @param x copied object
	 */
	public Options(Options x){
		if(x==null){return;}
		if(x.getLang()!=null) this.lang=new String(x.getLang());
	}
		
	@Override
	public String toString(){
		String out="";
		out=this.lang+System.getProperty("line.separator");
		return out;
	}

	/////////////////////////////////////////////////////////
	//GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the lang
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * @param lang the lang to set
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}

}

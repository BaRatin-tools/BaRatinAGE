package Utils;

import java.util.HashMap;
import java.util.Iterator;

import commons.ReadWrite;

@SuppressWarnings("serial")
public class Dico extends HashMap<String,String> {

	private static Dico instance;
	public static synchronized Dico getInstance(String lang){
		if (instance == null){
			instance = new Dico(lang);
		}
		return instance;
	}
	private static final String unknown="Unknown dictionary entry";
	private static final int indx_fr=0;
	private static final int indx_en=1;
	private static final int indx_es=2;
	private static final int indx_de=3;
	private static final int indx_it=4;
	private static final int indx_br=5;
	private static final int indx_def=indx_en;

	public Dico(String lang){
		int indx=indx_def;
		HashMap<String,String[]> ML=GetMLdico();
		Iterator<String> key = ML.keySet().iterator();		
		if(lang.equals("fr")){indx=indx_fr;}
		else if(lang.equals("en")){indx=indx_en;}
		else if(lang.equals("es")){indx=indx_es;}
		else if(lang.equals("de")){indx=indx_de;}
		else if(lang.equals("it")){indx=indx_it;}
		else if(lang.equals("br")){indx=indx_br;}
		this.clear();
		while(key.hasNext()){
			String k = key.next();
			this.put(k, ML.get(k)[indx]);
		}
	}

	public String entry(String key){
		String out;
		if(this.get(key) == null){out=unknown;}
		else {out=this.get(key);}	
		return out;}

	public String[] entry(String[] keys){
		int n=keys.length;
		String[] out=new String[n];
		for(int i=0;i<n;i++){
			if(this.get(keys[i]) == null){out[i]=unknown;}
			else {out[i]=this.get(keys[i]);}
		}		
		return out;}

	private HashMap<String,String[]> GetMLdico(){
		HashMap<String,String[]> dico=new HashMap<String,String[]>();
		dico.clear();
		try {
			dico=ReadWrite.readDico(Defaults.dicoFile, ";");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return dico;
		}

}

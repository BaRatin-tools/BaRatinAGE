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
	private String lang="en";
	private String[] available=null;

	public Dico(String lang){
		HashMap<String,String[]> ML=GetMLdico();
		Iterator<String> key = ML.keySet().iterator();		
		// get available languages
		String[] head = ML.get("﻿key DO NOT MODIFY");
		// get index of available language corresponding to the requested one
		int indx = -1;
		for(int i=0;i<head.length;i++){
			if(head[i].equalsIgnoreCase(lang)) {indx=i;}
		}
		this.clear();
		this.setLang(lang);
		this.setAvailable(head);
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

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String[] getAvailable() {
		return available;
	}

	public void setAvailable(String[] available) {
		this.available = available;
	}

}

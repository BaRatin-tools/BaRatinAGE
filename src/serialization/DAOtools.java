package serialization;

import java.io.BufferedWriter;
import java.io.IOException;

import commons.Constants;

public class DAOtools {
	
	private DAOtools(){}
	
	public static void safeWrite(BufferedWriter bw,Object o) throws IOException{
		if(o==null){bw.write("");bw.newLine();}
		else{bw.write(o.toString());bw.newLine();}
	}

	public static void safeWrite(BufferedWriter bw,int i) throws IOException{
		bw.write(Integer.toString(i));bw.newLine();
	}
	
	public static int safeRead_i(String s){
		if(s.equals("")){return(Constants.I_MISSING);}
		else{return(Integer.valueOf(s));}
	}
		
	public static Double safeRead_d(String s){
		if(s.equals("")){return(null);}
		else{return(Double.valueOf(s));}
	}
	

}

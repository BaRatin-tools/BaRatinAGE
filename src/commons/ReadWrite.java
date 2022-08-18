package commons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
/**
 * Basic read/write utilities
 * @author Ben Renard, Irstea Lyon
 *
 */
public class ReadWrite {

	public ReadWrite(){}

	/**
	 * Write matrix
	 * @param x, matrix of data to be written
	 * @param head, headers
	 * @param file, target file
	 * @param sep, separator
	 * @throws IOException
	 */
	public static void write(Double[][] x,String[] head, String file,String sep) throws FileNotFoundException,IOException,Exception{
		File f;FileWriter fw;BufferedWriter bw;
		f=new File(file);
		if (!f.exists()) {f.createNewFile();}
		fw = new FileWriter(f.getAbsoluteFile());bw = new BufferedWriter(fw);
		String foo;
		// headers
		if(head!=null){
			foo="";
			for (int i=0;i<head.length;i++){foo=foo+head[i]+sep;}
			bw.write(foo);bw.newLine();
		}
		// data
		for (int i=0;i<x[0].length;i++){
			foo="";
			for (int j=0;j<x.length;j++){
				foo=foo+Double.toString(x[j][i])+sep;
			}
			bw.write(foo);bw.newLine();
		}
		bw.close();
	}

	/**
	 * Read matrix
	 * @param file File to read
	 * @param sep separator
	 * @param nhead number of header line(s) (skipped)
	 * @param skipcol number of leading columns to skip
	 * @return each column of the datafile
	 * @throws Exception
	 * @throws IOException
	 */
	public static Double[][] read(String file,String sep,int nhead,int skipcol) throws FileNotFoundException,Exception,IOException{
		ArrayList<Double[]> y=new ArrayList<Double[]>();
		String line;String[] foo0;String[] foo1;String[] foo;
		int ncol = 0;
		Double[] z;
		File f=new File(file);
		Scanner sc = new Scanner(f);
		// skip headers
		for(int i=0;i<nhead;i++){line=sc.nextLine();}
		// read data
		int i=0;
		while(sc.hasNextLine()){
			line=sc.nextLine();
			foo0=line.split(sep+"+");
			// next line is to avoid pb due to leading separators (space typically)
			if(foo0[0].equals("")){foo1=Arrays.copyOfRange(foo0, 1,foo0.length);} else {foo1=foo0;}
			// remove undesired leading columns
			foo=Arrays.copyOfRange(foo1, skipcol,foo1.length);
			// get ncol
			if(i==0){ncol=foo.length;}
			// get values
			z=new Double[ncol];
			for (int j=0;j<foo.length;j++){z[j]=Double.parseDouble(foo[j]);}
			y.add(z);
			i=i+1;
		}
		sc.close();
		Double[][] w=new Double[ncol][y.size()];
		for (int i1=0;i1<ncol;i1++){
			for(int j=0;j<y.size();j++){
				w[i1][j]=y.get(j)[i1];
			}
		}
		return w;
	}

	/**
	 * Read matrix
	 * @param file File to read
	 * @param sep separator
	 * @param nhead number of header line(s) (skipped)
	 * @return each column of the datafile
	 * @throws Exception
	 * @throws IOException
	 */
	public static Double[][] read(String file,String sep,int nhead) throws FileNotFoundException,Exception,IOException{
		return read(file,sep,nhead,0); 
	}

	/**
	 * write lines of text
	 * @param text lines of text
	 * @param file target file
	 * @throws IOException 
	 */
	public static void write(String[] text,String file) throws IOException{
		File f;FileWriter fw;BufferedWriter bw;
		f=new File(file);
		if (!f.exists()) {f.createNewFile();} 
		fw = new FileWriter(f.getAbsoluteFile());bw = new BufferedWriter(fw);
		// data
		for (int i=0;i<text.length;i++){
			bw.write(text[i]);bw.newLine();
		}
		bw.close();
	}
	
	/**
	 * read lines of text
	 * @param file the target file
	 * @return lines of text as a String table
	 * @throws FileNotFoundException 
	 */
	public static String[] read(String file) throws FileNotFoundException{
		File f=new File(file);
		Scanner sc = new Scanner(f);
		// count number of lines data
		int i=0;
		while(sc.hasNextLine()){
			i=i+1;
			sc.nextLine();
		}
		// read lines
		sc.close();sc = new Scanner(f);
		String[] out=new String[i];
		for(int j=0;j<i;j++){
			out[j]=sc.nextLine();
		}
		sc.close();
		return out;
	}
	
	/**
	 * Read matrix of text items and store it into a HashMap - typically for a dictionary
	 * The key is in the first column
	 * The first header row is not used 
	 * @param file File to read
	 * @param sep separator
	 * @return a HashMap containing the text matrix, with key taken as the 1st column
	 * @throws Exception
	 * @throws IOException
	 */
	public static HashMap<String,String[]> readDico(String file,String sep) throws FileNotFoundException,Exception,IOException{
		HashMap<String,String[]> dico=new HashMap<String,String[]>();
		String line;String[] foo0;String[] foo;
		File f=new File(file);
		Scanner sc = new Scanner(f);
		// read headers
		line=sc.nextLine();
		foo0=line.split(sep+"+");
		// next line is to avoid pb due to leading separators (space typically)
		if(foo0[0].equals("")){foo=Arrays.copyOfRange(foo0, 1,foo0.length);} else {foo=foo0;}
		dico.put(foo[0],Arrays.copyOfRange(foo,1,foo.length));
		// read data
		while(sc.hasNextLine()){
			line=sc.nextLine();
			foo0=line.split(sep+"+");
			// next line is to avoid pb due to leading separators (space typically)
			if(foo0[0].equals("")){foo=Arrays.copyOfRange(foo0, 1,foo0.length);} else {foo=foo0;}
			// get values
			dico.put(foo[0],Arrays.copyOfRange(foo,1,foo.length));
		}
		sc.close();
		return dico;
	}
}

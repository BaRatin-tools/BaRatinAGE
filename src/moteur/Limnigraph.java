package moteur;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import Utils.Defaults;
import commons.Constants;
import commons.Observation;
import commons.Plots;
import commons.ReadWrite;
import commons.Time;
import commons.TimeSerie;

/**
 * Limnigraph object
 * @author Ben Renard, Irstea Lyon
 */
public class Limnigraph extends TimeSerie {

	private String filePath=Constants.S_EMPTY;  //the file path of limnigraph
	private Double[] uH;
	private int[] bHindx;
	private Double[] bH;

	/**
	 * default constructor
	 */
	public Limnigraph(){
		super();
	}

	/**
	 * full constructor
	 * @param name
	 * @param description
	 * @param type
	 * @param observations
	 * @param uH
	 * @param bHindx
	 * @param bH
	 */
	public Limnigraph(String name,String description,String type,List<Observation> observations,Double[] uH,int[] bHindx,Double[] bH){
		super(name,description,type,observations);
		this.uH=uH;
		this.bHindx=bHindx;
		this.bH=bH;		
	}

	/**
	 * partial constructor
	 * @param name
	 * @param description
	 * @param observations
	 * @param uH
	 * @param bHindx
	 * @param bH
	 */
	public Limnigraph(String name,String description,List<Observation> observations,Double[] uH,int[] bHindx,Double[] bH){
		this(name,description,"",observations,uH,bHindx,bH);
	}

	/**
	 * partial constructor
	 * @param name
	 */
	public Limnigraph(String name){
		super(name);
	}

	/**
	 * copy constructor
	 * @param x copied object
	 */
	public Limnigraph(Limnigraph x){
		super(x);
		if(x==null){return;}
		if(x.getFilePath()!=null) this.filePath=new String(x.getFilePath());
		if(x.getObservations()!=null) {
			int n=x.getObservations().size();
			this.uH=new Double[n];
			this.bHindx=new int[n];
			this.bH=new Double[n];
			for(int i=0;i<n;i++){
				if(x.getuH()[i]!=null) this.uH[i]=new Double(x.getuH()[i]);
				if(x.getbH()[i]!=null) this.bH[i]=new Double(x.getbH()[i]);
				this.bHindx[i]=new Integer(x.getbHindx()[i]);		
			}
		}
	}

	public void read() throws Exception,FileNotFoundException{
		// read data file
		Double[][] y;
		try{y=ReadWrite.read(this.getFilePath(), Defaults.csvSep, 1);}
		catch(Exception e) {
			// try with alternative csv separator
			y=ReadWrite.read(this.getFilePath(), Defaults.csvSep2, 1);
		}
		int n=y[0].length;
		// put content in object
		Double[] uH= new Double[n];
		Double[] bH= new Double[n];
		int[] bHindx= new int[n];
		List<Observation> obs = new ArrayList<Observation>();
		for (int i=0;i<n;i++){
			obs.add(new Observation(y[6][i],
					new Time(y[0][i].intValue(),y[1][i].intValue(),y[2][i].intValue(),y[3][i].intValue(),y[4][i].intValue(),y[5][i].intValue()),
					null));			
			uH[i]=y[7][i];
			bHindx[i]=y[8][i].intValue();
			bH[i]=y[9][i];
		}
		this.setObservations(obs);
		this.setuH(uH);
		this.setbH(bH);
		this.setbHindx(bHindx);
	}

	public ChartPanel plot(String title,String xlab,String ylab,
			Color lineColor, Color bkgColor, Color gridColor,boolean ylog){
		JFreeChart chart=Plots.TimeSeriesPlot(this, title, xlab, ylab,lineColor,bkgColor,gridColor,ylog);
		ChartPanel CP = new ChartPanel(chart);
		return(CP);
	}

	public ChartPanel plot(String title,String xlab,String ylab,boolean ylog){
		return 	plot(title,xlab,ylab,Color.BLACK,Color.WHITE, Color.GRAY,ylog);
	}

	public Double[][] toMatrix(){
		int n=this.getObservations().size();
		Double[][] out = new Double[11][n];
		for(int i=0;i<n;i++){
			Observation o = this.getObservations().get(i);
			if(o.getObsDate()!=null){
				out[0][i]=o.getObsDate().getYear()*1.0;
				out[1][i]=o.getObsDate().getMonth()*1.0;
				out[2][i]=o.getObsDate().getDay()*1.0;
				out[3][i]=o.getObsDate().getHour()*1.0;
				out[4][i]=o.getObsDate().getMinute()*1.0;
				out[5][i]=o.getObsDate().getSecond()*1.0;
			}
			if(o.getValue()!=null) out[6][i]=o.getValue();
			if(this.getuH()!=null) out[7][i]=this.getuH()[i];
			out[8][i]=this.getbHindx()[i]*1.0;
			if(this.getbH()!=null) out[9][i]=this.getbH()[i];
			if(o.getQualityCode()!=null) {out[10][i]=o.getQualityCode()*1.0;} else {out[10][i]=Constants.D_MISSING;}
		}
		return(out);
	}

	public void fromMatrix(Double[][] m){
		int n=m[0].length;
		ArrayList<Observation> limni = new ArrayList<Observation>();
		Double[] uH=m[7];
		Double[] bH=m[9];
		int[] bHindx=new int[n];
		for(int i=0;i<n;i++){
			Observation o = new Observation();
			o.setObsDate(new Time(m[0][i].intValue(),m[1][i].intValue(),m[2][i].intValue(),m[3][i].intValue(),m[4][i].intValue(),m[5][i].intValue()));
			o.setValue(m[6][i]);
			o.setQualityCode(m[10][i].intValue());
			limni.add(o);
			bHindx[i]=m[8][i].intValue();
		}
		this.setObservations(limni);
		this.setuH(uH);
		this.setbH(bH);
		this.setbHindx(bHindx);
	}


	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public Double[] getuH() {
		return uH;
	}

	public void setuH(Double[] uH) {
		this.uH = uH;
	}

	public int[] getbHindx() {
		return bHindx;
	}

	public void setbHindx(int[] bHindx) {
		this.bHindx = bHindx;
	}

	public Double[] getbH() {
		return bH;
	}

	public void setbH(Double[] bH) {
		this.bH = bH;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(bH);
		result = prime * result + Arrays.hashCode(bHindx);
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + Arrays.hashCode(uH);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Limnigraph other = (Limnigraph) obj;
		if (!Arrays.equals(bH, other.bH))
			return false;
		if (!Arrays.equals(bHindx, other.bHindx))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (!Arrays.equals(uH, other.uH))
			return false;
		return true;
	}	

}

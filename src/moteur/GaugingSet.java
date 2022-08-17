package moteur;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import Utils.Defaults;
import Utils.Dictionnary;

import com.opencsv.CSVReader;

import commons.Constants;
import commons.ReadWrite;
import commons.Time;

public class GaugingSet extends Item {
	//class for manipulating gaugings

	private String filePath=Constants.S_EMPTY;  //the file path of gaugings
	private ArrayList<Gauging> gaugings = new ArrayList<Gauging>();

	/**
	 * empty constructor
	 */
	public GaugingSet() {
	}

	/**
	 * Constructor by copy
	 * @param x copied object
	 */
	public GaugingSet(GaugingSet x) {
		super(x);
		if(x==null){return;}
		if(x.getFilePath()!=null) this.filePath=new String(x.getFilePath());
		if(x.getGaugings()!=null){
			this.gaugings=new ArrayList<Gauging>();
			for (int i=0;i<x.getGaugings().size();i++){
				if(x.getGaugings().get(i)!=null) this.gaugings.add(new Gauging(x.getGaugings().get(i)));
			}
		}
	}

	/**
	 * partial constructor
	 * @param name
	 */
	public GaugingSet(String name) {
		super(name);
	}

	/**
	 * full constructor, reads gaugings from file 
	 * @param file
	 * @param name
	 */
	public GaugingSet(String file, String name) {
		super(name);
		this.filePath = file ;

		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(this.filePath)); //by default the separator is the semicolon

			String [] nextLine;
			nextLine = reader.readNext();    //to dodge the head line

			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is table of strings of the read line
				gaugings.add(new Gauging(Double.valueOf(nextLine[0]),Double.valueOf(nextLine[1]),Double.valueOf(nextLine[2]),Double.valueOf(nextLine[3])));
			}
		} 
		catch (FileNotFoundException e) {
			System.out.println(Dictionnary.getInstance().getFileNotFound());
		}
		catch (IOException e) {
			System.out.println(Dictionnary.getInstance().getIOException());
		}
		catch (NumberFormatException e) { 
			System.out.println(Dictionnary.getInstance().getNumFormatException());
		}
	}

	/**
	 * full constructor, without reading
	 * @param filePath
	 * @param gaugings
	 */
	public GaugingSet(String filePath, ArrayList<Gauging> gaugings) {
		super();
		this.filePath = filePath;
		this.gaugings = gaugings;
	}

	/**
	 * Constructor from a matrix of gaugings
	 * @param m
	 */
	public GaugingSet(Double[][] m){
		//TODO 
	}
	
	public void read(String fmt) throws Exception,FileNotFoundException{
		boolean active=true;
		String sep="";
		if(fmt.equalsIgnoreCase("csv")){sep=Defaults.csvSep;}
		else if(fmt.equalsIgnoreCase("bad")){sep=" ";}
		else {throw new Exception("UnknownFormat");}
		Double[][] y;
		try{y=ReadWrite.read(this.getFilePath(),sep,1);}
		catch(Exception e){
			// try with alternative csv separator
			y=ReadWrite.read(this.getFilePath(),Defaults.csvSep2,1);
		}
		this.gaugings=new ArrayList<Gauging>();
		for (int i=0;i<y[0].length;i++){
			if(fmt.equalsIgnoreCase("bad")){
				active=true;
				this.gaugings.add(new Gauging(y[0][i],2.0*y[1][i],y[2][i],(100.0*2.0*y[3][i])/y[2][i],active));			
				}
			else if(fmt.equalsIgnoreCase("csv")){
				if(y[4][i].equals(0.0)){active=false;} else{active=true;}
				this.gaugings.add(new Gauging(y[0][i],y[1][i],y[2][i],y[3][i],active));			
			}
		}
	}

	public ChartPanel plot(String title,String xlab,String ylab,
			Color pointColor, Color bkgColor, Color gridColor,boolean ylog){
        //TODO: use "Plots" class
		// intervals
 		XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
        XYIntervalSeries intervals = new XYIntervalSeries("uncertainty");
		int n=this.getGaugings().size();
		double q,qlow,qhigh;
		for (int i=0;i<n;i++){
			if(this.getGaugings().get(i).getActive()){
				q=this.getGaugings().get(i).getQ();
				qlow=this.getGaugings().get(i).getQ()-this.getGaugings().get(i).getQ()*this.getGaugings().get(i).getuQ()*0.01;
				qhigh=this.getGaugings().get(i).getQ()+this.getGaugings().get(i).getQ()*this.getGaugings().get(i).getuQ()*0.01;
				if(ylog) { // Avoid Q<=0
					if(qlow<=0) {qlow=Constants.D_TINY;}
					if(qhigh<=0) {qhigh=Constants.D_TINY;}
					if(q<=0) {q=Constants.D_TINY;}
				}
				intervals.add(this.getGaugings().get(i).getH(),
						this.getGaugings().get(i).getH()-this.getGaugings().get(i).getuH(),
						this.getGaugings().get(i).getH()+this.getGaugings().get(i).getuH(),
						q,qlow,qhigh);
			}
		}
		dataset.addSeries(intervals);
        NumberAxis x = new NumberAxis(xlab);
        NumberAxis y = new NumberAxis(ylab);

		// Points
        XYErrorRenderer xyerrorrenderer = new XYErrorRenderer();
		xyerrorrenderer.setSeriesPaint(0,pointColor);
		xyerrorrenderer.setSeriesShape(0, new Ellipse2D.Double(-0.5*Defaults.plot_pointSize,-0.5*Defaults.plot_pointSize,Defaults.plot_pointSize,Defaults.plot_pointSize));
		xyerrorrenderer.setSeriesShapesVisible(0, true);
        // do plot
		XYPlot xyplot = new XYPlot(dataset, x, y, xyerrorrenderer);
        JFreeChart chart = new JFreeChart("gaugings", xyplot);
        chart.setTitle(title);
		chart.removeLegend();

		// Background
		chart.setBackgroundPaint(bkgColor);
		chart.getPlot().setBackgroundPaint(bkgColor);
		xyplot.setRangeGridlinePaint(gridColor);
		xyplot.setDomainGridlinePaint(gridColor);
		xyplot.setDomainAxis(x);
		xyplot.setRangeAxis(y);
		x.setAutoRangeIncludesZero(false);
		
		final NumberAxis logaxis = new LogarithmicAxis(ylab);
		if(ylog){chart.getXYPlot().setRangeAxis(logaxis);}		
		ChartPanel CP = new ChartPanel(chart);
		return(CP);
	}

	public ChartPanel plot(String title,String xlab,String ylab,boolean ylog){
		return 	plot(title,xlab,ylab,Color.BLACK,Color.WHITE, Color.GRAY,ylog);
	}

	public Double[][] toMatrix(){
		int n=this.gaugings.size();
		Double[][] out = new Double[11][n];
		for(int i=0;i<n;i++){
			Gauging g = this.getGaugings().get(i);
			out[0][i]=g.getT().getYear()*1.0;
			out[1][i]=g.getT().getMonth()*1.0;
			out[2][i]=g.getT().getDay()*1.0;
			out[3][i]=g.getT().getHour()*1.0;
			out[4][i]=g.getT().getMinute()*1.0;
			out[5][i]=g.getT().getSecond()*1.0;
			out[6][i]=g.getH();
			out[7][i]=g.getuH();
			out[8][i]=g.getQ();
			out[9][i]=g.getuQ();
			if(g.getActive()){out[10][i]=1.0;} else {out[10][i]=0.0;}
		}
		return(out);
	}
	
	public void fromMatrix(Double[][] m){
		int n=m[0].length;
		ArrayList<Gauging> list = new ArrayList<Gauging>();
		for(int i=0;i<n;i++){
			Gauging g = new Gauging();
			g.setT(new Time(m[0][i].intValue(),m[1][i].intValue(),m[2][i].intValue(),m[3][i].intValue(),m[4][i].intValue(),m[5][i].intValue()));
			g.setH(m[6][i]);
			g.setuH(m[7][i]);
			g.setQ(m[8][i]);
			g.setuQ(m[9][i]);
			g.setActive(m[10][i]==1.0);
			list.add(g);
		}
		this.setGaugings(list);		
	}
	
	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return the lesJaugeages
	 */
	public ArrayList<Gauging> getGaugings() {
		return gaugings;
	}

	/**
	 * @param gaugings the lesJaugeages to set
	 */
	public void setGaugings(ArrayList<Gauging> gaugings) {
		this.gaugings = gaugings;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result
				+ ((gaugings == null) ? 0 : gaugings.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof GaugingSet)) {
			return false;
		}
		GaugingSet other = (GaugingSet) obj;
		if (filePath == null) {
			if (other.filePath != null) {
				return false;
			}
		} else if (!filePath.equals(other.filePath)) {
			return false;
		}
		if (gaugings == null) {
			if (other.gaugings != null) {
				return false;
			}
		} else if (!gaugings.equals(other.gaugings)) {
			return false;
		}
		return true;
	}

}

package moteur;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import commons.Constants;

public class Spaghetti {

	private int nx;
	private int nspag;
	private Double[] x; 
	private Double[][] y;

	/**
	 * empty constructor
	 */
	public Spaghetti(){}

	/**
	 * full constructor (nx/ny deduced from x and y)
	 * @param x
	 * @param y
	 */
	public Spaghetti(Double[] x, Double[][] y) {
		super();
		this.x = x;
		this.y = y;
		if(x!=null){this.nx=x.length;} else {this.nx=0;}
		if(y!=null){this.nspag=y.length;} else {this.nspag=0;}
	}

	/**
	 * full constructor using a matrix format
	 * @param w spaghetti as a matrix
	 */
	public Spaghetti(Double[][] w){
		this(w[0],Arrays.copyOfRange(w,1,w.length));
	}

	/**
	 * copy constructor
	 * @param x copied object
	 */
	public Spaghetti(Spaghetti x) {
		if(x==null){return;}
		this.nx=new Integer(x.getNx());
		this.nspag=new Integer(x.getNspag());
		this.x=new Double[nx];
		this.y=new Double[nspag][nx];
		if(x.getX()!=null){
			for(int i=0;i<this.nx;i++){
				if(x.getX()[i]!=null) this.x[i]=new Double(x.getX()[i]);
			}
		}
		if(x.getY()!=null){
			for(int i=0;i<this.nx;i++){
				for(int j=0;j<this.nspag;j++){
					if(x.getY()[j][i]!=null) this.y[j][i]=new Double(x.getY()[j][i]);
				}
			}
		}
	}

	public ChartPanel plot(String title,String xlab,String ylab,
			Color lineColor, Color bkgColor, Color gridColor,int rmax,boolean ylog){
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries rep;
		int rstep=Math.max((int) this.getNspag()/rmax,1);
		for(int r=0;r<this.getNspag();r+=rstep){
			rep = new XYSeries("rep"+Integer.toString(r));
			for (int i=0;i<this.getNx();i++){
				if(ylog & this.getY()[r][i]<=0.0){rep.add(this.getX()[i],Constants.D_TINY);} else {rep.add(this.getX()[i],this.getY()[r][i]);}
			}			
			dataset.addSeries(rep);
		}
		JFreeChart chart = ChartFactory.createXYLineChart(
				title, // Title
				xlab, // x-axis Label
				ylab, // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				false, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
				);
		final XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		BasicStroke solid = new BasicStroke(0.5f);
		final NumberAxis logaxis = new LogarithmicAxis(ylab);
		if(ylog){chart.getXYPlot().setRangeAxis(logaxis);}
		// Lines
		for(int r=0;r<dataset.getSeriesCount();r++){
			renderer.setSeriesPaint(r,lineColor);
			renderer.setSeriesStroke(r, solid);
		}

		// Background
		chart.setBackgroundPaint(bkgColor);
		chart.getPlot().setBackgroundPaint(bkgColor);
		chart.getXYPlot().setRangeGridlinePaint(gridColor);
		chart.getXYPlot().setDomainGridlinePaint(gridColor);

		ChartPanel CP = new ChartPanel(chart);
		return(CP);
	}

	public ChartPanel plot(String title,String xlab,String ylab,boolean ylog){
		return 	plot(title,xlab,ylab,Color.BLACK,Color.WHITE, Color.GRAY,100,ylog);
	}

	public Double[][] toMatrix(){
		Double[][] out = new Double[this.nspag+1][this.nx];
		out[0]=this.x;
		for(int i=0;i<this.nspag;i++){
			out[i+1]=this.y[i];
		}
		return(out);
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public int getNx() {
		return nx;
	}

	public void setNx(int nx) {
		this.nx = nx;
	}

	public int getNspag() {
		return nspag;
	}

	public void setNspag(int nspag) {
		this.nspag = nspag;
	}

	public Double[] getX() {
		return x;
	}

	public void setX(Double[] x) {
		this.x = x;
	}

	public Double[][] getY() {
		return y;
	}

	public void setY(Double[][] y) {
		this.y = y;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nspag;
		result = prime * result + nx;
		result = prime * result + Arrays.hashCode(x);
		result = prime * result + Arrays.hashCode(y);
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
		Spaghetti other = (Spaghetti) obj;
		if (nspag != other.nspag)
			return false;
		if (nx != other.nx)
			return false;
		if (!Arrays.equals(x, other.x))
			return false;
		if (!Arrays.deepEquals(y, other.y))
			return false;
		return true;
	}


}

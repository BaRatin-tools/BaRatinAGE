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
import commons.Plots;

public class Envelop {

	private int nx;
	private Double[] x; 
	private Double[] maxpost;
	private Double[] median;
	private Double[] qlow;
	private Double[] qhigh;
	
	/**
	 * empty constructor
	 */
	public Envelop(){}
	
	/**
	 * copy constructor
	 * @param x copied object
	 */
	public Envelop(Envelop x){
		if(x==null) {return;}
		this.nx=new Integer(x.getNx());
		this.x=new Double[this.nx];
		this.maxpost=new Double[this.nx];
		this.median=new Double[this.nx];
		this.qlow=new Double[this.nx];
		this.qhigh=new Double[this.nx];
		for(int i=0;i<this.nx;i++){
			if(x.getX()!=null) {
				if(x.getX()[i]!=null) this.x[i]=new Double(x.getX()[i]);
			}
			if(x.getMaxpost()!=null) {
				if(x.getMaxpost()[i]!=null) this.maxpost[i]=new Double(x.getMaxpost()[i]);
			}
			if(x.getMedian()!=null) {
				if(x.getMedian()[i]!=null) this.median[i]=new Double(x.getMedian()[i]);
			}
			if(x.getQlow()!=null) {
				if(x.getQlow()[i]!=null) this.qlow[i]=new Double(x.getQlow()[i]);
			}
			if(x.getQhigh()!=null) {
				if(x.getQhigh()[i]!=null) this.qhigh[i]=new Double(x.getQhigh()[i]);		
			}
		}
	}
	
	/**
	 * full constructor
	 * @param x
	 * @param maxpost
	 * @param median
	 * @param qlow
	 * @param qhigh
	 */
	public Envelop(Double[] x, Double[] maxpost,Double[] median, Double[] qlow,Double[]qhigh) {
		super();
		if(x!=null){this.nx=x.length;} else {this.nx=0;}
		this.x = x;
		this.maxpost = maxpost;
		this.median = median;
		this.qlow = qlow;
		this.qhigh = qhigh;
	}
	
	/**
	 * full constructor using matrix format 
	 * @param mat
	 */
	public Envelop(Double[][] mat) {
		this(mat[0],mat[1],mat[2],mat[3],mat[4]);
	}
	
	public ChartPanel intervalPlot(String title,String xlab,String ylab,
			Color lineColor, Color bkgColor, Color gridColor,boolean ylog){
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries map = new XYSeries("maxpost");
		XYSeries low = new XYSeries("qlow");
		XYSeries high = new XYSeries("qhigh");
		for (int i=0;i<this.getNx();i++){
			if(ylog & this.getMaxpost()[i]<=0.0){map.add(this.getX()[i],Constants.D_TINY);} else {map.add(this.getX()[i],this.getMaxpost()[i]);}
			if(ylog & this.getQlow()[i]<=0.0){low.add(this.getX()[i],Constants.D_TINY);} else {low.add(this.getX()[i],this.getQlow()[i]);}
			if(ylog & this.getQhigh()[i]<=0.0){high.add(this.getX()[i],Constants.D_TINY);} else {high.add(this.getX()[i],this.getQhigh()[i]);}		
			}
		dataset.addSeries(map);dataset.addSeries(low);dataset.addSeries(high);
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
		BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,1.0f, new float[] {6.0f, 6.0f}, 0.0f);
		BasicStroke solid = new BasicStroke(3.0f);
		final NumberAxis logaxis = new LogarithmicAxis(ylab);
		if(ylog){chart.getXYPlot().setRangeAxis(logaxis);}

		// Lines
		renderer.setSeriesPaint(0,lineColor);
		renderer.setSeriesPaint(1, lineColor);
		renderer.setSeriesPaint(2, lineColor);
		renderer.setSeriesStroke(0, solid);
		renderer.setSeriesStroke(1, dashed);
		renderer.setSeriesStroke(2, dashed);

		// Background
		chart.setBackgroundPaint(bkgColor);
		chart.getPlot().setBackgroundPaint(bkgColor);
		chart.getXYPlot().setRangeGridlinePaint(gridColor);
		chart.getXYPlot().setDomainGridlinePaint(gridColor);

		ChartPanel CP = new ChartPanel(chart);
		return(CP);
		}

	public ChartPanel intervalPlot(String title,String xlab,String ylab,boolean ylog){
		return 	intervalPlot(title,xlab,ylab,Color.BLACK,Color.WHITE, Color.GRAY,ylog);
	}

	public ChartPanel plot(String title,String xlab,String ylab,
			Color lineColor,Color fillColor, float alpha,Color bkgColor, Color gridColor,boolean ylog){
		JFreeChart chart=Plots.BandPlot(this.getX(),this.getMaxpost(),this.getQlow(),this.getQhigh(),title,xlab,ylab,
				lineColor,fillColor,alpha,bkgColor,gridColor,ylog);
		ChartPanel CP = new ChartPanel(chart);
		return(CP);		
	}
	
	public ChartPanel plot(String title,String xlab,String ylab,boolean ylog){
		return 	plot(title,xlab,ylab,Color.BLACK,Color.BLACK,1.0f,Color.WHITE, Color.GRAY,ylog);
	}
	
	public Double[][] toMatrix(){
		Double[][] out = new Double[5][this.nx];
		out[0]=this.x;
		out[1]=this.maxpost;
		out[2]=this.median;
		out[3]=this.qlow;
		out[4]=this.qhigh;
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

	public Double[] getX() {
		return x;
	}

	public void setX(Double[] x) {
		this.x = x;
	}

	public Double[] getMaxpost() {
		return maxpost;
	}

	public void setMaxpost(Double[] maxpost) {
		this.maxpost = maxpost;
	}

	public Double[] getMedian() {
		return median;
	}

	public void setMedian(Double[] median) {
		this.median = median;
	}

	public Double[] getQlow() {
		return qlow;
	}

	public void setQlow(Double[] qlow) {
		this.qlow = qlow;
	}

	public Double[] getQhigh() {
		return qhigh;
	}

	public void setQhigh(Double[] qhigh) {
		this.qhigh = qhigh;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(maxpost);
		result = prime * result + Arrays.hashCode(median);
		result = prime * result + nx;
		result = prime * result + Arrays.hashCode(qhigh);
		result = prime * result + Arrays.hashCode(qlow);
		result = prime * result + Arrays.hashCode(x);
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
		Envelop other = (Envelop) obj;
		if (!Arrays.equals(maxpost, other.maxpost))
			return false;
		if (!Arrays.equals(median, other.median))
			return false;
		if (nx != other.nx)
			return false;
		if (!Arrays.equals(qhigh, other.qhigh))
			return false;
		if (!Arrays.equals(qlow, other.qlow))
			return false;
		if (!Arrays.equals(x, other.x))
			return false;
		return true;
	}
	
}

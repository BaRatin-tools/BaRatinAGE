package moteur;

import java.util.ArrayList;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

import commons.Plots;

import Utils.Defaults;

public class ConfigHydrau extends Item {
	//classe regroupant une BonnifaitMatrix et les HydrauControl associés

	private BonnifaitMatrix matrix;
	private ArrayList<HydrauControl> controls;
	private PriorRatingCurveOptions priorRCoptions;
	private Envelop priorEnv;
	private Spaghetti priorSpag;

	public ConfigHydrau(){
	}

	/**
	 * Constructor by copy
	 * @param x copied object
	 */
	public ConfigHydrau(ConfigHydrau x){
		super(x);
		if(x==null){return;}
		if(x.getMatrix()!=null){
			this.matrix=new BonnifaitMatrix(x.getMatrix());
		}
		if(x.getControls()!=null){
			this.controls=new ArrayList<HydrauControl>();
			for(int i=0;i<x.getControls().size();i++){
				if(x.getControls().get(i)!=null)
					this.controls.add(new HydrauControl(x.getControls().get(i)));
			}
		}
		if(x.getPriorRCoptions()!=null){
			this.priorRCoptions=new PriorRatingCurveOptions(x.getPriorRCoptions());
		}
		if(x.getPriorEnv()!=null){
			this.priorEnv=new Envelop(x.getPriorEnv());
		}
		if(x.getPriorSpag()!=null){
			this.priorSpag=new Spaghetti(x.getPriorSpag());
		}
	}

	/**
	 * Constructor by copy of an Item object - useful for downcasting
	 * @param item
	 */
	public ConfigHydrau(Item item){
		this.setName(item.getName());
		this.setDescription(item.getDescription());
	}

	public ConfigHydrau(String name){
		super(name);
	}

	@Override
	public String toString(){
		String out="";
		if(this.controls.size()<1){return out;}
		// first control: a k c
		out=out+this.controls.get(0).toString_akc();
		out=out+System.getProperty("line.separator");
		// next controls:k a c
		for(int i=1;i<this.controls.size();i++){
			out=out+this.controls.get(i).toString_kac();
			if(i+1<this.controls.size()){out=out+System.getProperty("line.separator");}
		}
		return out;}

	public ChartPanel plot(String title,String xlab,String ylab,boolean ylog){
		ChartPanel chart = this.getPriorEnv().plot(title,xlab,ylab,
				Defaults.plot_lineColor,Defaults.plot_priorColor_light,1.0f,Defaults.plot_bkgColor,Defaults.plot_gridColor,ylog);
		int ncontrol = this.getControls().size();
		ChartPanel[] chart_k=new ChartPanel[ncontrol];
		JFreeChart[] line_k=new JFreeChart[ncontrol];
		for(int i=0;i<ncontrol;i++){
			Double[] k = this.getControls().get(i).getK().getPrior().getParval();
			double low = k[0]-2*k[1];
			double high = k[0]+2*k[1];
			double maxi = chart.getChart().getXYPlot().getRangeAxis().getUpperBound();
			Envelop env = new Envelop(new Double[] {low,high},new Double[] {0.0,0.0},new Double[] {0.0,0.0},new Double[] {0.0,0.0},new Double[] {maxi,maxi});
			chart_k[i] = env.plot(title,xlab,ylab,
					Defaults.plot_kColor_light,Defaults.plot_kColor,0.5f,Defaults.plot_bkgColor,Defaults.plot_gridColor,ylog);
			line_k[i]=Plots.LinePlot(new Double[] {k[0],k[0]},new Double[] {0.0,maxi},title,xlab,ylab,
					Defaults.plot_kColor,Defaults.plot_bkgColor,Defaults.plot_gridColor,ylog);
		}
		// create common plotting domain
		ValueAxis domain = new NumberAxis("Domain");
		ValueAxis range = new NumberAxis("Range");
		// Extract datasets
		XYDataset d0 = chart.getChart().getXYPlot().getDataset(0);
		XYDataset[] d=new XYDataset[ncontrol];
		XYDataset[] dline=new XYDataset[ncontrol];
		for(int i=0;i<ncontrol;i++){
			d[i]=chart_k[i].getChart().getXYPlot().getDataset(0);
			dline[i]=line_k[i].getXYPlot().getDataset(0);
		}
		// Extract renderers
		XYItemRenderer r0 = chart.getChart().getXYPlot().getRenderer(0);
		XYItemRenderer[] r=new XYItemRenderer[ncontrol];
		XYItemRenderer[] rline=new XYItemRenderer[ncontrol];
		for(int i=0;i<ncontrol;i++){
			r[i]=chart_k[i].getChart().getXYPlot().getRenderer(0);
			rline[i]=line_k[i].getXYPlot().getRenderer(0);
		}
		// Make a new plot and assign datasets		
		XYPlot plot = new XYPlot();
		plot.setDataset(0, d0);
		for(int i=0;i<ncontrol;i++){
			plot.setDataset(1+2*i, d[i]);
			plot.setDataset(1+2*i+1, dline[i]);
		}		
		plot.setRenderer(0, r0);
		for(int i=0;i<ncontrol;i++){
			plot.setRenderer(1+2*i, r[i]);
			plot.setRenderer(1+2*i+1, rline[i]);
		}		
		// assign domain/range
		plot.setDomainAxis(0, domain);
		plot.setRangeAxis(0, range);
		plot.getDomainAxis().setLabel(xlab);
		plot.getRangeAxis().setLabel(ylab);
		plot.mapDatasetToDomainAxis(0,0);
		plot.mapDatasetToRangeAxis(0,0);
		for(int i=0;i<ncontrol;i++){
			plot.mapDatasetToDomainAxis(1+2*i,0);
			plot.mapDatasetToRangeAxis(1+2*i,0);
			plot.mapDatasetToDomainAxis(1+2*i+1,0);
			plot.mapDatasetToRangeAxis(1+2*i+1,0);
		}		

		// Create the chart with all plots 
		JFreeChart j = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		j.removeLegend();
		j.setTitle(title);
		j.setBackgroundPaint(Defaults.bkgColor);
		j.getPlot().setBackgroundPaint(Defaults.bkgColor);
		plot.setRangeGridlinePaint(Defaults.plot_gridColor);
		plot.setDomainGridlinePaint(Defaults.plot_gridColor);

		final NumberAxis logaxis = new LogarithmicAxis(ylab);
		if(ylog){j.getXYPlot().setRangeAxis(logaxis);}		
		ChartPanel CP = new ChartPanel(j);
		return(CP);	
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ConfigHydrau)) {
			return false;
		}
		ConfigHydrau other = (ConfigHydrau) obj;
		if (controls == null) {
			if (other.controls != null) {
				return false;
			}
		} else if (!controls.equals(other.controls)) {
			return false;
		}
		if (matrix == null) {
			if (other.matrix != null) {
				return false;
			}
		} else if (!matrix.equals(other.matrix)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((controls == null) ? 0 : controls.hashCode());
		result = prime * result + ((matrix == null) ? 0 : matrix.hashCode());
		return result;
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	/**
	 * @return the matrix
	 */
	public BonnifaitMatrix getMatrix() {
		return matrix;
	}

	/**
	 * @param matrix the matrix to set
	 */
	public void setMatrix(BonnifaitMatrix matrix) {
		this.matrix = matrix;
	}

	/**
	 * @return the controls
	 */
	public ArrayList<HydrauControl> getControls() {
		return controls;
	}

	/**
	 * @param controls the controls to set
	 */
	public void setControls(ArrayList<HydrauControl> controls) {
		this.controls = controls;
	}

	public void setControlAt(int indx,HydrauControl control) {
		this.controls.set(indx, control);
	}

	/**
	 * @return the priorRCoptions
	 */
	public PriorRatingCurveOptions getPriorRCoptions() {
		return priorRCoptions;
	}

	/**
	 * @param priorRCoptions the priorRCoptions to set
	 */
	public void setPriorRCoptions(PriorRatingCurveOptions priorRCoptions) {
		this.priorRCoptions = priorRCoptions;
	}

	public Envelop getPriorEnv() {
		return priorEnv;
	}

	public void setPriorEnv(Envelop priorEnv) {
		this.priorEnv = priorEnv;
	}

	public Spaghetti getPriorSpag() {
		return priorSpag;
	}

	public void setPriorSpag(Spaghetti priorSpag) {
		this.priorSpag = priorSpag;
	}

}

package moteur;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

import Utils.Defaults;
import commons.Constants;
import commons.Observation;
import commons.Time;
import commons.TimeSerie;

/**
 * Hydrograph object
 * @author Ben Renard, Irstea Lyon
 */
public class Hydrograph extends TimeSerie{
	
	private String limni_id;
	private String rc_id;
	private Envelop env_total;
	private Envelop env_hparam;
	private Envelop env_h;
	private Spaghetti spag_total;
	private Spaghetti spag_hparam;
	private Spaghetti spag_h;

	/**
	 * partial constructor
	 * @param name
	 * @param description
	 * @param type
	 * @param observations
	 */
	public Hydrograph(String name,String description,String type,List<Observation> observations){
		super(name,description,type,observations);
	}
	
	/**
	 * partial constructor
	 * @param name
	 * @param description
	 * @param observations
	 */
	public Hydrograph(String name,String description,List<Observation> observations){
		this(name,description,"",observations);
	}
	
	/**
	 * partial constructor
	 * @param name
	 */
	public Hydrograph(String name){
		super(name);
	}
	
	/**
	 * partial constructor
	 */
	public Hydrograph(){
		super();
	}
	
	/**
	 * copy constructor
	 * @param x copied object
	 */
	public Hydrograph(Hydrograph x) {
		super(x);
		if(x==null){return;}
		if(x.getLimni_id()!=null) this.limni_id = new String(x.getLimni_id());
		if(x.getRc_id()!=null) this.rc_id =new String(x.getRc_id());
		if(x.getEnv_total()!=null) this.env_total = new Envelop(x.getEnv_total());
		if(x.getEnv_hparam()!=null) this.env_hparam = new Envelop(x.getEnv_hparam());
		if(x.getEnv_h()!=null) this.env_h = new Envelop(x.getEnv_h());
		if(x.getSpag_total()!=null) this.spag_total = new Spaghetti(x.getSpag_total());
		if(x.getSpag_hparam()!=null) this.spag_hparam =new Spaghetti(x.getSpag_hparam());
		if(x.getSpag_h()!=null) this.spag_h = new Spaghetti(x.getSpag_h());
	}

	public ChartPanel plot(String title,String xlab,String ylab,boolean ylog){
		// Create individual charts
		ChartPanel chart_total = this.getEnv_total().plot(title,xlab,ylab,
				Defaults.plot_lineColor,Defaults.plot_postColor,1.0f,Defaults.plot_bkgColor,Defaults.plot_gridColor,ylog);
		ChartPanel chart_param = this.getEnv_hparam().plot(title,xlab,ylab,
				Defaults.plot_lineColor,Defaults.plot_postColor_light,1.0f,Defaults.plot_bkgColor,Defaults.plot_gridColor,ylog);
		ChartPanel chart_h = this.getEnv_h().plot(title,xlab,ylab,
				Defaults.plot_lineColor,Defaults.plot_stageColor,1.0f,Defaults.plot_bkgColor,Defaults.plot_gridColor,ylog);
		
		// create common plotting domain
		NumberAxis domain = new NumberAxis("Domain");
		NumberAxis range = new NumberAxis("Range");
		domain.setLabel(xlab);
		range.setLabel(ylab);
		domain.setAutoRangeIncludesZero(false);
		range.setRangeWithMargins(
				Collections.min(Arrays.asList(this.getEnv_total().getQlow())),
				Collections.max(Arrays.asList(this.getEnv_total().getQhigh())));
		// Extract datasets
		XYDataset d2 = chart_total.getChart().getXYPlot().getDataset(0);
		XYDataset d1 = chart_param.getChart().getXYPlot().getDataset(0);
		XYDataset d0 = chart_h.getChart().getXYPlot().getDataset(0);
		// Extract renderers
		XYItemRenderer r2 = chart_total.getChart().getXYPlot().getRenderer(0);
		XYItemRenderer r1 = chart_param.getChart().getXYPlot().getRenderer(0);
		XYItemRenderer r0 = chart_h.getChart().getXYPlot().getRenderer(0);
		// Make a new plot and assign datasets		
		XYPlot plot = new XYPlot();
		plot.setDataset(0, d0);
		plot.setDataset(1, d1);
		plot.setDataset(2, d2);
		plot.setRenderer(0, r0);
		plot.setRenderer(1, r1);
		plot.setRenderer(2, r2);
		// assign domain/range
		plot.setDomainAxis(0, domain);
		plot.setRangeAxis(0, range);
		plot.mapDatasetToDomainAxis(0,0);
		plot.mapDatasetToRangeAxis(0,0);
		plot.mapDatasetToDomainAxis(1,0);
		plot.mapDatasetToRangeAxis(1,0);
		plot.mapDatasetToDomainAxis(2,0);
		plot.mapDatasetToRangeAxis(2,0);
		
		// Create the chart with all plots 
		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.removeLegend();
		chart.setTitle(title);
		chart.setBackgroundPaint(Defaults.bkgColor);
		chart.getPlot().setBackgroundPaint(Defaults.bkgColor);
		plot.setRangeGridlinePaint(Defaults.plot_gridColor);
		plot.setDomainGridlinePaint(Defaults.plot_gridColor);
		
		final NumberAxis logaxis = new LogarithmicAxis(ylab);
		if(ylog){chart.getXYPlot().setRangeAxis(logaxis);}		
		ChartPanel CP = new ChartPanel(chart);
		return(CP);
	}

	public Double[][] toMatrix(){
		int n=this.getObservations().size();
		Double[][] out = new Double[8][n];
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
			if(o.getQualityCode()!=null) {out[7][i]=o.getQualityCode()*1.0;} else {out[7][i]=Constants.D_MISSING;}
		}
		return(out);
	}
	
	public void fromMatrix(Double[][] m){
		int n=m[0].length;
		ArrayList<Observation> hydro = new ArrayList<Observation>();
		for(int i=0;i<n;i++){
			Observation o = new Observation();
			o.setObsDate(new Time(m[0][i].intValue(),m[1][i].intValue(),m[2][i].intValue(),m[3][i].intValue(),m[4][i].intValue(),m[5][i].intValue()));
			o.setValue(m[6][i]);
			o.setQualityCode(m[7][i].intValue());
			hydro.add(o);
		}
		this.setObservations(hydro);
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getLimni_id() {
		return limni_id;
	}

	public void setLimni_id(String limni_id) {
		this.limni_id = limni_id;
	}

	public String getRc_id() {
		return rc_id;
	}

	public void setRc_id(String rc_id) {
		this.rc_id = rc_id;
	}

	public Envelop getEnv_total() {
		return env_total;
	}

	public void setEnv_total(Envelop env_total) {
		this.env_total = env_total;
	}

	public Envelop getEnv_hparam() {
		return env_hparam;
	}

	public void setEnv_hparam(Envelop env_hparam) {
		this.env_hparam = env_hparam;
	}

	public Envelop getEnv_h() {
		return env_h;
	}

	public void setEnv_h(Envelop env_h) {
		this.env_h = env_h;
	}

	public Spaghetti getSpag_total() {
		return spag_total;
	}

	public void setSpag_total(Spaghetti spag_total) {
		this.spag_total = spag_total;
	}

	public Spaghetti getSpag_hparam() {
		return spag_hparam;
	}

	public void setSpag_hparam(Spaghetti spag_hparam) {
		this.spag_hparam = spag_hparam;
	}

	public Spaghetti getSpag_h() {
		return spag_h;
	}

	public void setSpag_h(Spaghetti spag_h) {
		this.spag_h = spag_h;
	}
	
}

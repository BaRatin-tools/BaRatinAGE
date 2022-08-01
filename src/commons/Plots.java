package commons;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import Utils.Defaults;

/**
 * Utilities for basic plots
 * @author Kevin Mokili & Ben Renard
 */
public class Plots{

	public Plots(){}

	public static JFreeChart LinePlot(Double[] x,Double[] y,String title, String xlab, String ylab,Color lineColor, Color bkgColor, Color gridColor,boolean ylog){
		// create XYDataset
		int n = x.length;
		XYSeries s = new XYSeries("data");
		for(int i=0;i<n;i++){
			if(ylog & y[i]<=0){s.add(x[i],Constants.D_TINY);} else {s.add(x[i],y[i]);}
		}
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(s);
		// do chart
		JFreeChart chart = ChartFactory.createXYLineChart(title,xlab,ylab,dataset, PlotOrientation.VERTICAL, false, true, false);
		// apply options
		chart.setTitle(title);
		chart.removeLegend();
		chart.setBackgroundPaint(bkgColor);
		chart.getXYPlot().getRenderer().setSeriesPaint(0,lineColor);
		chart.getPlot().setBackgroundPaint(bkgColor);
		chart.getXYPlot().setRangeGridlinePaint(gridColor);
		chart.getXYPlot().setDomainGridlinePaint(gridColor);
		final NumberAxis logaxis = new LogarithmicAxis(ylab);
		if(ylog){chart.getXYPlot().setRangeAxis(logaxis);}	
		((NumberAxis)chart.getXYPlot().getRangeAxis()).setAutoRangeIncludesZero(false);
		return chart;
	}

	public static JFreeChart TimeSeriesPlot(TimeSerie z,String title, String xlab, String ylab,
			Color lineColor, Color bkgColor, Color gridColor,boolean ylog){
		// Convert to continuous time
		int n = z.length();
		Double[] x=new Double[n];
		Double[] y=new Double[n];
		Observation obs;
		for(int i=0;i<n;i++){
			obs=z.getObservations().get(i);
			x[i]=obs.getObsDate().toYear();
			if(ylog & obs.getValue()<=0) {y[i]=Constants.D_TINY;} else {y[i]=obs.getValue();}
		}
		// Do plot
		return LinePlot(x,y,title,xlab,ylab,lineColor,bkgColor,gridColor,ylog);
	}

	public static JFreeChart BandPlot(Double[] x,Double[] y,Double[] ylow,Double[] yhigh,String title, String xlab, String ylab,
			Color lineColor,Color fillColor, float alpha,Color bkgColor, Color gridColor,boolean ylog){
		// create YIntervalSeries
		int n=x.length;
		YIntervalSeries series = new YIntervalSeries("Series");
		for(int i=0;i<n;i++){
			if(ylog & yhigh[i]<=0){series.add(x[i],Constants.D_TINY,Constants.D_TINY,Constants.D_TINY);}
			else if(ylog & y[i]<=0){series.add(x[i],Constants.D_TINY,Constants.D_TINY,yhigh[i]);}
			else if(ylog & ylow[i]<=0){series.add(x[i],y[i],Constants.D_TINY,yhigh[i]);}
			else {series.add(x[i],y[i],ylow[i],yhigh[i]);}
		}
		YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
		dataset.addSeries(series);
		// do area chart
		JFreeChart chart = ChartFactory.createXYLineChart(
				title,      // chart title
				xlab,                      // x axis label
				ylab,                      // y axis label
				dataset,                  // data
				PlotOrientation.VERTICAL,
				true,                     // include legend
				true,                     // tooltips
				false                     // urls
				);

		XYPlot plot = (XYPlot) chart.getPlot();
		DeviationRenderer renderer = new DeviationRenderer(true, false,alpha);
		renderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		renderer.setSeriesStroke(0, new BasicStroke(3.0f));
		renderer.setSeriesFillPaint(0, fillColor);
		renderer.setSeriesPaint(0, lineColor);
		plot.setRenderer(renderer);

		chart.removeLegend();
		chart.setBackgroundPaint(bkgColor);
		chart.getPlot().setBackgroundPaint(bkgColor);
		plot.setRangeGridlinePaint(gridColor);
		plot.setDomainGridlinePaint(gridColor);
		final NumberAxis logaxis = new LogarithmicAxis(ylab);
		if(ylog){chart.getXYPlot().setRangeAxis(logaxis);}		

		return chart;
	}

	/**
	 * Overlaid histogram + gaussian pdf
	 * @param mean
	 * @param sd
	 * @param sim
	 * @param title
	 * @param xlab
	 * @param ylab
	 * @param pdfColor
	 * @param histColor
	 * @param bkgColor
	 * @param gridColor
	 * @return
	 */
	public static JFreeChart gaussHistPlot(Double mean, Double sd, Double[] sim,String title, String xlab, String ylab,
			Color pdfColor,Color histColor, Color bkgColor, Color gridColor){
		// Gaussian pdf
		int nx=100;
		Double low=mean-3.0*sd;
		Double step=6.0*sd/(nx-1);
		Double[] x=new Double[nx];
		Double[] y=new Double[nx];
		XYSeries s = new XYSeries("pdf");
		for(int i=0;i<nx;i++){
			x[i]=low+i*step;
			y[i]=Math.exp(-0.5*( Math.pow(x[i]-mean,2)/Math.pow(sd,2) ))/(Math.sqrt(2.0*Math.PI)*sd);
			s.add(x[i],y[i]);
		}
		XYSeriesCollection pdf = new XYSeriesCollection();
		pdf.addSeries(s);
		JFreeChart chart_pdf = ChartFactory.createXYLineChart(title,xlab,ylab,pdf, PlotOrientation.VERTICAL, false, true, false);
		chart_pdf.getXYPlot().getRenderer().setSeriesPaint(0,pdfColor);
		// Histogram
		int nbins=50;
		HistogramDataset histo = new HistogramDataset();
		histo.setType(HistogramType.SCALE_AREA_TO_1);
		double[] foo=new double[sim.length];
		for(int i=0;i<sim.length;i++){foo[i]=sim[i];}
        histo.addSeries("histogram",foo,nbins);
        JFreeChart chart_histo = ChartFactory.createHistogram(title, xlab,ylab,histo, PlotOrientation.VERTICAL, true, true, false);
        XYBarRenderer rend = (XYBarRenderer)chart_histo.getXYPlot().getRenderer();
        rend.setDrawBarOutline(false);
        chart_histo.getXYPlot().getRenderer().setSeriesPaint(0, histColor);
		// create common plotting domain
		ValueAxis domain = new NumberAxis("Domain");
		ValueAxis range = new NumberAxis("Range");
		((NumberAxis) domain).setAutoRangeIncludesZero(false);
		// Extract datasets
		XYDataset d1 = chart_histo.getXYPlot().getDataset(0);
		XYDataset d0 = chart_pdf.getXYPlot().getDataset(0);
		// Extract renderers
		XYItemRenderer r1 = chart_histo.getXYPlot().getRenderer(0);
		XYItemRenderer r0 = chart_pdf.getXYPlot().getRenderer(0);
		// Make a new plot and assign datasets		
		XYPlot plot = new XYPlot();
		plot.setDataset(0, d0);
		plot.setDataset(1, d1);	
		plot.setRenderer(0, r0);
		plot.setRenderer(1, r1);
		plot.getRenderer(0).setSeriesStroke(0, new BasicStroke(3.0f));
		// assign domain/range
		plot.setDomainAxis(0, domain);
		plot.setRangeAxis(0, range);
		plot.getDomainAxis().setLabel(xlab);
		plot.getRangeAxis().setLabel(ylab);
		plot.mapDatasetToDomainAxis(0,0);
		plot.mapDatasetToRangeAxis(0,0);
		plot.mapDatasetToDomainAxis(1,0);
		plot.mapDatasetToRangeAxis(1,0);
		// Create the chart with all plots 
		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.removeLegend();
		chart.setTitle(title);
		chart.setBackgroundPaint(bkgColor);
		chart.getPlot().setBackgroundPaint(Defaults.bkgColor);
		plot.setRangeGridlinePaint(Defaults.plot_gridColor);
		plot.setDomainGridlinePaint(Defaults.plot_gridColor);
		return chart;
	}
}

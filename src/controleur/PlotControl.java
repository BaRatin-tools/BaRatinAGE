package controleur;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

import commons.Distribution;
import commons.GridBag_Label;
import commons.GridBag_Layout;
import commons.GridBag_TextArea_Titled;
import commons.GridBag_TextField_Titled;
import commons.GridBag_Text_Titled;
import commons.Parameter;
import commons.Plots;
import vue.Combo_ControlType;
import vue.ConfigHydrauPanel;
import vue.HydrographPanel;
import vue.RatingCurvePanel;
import moteur.ConfigHydrau;
import moteur.Envelop;
import moteur.HydrauControl;
import moteur.Hydrograph;
import moteur.RatingCurve;
import moteur.Station;
import Utils.Config;
import Utils.Defaults;
import Utils.Dico;

/**
 * Controller for specialized plots
 * @author Ben Renard, Irstea Lyon
 *
 */
public class PlotControl {
	private static PlotControl instance;

	public static synchronized PlotControl getInstance(){
		if (instance == null){
			instance = new PlotControl();
		}
		return instance;
	}

	// locals
	private Station station=Station.getInstance();
	private Config config=Config.getInstance();
	private Dico dico=Dico.getInstance(config.getLanguage());

	private PlotControl(){}

	public JPanel priorVsPost_par(RatingCurvePanel panel){
		JPanel panout = new JPanel();
		panout.setBackground(Defaults.bkgColor);
		RatingCurve rc=station.getRatingCurve(panel.getId().getText());
		ConfigHydrau h=station.getHydrauConfig(rc.getHydrau_id());
		if(rc.getMcmc()==null){return(null);}
		int ncontrol=h.getControls().size();
		int[] sizeRow=new int[ncontrol];Arrays.fill(sizeRow, 0);
		int[] sizeCol=new int[4];Arrays.fill(sizeCol, 0);
		double[] weightRow=new double[ncontrol];Arrays.fill(weightRow, 1.0);
		double[] weightCol=new double[4];Arrays.fill(weightCol, 1.0);
		GridBag_Layout.SetGrid(panout, sizeRow, sizeCol, weightRow, weightCol);
		// loop on all controls
		int ai,ci,ki,bi;
		Distribution gauss;
		JFreeChart chart;
		ChartPanel CP;
		Double[][] mcmc= rc.getMcmc_cooked();
		// b0 is the last column before derived b's
		int b0 = mcmc.length-ncontrol;
		for(int i=0;i<ncontrol;i++){
			if(i==0){ai=0;ki=1;ci=2;bi=1;} else {ki=3*i;ai=3*i+1;ci=3*i+2;bi=b0+i;}
			// K
			gauss = h.getControls().get(i).getK().getPrior();
			chart=Plots.gaussHistPlot(gauss.getParval()[0],gauss.getParval()[1],mcmc[ki],
					"k - "+dico.entry("Control")+" "+(i+1), "k", dico.entry("Density"),
					Defaults.plot_priorColor,Defaults.plot_postColor, Defaults.plot_bkgColor, Defaults.plot_gridColor);
			CP = new ChartPanel(chart);
			GridBag_Layout.putIntoGrid(CP, panout,0,i,1,1,true,true);
			// A
			gauss = h.getControls().get(i).getA().getPrior();
			chart=Plots.gaussHistPlot(gauss.getParval()[0],gauss.getParval()[1],mcmc[ai],
					"a - "+dico.entry("Control")+" "+(i+1), "a", dico.entry("Density"),
					Defaults.plot_priorColor,Defaults.plot_postColor, Defaults.plot_bkgColor, Defaults.plot_gridColor);
			CP = new ChartPanel(chart);
			GridBag_Layout.putIntoGrid(CP, panout,1,i,1,1,true,true);
			// C
			gauss = h.getControls().get(i).getC().getPrior();
			chart=Plots.gaussHistPlot(gauss.getParval()[0],gauss.getParval()[1],mcmc[ci],
					"c - "+dico.entry("Control")+" "+(i+1), "c", dico.entry("Density"),
					Defaults.plot_priorColor,Defaults.plot_postColor, Defaults.plot_bkgColor, Defaults.plot_gridColor);
			CP = new ChartPanel(chart);
			GridBag_Layout.putIntoGrid(CP, panout,2,i,1,1,true,true);
			// B
			chart=Plots.histPlot(mcmc[bi],"b - "+dico.entry("Control")+" "+(i+1), "b", dico.entry("Density"),
					Defaults.plot_postColor, Defaults.plot_bkgColor, Defaults.plot_gridColor);
			CP = new ChartPanel(chart);
			GridBag_Layout.putIntoGrid(CP, panout,3,i,1,1,true,true);
		}
		return(panout);
	} 

	public JPanel priorVsPost_parTable(RatingCurvePanel panel){
		// Format of numbers in tabkle
		DecimalFormat fmt = new DecimalFormat("### ### ###.###");	
		// returning panel
		JPanel panout = new JPanel();
		panout.setBackground(Defaults.bkgColor);
		// get objects
		RatingCurve rc=station.getRatingCurve(panel.getId().getText());
		ConfigHydrau h=station.getHydrauConfig(rc.getHydrau_id());
		if(rc.getMcmc()==null){return(null);}
		int ncontrol=h.getControls().size();
		int[] sizeRow=new int[ncontrol+1];Arrays.fill(sizeRow, 0);
		int[] sizeCol=new int[9];Arrays.fill(sizeCol, 0);
		double[] weightRow=new double[ncontrol+1];Arrays.fill(weightRow, 0.0);
		double[] weightCol=new double[9];Arrays.fill(weightCol, 1.0);
		GridBag_Layout.SetGrid(panout, sizeRow, sizeCol, weightRow, weightCol);
		// headers
		new GridBag_Label(panout,dico.entry("kpar"),config.getFontBigLbl(),Defaults.txtColor,SwingConstants.CENTER,1,0,2,1,true,true);
		new GridBag_Label(panout,dico.entry("apar"),config.getFontBigLbl(),Defaults.txtColor,SwingConstants.CENTER,3,0,2,1,true,true);
		new GridBag_Label(panout,dico.entry("cpar"),config.getFontBigLbl(),Defaults.txtColor,SwingConstants.CENTER,5,0,2,1,true,true);
		new GridBag_Label(panout,dico.entry("bpar"),config.getFontBigLbl(),Defaults.txtColor,SwingConstants.CENTER,7,0,2,1,true,true);
		// loop on all controls
		int ai,ci,ki,bi;
		Distribution gauss;
		String priortxt,posttxt;
		Double[][] mcmc= rc.getMcmc_summary();
		// b0 is the last column before derived b's
		int b0 = mcmc.length-ncontrol;
		int x;
		for(int i=0;i<ncontrol;i++){
			if(i==0){ai=0;ki=1;ci=2;bi=1;} else {ki=3*i;ai=3*i+1;ci=3*i+2;bi=b0+i;}
			x=0;
			// rower
			new GridBag_Label(panout,dico.entry("Control")+" "+(i+1),config.getFontBigLbl(),Defaults.txtColor,SwingConstants.CENTER,x,i+1,1,1,true,true);
			// K
			gauss = h.getControls().get(i).getK().getPrior();
			//TODO: replace +/- 2*sdev by a proper 95% interval - need to modify BaRatin for that
			priortxt=fmt.format(gauss.getParval()[0]) + " +/- " + fmt.format(2*gauss.getParval()[1]);
			posttxt=fmt.format(mcmc[ki][15])+" +/- "+ fmt.format(2*mcmc[ki][10]);
			x=x+1;
			new GridBag_Text_Titled(panout,priortxt,dico.entry("Prior"),config.getFontTxt(),config.getFontLbl(),Color.BLUE,Color.BLUE,x,i+1,1,1);
			x=x+1;
			new GridBag_Text_Titled(panout,posttxt,dico.entry("Posterior"),config.getFontTxt(),config.getFontLbl(),Color.RED,Color.RED,x,i+1,1,1);
			// A
			gauss = h.getControls().get(i).getA().getPrior();
			priortxt=fmt.format(gauss.getParval()[0]) + " +/- " + fmt.format(2*gauss.getParval()[1]);
			posttxt=fmt.format(mcmc[ai][15]) + " +/- " + fmt.format(2*mcmc[ai][10]);
			x=x+1;
			new GridBag_Text_Titled(panout,priortxt,dico.entry("Prior"),config.getFontTxt(),config.getFontLbl(),Color.BLUE,Color.BLUE,x,i+1,1,1);
			x=x+1;
			new GridBag_Text_Titled(panout,posttxt,dico.entry("Posterior"),config.getFontTxt(),config.getFontLbl(),Color.RED,Color.RED,x,i+1,1,1);
			// C
			gauss = h.getControls().get(i).getC().getPrior();
			priortxt=fmt.format(gauss.getParval()[0]) + " +/- " + fmt.format(2*gauss.getParval()[1]);
			posttxt=fmt.format(mcmc[ci][15]) + " +/- " + fmt.format(2*mcmc[ci][10]);
			x=x+1;
			new GridBag_Text_Titled(panout,priortxt,dico.entry("Prior"),config.getFontTxt(),config.getFontLbl(),Color.BLUE,Color.BLUE,x,i+1,1,1);
			x=x+1;
			new GridBag_Text_Titled(panout,posttxt,dico.entry("Posterior"),config.getFontTxt(),config.getFontLbl(),Color.RED,Color.RED,x,i+1,1,1);
			// B
			priortxt="    ------    ";
			posttxt=fmt.format(mcmc[bi][15]) + " +/- " + fmt.format(2*mcmc[bi][10]);
			x=x+1;
			new GridBag_Text_Titled(panout,priortxt,dico.entry("Prior"),config.getFontTxt(),config.getFontLbl(),Color.BLUE,Color.BLUE,x,i+1,1,1);
			x=x+1;
			new GridBag_Text_Titled(panout,posttxt,dico.entry("Posterior"),config.getFontTxt(),config.getFontLbl(),Color.RED,Color.RED,x,i+1,1,1);
		}
		return(panout);
	} 

	public JPanel priorVsPost_RC(RatingCurvePanel panel){
		JPanel panout = new JPanel();
		panout.setBackground(Defaults.bkgColor);
		RatingCurve rc=station.getRatingCurve(panel.getId().getText());
		ConfigHydrau h=station.getHydrauConfig(rc.getHydrau_id());
		if(rc.getMcmc()==null){return(null);}
		int[] sizeRow=new int[1];Arrays.fill(sizeRow, 0);
		int[] sizeCol=new int[1];Arrays.fill(sizeCol, 0);
		double[] weightRow=new double[1];Arrays.fill(weightRow, 1.0);
		double[] weightCol=new double[1];Arrays.fill(weightCol, 1.0);
		GridBag_Layout.SetGrid(panout, sizeRow, sizeCol, weightRow, weightCol);
		Envelop post = rc.getEnv_param();
		Envelop prior = h.getPriorEnv();
		ChartPanel chart_post = post.plot("","","]",
				Defaults.plot_postColor,Defaults.plot_postColor_light,0.6f,Defaults.plot_bkgColor,Defaults.plot_gridColor,false);
		ChartPanel chart_prior = prior.plot("","","",
				Defaults.plot_priorColor,Defaults.plot_priorColor_light,0.6f,Defaults.plot_bkgColor,Defaults.plot_gridColor,false);
		// create common plotting domain
		ValueAxis domain = new NumberAxis("Domain");
		ValueAxis range = new NumberAxis("Range");
		// Extract datasets
		XYDataset d1 = chart_prior.getChart().getXYPlot().getDataset(0);
		XYDataset d0 = chart_post.getChart().getXYPlot().getDataset(0);
		// Extract renderers
		XYItemRenderer r1 = chart_prior.getChart().getXYPlot().getRenderer(0);
		XYItemRenderer r0 = chart_post.getChart().getXYPlot().getRenderer(0);
		// Make a new plot and assign datasets		
		XYPlot plot = new XYPlot();
		plot.setDataset(0, d0);
		plot.setDataset(1, d1);	
		plot.setRenderer(0, r0);
		plot.setRenderer(1, r1);
		// assign domain/range
		plot.setDomainAxis(0, domain);
		plot.setRangeAxis(0, range);
		plot.getDomainAxis().setLabel(dico.entry("Hunit"));
		plot.getRangeAxis().setLabel(dico.entry("Qunit"));
		plot.mapDatasetToDomainAxis(0,0);
		plot.mapDatasetToRangeAxis(0,0);
		plot.mapDatasetToDomainAxis(1,0);
		plot.mapDatasetToRangeAxis(1,0);
		// Create the chart with all plots 
		JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.removeLegend();
		chart.setTitle("");
		chart.getPlot().setBackgroundPaint(Defaults.bkgColor);
		plot.setRangeGridlinePaint(Defaults.plot_gridColor);
		plot.setDomainGridlinePaint(Defaults.plot_gridColor);
		ChartPanel CP = new ChartPanel(chart);
		GridBag_Layout.putIntoGrid(CP, panout,0,0,1,1,true,true);
		return(panout);
	} 

	public JPanel MCMCtraces(RatingCurvePanel panel){
		JPanel panout = new JPanel();
		panout.setBackground(Defaults.bkgColor);
		RatingCurve rc=station.getRatingCurve(panel.getId().getText());
		ConfigHydrau h=station.getHydrauConfig(rc.getHydrau_id());
		if(rc.getMcmc()==null){return(null);}
		int ncontrol=h.getControls().size();
		int[] sizeRow=new int[ncontrol];Arrays.fill(sizeRow, 0);
		int[] sizeCol=new int[4];Arrays.fill(sizeCol, 0);
		double[] weightRow=new double[ncontrol];Arrays.fill(weightRow, 1.0);
		double[] weightCol=new double[4];Arrays.fill(weightCol, 1.0);
		GridBag_Layout.SetGrid(panout, sizeRow, sizeCol, weightRow, weightCol);
		// loop on all controls
		int ai,ci,ki,bi;
		JFreeChart chart;
		ChartPanel CP;
		Double[] x,y;
		Double[][] mcmc= rc.getMcmc_cooked();
		// b0 is the last column before derived b's
		int b0 = mcmc.length-ncontrol;
		x=new Double[mcmc[0].length];
		for(int i=0;i<x.length;i++){x[i]=1.0*i;}
		for(int i=0;i<ncontrol;i++){
			if(i==0){ai=0;ki=1;ci=2;bi=1;} else {ki=3*i;ai=3*i+1;ci=3*i+2;bi=b0+i;}
			// K
			y = mcmc[ki];
			chart=Plots.LinePlot(x,y,"k - "+dico.entry("Control")+" "+(i+1), dico.entry("Iteration"),"k",
					Defaults.plot_lineColor, Defaults.plot_bkgColor, Defaults.plot_gridColor,false);
			CP = new ChartPanel(chart);
			GridBag_Layout.putIntoGrid(CP, panout,0,i,1,1,true,true);
			// A
			y = mcmc[ai];
			chart=Plots.LinePlot(x,y,"a - "+dico.entry("Control")+" "+(i+1), dico.entry("Iteration"),"a",
					Defaults.plot_lineColor, Defaults.plot_bkgColor, Defaults.plot_gridColor,false);
			CP = new ChartPanel(chart);
			GridBag_Layout.putIntoGrid(CP, panout,1,i,1,1,true,true);
			// C
			y = mcmc[ci];
			chart=Plots.LinePlot(x,y,"c - "+dico.entry("Control")+" "+(i+1), dico.entry("Iteration"),"c",
					Defaults.plot_lineColor, Defaults.plot_bkgColor, Defaults.plot_gridColor,false);
			CP = new ChartPanel(chart);
			GridBag_Layout.putIntoGrid(CP, panout,2,i,1,1,true,true);
			// B
			y = mcmc[bi];
			chart=Plots.LinePlot(x,y,"b - "+dico.entry("Control")+" "+(i+1), dico.entry("Iteration"),"b",
					Defaults.plot_lineColor, Defaults.plot_bkgColor, Defaults.plot_gridColor,false);
			CP = new ChartPanel(chart);
			GridBag_Layout.putIntoGrid(CP, panout,3,i,1,1,true,true);
		}
		return(panout);
	} 

	public JPanel remarks(RatingCurvePanel panel){
		JPanel panout = new JPanel();
		panout.setBackground(Defaults.bkgColor);
		RatingCurve rc=station.getRatingCurve(panel.getId().getText());
		ConfigHydrau h=station.getHydrauConfig(rc.getHydrau_id());
		int ncontrol=h.getControls().size();
		Double[][] mcmc= rc.getMcmc_cooked();
		Double[][] summary= rc.getMcmc_summary();
		int ai,ci,ki,bi;
		int b0 = mcmc.length-ncontrol;
		new Combo_ControlType();
		String[] ctypes = Combo_ControlType.getStringList();
		
		GridBag_Layout.SetGrid(panout, new int[]{0},new int[]{0},new double[]{1.0},new double[]{1.0});
		GridBag_TextArea_Titled txtArea = new GridBag_TextArea_Titled(panout,"",dico.entry("Remarks"),
				config.getFontTxt(),config.getFontLbl(),
				Defaults.txtColor,Defaults.lblColor,0,0,1,1);
		String txt;
		String sep="----------------------------------------------------------------------";
		txt= sep + System.lineSeparator();
		txtArea.append(txt);
		txt= dico.entry("PriorVsPost_check")+ System.lineSeparator();//dico.entry("Remarks")
		txtArea.append(txt);
		txt= sep + System.lineSeparator();
		txtArea.append(txt);
		txt= System.lineSeparator();
		txtArea.append(txt);
		txtArea.append(txt);
		txt= sep + System.lineSeparator();
		txtArea.append(txt);
		txt= dico.entry("hydraulicAssumptions")+ System.lineSeparator();
		txtArea.append(txt);
		txt= sep + System.lineSeparator();
		txtArea.append(txt);
		for(int i=0;i<ncontrol;i++){
			if(i==0){ai=0;ki=1;ci=2;bi=1;} else {ki=3*i;ai=3*i+1;ci=3*i+2;bi=b0+i;}
			HydrauControl con = h.getControls().get(i); String ctype =
			ctypes[con.getType()]; txt="C"+(i+1)+": "+dico.entry(ctype)+": ";
			/* if(ctype.equalsIgnoreCase("RectangularChannel")) { Double b =
			 * summary[bi][15]; Parameter[] width = con.getSpecifix(); } else
			 * if(ctype.equalsIgnoreCase("ParabolicChannel")) {} else {
			 * txt=txt+"OK"+System.lineSeparator(); }
			 */
			txt=txt+"OK"+System.lineSeparator();
			txtArea.append(txt);
		}

		return(panout);
	} 

	
	public JPanel priorSpag(ConfigHydrauPanel panel){
		JPanel panout = new JPanel();
		panout.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(panout, new int[]{0},new int[]{0},new double[]{1.0},new double[]{1.0});
		ConfigHydrau h=station.getHydrauConfig(panel.getId().getText());
		if(h.getPriorSpag()==null){return(null);}
		ChartPanel chart = h.getPriorSpag().plot("", dico.entry("Hunit"),dico.entry("Qunit"),false);
		GridBag_Layout.putIntoGrid(chart, panout,0,0,1,1,true,true);
		return(panout);
	} 

	public JPanel postSpag(RatingCurvePanel panel){
		JPanel panout = new JPanel();
		panout.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(panout, new int[]{0},new int[]{0},new double[]{1.0},new double[]{1.0});
		RatingCurve rc=station.getRatingCurve(panel.getId().getText());
		if(rc.getSpag_total()==null){return(null);}
		ChartPanel chart = rc.getSpag_total().plot("", dico.entry("Hunit"),dico.entry("Qunit"),false);
		GridBag_Layout.putIntoGrid(chart, panout,0,0,1,1,true,true);
		return(panout);
	} 

	public JPanel hydroSpag(HydrographPanel panel){
		JPanel panout = new JPanel();
		panout.setBackground(Defaults.bkgColor);
		GridBag_Layout.SetGrid(panout, new int[]{0},new int[]{0},new double[]{1.0},new double[]{1.0});
		Hydrograph h=station.getHydrograph(panel.getId().getText());
		if(h.getSpag_total()==null){return(null);}
		ChartPanel chart = h.getSpag_total().plot("", dico.entry("TimeInYears"), dico.entry("Qunit"),false);
		GridBag_Layout.putIntoGrid(chart, panout,0,0,1,1,true,true);
		return(panout);
	} 

}

package moteur;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

import commons.Parameter;
import commons.Plots;
import commons.ReadWrite;
import commons.Time;
import Utils.Defaults;

/**
 * Rating Curve object
 * 
 * @author Sylvai Vigneau & Ben Renard, Irstea Lyon
 */
public class RatingCurve extends Item {

	private String gauging_id;
	private String hydrau_id;
	private String error_id;
	private PostRatingCurveOptions postRCoptions;
	private Envelop env_total;
	private Envelop env_param;
	private Spaghetti spag_total;
	private Spaghetti spag_param;
	private Envelop[] par;
	private Double[][] mcmc;
	private Double[][] mcmc_cooked;
	private Double[][] mcmc_summary;
	private Double[][] HQ;

	// constants
	public static final int BAREME_NMAX = 100;

	/**
	 * empty constructor
	 */
	public RatingCurve() {
	}

	/**
	 * partial constructor
	 * 
	 * @param name
	 */
	public RatingCurve(String name) {
		super(name);
	}

	/**
	 * partial constructor
	 * 
	 * @param name
	 * @param description
	 * @param gauging_id
	 * @param hydrau_id
	 * @param error_id
	 */
	public RatingCurve(String name, String description, String gauging_id, String hydrau_id, String error_id) {
		super(name, description);
		this.gauging_id = gauging_id;
		this.hydrau_id = hydrau_id;
		this.error_id = error_id;
	}

	/**
	 * copy constructor
	 * 
	 * @param x copied object
	 */
	public RatingCurve(RatingCurve x) {
		super(x);
		if (x == null) {
			return;
		}
		if (x.getGauging_id() != null)
			this.gauging_id = new String(x.getGauging_id());
		if (x.getHydrau_id() != null)
			this.hydrau_id = new String(x.getHydrau_id());
		if (x.getError_id() != null)
			this.error_id = new String(x.getError_id());
		if (x.getPostRCoptions() != null)
			this.postRCoptions = new PostRatingCurveOptions(x.getPostRCoptions());
		if (x.getEnv_total() != null)
			this.env_total = new Envelop(x.getEnv_total());
		if (x.getEnv_param() != null)
			this.env_param = new Envelop(x.getEnv_param());
		if (x.getSpag_total() != null)
			this.spag_total = new Spaghetti(x.getSpag_total());
		if (x.getSpag_param() != null)
			this.spag_param = new Spaghetti(x.getSpag_param());
		if (x.getPar() != null) {
			int n = x.getPar().length;
			this.par = new Envelop[n];
			for (int i = 0; i < n; i++) {
				if (x.getPar()[i] != null)
					this.par[i] = new Envelop(x.getPar()[i]);
			}
		}
		if (x.getMcmc() != null) {
			int row = x.getMcmc()[0].length;
			int col = x.getMcmc().length;
			this.mcmc = new Double[col][row];
			for (int i = 0; i < row; i++) {
				for (int j = 0; j < col; j++) {
					if (x.getMcmc()[j][i] != null)
						this.mcmc[j][i] = Double.valueOf(x.getMcmc()[j][i]);
				}
			}
		}
		if (x.getMcmc_cooked() != null) {
			int row = x.getMcmc_cooked()[0].length;
			int col = x.getMcmc_cooked().length;
			this.mcmc_cooked = new Double[col][row];
			for (int i = 0; i < row; i++) {
				for (int j = 0; j < col; j++) {
					if (x.getMcmc_cooked()[j][i] != null)
						this.mcmc_cooked[j][i] = Double.valueOf(x.getMcmc_cooked()[j][i]);
				}
			}
		}
		if (x.getMcmc_summary() != null) {
			int row = x.getMcmc_summary()[0].length;
			int col = x.getMcmc_summary().length;
			this.mcmc_summary = new Double[col][row];
			for (int i = 0; i < row; i++) {
				for (int j = 0; j < col; j++) {
					if (x.getMcmc_summary()[j][i] != null)
						this.mcmc_summary[j][i] = Double.valueOf(x.getMcmc_summary()[j][i]);
				}
			}
		}
		if (x.getHQ() != null) {
			int row = x.getHQ()[0].length;
			int col = x.getHQ().length;
			this.HQ = new Double[col][row];
			for (int i = 0; i < row; i++) {
				for (int j = 0; j < col; j++) {
					if (x.getHQ()[j][i] != null)
						this.HQ[j][i] = Double.valueOf(x.getHQ()[j][i]);
				}
			}
		}
	}

	public ChartPanel plot(String title, String xlab, String ylab, boolean ylog) {
		// Retrieve number of hydraulic controls
		ConfigHydrau h = Station.getInstance().getHydrauConfig(this.getHydrau_id());
		int ncontrol = h.getControls().size();
		// Create individual charts
		GaugingSet g = Station.getInstance().getGauging(this.getGauging_id());
		ChartPanel chart_gaugings = g.plot(title, xlab, ylab, ylog);
		ChartPanel chart_total = this.getEnv_total().plot(title, xlab, ylab,
				Defaults.plot_lineColor, Defaults.plot_postColor, 1.0f, Defaults.plot_bkgColor, Defaults.plot_gridColor,
				ylog);
		ChartPanel chart_param = this.getEnv_param().plot(title, xlab, ylab,
				Defaults.plot_lineColor, Defaults.plot_postColor_light, 1.0f, Defaults.plot_bkgColor,
				Defaults.plot_gridColor, ylog);
		ChartPanel[] chart_k = new ChartPanel[ncontrol];
		JFreeChart[] line_k = new JFreeChart[ncontrol];
		Double[][] summary = this.getMcmc_summary();
		int indx;
		for (int i = 0; i < ncontrol; i++) {
			if (i == 0) {
				indx = 1;
			} else {
				indx = 3 * i;
			}
			// TODO: replace +/- 2*sdev by a proper 95% interval - need to modify BaRatin
			// for that
			double maxpost = summary[indx][15];
			double low = maxpost - 2 * summary[indx][10];
			double high = maxpost + 2 * summary[indx][10];
			double maxi = chart_total.getChart().getXYPlot().getRangeAxis().getUpperBound();
			Envelop env = new Envelop(new Double[] { low, high }, new Double[] { 0.0, 0.0 }, new Double[] { 0.0, 0.0 },
					new Double[] { 0.0, 0.0 }, new Double[] { maxi, maxi });
			chart_k[i] = env.plot(title, xlab, ylab,
					Defaults.plot_kColor, Defaults.plot_kColor_light, 0.5f, Defaults.plot_bkgColor,
					Defaults.plot_gridColor,
					ylog);
			line_k[i] = Plots.LinePlot(new Double[] { maxpost, maxpost }, new Double[] { 0.0, maxi }, title, xlab, ylab,
					Color.BLACK, Color.BLACK, Defaults.plot_gridColor, ylog);
		}
		// create common plotting domain
		ValueAxis domain = new NumberAxis("Domain");
		ValueAxis range = new NumberAxis("Range");
		((NumberAxis) domain).setAutoRangeIncludesZero(false);

		// Extract datasets
		XYDataset d2 = chart_total.getChart().getXYPlot().getDataset(0);
		XYDataset d1 = chart_param.getChart().getXYPlot().getDataset(0);
		XYDataset d0 = chart_gaugings.getChart().getXYPlot().getDataset(0);
		XYDataset[] d = new XYDataset[ncontrol];
		XYDataset[] dline = new XYDataset[ncontrol];
		for (int i = 0; i < ncontrol; i++) {
			d[i] = chart_k[i].getChart().getXYPlot().getDataset(0);
			dline[i] = line_k[i].getXYPlot().getDataset(0);
		}
		// Extract renderers
		XYItemRenderer r2 = chart_total.getChart().getXYPlot().getRenderer(0);
		XYItemRenderer r1 = chart_param.getChart().getXYPlot().getRenderer(0);
		XYItemRenderer r0 = chart_gaugings.getChart().getXYPlot().getRenderer(0);
		XYItemRenderer[] r = new XYItemRenderer[ncontrol];
		XYItemRenderer[] rline = new XYItemRenderer[ncontrol];
		for (int i = 0; i < ncontrol; i++) {
			r[i] = chart_k[i].getChart().getXYPlot().getRenderer(0);
			rline[i] = line_k[i].getXYPlot().getRenderer(0);
		}
		// Make a new plot and assign datasets
		XYPlot plot = new XYPlot();
		plot.setDataset(0, d0);
		plot.setDataset(1, d1);
		plot.setDataset(2, d2);
		for (int i = 0; i < ncontrol; i++) {
			plot.setDataset(3 + 2 * i, d[i]);
			plot.setDataset(3 + 2 * i + 1, dline[i]);
		}
		plot.setRenderer(0, r0);
		plot.setRenderer(1, r1);
		plot.setRenderer(2, r2);
		for (int i = 0; i < ncontrol; i++) {
			// quick fix to remove ugmy irrelevant horizontal lines at the bottom
			// of vertical transition stage line/range
			r[i].setSeriesStroke(0, new BasicStroke(0.0f));
			rline[i].setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
					new float[] { 5.0f, 4.0f }, 0.0f));
			plot.setRenderer(3 + 2 * i, r[i]);
			// plot.setRenderer(3 + 2 * i + 1, r[i]);
			plot.setRenderer(3 + 2 * i + 1, rline[i]);
		}
		// assign domain/range
		plot.setDomainAxis(0, domain);
		plot.setRangeAxis(0, range);
		plot.getDomainAxis().setLabel(xlab);
		plot.getRangeAxis().setLabel(ylab);
		plot.mapDatasetToDomainAxis(0, 0);
		plot.mapDatasetToRangeAxis(0, 0);
		plot.mapDatasetToDomainAxis(1, 0);
		plot.mapDatasetToRangeAxis(1, 0);
		plot.mapDatasetToDomainAxis(2, 0);
		plot.mapDatasetToRangeAxis(2, 0);
		for (int i = 0; i < ncontrol; i++) {
			plot.mapDatasetToDomainAxis(3 + 2 * i, 0);
			plot.mapDatasetToRangeAxis(3 + 2 * i, 0);
			plot.mapDatasetToDomainAxis(3 + 2 * i + 1, 0);
			plot.mapDatasetToRangeAxis(3 + 2 * i + 1, 0);
		}

		// Create the chart with all plots
		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.removeLegend();
		chart.setTitle(title);
		chart.setBackgroundPaint(Defaults.bkgColor);
		chart.getPlot().setBackgroundPaint(Defaults.bkgColor);
		plot.setRangeGridlinePaint(Defaults.plot_gridColor);
		plot.setDomainGridlinePaint(Defaults.plot_gridColor);

		final NumberAxis logaxis = new LogarithmicAxis(ylab);
		if (ylog) {
			chart.getXYPlot().setRangeAxis(logaxis);
		}
		ChartPanel CP = new ChartPanel(chart);
		return (CP);
	}

	public void export_csv(String file) throws FileNotFoundException, IOException, Exception {
		Envelop env = this.getEnv_param();
		Double[][] w = new Double[6][env.getNx()];
		w[0] = env.getX();
		w[1] = env.getMaxpost();
		w[2] = env.getQlow();
		w[3] = env.getQhigh();
		env = this.getEnv_total();
		w[4] = env.getQlow();
		w[5] = env.getQhigh();
		ReadWrite.write(w,
				new String[] { "H[m]", "Qmaxpost[m3/s]", "Qlow_param[m3/s]", "Qhigh_param[m3/s]", "Qlow_total[m3/s]",
						"Qhigh_total[m3/s]" },
				file, Defaults.csvSep);
	}

	public void export_bareme(String file, String code, String name, Time start, Time end)
			throws FileNotFoundException, IOException, Exception {
		File f;
		FileWriter fw;
		BufferedWriter bw;
		f = new File(file);
		if (!f.exists()) {
			f.createNewFile();
		}
		fw = new FileWriter(f.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		String line;
		Double[] H, Q;
		/////////////////////////////////////////////////////////////////
		// line 1 - no idea what this means...
		line = "DEC;  6 13";
		bw.write(line);
		bw.newLine();
		/////////////////////////////////////////////////////////////////
		// line 2 - creation date, code, and various stuff I don't understand...
		Calendar d = Calendar.getInstance(); // current time
		Time now = new Time(d.get(Calendar.YEAR), d.get(Calendar.MONTH) + 1, d.get(Calendar.DAY_OF_MONTH),
				d.get(Calendar.HOUR_OF_DAY), d.get(Calendar.MINUTE), d.get(Calendar.SECOND));
		line = "DEB;BA-HYDRO;Baratin;" + now.toString("YYYYMMDDhhmmss") + ";" + code + ";1111-1;;;";
		bw.write(line);
		bw.newLine();
		/////////////////////////////////////////////////////////////////
		// line 3 - lower envelop RC - not sure what the "2;12;5000" refers to...
		line = "C;TAR;" + code + ";" + name + "_inf" + ";" + "2;12;5000;;;h/q Baratin : Qmin;";
		bw.write(line);
		bw.newLine();
		/////////////////////////////////////////////////////////////////
		// line 4 - lower envelop RC validity
		line = "C;PAT;" + code + ";" + name + "_inf" + ";" +
				start.toString("YYYYMMDD") + ";" + start.toString("hh") + ":" + start.toString("mm") + ";" +
				end.toString("YYYYMMDD") + ";" + end.toString("hh") + ":" + end.toString("mm") + ";";
		bw.write(line);
		bw.newLine();
		/////////////////////////////////////////////////////////////////
		// line 5 - upper envelop RC - not sure what the "2;12;5000" refers to...
		line = "C;TAR;" + code + ";" + name + "_sup" + ";" + "2;12;5000;;;h/q Baratin : Qmax;";
		bw.write(line);
		bw.newLine();
		/////////////////////////////////////////////////////////////////
		// line 6 - lower envelop RC validity
		line = "C;PAT;" + code + ";" + name + "_sup" + ";" +
				start.toString("YYYYMMDD") + ";" + start.toString("hh") + ":" + start.toString("mm") + ";" +
				end.toString("YYYYMMDD") + ";" + end.toString("hh") + ":" + end.toString("mm") + ";";
		bw.write(line);
		bw.newLine();
		/////////////////////////////////////////////////////////////////
		// line 7 - maxpost RC - not sure what the "2;12;5000" refers to...
		line = "C;TAR;" + code + ";" + name + ";" + "2;12;5000;;;h/q Baratin : Qmaxpost;";
		bw.write(line);
		bw.newLine();
		/////////////////////////////////////////////////////////////////
		// line 8 - maxpost RC validity
		line = "C;PAT;" + code + ";" + name + ";" +
				start.toString("YYYYMMDD") + ";" + start.toString("hh") + ":" + start.toString("mm") + ";" +
				end.toString("YYYYMMDD") + ";" + end.toString("hh") + ":" + end.toString("mm") + ";";
		bw.write(line);
		bw.newLine();
		/////////////////////////////////////////////////////////////////
		// inf CT block
		H = this.getEnv_total().getX();
		Q = this.getEnv_total().getQlow();
		int n = Math.min(H.length, BAREME_NMAX);
		for (int i = 0; i < n; i++) {
			line = "C;PIV;" + code + ";" + name + "_inf;" +
					Integer.toString((int) (1000 * H[i])) + ";" +
					Integer.toString((int) (1000 * Q[i])) + ";";
			bw.write(line);
			bw.newLine();
		}
		/////////////////////////////////////////////////////////////////
		// sup CT block
		H = this.getEnv_total().getX();
		Q = this.getEnv_total().getQhigh();
		for (int i = 0; i < n; i++) {
			line = "C;PIV;" + code + ";" + name + "_sup;" +
					Integer.toString((int) (1000 * H[i])) + ";" +
					Integer.toString((int) (1000 * Q[i])) + ";";
			bw.write(line);
			bw.newLine();
		}
		/////////////////////////////////////////////////////////////////
		// Maxpost CT block
		H = this.getEnv_param().getX();
		Q = this.getEnv_param().getMaxpost();
		for (int i = 0; i < n; i++) {
			line = "C;PIV;" + code + ";" + name + ";" +
					Integer.toString((int) (1000 * H[i])) + ";" +
					Integer.toString((int) (1000 * Q[i])) + ";";
			bw.write(line);
			bw.newLine();
		}
		/////////////////////////////////////////////////////////////////
		// Ending line - no idea what this means...
		line = "FIN;EXP-HYDRO;" + Integer.toString(8 + 3 * n + 1) + ";";
		bw.write(line);
		bw.newLine();
		bw.close();
	}

	public void export_equation(String file) throws FileNotFoundException, IOException, Exception {
		// Maxpost parameters
		Double[][] pars = this.getMcmc_summary();
		int imaxpost = 15;
		// Hydrau-config and constituting controls
		ConfigHydrau h = Station.getInstance().getHydrauConfig(this.getHydrau_id());
		ArrayList<HydrauControl> controls = h.getControls();
		int ncontrol = controls.size();
		ArrayList<ArrayList<Boolean>> M = h.getMatrix().getMatrix();
		// b0 is the last column before derived b's
		int b0 = pars.length - ncontrol;
		// Columns defining k-a-c-b RC parameters
		int ai, ci, ki, bi;
		// Equations
		String[] eqs = new String[ncontrol]; // individual equation for each control
		// First pass to get equation of each control
		for (int i = 0; i < ncontrol; i++) {
			if (i == 0) {
				ai = 0;
				ki = 1;
				ci = 2;
				bi = 1;
			} else {
				ki = 3 * i;
				ai = 3 * i + 1;
				ci = 3 * i + 2;
				bi = b0 + i;
			}
			HydrauControl dummy = new HydrauControl(new Parameter(pars[ai][imaxpost]),
					new Parameter(pars[bi][imaxpost]),
					new Parameter(pars[ci][imaxpost]),
					new Parameter(pars[ki][imaxpost]));
			eqs[i] = dummy.toEquation();
		}

		// Second pass to work out succession or addition of controls
		String[] fullEq = new String[ncontrol + 1];
		fullEq[0] = "h < " + pars[1][imaxpost] + " : Q = 0";
		for (int i = 0; i < ncontrol; i++) {
			if (i == 0) {
				ki = 1;
			} else {
				ki = 3 * i;
			}
			if (i == ncontrol - 1) {
				fullEq[i + 1] = "h > " + pars[ki][imaxpost];
			} else {
				fullEq[i + 1] = pars[ki][imaxpost] + " < h < " + pars[3 * (i + 1)][imaxpost];
			}
			fullEq[i + 1] = fullEq[i + 1] + " : Q = ";
			for (int j = 0; j <= i; j++) {
				if (M.get(j).get(i)) {
					// Determine if a '+' is required
					String foo = fullEq[i + 1].replaceAll("\\s+", ""); // remove all spaces
					String lastChar = String.valueOf(foo.charAt(foo.length() - 1)); // last character of eq.
					if (!lastChar.equals("=")) {
						fullEq[i + 1] = fullEq[i + 1] + " + ";
					} // if it's not '=', we're adding up
					fullEq[i + 1] = fullEq[i + 1] + eqs[j];
				}
			}
		}
		ReadWrite.write(fullEq, file);
	}

	public String[] buildMCMCheaders() {
		Double[][] mcmc = this.getMcmc_cooked();
		int ncol = mcmc.length;
		String[] head = new String[ncol];
		ConfigHydrau h = Station.getInstance().getHydrauConfig(this.getHydrau_id());
		ArrayList<HydrauControl> controls = h.getControls();
		int ncontrol = controls.size();
		ArrayList<Gauging> g = Station.getInstance().getGauging(this.getGauging_id()).getGaugings();
		RemnantError r = Station.getInstance().getRemnant(this.getError_id());
		int m = 0;
		// b/k-a-c parameters
		for (int i = 0; i < ncontrol; i++) {
			if (i == 0) {
				head[m] = "a" + 1;
				m = m + 1;
				head[m] = "b" + 1;
				m = m + 1;
				head[m] = "c" + 1;
				m = m + 1;
			} else {
				head[m] = "k" + (i + 1);
				m = m + 1;
				head[m] = "a" + (i + 1);
				m = m + 1;
				head[m] = "c" + (i + 1);
				m = m + 1;
			}
		}
		// remnant errors parameters
		for (int i = 0; i < r.getNpar(); i++) {
			head[m] = "gamma" + (i + 1);
			m = m + 1;
		}
		// true stages
		for (int i = 0; i < g.size(); i++) {
			if (g.get(i).getuH() > 0 & g.get(i).getActive()) { // uncertainty in gauged h, Htrue is estimated
				head[m] = "Htrue" + (i + 1);
				m = m + 1;
			}
		}
		// log-post
		head[m] = "LogPost";
		m = m + 1;
		// derived b's
		if (ncontrol > 1) {
			for (int i = 1; i < ncontrol; i++) {
				head[m] = "b" + (i + 1);
				m = m + 1;
			}
		}

		return head;
	}

	public void export_mcmc(String file) throws FileNotFoundException, IOException, Exception {
		// MCMC simulations
		Double[][] mcmc = this.getMcmc_cooked();
		// headers
		String[] head = buildMCMCheaders();
		ReadWrite.write(mcmc, head, file, Defaults.csvSep);
	}

	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS
	/////////////////////////////////////////////////////////

	public String getGauging_id() {
		return gauging_id;
	}

	public void setGauging_id(String gauging_id) {
		this.gauging_id = gauging_id;
	}

	public String getHydrau_id() {
		return hydrau_id;
	}

	public void setHydrau_id(String hydrau_id) {
		this.hydrau_id = hydrau_id;
	}

	public String getError_id() {
		return error_id;
	}

	public void setError_id(String error_id) {
		this.error_id = error_id;
	}

	public PostRatingCurveOptions getPostRCoptions() {
		return postRCoptions;
	}

	public void setPostRCoptions(PostRatingCurveOptions postRCoptions) {
		this.postRCoptions = postRCoptions;
	}

	public Envelop getEnv_total() {
		return env_total;
	}

	public void setEnv_total(Envelop env_total) {
		this.env_total = env_total;
	}

	public Envelop getEnv_param() {
		return env_param;
	}

	public void setEnv_param(Envelop env_param) {
		this.env_param = env_param;
	}

	public Spaghetti getSpag_total() {
		return spag_total;
	}

	public void setSpag_total(Spaghetti spag_total) {
		this.spag_total = spag_total;
	}

	public Spaghetti getSpag_param() {
		return spag_param;
	}

	public void setSpag_param(Spaghetti spag_param) {
		this.spag_param = spag_param;
	}

	public Envelop[] getPar() {
		return par;
	}

	public void setPar(Envelop[] par) {
		this.par = par;
	}

	public Double[][] getMcmc() {
		return mcmc;
	}

	public void setMcmc(Double[][] mcmc) {
		this.mcmc = mcmc;
	}

	public Double[][] getMcmc_cooked() {
		return mcmc_cooked;
	}

	public void setMcmc_cooked(Double[][] mcmc_cooked) {
		this.mcmc_cooked = mcmc_cooked;
	}

	public Double[][] getMcmc_summary() {
		return mcmc_summary;
	}

	public void setMcmc_summary(Double[][] mcmc_summary) {
		this.mcmc_summary = mcmc_summary;
	}

	public Double[][] getHQ() {
		return HQ;
	}

	public void setHQ(Double[][] hQ) {
		HQ = hQ;
	}

}

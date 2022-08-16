/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2007, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA. 
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ----------------------
 * DeviationRenderer.java
 * ----------------------
 * (C) Copyright 2007, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: $
 *
 * Changes
 * -------
 * 21-Feb-2007 : Version 1 (DG);
 *
 */

package commons;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 * A specialised subclass of the {@link XYLineAndShapeRenderer} that requires
 * an {@link IntervalXYDataset} and represents the y-interval by shading an
 * area behind the y-values on the chart.
 */
@SuppressWarnings("serial")
public class DeviationRenderer extends XYLineAndShapeRenderer {

	/**
	 * A state object that is passed to each call to <code>drawItem</code>.
	 */
	public static class State extends XYLineAndShapeRenderer.State {

		/**
		 * A list of coordinates for the upper y-values in the current series
		 * (after translation into Java2D space).
		 */
		public List<double[]> upperCoordinates;

		/**
		 * A list of coordinates for the lower y-values in the current series
		 * (after translation into Java2D space).
		 */
		public List<double[]> lowerCoordinates;

		/**
		 * Creates a new state instance.
		 *
		 * @param info  the plot rendering info.
		 */
		public State(PlotRenderingInfo info) {
			super(info);
			this.lowerCoordinates = new java.util.ArrayList<double[]>();
			this.upperCoordinates = new java.util.ArrayList<double[]>();
		}

	}

	/** The alpha transparency for the interval shading. */
	private float alpha;

	/**
	 * Creates a new renderer that displays lines and shapes for the data
	 * items, as well as the shaded area for the y-interval.
	 */
	public DeviationRenderer() {
		this(true, true,1.0f);
	}

	/**
	 * Creates a new renderer.
	 *
	 * @param lines  show lines between data items?
	 * @param shapes  show a shape for each data item?
	 */
	public DeviationRenderer(boolean lines, boolean shapes,float alpha) {
		super(lines, shapes);
		super.setDrawSeriesLineAsPath(true);
		this.alpha = alpha;
	}

	/**
	 * Returns the alpha transparency for the background shading.
	 *
	 * @return The alpha transparency.
	 */
	public float getAlpha() {
		return this.alpha;
	}

	/**
	 * Sets the alpha transparency for the background shading, and sends a
	 * {@link RendererChangeEvent} to all registered listeners.
	 *
	 * @param alpha   the alpha to set
	 */
	public void setAlpha(float alpha) {
		if (alpha < 0.0f || alpha > 1.0f) {
			throw new IllegalArgumentException(
					"Requires 'alpha' in the range 0.0 to 1.0.");
		}
		this.alpha = alpha;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * This method is overridden so that this flag cannot be changed---it is
	 * set to <code>true</code> for this renderer.
	 *
	 * @param flag  ignored.
	 */
	public void setDrawSeriesLineAsPath(boolean flag) {
		// ignore
	}

	/**
	 * Initialises and returns a state object that can be passed to each
	 * invocation of the {@link #drawItem} method.
	 *
	 * @param g2  the graphics target.
	 * @param dataArea  the data area.
	 * @param plot  the plot.
	 * @param dataset  the dataset.
	 * @param info  the plot rendering info.
	 *
	 * @return A newly initialised state object.
	 */
	public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea,
			XYPlot plot, XYDataset dataset, PlotRenderingInfo info) {
		State state = new State(info);
		state.seriesPath = new GeneralPath();
		return state;
	}

	public int getPassCount() {
		return 3;
	}

	protected boolean isItemPass(int pass) {
		return (pass == 2);
	}

	protected boolean isLinePass(int pass) {
		return (pass == 1);
	}

	/**
	 * Draws the visual representation of a single data item.
	 *
	 * @param g2  the graphics device.
	 * @param state  the renderer state.
	 * @param dataArea  the area within which the data is being drawn.
	 * @param info  collects information about the drawing.
	 * @param plot  the plot (can be used to obtain standard color
	 *              information etc).
	 * @param domainAxis  the domain axis.
	 * @param rangeAxis  the range axis.
	 * @param dataset  the dataset.
	 * @param series  the series index (zero-based).
	 * @param item  the item index (zero-based).
	 * @param crosshairState  crosshair information for the plot
	 *                        (<code>null</code> permitted).
	 * @param pass  the pass index.
	 */
	public void drawItem(Graphics2D g2,
			XYItemRendererState state,
			Rectangle2D dataArea,
			PlotRenderingInfo info,
			XYPlot plot,
			ValueAxis domainAxis,
			ValueAxis rangeAxis,
			XYDataset dataset,
			int series,
			int item,
			CrosshairState crosshairState,
			int pass) {

		// do nothing if item is not visible
		if (!getItemVisible(series, item)) {
			return;   
		}

		// first pass draws the shading
		if (pass == 0) {
			IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;
			State drState = (State) state;

			double x = intervalDataset.getXValue(series, item);
			double yLow = intervalDataset.getStartYValue(series, item);
			double yHigh  = intervalDataset.getEndYValue(series, item);

			RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
			RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

			double xx = domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
			double yyLow = rangeAxis.valueToJava2D(yLow, dataArea,
					yAxisLocation);
			double yyHigh = rangeAxis.valueToJava2D(yHigh, dataArea,
					yAxisLocation);

			PlotOrientation orientation = plot.getOrientation();
			if (orientation == PlotOrientation.HORIZONTAL) {
				drState.lowerCoordinates.add(new double[] {yyLow, xx});
				drState.upperCoordinates.add(new double[] {yyHigh, xx});
			}
			else if (orientation == PlotOrientation.VERTICAL) {
				drState.lowerCoordinates.add(new double[] {xx, yyLow});
				drState.upperCoordinates.add(new double[] {xx, yyHigh});
			}

			plot.getRangeAxis().getRange().getUpperBound();
			
			if (item == (dataset.getItemCount(series) - 1) | x>=plot.getDomainAxis().getRange().getUpperBound()) {
				// last item in series, draw the lot...
				// set up the alpha-transparency...
				Composite originalComposite = g2.getComposite();
				g2.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, this.alpha));
				g2.setPaint(getItemFillPaint(series, item));
				GeneralPath area = new GeneralPath();
				double[] coords = drState.lowerCoordinates.get(0);
				area.moveTo(coords[0], coords[1]);
				for (int i = 1; i < drState.lowerCoordinates.size(); i++) {
					coords = drState.lowerCoordinates.get(i);
					area.lineTo(coords[0], coords[1]);
				}
				int count = drState.upperCoordinates.size();
				coords = drState.upperCoordinates.get(count - 1);
				area.lineTo(coords[0], coords[1]);
				for (int i = count - 2; i >= 0; i--) {
					coords = drState.upperCoordinates.get(i);
					area.lineTo(coords[0], coords[1]);
				}
				area.closePath();
				g2.fill(area);
				g2.setComposite(originalComposite);
				drState.lowerCoordinates.clear();
				drState.upperCoordinates.clear();
			}           
		}
		if (isLinePass(pass)) {

			// the following code handles the line for the y-values...it's
			// all done by code in the super class
			if (item == 0) {
				State s = (State) state;
				s.seriesPath.reset();
				s.setLastPointGood(false);     
			}

			if (getItemLineVisible(series, item)) {
				drawPrimaryLineAsPath(state, g2, plot, dataset, pass,
						series, item, domainAxis, rangeAxis, dataArea);
			}
		}

		// second pass adds shapes where the items are ..
		else if (isItemPass(pass)) {

			// setup for collecting optional entity info...
			EntityCollection entities = null;
			if (info != null) {
				entities = info.getOwner().getEntityCollection();
			}

			drawSecondaryPass(g2, plot, dataset, pass, series, item,
					domainAxis, dataArea, rangeAxis, crosshairState, entities);
		}
	}

	/**
	 * Tests this renderer for equality with an arbitrary object.
	 *
	 * @param obj  the object (<code>null</code> permitted).
	 *
	 * @return A boolean.
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof DeviationRenderer)) {
			return false;
		}
		DeviationRenderer that = (DeviationRenderer) obj;
		if (this.alpha != that.alpha) {
			return false;
		}
		return super.equals(obj);
	}

}
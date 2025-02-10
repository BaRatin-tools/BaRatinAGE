package org.baratinage.ui.plot;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BasicStroke;
import java.awt.Color;

import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleColorField;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.component.SimpleSlider;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.plot.PlotItem.LineType;
import org.baratinage.ui.plot.PlotItem.ShapeType;
import org.baratinage.utils.ConsoleLogger;
import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.XYItemRenderer;

public class EditablePlotItem implements IPlotItemRendererSettings {
    public static enum TYPE {
        LINE, BAND, POINT, UNSUPPORTED
    }

    public final TYPE type;
    public final PlotItem plotItem;

    private String label;

    private Paint linePaint;
    private float lineWidth;
    private LineType lineType;
    private ShapeType shapeType;
    private double shapeSize;
    private Paint fillPaint;
    private float fillAlpha;

    public boolean visible = true;
    public boolean showLegend = true;

    private static TYPE getTypeFromInstance(PlotItem plotItem) {
        if (plotItem instanceof PlotLine |
                plotItem instanceof PlotInfiniteLine |
                plotItem instanceof PlotTimeSeriesLine) {
            return TYPE.LINE;
        } else if (plotItem instanceof PlotBand |
                plotItem instanceof PlotInfiniteBand |
                plotItem instanceof PlotTimeSeriesBand |
                plotItem instanceof PlotBar) {
            return TYPE.BAND;
        } else if (plotItem instanceof PlotPoints) {
            return TYPE.POINT;
        }
        return TYPE.UNSUPPORTED;
    }

    public EditablePlotItem(PlotItem plotItem) {
        this(plotItem, getTypeFromInstance(plotItem));
    }

    public EditablePlotItem(PlotItem plotItem, TYPE type) {
        this.type = type;
        this.plotItem = plotItem;

        LegendItem legendItem = plotItem.getLegendItem();
        XYItemRenderer renderer = plotItem.getRenderer();

        label = legendItem.getLabel();

        linePaint = renderer.getSeriesPaint(0);
        Stroke lineStroke = renderer.getSeriesStroke(0);
        lineWidth = 1F;
        if (lineStroke instanceof BasicStroke) {
            lineWidth = ((BasicStroke) lineStroke).getLineWidth();
        }
        lineType = LineType.getLineTypeFromStroke(lineStroke);

        Shape shape = renderer.getSeriesShape(0);
        shapeType = null;
        shapeSize = shape.getBounds().getWidth();
        if (shape != null) {
            if (shape instanceof Ellipse2D.Double) {
                shapeType = ShapeType.CIRCLE;
                shapeSize = ((Ellipse2D.Double) shape).getWidth();
            } else if (shape instanceof Rectangle) {
                shapeType = ShapeType.SQUARE;
                shapeSize = ((Rectangle) shape).getWidth();
            }
        }

        fillPaint = renderer.getSeriesFillPaint(0);
        fillAlpha = 0F;
        if (renderer instanceof CustomAreaRenderer) {
            CustomAreaRenderer r = (CustomAreaRenderer) renderer;
            fillAlpha = r.getFillAlpha();
        }

    }

    public final List<PlotItem> siblings = new ArrayList<>();

    public void addSibling(PlotItem item) {
        if (!getTypeFromInstance(item).equals(type)) {
            ConsoleLogger.error("Cannot add a sibling of different type!");
            return;
        }
        siblings.add(item);
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    private void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    /**
     * - create a SimpleSliderField component
     * - create a LineDashArrayField component extending a SimpleComboboxField
     * -
     * 
     * @return
     */

    public RowColPanel getEditionPanel() {
        RowColPanel editionPanel = new RowColPanel(RowColPanel.AXIS.COL);
        editionPanel.setPadding(0, 0, 5, 0);

        JLabel labelFieldLabel = new JLabel();
        labelFieldLabel.setText(T.text("legend_text"));

        SimpleTextField labelField = new SimpleTextField();
        labelField.setText(label);
        labelField.addChangeListener(l -> {
            label = labelField.getText();
            plotItem.setLabel(label);
            fireChangeListeners();
        });

        editionPanel.appendChild(labelFieldLabel);
        editionPanel.appendChild(labelField);

        if (type == TYPE.LINE) {
            JLabel linePaintFieldLabel = new JLabel();
            linePaintFieldLabel.setText(T.text("line_color"));
            SimpleColorField linePaintChooser = new SimpleColorField();
            linePaintChooser.setColor(linePaint);
            linePaintChooser.addChangeListener(l -> {
                linePaint = linePaintChooser.getColor();
                updatePlotItems();
                fireChangeListeners();
            });

            editionPanel.appendChild(linePaintFieldLabel);
            editionPanel.appendChild(linePaintChooser);

            JLabel lineWidthLabel = new JLabel();
            lineWidthLabel.setText(T.text("line_width"));
            SimpleSlider lineWidthSlider = new SimpleSlider(1, 10, 1);
            lineWidthSlider.setValue((double) lineWidth);
            lineWidthSlider.addChangeListener(l -> {
                lineWidth = (float) lineWidthSlider.getValue();
                System.out.println(lineWidth);
                updatePlotItems();
                fireChangeListeners();
            });

            editionPanel.appendChild(lineWidthLabel);
            editionPanel.appendChild(lineWidthSlider);

            JLabel lineDashFieldLabel = new JLabel();
            lineDashFieldLabel.setText(T.text("line_type"));

            SimpleComboBox lineDashCombobox = new SimpleComboBox();
            JLabel solidLineLabel = new JLabel();
            solidLineLabel.setText(T.text("line_solid"));
            solidLineLabel.setIcon(new ImageIcon(PlotUtils.buildImage(
                    PlotItem.buildLineShape(20),
                    PlotItem.buildStroke(1, LineType.SOLID.getDashArray()), Color.BLACK,
                    20, 20)));
            JLabel dashLineLabel = new JLabel();
            dashLineLabel.setText(T.text("line_dashed"));
            dashLineLabel.setIcon(new ImageIcon(PlotUtils.buildImage(
                    PlotItem.buildLineShape(20),
                    PlotItem.buildStroke(1, LineType.DASHED.getDashArray()), Color.BLACK,
                    20, 20)));
            JLabel dottedLineLabel = new JLabel();
            dottedLineLabel.setText(T.text("line_dotted"));
            dottedLineLabel.setIcon(new ImageIcon(PlotUtils.buildImage(
                    PlotItem.buildLineShape(20),
                    PlotItem.buildStroke(1, LineType.DOTTED.getDashArray()), Color.BLACK,
                    20, 20)));
            lineDashCombobox.setItems(new JLabel[] { solidLineLabel, dashLineLabel, dottedLineLabel }, false);
            if (lineType == LineType.SOLID)
                lineDashCombobox.setSelectedItem(0, true);
            if (lineType == LineType.DASHED)
                lineDashCombobox.setSelectedItem(1, true);
            if (lineType == LineType.DOTTED)
                lineDashCombobox.setSelectedItem(2, true);
            lineDashCombobox.addChangeListener(l -> {
                int i = lineDashCombobox.getSelectedIndex();
                if (i == 0)
                    lineType = LineType.SOLID;
                if (i == 1)
                    lineType = LineType.DASHED;
                if (i == 2)
                    lineType = LineType.DOTTED;
                updatePlotItems();
                fireChangeListeners();
            });

            editionPanel.appendChild(lineDashFieldLabel);
            editionPanel.appendChild(lineDashCombobox);

        } else if (type == TYPE.BAND) {
            JLabel fillPaintFieldLabel = new JLabel();
            fillPaintFieldLabel.setText(T.text("fill_color"));
            SimpleColorField fillPaintChooser = new SimpleColorField();
            fillPaintChooser.setColor(fillPaint);
            fillPaintChooser.addChangeListener(l -> {
                fillPaint = fillPaintChooser.getColor();
                updatePlotItems();
                fireChangeListeners();
            });

            editionPanel.appendChild(fillPaintFieldLabel);
            editionPanel.appendChild(fillPaintChooser);

            JLabel fillAlphaLabel = new JLabel();
            fillAlphaLabel.setText(T.text("fill_alpha"));
            SimpleSlider fillAlphaSlider = new SimpleSlider(0, 100, 1);
            fillAlphaSlider.setValue((double) (1 - fillAlpha) * 100);
            fillAlphaSlider.addChangeListener(l -> {
                fillAlpha = (float) (1 - (fillAlphaSlider.getValue() / 100));
                updatePlotItems();
                fireChangeListeners();
            });

            editionPanel.appendChild(fillAlphaLabel);
            editionPanel.appendChild(fillAlphaSlider);

        } else {
            JLabel nothingLabel = new JLabel("<NOT IMPLEMENTED>");
            editionPanel.appendChild(nothingLabel);
        }

        return editionPanel;
    }

    private void updatePlotItems() {
        plotItem.configureRenderer(this);
        for (PlotItem pi : siblings) {
            pi.configureRenderer(this);
        }
    }

    public void applyState(EditablePlotItem other) {
        label = other.label;
        linePaint = other.linePaint;
        lineWidth = other.lineWidth;
        lineType = other.lineType;
        shapeType = other.shapeType;
        shapeSize = other.shapeSize;
        fillPaint = other.fillPaint;
        fillAlpha = other.fillAlpha;
        visible = other.visible;
        showLegend = other.showLegend;
        updatePlotItems();
    }

    public String getLabel() {
        return label;
    }

    @Override
    public Paint getLinePaint() {
        return linePaint;
    }

    @Override
    public float getLineWidth() {
        return lineWidth;
    }

    @Override
    public float[] getLineDashArray() {
        return lineType.getDashArray(lineWidth);
    }

    @Override
    public double getShapeSize() {
        return shapeSize;
    }

    @Override
    public ShapeType getShapeType() {
        return shapeType;
    }

    @Override
    public Paint getFillPaint() {
        return fillPaint;
    }

    @Override
    public float getFillAlpha() {
        return fillAlpha;
    }

}

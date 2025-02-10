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
import org.baratinage.ui.component.SimpleIntegerField;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.plot.PlotItem.LineType;
import org.baratinage.ui.plot.PlotItem.ShapeType;
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

    private static TYPE getTypeFromInstance(PlotItem plotItem) {
        if (plotItem instanceof PlotLine) {
            return TYPE.LINE;
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
                plotItem.configureRenderer(this);
                fireChangeListeners();
            });

            editionPanel.appendChild(linePaintFieldLabel);
            editionPanel.appendChild(linePaintChooser);

            JLabel lineWidthLabel = new JLabel();
            lineWidthLabel.setText(T.text("line_width"));
            SimpleIntegerField lineWidthSlider = new SimpleIntegerField(1, 10, 1);
            lineWidthSlider.setValue(lineWidth);

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

            editionPanel.appendChild(lineDashFieldLabel);
            editionPanel.appendChild(lineDashCombobox);

        } else {
            JLabel nothingLabel = new JLabel("<NOT IMPLMENTED>");
            editionPanel.appendChild(nothingLabel);
        }

        return editionPanel;
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

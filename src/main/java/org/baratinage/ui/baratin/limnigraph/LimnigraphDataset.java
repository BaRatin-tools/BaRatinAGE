package org.baratinage.ui.baratin.limnigraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.time.LocalDateTime;
import java.util.List;

import org.baratinage.ui.component.ImportedDataset;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotTimeSeriesLine;
import org.jfree.data.time.Second;

// FIXME: do I need specific/generic data structure such as:
// - TimeSeriesMatrix
// - TimeSeriesVector
// - TimeSeriesTimestep
public class LimnigraphDataset extends ImportedDataset {

    private LocalDateTime[] dateTime;

    public LimnigraphDataset(String name, LocalDateTime[] dateTime, List<double[]> stage) {

        int nCol = stage.size();
        int nRow = dateTime.length;

        String[] headers = new String[nCol];
        for (int k = 0; k < stage.size(); k++) {
            if (stage.get(k).length != nRow) {
                throw new IllegalArgumentException("All stage vectors must match the length of the date/time vector!");
            }
            headers[k] = "h_" + (k + 1);
        }
        setData(stage, headers);
        setDatasetName(name);

        this.dateTime = dateTime;
    }

    public PlotItem[] getPlotLines() {
        int n = getNumberOfColumns();
        if (n <= 0) {
            return null;
        }
        Second[] timeVector = PlotTimeSeriesLine.localDateTimeToSecond(dateTime);
        PlotTimeSeriesLine[] tsLines = new PlotTimeSeriesLine[n];
        for (int k = 0; k < n; k++) {
            tsLines[k] = new PlotTimeSeriesLine(
                    name,
                    timeVector,
                    getStageVector(k),
                    Color.BLACK, new BasicStroke(1));
        }
        return tsLines;
    }

    public LocalDateTime[] getDateTimeVector() {
        return dateTime;
    }

    public LocalDateTime getDateTime(int rowIndex) {
        return dateTime[rowIndex];
    }

    public List<double[]> getStageMatrix() {
        return getData();
    }

    public double[] getStageVector(int index) {
        return getData().get(index);
    }

    public double getStageValue(int rowIndex, int colIndex) {
        return getData().get(colIndex)[rowIndex];
    }

}

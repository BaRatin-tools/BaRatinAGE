package org.baratinage.ui.baratin.limnigraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.time.LocalDateTime;
import java.util.List;

import org.baratinage.ui.component.ImportedDataset;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotTimeSeriesLine;
import org.baratinage.utils.DateTime;
import org.jfree.data.time.Second;

// FIXME: do I need specific/generic data structure such as:
// - TimeSeriesMatrix
// - TimeSeriesVector
// - TimeSeriesTimestep ?
public class LimnigraphDataset extends ImportedDataset {

    private LocalDateTime[] dateTime = null;

    public static LimnigraphDataset buildLimnigraphDataset(String name, LocalDateTime[] dateTime,
            List<double[]> stage) {

        int nCol = stage.size() + 1;
        int nRow = dateTime.length;

        String[] headers = new String[nCol];
        List<double[]> data = stage;
        data.add(0, DateTime.dateTimeToDoubleVector(dateTime));
        headers[0] = "Date/Time";
        for (int k = 0; k < stage.size(); k++) {
            if (stage.get(k).length != nRow) {
                throw new IllegalArgumentException("All stage vectors must match the length of the date/time vector!");
            }
            headers[k] = "h_" + k;
        }

        return new LimnigraphDataset(name, data, headers);
    }

    public static LimnigraphDataset buildLimnigraphDataset(String name, String dataFilePath) {
        return new LimnigraphDataset(name, dataFilePath);
    }

    private LimnigraphDataset(String name, List<double[]> data, String[] headers) {
        super(name, data, headers);
        if (data.size() > 0) {
            dateTime = DateTime.doubleToDateTimeVector(data.get(0));
        }
    }

    private LimnigraphDataset(String name, String dataFilePath) {
        super(name, dataFilePath);
        if (getNumberOfColumns() > 0) {
            dateTime = DateTime.doubleToDateTimeVector(getColumn(0));
        }
    }

    public PlotItem[] getPlotLines() {
        int n = getNumberOfColumns();
        if (n <= 0) {
            return null;
        }
        Second[] timeVector = PlotTimeSeriesLine.localDateTimeToSecond(dateTime);
        PlotTimeSeriesLine[] tsLines = new PlotTimeSeriesLine[n - 1];
        List<double[]> stageMatrix = getStageMatrix();
        for (int k = 0; k < n - 1; k++) {
            tsLines[k] = new PlotTimeSeriesLine(
                    getDatasetName(),
                    timeVector,
                    stageMatrix.get(k),
                    Color.BLACK, new BasicStroke(2));
        }
        return tsLines;
    }

    public double[] getDateTimeAsDouble() {
        return getColumn(0);
    }

    public LocalDateTime[] getDateTimeVector() {
        return dateTime;
    }

    public LocalDateTime getDateTime(int rowIndex) {
        dateTime = getDateTimeVector();
        return dateTime == null || rowIndex >= dateTime.length ? null : dateTime[rowIndex];
    }

    public List<double[]> getStageMatrix() {
        List<double[]> data = getData();
        if (data == null) {
            return null;
        }
        data.remove(0);// possible because getDate makes a shallow copy of data object
        return data;
    }

}

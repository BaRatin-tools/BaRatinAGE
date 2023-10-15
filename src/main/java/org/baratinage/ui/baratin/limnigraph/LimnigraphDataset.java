package org.baratinage.ui.baratin.limnigraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.component.ImportedDataset;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotTimeSeriesLine;
import org.baratinage.utils.DateTime;
import org.jfree.data.time.Second;
import org.json.JSONObject;

// FIXME: do I need specific/generic data structure such as:
// - TimeSeriesMatrix
// - TimeSeriesVector
// - TimeSeriesTimestep ?
public class LimnigraphDataset extends ImportedDataset {

    public static LimnigraphDataset buildLimnigraphDataset(
            String name,
            LocalDateTime[] dateTime,
            double[] stage) {
        int nRow = dateTime.length;
        if (stage.length != nRow) {
            System.err.println("LimnigraphDataset Error: stage vector length must match dateTime vector length!");
            return null;
        }
        List<double[]> data = new ArrayList<>();
        data.add(DateTime.dateTimeToDoubleVector(dateTime));
        data.add(stage);
        return new LimnigraphDataset(name, data, new String[] { "date_time", "stage" }, dateTime);
    }

    public static LimnigraphDataset buildFromJSON(JSONObject json) {
        return new LimnigraphDataset(json);
    }

    private LocalDateTime[] dateTime = null;
    private int nonSysErrStdIndex = -1;
    private int sysErrStdIndex = -1;
    private int sysErrResamplingIndicesIndex = -1;
    private int errorMatrixStartIndex = -1;
    private int errorMatrixSize = -1;
    private int[] sysErrInd = null;

    private LimnigraphDataset(String name, List<double[]> data, String[] headers, LocalDateTime[] dateTime) {
        super(name, data, headers);
        this.dateTime = dateTime;

    }

    private LimnigraphDataset(JSONObject json) {
        super(json);
        if (getNumberOfColumns() > 1) {
            dateTime = DateTime.doubleToDateTimeVector(getColumn(0));
        } else {
            System.err.println("LimnigraphDataset Error: At least two columns are expected for a limnigraph dataset!");
        }
        if (json.has("nonSysErrStdIndex")) {
            nonSysErrStdIndex = json.getInt("nonSysErrStdIndex");
        }
        if (json.has("nonSysErrStdIndex")) {
            sysErrStdIndex = json.getInt("sysErrStdIndex");
        }
        if (json.has("sysErrResamplingIndicesIndex")) {
            sysErrResamplingIndicesIndex = json.getInt("sysErrResamplingIndicesIndex");
            sysErrInd = toInt(getColumn(sysErrResamplingIndicesIndex));
        }
        if (json.has("errorMatrixStartIndex")) {
            errorMatrixStartIndex = json.getInt("errorMatrixStartIndex");
        }
        if (json.has("errorMatrixSize")) {
            errorMatrixSize = json.getInt("errorMatrixSize");
        } else {

        }
    }

    public void addNonSysError(double[] nonSysErrStd) {
        if (nonSysErrStd.length != getNumberOfRows()) {
            System.err.println("LimnigraphDataset Error: nonSysErrStd length doesn't match the number of timesteps!");
            return;
        }
        nonSysErrStdIndex = data.size();
        data.add(nonSysErrStd);
        headers.add("non_sys_std");
    }

    public void addSysError(double[] sysErrStd, int[] sysErrResampling) {
        int nRow = getNumberOfRows();
        if (sysErrStd.length != nRow) {
            System.err.println("LimnigraphDataset Error: sysErrStd length doesn't match the number of timesteps!");
            return;
        }
        if (sysErrResampling.length != nRow) {
            System.err
                    .println("LimnigraphDataset Error: sysErrResampling length doesn't match the number of timesteps!");
            return;
        }
        sysErrStdIndex = data.size();
        sysErrResamplingIndicesIndex = data.size() + 1;
        sysErrInd = sysErrResampling;
        data.add(sysErrStd);
        data.add(toDouble(sysErrResampling));
        headers.add("sys_std");
        headers.add("sys_resampling_indices");
    }

    public void addErrorMatrix(List<double[]> errorMatrix) {
        int nRow = getNumberOfRows();
        int nCol = errorMatrix.size();
        List<String> newHeaders = new ArrayList<>();
        for (int k = 0; k < nCol; k++) {
            if (errorMatrix.get(k).length != nRow) {
                System.err
                        .println(
                                "LimnigraphDataset Error: errorMatrix column #+ " + k
                                        + " length doesn't match the number of timesteps!");
                return;
            }
            newHeaders.add("h_" + (k + 1));
        }
        errorMatrixStartIndex = data.size();
        errorMatrixSize = nCol;
        data.addAll(errorMatrix);
        headers.addAll(newHeaders);
    }

    public PlotItem getPlotLine() {
        Second[] timeVector = PlotTimeSeriesLine.localDateTimeToSecond(dateTime);
        PlotTimeSeriesLine plotLine = new PlotTimeSeriesLine(
                getDatasetName(),
                timeVector,
                getStage(),
                Color.BLACK, new BasicStroke(2));

        return plotLine;
    }

    public double[] getDateTimeAsDouble() {
        return getColumn(0);
    }

    public LocalDateTime[] getDateTime() {
        return dateTime;
    }

    public double[] getStage() {
        return getColumn(1);
    }

    public double[] getNonSysErrStd() {
        if (!hasNonSysErr()) {
            return null;
        }
        return getColumn(nonSysErrStdIndex);
    }

    public double[] getSysErrStd() {
        if (!hasSysErr()) {
            return null;
        }
        return getColumn(sysErrStdIndex);
    }

    public int[] getSysErrInd() {
        if (!hasSysErr()) {
            return null;
        }
        return sysErrInd;
    }

    public List<double[]> getStageErrMatrix() {
        List<double[]> data = getData();
        if (!hasStageErrMatrix()) {
            return null;
        }
        return data.subList(errorMatrixStartIndex, errorMatrixStartIndex + errorMatrixSize);
    }

    public boolean hasNonSysErr() {
        return nonSysErrStdIndex != -1;
    }

    public boolean hasSysErr() {
        return sysErrStdIndex != -1 && sysErrResamplingIndicesIndex != -1;
    }

    public boolean hasStageErrMatrix() {
        return errorMatrixStartIndex != -1 && errorMatrixSize != -1;
    }

    @Override
    public JSONObject toJSON(BamProject project) {
        JSONObject json = super.toJSON(project);
        json.put("nonSysErrStdIndex", nonSysErrStdIndex);
        json.put("sysErrStdIndex", sysErrStdIndex);
        json.put("sysErrResamplingIndicesIndex", sysErrResamplingIndicesIndex);
        json.put("errorMatrixSize", errorMatrixSize);
        return json;
    }

}

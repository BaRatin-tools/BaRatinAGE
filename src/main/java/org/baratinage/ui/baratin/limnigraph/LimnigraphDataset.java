package org.baratinage.ui.baratin.limnigraph;

import java.time.LocalDateTime;
import java.util.List;

import org.baratinage.ui.component.ImportedDataset;

// FIXME: do I need specific/generic data structure suchg as:
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

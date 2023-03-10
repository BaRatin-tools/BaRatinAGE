package org.baratinage.ui;

import java.awt.Color;
import java.awt.Dimension;
// import java.awt.Point;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.baratinage.ui.container.FlexPanel;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotItem;

public class PredictionResultsPanel extends FlexPanel {

    DefaultListModel<PredictionResult> predictions;
    JList<PredictionResult> predictionsList;

    DefaultListModel<PredictionInput> inputs;
    JList<PredictionInput> inputsList;

    DefaultListModel<PredictionOutput> outputs;
    JList<PredictionOutput> outputsList;

    int maxpostIndex;
    int selectedPredictionIndex = -1;
    int selectedInputIndex = -1;
    int selectedOutputIndex = -1;

    FlexPanel plotPanel;

    public PredictionResultsPanel() {
        super(FlexPanel.AXIS.ROW);
        this.setGap(5);
        this.setPadding(5);
        predictions = new DefaultListModel<>();
        inputs = new DefaultListModel<>();
        outputs = new DefaultListModel<>();

        FlexPanel dataSelectorPanel = new FlexPanel(
                FlexPanel.AXIS.COL, FlexPanel.ALIGN.START);
        dataSelectorPanel.setGap(5);

        this.appendChild(dataSelectorPanel);

        // Prediction selection -------------------------------------
        JLabel predSelectTitle = new JLabel();
        predSelectTitle.setText("Select a prediction:");
        dataSelectorPanel.appendChild(predSelectTitle);

        predictionsList = new JList<>(predictions);
        JScrollPane predSelectListScroll = new JScrollPane(predictionsList);
        predSelectListScroll.setPreferredSize(new Dimension(200, 100));
        predSelectListScroll.setMinimumSize(new Dimension(200, 100));
        dataSelectorPanel.appendChild(predSelectListScroll);
        predictionsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        // predictionsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        predictionsList.setVisibleRowCount(-1);

        predictionsList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                System.out.println("Prediction Selection List Value Changed");
                PredictionResult p = predictionsList.getSelectedValue();

                inputs.clear();

                for (PredictionInput i : p.getPredictionConfig().getPredictionInputs()) {
                    inputs.addElement(i);
                }

                outputs.clear();

                for (PredictionOutput o : p.getPredictionConfig().getPredictionOutputs()) {
                    outputs.addElement(o);
                }

                selectedPredictionIndex = predictionsList.getSelectedIndex();
                selectedInputIndex = -1;
                selectedOutputIndex = -1;
            }

        });

        // Input selection -------------------------------------
        JLabel inputsSelectTitle = new JLabel();
        inputsSelectTitle.setText("Select an input:");
        dataSelectorPanel.appendChild(inputsSelectTitle);

        inputsList = new JList<>(inputs);
        JScrollPane inputsSelectListScroll = new JScrollPane(inputsList);
        inputsSelectListScroll.setPreferredSize(new Dimension(200, 100));
        inputsSelectListScroll.setMinimumSize(new Dimension(200, 100));
        dataSelectorPanel.appendChild(inputsSelectListScroll);
        inputsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        inputsList.setVisibleRowCount(-1);

        inputsList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectedInputIndex = inputsList.getSelectedIndex();
                updatePlot();
            }

        });

        // Output selection -------------------------------------
        JLabel outputsSelectTitle = new JLabel();
        outputsSelectTitle.setText("Select an output:");
        dataSelectorPanel.appendChild(outputsSelectTitle);

        outputsList = new JList<>(outputs);
        JScrollPane outputsSelectListScroll = new JScrollPane(outputsList);
        outputsSelectListScroll.setPreferredSize(new Dimension(200, 100));
        outputsSelectListScroll.setMinimumSize(new Dimension(200, 100));
        dataSelectorPanel.appendChild(outputsSelectListScroll);
        outputsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        outputsList.setVisibleRowCount(-1);

        outputsList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectedOutputIndex = outputsList.getSelectedIndex();
                updatePlot();
            }

        });

        plotPanel = new FlexPanel();
        this.appendChild(plotPanel, 1);

    }

    public void setPredictionResults(PredictionResult[] predictionResults, int maxpostIndex) {

        predictions.clear();

        for (PredictionResult p : predictionResults) {
            predictions.addElement(p);
        }

        // predictionsList.updateUI();
    }

    private void updatePlot() {
        plotPanel.removeAll();
        plotPanel.updateUI();

        System.out.println("UPDATE ALL");
        System.out.println("selectedInputIndex=" + selectedInputIndex);
        System.out.println("selectedOutputIndex=" + selectedOutputIndex);
        System.out.println("selectedPredictionIndex=" + selectedPredictionIndex);

        if (selectedInputIndex == -1 || selectedOutputIndex == -1 || selectedPredictionIndex == -1)
            return;

        PredictionResult r = predictions.get(selectedPredictionIndex);
        PredictionInput i = inputs.get(selectedInputIndex);
        PredictionOutput o = outputs.get(selectedOutputIndex);

        // int maxpostIndex = r.get
        double[] x = i.getDataColumns().get(0);
        double[] y = r.getOutputResults().get(o.getName()).spag().get(maxpostIndex);
        // PredictionOutputResult

        Plot plot = new Plot(i.getName(), o.getName(), true);

        plot.addXYItem(new PlotPoints(
                "Maxpost",
                x,
                y,
                Color.GREEN,
                PlotItem.SHAPE.CIRCLE,
                5));

        PlotContainer plotContainer = new PlotContainer(plot.getChart());
        plotPanel.appendChild(plotContainer, 1);

        plotPanel.updateUI();
        System.out.println("DONE");
    }
}

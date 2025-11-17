package org.baratinage.ui.commons;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.EstimatedParameterWrapper;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.WriteFile;

public class McmcTraceResultsPanel extends SimpleFlowPanel {
    public final TracePlotGrid paramTracePlots;
    private final BamProject project;

    private List<EstimatedParameterWrapper> parameters;

    public McmcTraceResultsPanel(BamProject project) {
        super(true);
        this.project = project;
        paramTracePlots = new TracePlotGrid();

        SimpleFlowPanel actionPanel = new SimpleFlowPanel();

        JButton mcmcToCsvButton = new JButton();
        mcmcToCsvButton.setIcon(AppSetup.ICONS.SAVE);

        mcmcToCsvButton.addActionListener((e) -> saveMcmcFile());

        actionPanel.addChild(mcmcToCsvButton, false);

        addChild(actionPanel, 0, 5);
        addChild(paramTracePlots, 1);

        T.t(this, () -> {
            mcmcToCsvButton.setText(T.text("export_mcmc"));
            mcmcToCsvButton.setToolTipText(T.text("export_mcmc"));
        });

        T.updateHierarchy(this, paramTracePlots);
    }

    public void updateResults(List<EstimatedParameterWrapper> parameters) {
        this.parameters = parameters;
        updatePlots();
    }

    private void updatePlots() {
        // update trace plots
        paramTracePlots.clearPlots();
        for (EstimatedParameterWrapper p : parameters) {
            paramTracePlots.addPlot(p);
        }
        paramTracePlots.updatePlots();

    }

    private void saveMcmcFile() {

        File f = CommonDialog.saveFileDialog("MCMC_results_" +
                project.getProjectName(), T.text("export_mcmc"),
                new CommonDialog.CustomFileFilter(T.text("csv_format"), "csv", "CSV"));

        if (f != null) {
            // int n = parameters.size() + gammas.size() + 1;
            int n = parameters.size();
            List<double[]> matrix = new ArrayList<>(n);
            List<String> headers = new ArrayList<>();

            for (EstimatedParameterWrapper p : parameters) {
                matrix.add(p.parameter.mcmc);
                headers.add(p.symbol);
            }

            try {
                WriteFile.writeMatrix(
                        f.getAbsolutePath(),
                        matrix,
                        ",",
                        "-9999",
                        headers.toArray(new String[n]));
            } catch (IOException ioe) {
                ConsoleLogger.error("error while exporting MCMC simulation\n" + ioe);
            }
        }
    }

}

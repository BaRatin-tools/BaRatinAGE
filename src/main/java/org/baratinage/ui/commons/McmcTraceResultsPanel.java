package org.baratinage.ui.commons;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.WriteFile;

public class McmcTraceResultsPanel extends RowColPanel {
    private final TracePlotGrid paramTracePlots;
    private final BamProject project;

    private List<BamEstimatedParameter> parameters;
    private List<BamEstimatedParameter> gammas;
    private BamEstimatedParameter logPost;

    public McmcTraceResultsPanel(BamProject project) {
        super(AXIS.COL);
        this.project = project;
        paramTracePlots = new TracePlotGrid();

        RowColPanel actionPanel = new RowColPanel(RowColPanel.AXIS.ROW,
                RowColPanel.ALIGN.START);

        JButton mcmcToCsvButton = new JButton();
        mcmcToCsvButton.setIcon(AppSetup.ICONS.SAVE);

        mcmcToCsvButton.addActionListener((e) -> saveMcmcFile());

        actionPanel.appendChild(mcmcToCsvButton, 0);

        appendChild(actionPanel, 0, 5);
        appendChild(paramTracePlots, 1);

        T.t(this, () -> {
            mcmcToCsvButton.setText(T.text("export_mcmc"));
            mcmcToCsvButton.setToolTipText(T.text("export_mcmc"));
        });

        T.updateHierarchy(this, paramTracePlots);
    }

    public void updateResults(List<BamEstimatedParameter> parameters,
            List<BamEstimatedParameter> gammas,
            BamEstimatedParameter logPost) {

        this.parameters = parameters;
        this.gammas = gammas;
        this.logPost = logPost;

        updatePlots();
    }

    private void updatePlots() {
        // update trace plots
        paramTracePlots.clearPlots();
        for (BamEstimatedParameter p : parameters) {
            paramTracePlots.addPlot(p);
        }
        for (BamEstimatedParameter p : gammas) {
            paramTracePlots.addPlot(p);
        }
        paramTracePlots.addPlot(logPost);
        paramTracePlots.updatePlots();

    }

    private void saveMcmcFile() {

        File f = CommonDialog.saveFileDialog("MCMC_results_" +
                project.getProjectName(), T.text("export_mcmc"),
                new CommonDialog.CustomFileFilter(T.text("csv_format"), "csv", "CSV"));

        if (f != null) {
            int n = parameters.size() + gammas.size() + 1;
            List<double[]> matrix = new ArrayList<>(n);
            List<String> headers = new ArrayList<>();

            for (BamEstimatedParameter p : parameters) {
                matrix.add(p.mcmc);
                headers.add(p.shortName);
            }

            for (BamEstimatedParameter p : gammas) {
                matrix.add(p.mcmc);
                headers.add(p.name);
            }

            matrix.add(logPost.mcmc);
            headers.add(logPost.shortName);

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

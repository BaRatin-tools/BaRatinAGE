package org.baratinage.ui.component.data_import;

import java.awt.Dimension;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.AbstractDataset;
import org.baratinage.ui.component.DataFileReader;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.Misc;

public abstract class DataImporter extends SimpleFlowPanel {

  protected JDialog dialog;
  protected final DataFileReader dataFileReader;
  protected final DataPreview dataPreview;
  protected final JButton validateButton;
  protected final SimpleFlowPanel errorPanel;
  protected final SimpleFlowPanel mainConfigPanel;

  protected final String ID = Misc.getTimeStampedId();

  public DataImporter() {
    super(true);

    dataFileReader = new DataFileReader();

    dataPreview = new DataPreview();

    validateButton = new JButton();

    SimpleFlowPanel actionPanel = new SimpleFlowPanel();
    actionPanel.setPadding(5);
    JButton cancelButton = new JButton();
    actionPanel.addChild(cancelButton, false);
    actionPanel.addExtensor();
    actionPanel.addChild(validateButton, false);

    mainConfigPanel = new SimpleFlowPanel();

    errorPanel = new SimpleFlowPanel(true);
    errorPanel.setGap(5);
    errorPanel.setPadding(5);

    addChild(dataFileReader, false);
    addChild(new SimpleSep(), false);
    addChild(mainConfigPanel, false);
    addChild(errorPanel, false);
    addChild(new SimpleSep(), false);
    addChild(dataPreview, true);
    addChild(new SimpleSep(), false);
    addChild(actionPanel, false);

    T.updateHierarchy(this, dataFileReader);
    T.updateHierarchy(this, dataPreview);
    T.t(this, validateButton, false, "import");
    T.t(this, cancelButton, false, "cancel");

    // when file selection changes

    dataFileReader.addChangeListener(l -> {
      if (dataFileReader.getFile() == null) {
        return;
      }

      List<String[]> data = dataFileReader.getData();
      String[] headers = dataFileReader.getHeaders();
      String missingValue = dataFileReader.getMissingValue();

      dataPreview.setData(data, headers);
      dataPreview.updatePreviewTable();
      applyInputFileChange(data, headers, missingValue);
    });

    // whenever the user click import / cancel

    cancelButton.addActionListener(e -> {
      dialog.setVisible(false);
    });

    validateButton.addActionListener(e -> {
      dialog.setVisible(false);
      if (dataFileReader.getFile() == null) {
        return;
      }
      buildDataset();
    });

  }

  protected abstract void applyInputFileChange(List<String[]> data, String[] headers, String missingValue);

  protected abstract void buildDataset();

  public abstract AbstractDataset getDataset();

  protected static String buildErrorMessage(Set<Integer> invalidIndices) {
    String msg = invalidIndices == null || invalidIndices.size() == 0 ? T.text("invalid_column_import_config")
        : T.text("invalid_column_invalid_rows",
            invalidIndices.size(),
            Misc.createIntegerStringList(
                invalidIndices.stream().map(i -> i + 1).toList(),
                5));
    return msg;
  }

  public void showDialog(String title) {

    dialog = new JDialog(AppSetup.MAIN_FRAME, true);
    dialog.setContentPane(this);

    dialog.setTitle(title);
    dialog.setMinimumSize(new Dimension(600, 400));
    dialog.setPreferredSize(new Dimension(900, 800));

    dialog.pack();
    dialog.setLocationRelativeTo(AppSetup.MAIN_FRAME);
    dialog.setVisible(true);
    dialog.dispose();
  }

}

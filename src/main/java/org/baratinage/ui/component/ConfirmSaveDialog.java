package org.baratinage.ui.component;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.bam.BamProjectSaver;
import org.baratinage.ui.container.SimpleFlowPanel;

public class ConfirmSaveDialog extends JDialog {

  public enum Result {
    SAVE, SAVE_AS, DO_NO_SAVE, CANCEL
  };

  private Result result = Result.CANCEL;

  public ConfirmSaveDialog(JFrame parent, BamProject currentProject) {
    super(parent, T.text("unsaved_changes_will_be_lost"), true);
    SimpleFlowPanel dialogPanel = new SimpleFlowPanel(true);
    dialogPanel.setPadding(10);
    dialogPanel.setGap(5);
    JLabel messageLabel = new JLabel(T.text("save_change_before_closing"));
    SimpleFlowPanel actionsPanel = new SimpleFlowPanel(true);
    actionsPanel.setGap(5);
    JButton saveBtn = new JButton(T.text("save_project"));
    JButton saveAsBtn = new JButton(T.text("save_project_as"));
    JButton doNotSaveBtn = new JButton(T.text("close_without_saving"));
    doNotSaveBtn.setForeground(AppSetup.COLORS.DANGER);
    JButton cancelBtn = new JButton(T.text("cancel"));
    actionsPanel.addChild(saveBtn, 1);
    actionsPanel.addChild(saveAsBtn, 0);
    actionsPanel.addChild(doNotSaveBtn, 0);
    actionsPanel.addChild(cancelBtn, 0);
    dialogPanel.addChild(messageLabel, 1);
    dialogPanel.addChild(actionsPanel, 0);

    ProgressFrame progressFrame = new ProgressFrame(this);

    saveBtn.addActionListener(l -> {
      result = Result.SAVE;
      BamProjectSaver.saveProject(progressFrame, currentProject, false,
          () -> {
            dispose();
          },
          null);
    });
    saveAsBtn.addActionListener(l -> {
      result = Result.SAVE_AS;
      BamProjectSaver.saveProject(progressFrame, currentProject, true,
          () -> {
            dispose();
          },
          null);
    });
    doNotSaveBtn.addActionListener(l -> {
      result = Result.DO_NO_SAVE;
      dispose();
    });
    cancelBtn.addActionListener(l -> {
      result = Result.CANCEL;
      dispose();
    });

    setResizable(false);
    setContentPane(dialogPanel);
    pack();
    setLocationRelativeTo(AppSetup.MAIN_FRAME);

  }

  public Result getResult() {
    return this.result;
  }
}

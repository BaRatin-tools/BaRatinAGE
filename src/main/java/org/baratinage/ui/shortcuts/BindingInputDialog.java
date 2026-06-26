package org.baratinage.ui.shortcuts;

import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.baratinage.translation.T;
import org.baratinage.ui.container.SimpleFlowPanel;

public class BindingInputDialog extends JDialog {

  private Binding binding;
  private Shortcut initialShortcut;
  private final JTextField field;

  public static Binding showDialog(Window owner, Binding current) {
    BindingInputDialog dlg = new BindingInputDialog(owner, current);
    dlg.setVisible(true);
    return dlg.binding;
  }

  private BindingInputDialog(
      Window owner,
      Binding current) {
    super(owner, T.text("shortcut_edit"), ModalityType.APPLICATION_MODAL);

    binding = current;
    initialShortcut = binding.getShortcut();

    JLabel label = new JLabel(T.text("shortcut_edit_action"));

    field = new JTextField(20);
    field.setEditable(false);
    field.setHorizontalAlignment(JTextField.CENTER);

    if (current != null) {
      field.setText(current.getShortcut().toDisplayString());
    }

    field.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            Shortcut newShortcut = Shortcut.of(e.getKeyCode(), e.getModifiersEx());
            binding.setShortcut(newShortcut);
            field.setText(binding.getShortcut().toDisplayString());

            e.consume();
          }
        });

    JButton clear = new JButton(T.text("clear"));

    clear.addActionListener(e -> {
      binding.setShortcut(null);
      ;
      field.setText("");
    });

    JButton ok = new JButton(T.text("ok"));
    ok.addActionListener(e -> dispose());

    JButton cancel = new JButton(T.text("cancel"));

    cancel.addActionListener(e -> {
      binding.setShortcut(initialShortcut);
      dispose();
    });

    SimpleFlowPanel buttonsPanel = new SimpleFlowPanel();
    buttonsPanel.setGap(5);

    // buttonsPanel.addExtensor();
    buttonsPanel.addChild(clear);
    buttonsPanel.addChild(cancel);
    buttonsPanel.addChild(ok);

    SimpleFlowPanel mainPanel = new SimpleFlowPanel(true);
    mainPanel.setPadding(5);
    mainPanel.setGap(5);

    mainPanel.addChild(label, 0);
    mainPanel.addChild(field, 0);
    mainPanel.addChild(buttonsPanel, 0);

    setContentPane(mainPanel);

    pack();
    setLocationRelativeTo(owner);

    SwingUtilities.invokeLater(
        field::requestFocusInWindow);
  }
}
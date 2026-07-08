package org.baratinage.ui.shortcuts;

import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleDialog;
import org.baratinage.ui.container.SimpleFlowPanel;

public class BindingInputDialog extends JDialog {

  private Binding binding;
  private Shortcut newShortcut;
  private final JTextField field;

  public static Binding showDialog(Window owner, Binding current, Map<String, Binding> bindings) {
    BindingInputDialog dlg = new BindingInputDialog(owner, current, bindings);
    dlg.setVisible(true);
    return dlg.binding;
  }

  private BindingInputDialog(
      Window owner,
      Binding current,
      Map<String, Binding> bindings) {
    super(owner, T.text("shortcut_edit"), ModalityType.APPLICATION_MODAL);

    binding = current;
    newShortcut = binding.getShortcut();

    JLabel label = new JLabel(T.text("shortcut_edit_action"));

    field = new JTextField(20);
    field.setEditable(false);
    field.setHorizontalAlignment(JTextField.CENTER);

    if (current != null) {
      Shortcut shortcut = current.getShortcut();
      field.setText(shortcut == null ? "" : shortcut.toDisplayString());
    }

    field.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (Shortcut.isModifierKey(e.getKeyCode())) {
              return;
            }
            newShortcut = Shortcut.of(e.getKeyCode(), e.getModifiersEx());
            field.setText(newShortcut.toDisplayString());
            e.consume();
          }
        });

    JButton clear = new JButton(T.text("clear"));

    clear.addActionListener(e -> {
      binding.setShortcut(null);
      field.setText("");
    });

    JButton ok = new JButton(T.text("ok"));
    ok.addActionListener(e -> {
      Set<Binding> bindingsUsingSameShortcut = new HashSet<>();
      for (Binding b : bindings.values()) {
        Shortcut s = b.getShortcut();
        if (b != binding && s != null && s.equals(newShortcut)) {
          bindingsUsingSameShortcut.add(b);
        }
      }
      if (bindingsUsingSameShortcut.size() > 0) {
        SimpleDialog.buildOkCancelDialog(
            this,
            T.text("are_you_sure"),
            new JLabel(
                "<html><div style='width:200px'>%s</div></html>".formatted(T.text("shortcut_already_used"))),
            (event) -> {
              bindingsUsingSameShortcut.forEach(b -> b.setShortcut(null));
              binding.setShortcut(newShortcut);
              dispose();
            }, (event) -> {
            }).openDialog();
      } else {
        binding.setShortcut(newShortcut);
        dispose();
      }
    });

    JButton cancel = new JButton(T.text("cancel"));

    cancel.addActionListener(e -> dispose());

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
package org.baratinage.ui.shortcuts;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import org.baratinage.translation.T;
import org.baratinage.ui.container.SimpleFlowPanel;

import com.formdev.flatlaf.FlatClientProperties;

public class ShortcutDialog extends JDialog {

  private final ShortcutTableModel model;
  private final JTable table;

  public ShortcutDialog(
      Window owner,
      ShortcutManager shortctuManager) {
    super(owner, T.text("shortcuts"), ModalityType.APPLICATION_MODAL);

    this.model = new ShortcutTableModel(shortctuManager.bindings);
    this.table = new JTable(model);

    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setFillsViewportHeight(true);

    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2
            && SwingUtilities.isLeftMouseButton(e)) {
          editSelectedShortcut();
        }
      }
    });

    JTextField searchField = new JTextField(20);
    searchField.putClientProperty(
        FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON,
        true);
    searchField.putClientProperty(
        FlatClientProperties.PLACEHOLDER_TEXT,
        T.text("shortcut_search"));

    TableRowSorter<ShortcutTableModel> sorter = new TableRowSorter<>(model);

    table.setRowSorter(sorter);

    searchField.getDocument().addDocumentListener(
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            update();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            update();
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            update();
          }

          private void update() {
            String text = searchField.getText().trim();
            if (text.isEmpty()) {
              sorter.setRowFilter(null);
              return;
            }
            String lower = text.toLowerCase();
            sorter.setRowFilter(new RowFilter<>() {
              @Override
              public boolean include(
                  Entry<? extends ShortcutTableModel, ? extends Integer> entry) {
                ShortcutEntry e = model.getEntry(
                    entry.getIdentifier());
                return e.id().toLowerCase()
                    .contains(lower)
                    || e.context().toLowerCase()
                        .contains(lower)
                    || e.action().toLowerCase()
                        .contains(lower)
                    || e.binding().getShortcut().toDisplayString()
                        .toLowerCase()
                        .contains(lower);
              }
            });
          }
        });

    JButton editButton = new JButton(T.text("edit"));
    editButton.addActionListener(e -> editSelectedShortcut());

    JButton closeButton = new JButton(T.text("exit"));
    closeButton.addActionListener(e -> dispose());

    SimpleFlowPanel buttonsPanel = new SimpleFlowPanel();
    buttonsPanel.setGap(5);
    buttonsPanel.addExtensor();
    buttonsPanel.addChild(editButton, 0);
    buttonsPanel.addChild(closeButton, 0);

    SimpleFlowPanel mainPanel = new SimpleFlowPanel(true);
    mainPanel.setPadding(5);
    mainPanel.setGap(5);
    mainPanel.addChild(searchField, 0);
    mainPanel.addChild(new JScrollPane(table), 1);
    mainPanel.addChild(buttonsPanel, 0);

    setContentPane(mainPanel);
    setPreferredSize(new Dimension(700, 400));
    pack();
    setLocationRelativeTo(owner);
  }

  private void editSelectedShortcut() {
    int row = table.getSelectedRow();
    if (row < 0) {
      return;
    }

    row = table.convertRowIndexToModel(row);

    ShortcutEntry entry = model.getEntry(row);

    Binding newBinding = BindingInputDialog.showDialog(
        this,
        entry.binding());

    if (newBinding == null) {
      return;
    }

    // bindings.put(entry.id(), newBinding);
    model.refresh();
  }

  public record ShortcutEntry(
      String id,
      String context,
      String action,
      Binding binding) {
  }

  public class ShortcutTableModel extends AbstractTableModel {

    private final Map<String, Binding> bindings;
    private final List<ShortcutEntry> entries = new ArrayList<>();

    public ShortcutTableModel(
        Map<String, Binding> bindings) {
      this.bindings = bindings;
      rebuild();
    }

    public void refresh() {
      rebuild();
      fireTableDataChanged();
    }

    private void rebuild() {
      entries.clear();

      bindings.entrySet()
          .stream()
          .forEach(e -> {
            String id = e.getKey();
            int dot = id.indexOf('.');
            String context;
            String action;
            if (dot < 0) {
              context = "";
              action = id;
            } else {
              context = id.substring(0, dot);
              action = id.substring(dot + 1);
            }
            entries.add(
                new ShortcutEntry(
                    id,
                    T.text("shortcut_context_%s".formatted(context)),
                    T.text(action),
                    e.getValue()));
          });
    }

    public ShortcutEntry getEntry(int row) {
      return entries.get(row);
    }

    @Override
    public int getRowCount() {
      return entries.size();
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public String getColumnName(int column) {
      return switch (column) {
        case 0 -> T.text("shortcut_action");
        case 1 -> T.text("shortcut");
        case 2 -> T.text("shortcut_context");
        default -> "";
      };
    }

    @Override
    public Object getValueAt(
        int rowIndex,
        int columnIndex) {
      ShortcutEntry e = entries.get(rowIndex);

      return switch (columnIndex) {
        case 0 -> e.action();
        case 1 -> e.binding().getShortcut().toDisplayString();
        case 2 -> e.context();
        default -> null;
      };
    }
  }
}
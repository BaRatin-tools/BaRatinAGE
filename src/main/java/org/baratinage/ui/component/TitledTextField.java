package org.baratinage.ui.component;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.baratinage.ui.container.RowColPanel;

public class TitledTextField extends RowColPanel {

    @FunctionalInterface
    public interface TextChangeListener extends EventListener {
        public void hasChanged(String newText);
    }

    JTextField textField;
    JLabel titleLabel;

    List<TextChangeListener> textChangeListeners;

    public TitledTextField(String title) {
        super(AXIS.ROW);

        this.textChangeListeners = new ArrayList<>();

        titleLabel = new JLabel(title + ": ");
        // Dimension labelDim = titleLabel.getPreferredSize();
        // titleLabel.setPreferredSize(new Dimension(Math.max(labelDim.width, 100),
        // labelDim.height));
        textField = new JTextField();

        // textField.setPreferredSize(new Dimension(100, 0));
        appendChild(titleLabel);
        appendChild(textField, 1);

        textField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                System.out.println("INSERT >>> '" + textField.getText() + "'");
                fireChangeListeners();

            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                System.out.println("REMOVE >>> '" + textField.getText() + "'");
                fireChangeListeners();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                System.out.println("CHANGE >>> '" + textField.getText() + "'");
                fireChangeListeners();
            }

        });
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public String getText() {
        return textField.getText();
    }

    public void addChangeListener(TextChangeListener listener) {
        this.textChangeListeners.add(listener);
    }

    public void removeChangeListener(TextChangeListener listener) {
        this.textChangeListeners.remove(listener);
    }

    public void fireChangeListeners() {
        for (TextChangeListener cl : this.textChangeListeners) {
            cl.hasChanged(textField.getText());
        }
    }

}

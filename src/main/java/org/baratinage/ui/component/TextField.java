package org.baratinage.ui.component;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TextField extends JTextField {

    @FunctionalInterface
    public interface TextChangeListener extends EventListener {
        public void hasChanged(String newText);
    }

    List<TextChangeListener> textChangeListeners;

    public TextField() {
        super();

        this.textChangeListeners = new ArrayList<>();

        this.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                // System.out.println("INSERT >>> '" + textField.getText() + "'");
                fireChangeListeners();

            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // System.out.println("REMOVE >>> '" + textField.getText() + "'");
                fireChangeListeners();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // System.out.println("CHANGE >>> '" + textField.getText() + "'");
                fireChangeListeners();
            }

        });
    }

    public void addChangeListener(TextChangeListener listener) {
        this.textChangeListeners.add(listener);
    }

    public void removeChangeListener(TextChangeListener listener) {
        this.textChangeListeners.remove(listener);
    }

    public void fireChangeListeners() {
        for (TextChangeListener cl : this.textChangeListeners) {
            cl.hasChanged(getText());
        }
    }

}

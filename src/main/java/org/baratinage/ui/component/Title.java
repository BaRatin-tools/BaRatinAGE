package org.baratinage.ui.component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class Title extends JLabel {

    public Title() {
        super();
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    public Title(String text) {
        this();
        setText(text);
    }

    public Title(Icon icon, String text) {
        this();
        setIcon(icon);
        setText(text);
    }

    @Override
    public void setText(String text) {
        String t = "<html><b>%s</b></html>"
                .formatted(text);
        super.setText(t);
    }
}

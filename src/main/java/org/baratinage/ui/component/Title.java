package org.baratinage.ui.component;

import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class Title extends JLabel {

    private static Font font = new JLabel().getFont().deriveFont(Font.BOLD, 16f);

    public Title() {
        super();
        setFont(font);
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    public Title(Icon icon, String text) {
        this();
        setIcon(icon);
        setText(text);
    }
}

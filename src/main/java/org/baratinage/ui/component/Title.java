package org.baratinage.ui.component;

import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class Title extends JLabel {

    private static Font font = new Font(Font.SANS_SERIF, Font.BOLD, 16);

    public Title(Icon icon, String text) {
        super();
        setFont(font);
        setIcon(icon);
        setText(text);

        setBorder(new EmptyBorder(5, 5, 5, 5));

    }
}

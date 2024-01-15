package org.baratinage.ui.commons;

import javax.swing.JDialog;
import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.perf.TimedActions;

import com.formdev.flatlaf.ui.FlatButtonBorder;

public class ToasterMessage {

    public static void info(String message) {
        info(message, 2500);
    }

    public static void info(String message, int durationInMilliseconds) {

        JLabel label = new JLabel(message);
        RowColPanel toasterContent = new RowColPanel();
        toasterContent.setBackground(AppSetup.COLORS.INFO_BG);
        toasterContent.setBorder(new FlatButtonBorder());
        toasterContent.setPadding(10);
        toasterContent.appendChild(label);

        JDialog toaster = new JDialog(AppSetup.MAIN_FRAME);
        toaster.setUndecorated(true);
        toaster.setResizable(false);
        toaster.add(toasterContent);
        toaster.pack();
        toaster.setVisible(true);

        int h1 = AppSetup.MAIN_FRAME.getHeight();
        int y1 = AppSetup.MAIN_FRAME.getY();
        int x1 = AppSetup.MAIN_FRAME.getX();
        int h2 = toaster.getHeight();
        int margin = 20;
        toaster.setLocationRelativeTo(AppSetup.MAIN_FRAME);
        toaster.setLocation(x1 + margin, y1 + h1 - h2 - margin);

        TimedActions.delay(durationInMilliseconds, () -> {
            toaster.setVisible(false);
        });
    }

}

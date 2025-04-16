package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.translation.T;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.plot.PlotUtils;

public class SimpleColorField extends RowColPanel {

    private final JButton button;
    private Color currentColor;
    private static int colorIconSize = 20;

    public SimpleColorField() {
        currentColor = Color.BLACK;

        button = new JButton();

        button.setIcon(buildColorIcon(currentColor, colorIconSize));
        button.addActionListener(l -> {
            editColorPopup();
        });

        appendChild(button, 1);
    }

    public void setColor(Paint color) {
        if (color instanceof Color) {
            setColor((Color) color);
        }
    }

    public void setColor(Color color) {
        currentColor = color;
        button.setIcon(buildColorIcon(currentColor, colorIconSize));
    }

    public Color getColor() {
        return currentColor;
    }

    private static Icon buildColorIcon(Color color, int size) {
        Rectangle shape = new Rectangle(
                -size / 2,
                -size / 2,
                size, size);
        return new ImageIcon(PlotUtils.buildImageFromShape(shape, color, size, size));
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    private void fireChangeListeners() {
        for (ChangeListener cl : changeListeners) {
            cl.stateChanged(new ChangeEvent(this));
        }
    }

    private void editColorPopup() {

        setUI();

        RowColPanel colorChooserPanel = new RowColPanel(RowColPanel.AXIS.COL);

        RowColPanel previewPanel = new RowColPanel();
        previewPanel.setPadding(5);
        JLabel previewLabel = new JLabel(buildColorIcon(currentColor, colorIconSize));
        previewPanel.appendChild(previewLabel);

        JColorChooser colorChooser = new JColorChooser();

        colorChooser.setPreviewPanel(new JPanel());
        colorChooser.removeChooserPanel(colorChooser.getChooserPanels()[4]);
        colorChooser.removeChooserPanel(colorChooser.getChooserPanels()[2]);
        colorChooser.removeChooserPanel(colorChooser.getChooserPanels()[0]);

        colorChooser.setColor(currentColor);

        colorChooser.getSelectionModel().addChangeListener(l -> {
            previewLabel.setIcon(buildColorIcon(colorChooser.getColor(), colorIconSize));
        });

        colorChooserPanel.appendChild(colorChooser, 1);
        colorChooserPanel.appendChild(previewPanel, 0);

        SimpleDialog colorChooserDialog = SimpleDialog.buildOkCancelDialog(T.text("choose_a_color"), colorChooserPanel,
                (l) -> {
                    currentColor = colorChooser.getColor();
                    button.setIcon(buildColorIcon(currentColor, colorIconSize));
                    fireChangeListeners();
                }, (l) -> {
                });

        colorChooserDialog.openDialog();
    }

    private static void setUI() {
        UIManager.put("ColorChooser.preview", T.text("preview"));

        UIManager.put("ColorChooser.hsvNameText", T.text("colorchooser_hsv"));
        UIManager.put("ColorChooser.hsvHueText", T.text("colorchooser_hue"));
        UIManager.put("ColorChooser.hsvSaturationText", T.text("colorchooser_saturation"));
        UIManager.put("ColorChooser.hsvValueText", T.text("colorchooser_value"));
        UIManager.put("ColorChooser.hsvTransparencyText", T.text("colorchooser_transparency"));

        UIManager.put("ColorChooser.rgbNameText", T.text("colorchooser_rgb"));
        UIManager.put("ColorChooser.rgbRedText", T.text("colorchooser_red"));
        UIManager.put("ColorChooser.rgbGreenText", T.text("colorchooser_green"));
        UIManager.put("ColorChooser.rgbBlueText", T.text("colorchooser_blue"));
        UIManager.put("ColorChooser.rgbAlphaText", T.text("colorchooser_alpha"));
    }

}

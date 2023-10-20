package org.baratinage.ui.component;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.baratinage.translation.T;
import org.baratinage.ui.AppConfig;

public class SaveDialog {

    public static File openSaveDialog(String formatName, String... extensions) {

        JFileChooser fileChooser = new JFileChooser();

        if (extensions.length == 0) {
            System.err.println("SaveDialog Error: at least one extension must be provided");
            return null;
        }

        fileChooser.setFileFilter(
                new FileNameExtensionFilter(
                        formatName,
                        extensions));

        fileChooser.showSaveDialog(AppConfig.AC.APP_MAIN_FRAME);
        File file = fileChooser.getSelectedFile();
        if (file == null)
            return null;
        if (file.exists()) {
            String[] yesNoOptions = new String[] {
                    T.text("ok", "cancel")
            };
            int response = JOptionPane.showOptionDialog(AppConfig.AC.APP_MAIN_FRAME,
                    T.text("file_already_exists_overwrite"),
                    T.text("overwrite_file"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, yesNoOptions, yesNoOptions[1]);
            if (response != JOptionPane.YES_OPTION) {
                return null;
            }
        }
        file = addExtension(file, extensions);
        return file;

    }

    private static File addExtension(File file, String... extensions) {
        String path = file.getAbsolutePath();
        String lowerCasePath = path.toLowerCase();
        boolean addExt = true;
        for (String ext : extensions) {
            if (lowerCasePath.endsWith(ext.toLowerCase())) {
                addExt = false;
            }
        }
        if (addExt) {
            path = path + "." + extensions[0];
            file = new File(path);
        }
        return file;
    }
}

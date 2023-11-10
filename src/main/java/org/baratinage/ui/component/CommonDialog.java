package org.baratinage.ui.component;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.baratinage.translation.T;
import org.baratinage.ui.AppConfig;

public class CommonDialog {
    private static String defaultOpenTitle = "Open";
    private static String defaultSaveTitle = "Save";
    private static String defaultApproveButtonToolTipText = "Ok";

    public static void init() {
        T.t(AppConfig.AC.APP_MAIN_FRAME, () -> {
            UIManager.put("FileChooser.openButtonText", T.text("open"));
            UIManager.put("FileChooser.lookInLabelText", T.text("look_in"));

            UIManager.put("FileChooser.saveButtonText", T.text("save"));
            UIManager.put("FileChooser.saveInLabelText", T.text("save_in"));

            UIManager.put("FileChooser.cancelButtonText", T.text("cancel"));
            UIManager.put("FileChooser.cancelButtonToolTipText", T.text("cancel"));

            UIManager.put("FileChooser.fileNameLabelText", T.text("file_name"));
            UIManager.put("FileChooser.filesOfTypeLabelText", T.text("file_type"));
            UIManager.put("FileChooser.acceptAllFileFilterText", T.text("all_files"));
            UIManager.put("FileChooser.upFolderToolTipText", T.text("parent_folder"));
            UIManager.put("FileChooser.homeFolderToolTipText", T.text("desktop"));
            UIManager.put("FileChooser.newFolderToolTipText", T.text("new_folder"));

            // UIManager.put("FileChooser.other.newFolder", "Nouveau");
            // UIManager.put("FileChooser.win32.newFolder", "Nouveau"); // default name of
            // the folder

            UIManager.put("FileChooser.refreshActionLabelText", T.text("refresh"));
            UIManager.put("FileChooser.viewMenuLabelText", T.text("view"));
            UIManager.put("FileChooser.newFolderActionLabelText", T.text("new_folder"));

            // UIManager.put("FileChooser.renameErrorTitleText", "Impossible de renommer le
            // fichier ou dossier");
            // UIManager.put("FileChooser.renameErrorText", "Impossible de renommer");
            // UIManager.put("FileChooser.renameErrorFileExistsText",
            // "Un fichier ou dossier du même nom existe déjà ou le nom spécifié n'est pas
            // valide");

            UIManager.put("FileChooser.viewMenuToolTipText", T.text("view"));
            UIManager.put("FileChooser.listViewActionLabelText", T.text("list_view"));
            UIManager.put("FileChooser.detailsViewActionLabelText", T.text("details_view"));

            defaultOpenTitle = T.text("open");
            defaultSaveTitle = T.text("save");
            defaultApproveButtonToolTipText = T.text("ok");
        });
    }

    public static void errorDialog(String message) {
        JOptionPane.showOptionDialog(AppConfig.AC.APP_MAIN_FRAME,
                message,
                T.text("error"),
                JOptionPane.OK_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[] { T.text("ok") },
                "");
    }

    public static boolean confirmDialog(String message, String title) {
        String[] yesNoOptions = new String[] {
                T.text("ok"),
                T.text("cancel")
        };
        int response = JOptionPane.showOptionDialog(AppConfig.AC.APP_MAIN_FRAME,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, yesNoOptions, yesNoOptions[1]);
        return response == JOptionPane.YES_OPTION;
    }

    public static File saveFileDialog(String title, String formatName, String... extensions) {

        JFileChooser fileChooser = buildFileChooser(title == null ? defaultSaveTitle : title, formatName, extensions);

        int result = fileChooser.showSaveDialog(AppConfig.AC.APP_MAIN_FRAME);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = fileChooser.getSelectedFile();
        if (file == null)
            return null;
        if (file.exists()) {
            if (!confirmDialog(T.text("file_already_exists_overwrite"),
                    T.text("overwrite_file"))) {
                return null;
            }
        }
        file = addExtension(file, extensions);
        return file;

    }

    private static boolean hasExtension(File file, String... extensions) {
        String path = file.getAbsolutePath();
        String lowerCasePath = path.toLowerCase();
        boolean hasExt = false;
        for (String ext : extensions) {
            if (lowerCasePath.endsWith("." + ext.toLowerCase())) {
                hasExt = true;
                break;
            }
        }
        return hasExt;
    }

    private static File addExtension(File file, String... extensions) {
        if (!hasExtension(file, extensions)) {
            file = new File(file.getAbsolutePath() + "." + extensions[0]);
        }
        return file;
    }

    public static File openFileDialog(String title, String formatName, String... extensions) {
        JFileChooser fileChooser = buildFileChooser(title == null ? defaultOpenTitle : title, formatName, extensions);
        int result = fileChooser.showOpenDialog(AppConfig.AC.APP_MAIN_FRAME);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return fileChooser.getSelectedFile(); // can be null
    }

    private static JFileChooser fileChooser;

    private static JFileChooser buildFileChooser(String title, String formatName, String... extensions) {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        fileChooser.setDialogTitle(title);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setApproveButtonToolTipText(defaultApproveButtonToolTipText);
        fileChooser.setFileFilter(new CustomFileFilter(
                formatName,
                extensions));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(new File(AppConfig.AC.APP_ROOT_DIR));
        return fileChooser;
    }

    private static class CustomFileFilter extends FileFilter {

        private String description;
        private String[] extensions;

        public CustomFileFilter(String description, String... extensions) {
            this.description = description + " (." + String.join(", .", extensions) + ")";
            this.extensions = extensions;
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String fName = f.getName();
            for (String ext : extensions) {
                if (fName.endsWith("." + ext)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return description;
        }

    }
}

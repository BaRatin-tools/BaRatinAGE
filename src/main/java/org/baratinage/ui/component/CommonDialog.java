package org.baratinage.ui.component;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;

public class CommonDialog {
    private static String defaultOpenTitle = "Open";
    private static String defaultSaveTitle = "Save";
    private static String defaultApproveButtonToolTipText = "Ok";

    private static JFileChooser fileChooser;

    public static void init() {
        T.permanent(() -> {
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

            resetFileChooser();
        });
    }

    public static void errorDialog(String message) {
        errorDialog(message, null);
    }

    public static void errorDialog(String message, String title) {
        errorDialog(AppSetup.MAIN_FRAME, message, null);
    }

    public static void errorDialog(Component parent, String message, String title) {
        JOptionPane.showOptionDialog(parent,
                message,
                title == null ? T.text("error") : title,
                JOptionPane.OK_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[] { T.text("ok") },
                "");
    }

    public static void warnDialog(String message) {
        warnDialog(message, null);
    }

    public static void warnDialog(String message, String title) {
        warnDialog(AppSetup.MAIN_FRAME, message, null);
    }

    public static void warnDialog(Component parent, String message, String title) {
        JOptionPane.showOptionDialog(AppSetup.MAIN_FRAME,
                message,
                title == null ? T.text("warning") : title,
                JOptionPane.OK_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[] { T.text("ok") },
                "");
    }

    public static void infoDialog(String message) {
        infoDialog(message, null);
    }

    public static void infoDialog(String message, String title) {
        infoDialog(AppSetup.MAIN_FRAME, message, null);
    }

    public static void infoDialog(Component parent, String message, String title) {
        String[] okOption = new String[] {
                T.text("ok")
        };
        JOptionPane.showOptionDialog(parent,
                message,
                title == null ? T.text("info") : title,
                JOptionPane.OK_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, okOption, okOption[0]);
    }

    public static boolean confirmDialog(String message) {
        return confirmDialog(message, null);
    }

    public static boolean confirmDialog(String message, String title) {
        return confirmDialog(AppSetup.MAIN_FRAME, message, title);
    }

    public static boolean confirmDialog(Component parent, String message, String title) {
        String[] yesNoOptions = new String[] {
                T.text("ok"),
                T.text("cancel")
        };
        int response = JOptionPane.showOptionDialog(parent,
                message,
                title == null ? T.text("warning") : title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, yesNoOptions, yesNoOptions[1]);
        return response == JOptionPane.YES_OPTION;
    }

    public static File openFileDialog(String title, CustomFileFilter... fileFilters) {
        return openFileDialog(AppSetup.MAIN_FRAME, title, fileFilters);
    }

    public static File openFileDialog(Component parent, String title, CustomFileFilter... fileFilters) {
        JFileChooser fileChooser = configureFileChooser(
                title == null ? defaultOpenTitle : title,
                new File(""),
                fileFilters);
        int result = fileChooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return fileChooser.getSelectedFile(); // can be null
    }

    public static File saveFileDialog(
            String defaultFileName,
            String dialogTitle,
            CustomFileFilter... fileFilters) {
        return saveFileDialog(AppSetup.MAIN_FRAME, defaultFileName, dialogTitle, fileFilters);
    }

    public static File saveFileDialog(
            Component parent,
            String defaultFileName,
            String dialogTitle,
            CustomFileFilter... fileFilters) {

        JFileChooser fileChooser = configureFileChooser(
                dialogTitle == null ? defaultSaveTitle : dialogTitle,
                defaultFileName == null ? new File("") : new File(defaultFileName),
                fileFilters);

        int result = fileChooser.showSaveDialog(parent);

        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = fileChooser.getSelectedFile();
        if (file == null) {
            return null;
        }

        CustomFileFilter cff = (CustomFileFilter) fileChooser.getFileFilter();
        if (cff != null) {
            String[] extensions = cff.getExtensions();
            file = addExtension(file, extensions);
        }

        if (file.exists()) {
            if (!confirmDialog(T.html("file_already_exists_overwrite"),
                    T.text("overwrite_file"))) {
                return null;
            }
        }

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

    private static void resetFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(AppSetup.PATH_APP_ROOT_DIR));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setApproveButtonToolTipText(defaultApproveButtonToolTipText);
    }

    private static JFileChooser configureFileChooser(String title, File defaultFile, CustomFileFilter... fileFilters) {
        if (fileChooser == null) {
            resetFileChooser();
        }
        fileChooser.setSelectedFile(defaultFile);
        fileChooser.setDialogTitle(title);
        fileChooser.resetChoosableFileFilters();
        for (CustomFileFilter ff : fileFilters) {
            fileChooser.addChoosableFileFilter(ff);
        }
        return fileChooser;
    }

    public static class CustomFileFilter extends FileFilter {

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
            String fName = f.getName().toLowerCase();
            for (String ext : extensions) {
                if (fName.endsWith("." + ext.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return description;
        }

        public String[] getExtensions() {
            return extensions;
        }

    }
}

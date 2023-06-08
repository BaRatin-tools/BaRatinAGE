package org.baratinage.ui;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.component.NoScalingIcon;
import org.baratinage.ui.container.RowColPanel;

import java.awt.Dimension;
import java.awt.Point;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private RowColPanel projectPanel;
    private BamProject currentProject;

    public JMenuBar mainMenuBar;
    public JMenu baratinMenu;

    public MainFrame() {

        this.setSize(new Dimension(1200, 900));
        // this.setSize(new Dimension(773, 1118));
        // this.setLocation(new Point(1986, 0));

        // this.setSize(new Dimension(966, 1398));
        // this.setLocation(new Point(2482, 0));

        // TestPanel testPanel = new TestPanel();
        // this.add(testPanel);

        mainMenuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Fichier");
        mainMenuBar.add(fileMenu);

        JMenuItem openProjectMenuItem = new JMenuItem();
        openProjectMenuItem.setText("Ouvrir un projet");
        openProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        fileMenu.add(openProjectMenuItem);

        JMenuItem saveProjectMenuItem = new JMenuItem();
        saveProjectMenuItem.setText("Sauvegarder");
        saveProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveProjectMenuItem.addActionListener((e) -> {
            if (currentProject != null) {
                currentProject.saveProject();
            }
        });
        fileMenu.add(saveProjectMenuItem);

        fileMenu.addSeparator();

        JMenuItem closeMenuItem = new JMenuItem();
        closeMenuItem.setText("Quitter");
        closeMenuItem.setIcon(new NoScalingIcon("./resources/icons/close_16x16.png"));
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        closeMenuItem.addActionListener((e) -> {
            close();
        });
        fileMenu.add(closeMenuItem);

        this.setJMenuBar(mainMenuBar);

        // currentProject = new BaratinProject();

        projectPanel = new RowColPanel();
        this.add(projectPanel);

        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Dimension dim = MainFrame.this.getSize();
                System.out.println(String.format("Resized: %d x %d", dim.width, dim.height));
            }

            public void componentMoved(ComponentEvent e) {
                Point loc = MainFrame.this.getLocation();
                System.out.println(String.format("Relocated: %d, %d", loc.x, loc.y));
            }
        });

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                MainFrame.this.close();
            }
        });

        this.setVisible(true);
    }

    public void setCurrentProject(BamProject project) {
        currentProject = project;
        projectPanel.clear();
        projectPanel.appendChild(project);
    }

    private void close() {
        System.exit(0);
    }
}
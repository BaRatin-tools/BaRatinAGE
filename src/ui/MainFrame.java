package ui;

import javax.swing.JFrame;

// import java.awt.Dimension;
// import java.awt.Point;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame implements ActionListener {

    private static MainFrame instance;

    public static synchronized MainFrame getInstance() {
        if (instance == null) {
            instance = new MainFrame();
        }
        return instance;
    }

    public MainFrame() {

        // this.setSize(new Dimension(773, 1118));
        // this.setLocation(1986, 0);

        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                // Dimension dim = MainFrame.this.getSize();
                // System.out.println(String.format("Resized: %d x %d", dim.width, dim.height));
            }

            public void componentMoved(ComponentEvent e) {
                // Point loc = MainFrame.this.getLocation();
                // System.out.println(String.format("Relocated: %d, %d", loc.x, loc.y));
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

    @Override
    public void actionPerformed(ActionEvent event) {

    }

    private void close() {
        System.exit(0);
    }
}
package org.baratinage.ui.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;

public class SimpleFrame extends JFrame {
  public SimpleFrame() {
    this(null, 1000, 500);
  }

  public SimpleFrame(Component parent, int width, int height) {
    super();

    if (parent != null) {
      Window w = SwingUtilities.getWindowAncestor(parent);
      System.out.println(w);
      if (w != null) {
        Point p = w.getLocation();
        Dimension d = w.getSize();
        setLocation(
            new Point(
                p.x + d.width / 2 - width / 2,
                p.y + d.height / 2 - height / 2));
      }
    }

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        T.clear(this);
      }
    });

    setPreferredSize(new Dimension(width, height));
    setIconImage(AppSetup.MAIN_FRAME.getIconImage());
  }

  public void showContent(JComponent content) {
    setContentPane(content);
    updateFrame();
  }

  public void updateFrame() {
    pack();
    setVisible(true);
  }

}

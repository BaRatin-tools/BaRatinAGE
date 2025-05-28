package org.baratinage.ui.component;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.baratinage.ui.container.SimpleFlowPanel;

public class SimplePopup {

  private final JPopupMenu popup;
  private JComponent parent;
  private SimpleFlowPanel content;

  public SimplePopup(JComponent parent) {
    popup = new JPopupMenu();
    this.parent = parent;
    this.content = new SimpleFlowPanel();
    content.setOpaque(false);
    content.setPadding(5);
    popup.add(content);
  }

  public void setContent(JComponent content) {
    this.content.removeAll();
    this.content.addChild(content, false);
  }

  public void show() {

    Dimension dim = content.getPreferredSize();

    int x = dim.width * -1;
    int y = parent.getHeight() + 5;
    popup.show(parent, x, y);
  }
}

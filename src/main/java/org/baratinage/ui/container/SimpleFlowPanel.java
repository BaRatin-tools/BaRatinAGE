package org.baratinage.ui.container;

import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

/**
 * A panel to layout items horizontally or vertically forcing available space to
 * be:
 * - equally (with optional weights) shared
 * - taken by children according to their preferred size (fill = false)
 */
public class SimpleFlowPanel extends JPanel {

  private final boolean isVertical;

  private record ChildCompConfig(Component component, Float weight, Insets padding) {

    public boolean isSizeFixed() {
      return weight.equals(0f);
    }

    public int getTotalHeight() {
      return getPrefHeight() + getVerticalPadding();
    }

    public int getPrefHeight() {
      return component.getPreferredSize().height;
    }

    public int getVerticalPadding() {
      return padding.top + padding.bottom;
    }

    public int getTotalWidth() {
      return getPrefWidth() + getHorizontalPadding();
    }

    public int getPrefWidth() {
      return component.getPreferredSize().width;
    }

    public int getHorizontalPadding() {
      return padding.left + padding.right;
    }
  }

  private final Map<JComponent, ChildCompConfig> childrenConfigs = new HashMap<>();

  private Insets padding = new Insets(0, 0, 0, 0);
  private int gap = 0;

  public SimpleFlowPanel() {
    this(false);
  }

  public SimpleFlowPanel(boolean isVertical) {
    super(null); // we'll handle layout manually
    this.isVertical = isVertical;
  }

  public void addExtensor() {
    addChild(new JComponent() {
    }, true);
  }

  public void addChild(JComponent child) {
    addChild(child, 1f);
  }

  public void addChild(JComponent child, boolean fill) {
    addChild(child, fill ? 1f : 0f);
  }

  public void addChild(JComponent child, float weight) {
    addChild(child, weight, 0);
  }

  public void addChild(JComponent child, float weight, int padding) {
    addChild(child, weight, padding, padding);
  }

  public void addChild(JComponent child, float weight, int vPadding, int hPadding) {
    childrenConfigs.put(child, new ChildCompConfig(child, weight, new Insets(vPadding, hPadding, vPadding, hPadding)));
    super.add(child);
    revalidate();
    repaint();
  }

  public void setPadding(int padding) {
    setPadding(padding, padding, padding, padding);
  }

  public void setPadding(int vPadding, int hPadding) {
    setPadding(vPadding, hPadding, vPadding, hPadding);
  }

  public void setPadding(int top, int left, int bottom, int right) {
    this.padding = new Insets(top, left, bottom, right);
    revalidate();
    repaint();
  }

  public void setGap(int gap) {
    this.gap = gap;
    revalidate();
    repaint();
  }

  @Override
  public void doLayout() {
    Component[] components = getComponents();
    int nComp = components.length;
    if (nComp == 0) {
      return;
    }

    Insets insets = getInsets();
    int x = insets.left + padding.left;
    int y = insets.top + padding.top;
    int availableWidth = getWidth() - insets.left - insets.right - padding.left - padding.right;
    int availableHeight = getHeight() - insets.top - insets.bottom - padding.top - padding.bottom;

    // calculate total fixed width/height
    int fixedSize = 0;
    float flexCount = 0f;
    for (Component comp : components) {
      ChildCompConfig childConfig = childrenConfigs.get(comp);
      if (comp instanceof JComponent && childConfig.isSizeFixed()) {
        fixedSize += isVertical ? childConfig.getTotalHeight() : childConfig.getTotalWidth();
      } else {
        flexCount += childConfig.weight;
      }
    }
    Integer remaining = isVertical ? availableHeight - fixedSize : availableWidth - fixedSize;
    remaining = Math.max(0, remaining - (nComp - 1) * gap);
    Float eachSize = flexCount > 0 ? remaining.floatValue() / flexCount : 0;

    // layout
    if (isVertical) {
      for (int k = 0; k < nComp; k++) {
        Component comp = components[k];

        ChildCompConfig childConfig = childrenConfigs.get(comp);
        Float size = eachSize * childConfig.weight;
        int h = childConfig.isSizeFixed()
            ? childConfig.getPrefHeight()
            : size.intValue();
        comp.setBounds(
            x + childConfig.padding.left,
            y + childConfig.padding.top,
            availableWidth - childConfig.getHorizontalPadding(),
            h);
        y += h + childConfig.getVerticalPadding();
        y += k == nComp - 1 ? 0 : gap;
      }
    } else {
      for (int k = 0; k < nComp; k++) {
        Component comp = components[k];

        ChildCompConfig childConfig = childrenConfigs.get(comp);
        Float size = eachSize * childConfig.weight;
        int w = childConfig.isSizeFixed()
            ? childConfig.getPrefWidth()
            : size.intValue();
        comp.setBounds(
            x + childConfig.padding.left,
            y + childConfig.padding.top,
            w,
            availableHeight - childConfig.getVerticalPadding());
        x += w + childConfig.getHorizontalPadding();
        x += k == nComp - 1 ? 0 : gap;
      }
    }

  }

  @Override
  public Dimension getPreferredSize() {
    Component[] components = getComponents();
    int nComp = components.length;

    Insets insets = getInsets();
    int width = insets.left + insets.right + padding.left + padding.right;
    int height = insets.top + insets.bottom + padding.top + padding.bottom;
    if (isVertical) {
      for (int k = 0; k < nComp; k++) {
        Component comp = components[k];
        ChildCompConfig childConfig = childrenConfigs.get(comp);
        width = Math.max(width,
            childConfig.getTotalWidth() + insets.left + insets.right + padding.left + padding.right);
        height += childConfig.getTotalHeight();
        height += k == nComp - 1 ? 0 : gap;
      }
    } else {
      for (int k = 0; k < nComp; k++) {
        Component comp = components[k];
        ChildCompConfig childConfig = childrenConfigs.get(comp);
        height = Math.max(height,
            childConfig.getTotalHeight() + insets.top + insets.bottom + padding.top + padding.bottom);
        width += childConfig.getTotalWidth();
        width += k == nComp - 1 ? 0 : gap;
      }
    }
    return new Dimension(width, height);
  }

  @Override
  public void removeAll() {
    super.removeAll();
    revalidate();
    repaint();
  }

  public void setDebug(boolean debug) {
    if (debug) {
      setBackground(new Color(
          (int) (Math.random() * 255),
          (int) (Math.random() * 255),
          (int) (Math.random() * 255), 150));

    } else {
      setBackground(null);
    }
    revalidate();
    repaint();
  }

}

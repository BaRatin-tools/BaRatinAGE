package org.baratinage;

import java.awt.Color;

import javax.swing.UIManager;

public class AppColors {
    public final Color INVALID_DISABLED_BG;
    public final Color INVALID_BG;
    public final Color INVALID_FG;

    public final Color ERROR;
    public final Color DANGER;
    public final Color WARNING = new Color(230, 149, 0);
    public final Color INFO = new Color(0, 63, 179);
    public final Color INFO_BG = new Color(218, 231, 255);

    public final Color PLOT_HIGHLIGHT = new Color(0, 255, 255);

    public final Color PLOT_LINE = new Color(50, 50, 100);
    public final Color PLOT_ENVELOP = new Color(200, 200, 255);
    public final Color PRIOR_LINE = new Color(50, 50, 255);
    public final Color PRIOR_ENVELOP = new Color(200, 200, 255);
    public final Color POSTERIOR_LINE = new Color(253, 55, 50);
    public final Color POSTERIOR_ENVELOP = new Color(253, 195, 188);
    public final Color RATING_CURVE = PLOT_LINE;
    public final Color RATING_CURVE_TOTAL_UNCERTAINTY = new Color(182, 0, 4);
    public final Color RATING_CURVE_PARAM_UNCERTAINTY = new Color(253, 195, 188);
    public final Color GAUGING = new Color(0, 128, 255);
    public final Color DISCARDED_GAUGING = new Color(255, 80, 83);
    public final Color PRIOR_STAGE_ACTIVATION_VALUE = new Color(155, 60, 155);
    public final Color PRIOR_STAGE_ACTIVATION_UNCERTAINTY = new Color(231, 89, 231);
    public final Color POSTERIOR_STAGE_ACTIVATION_VALUE = new Color(4, 182, 0);
    public final Color POSTERIOR_STAGE_ACTIVATION_UNCERTAINTY = new Color(164, 255, 162);
    public final Color LIMNIGRAPH_STAGE_UNCERTAINTY = new Color(255, 255, 50);

    public final Color DEFAULT_FG;
    public final Color DEFAULT_FG_LIGHT;
    public final Color DEFAULT_BG;
    public final Color DEFAULT_BG_LIGHT;

    public AppColors() {
        boolean isDarkMode = AppSetup.CONFIG.DARK_MODE.get();
        DEFAULT_FG = getColorFromUIManager(isDarkMode ? Color.WHITE : Color.BLACK,
                "TextArea.foreground",
                "Label.forground");
        DEFAULT_FG_LIGHT = getColorFromUIManager(isDarkMode ? Color.WHITE : Color.BLACK,
                "TextArea.disabledForeground",
                "Label.disabledForeground");
        DEFAULT_BG = getColorFromUIManager(isDarkMode ? Color.BLACK : Color.WHITE,
                "TextArea.background",
                "Label.background");
        DEFAULT_BG_LIGHT = getColorFromUIManager(isDarkMode ? Color.BLACK : Color.WHITE,
                "TextArea.disabledBackground",
                "Label.disabledBackground");

        // INVALID_FG = new Color(200, 50, 40);

        ERROR = alterColor(DEFAULT_FG, 200, 0, 0, 0);
        DANGER = alterColor(DEFAULT_FG, 200, 0, 0, 0);

        INVALID_FG = alterColor(DEFAULT_FG, 200, -200, -200, 0);
        INVALID_DISABLED_BG = alterColor(DEFAULT_BG_LIGHT, 20, -20, -20, 0);
        INVALID_BG = alterColor(DEFAULT_BG, 20, -20, -20, 0);

    }

    private static Color getColorFromUIManager(Color defaultColor, String key, String... fallbacks) {
        Color color = UIManager.getColor(key);
        if (color != null) {
            return color;
        }
        for (String k : fallbacks) {
            color = UIManager.getColor(k);
            if (color != null) {
                return color;
            }
        }
        return defaultColor;
    }

    private static Color alterColor(Color color, int red, int green, int blue, int alpha) {
        int r = Math.min(Math.max(color.getRed() + red, 0), 255);
        int g = Math.min(Math.max(color.getGreen() + green, 0), 255);
        int b = Math.min(Math.max(color.getBlue() + blue, 0), 255);
        int a = Math.min(Math.max(color.getAlpha() + alpha, 0), 255);
        Color alteredColor = new Color(r, g, b, a);
        return alteredColor;
    }
}

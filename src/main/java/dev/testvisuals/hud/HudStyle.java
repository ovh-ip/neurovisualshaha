package dev.testvisuals.hud;

import dev.testvisuals.util.ColorUtils;

public final class HudStyle {

    public static int TEXT = 0xFFFFFFFF;
    public static int TEXT_DIM = 0xFF9AA0A6;
    public static int BG = 0xA6000000;
    public static int BG_SOFT = 0x66000000;
    public static int PANEL = 0xE61A1C1E;
    public static int BORDER = 0x33FFFFFF;
    public static int SHADOW = 0x40000000;
    public static int FILL = 0xFFFFFFFF;
    public static int EMPTY = 0x2EFFFFFF;
    public static int ACCENT = 0xFFFFFFFF;
    public static int BAR = 0x66FFFFFF;
    public static int DRAG_HIGHLIGHT = 0x59FFFFFF;
    public static int TOGGLE_ON = 0xFF000000;
    public static int TOGGLE_OFF = 0x33000000;

    private HudStyle() {
    }

    public static int text(float alphaFactor) {
        return ColorUtils.withAlpha(TEXT, alphaFactor);
    }

    public static int textDim(float alphaFactor) {
        return ColorUtils.withAlpha(TEXT_DIM, alphaFactor);
    }
}
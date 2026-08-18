package dev.testvisuals.menu;

import dev.testvisuals.hud.Config;
import dev.testvisuals.hud.HudStyle;

public final class ThemeManager {

    public static final class Theme {
        public final String name;
        public final int text;
        public final int textDim;
        public final int bg;
        public final int bgSoft;
        public final int panel;
        public final int border;
        public final int shadow;
        public final int fill;
        public final int empty;
        public final int accent;
        public final int bar;
        public final int dragHighlight;
        public final int toggleOn;
        public final int toggleOff;

        Theme(String name, int text, int textDim, int bg, int bgSoft, int panel, int border, int shadow,
              int fill, int empty, int accent, int bar, int dragHighlight, int toggleOn, int toggleOff) {
            this.name = name;
            this.text = text;
            this.textDim = textDim;
            this.bg = bg;
            this.bgSoft = bgSoft;
            this.panel = panel;
            this.border = border;
            this.shadow = shadow;
            this.fill = fill;
            this.empty = empty;
            this.accent = accent;
            this.bar = bar;
            this.dragHighlight = dragHighlight;
            this.toggleOn = toggleOn;
            this.toggleOff = toggleOff;
        }
    }

    public static final Theme DARK = new Theme("Тёмный",
            0xFFFFFFFF, 0xFF9AA0A6, 0xA6000000, 0x66000000, 0xE61A1C1E,
            0x33FFFFFF, 0x40000000, 0xFFFFFFFF, 0x2EFFFFFF, 0xFFFFFFFF,
            0x66FFFFFF, 0x59FFFFFF, 0xFF000000, 0x33000000);

    public static final Theme LIGHT = new Theme("Светлый",
            0xFF101114, 0xFF6E747B, 0xD9FFFFFF, 0x99FFFFFF, 0xF2F4F6,
            0x2E000000, 0x33000000, 0xFF101114, 0x14000000, 0xFF101114,
            0x26000000, 0x3D101114, 0xFFFFFFFF, 0x1F101114);

    public static final Theme GRAPHITE = new Theme("Графит",
            0xFFE8EAED, 0xFF8A9099, 0xC426282B, 0x80262A2E, 0xE62B2F33,
            0x2EFFFFFF, 0x40000000, 0xFFE8EAED, 0x2EFFFFFF, 0xFFE8EAED,
            0x59FFFFFF, 0x59FFFFFF, 0xFF191C1F, 0x33191C1F);

    public static final Theme[] ALL = {DARK, LIGHT, GRAPHITE};

    private static Theme current = DARK;

    private ThemeManager() {
    }

    public static void init() {
        String saved = Config.theme();
        for (Theme theme : ALL) {
            if (theme.name.equals(saved)) {
                current = theme;
                break;
            }
        }
        apply(current);
    }

    public static void apply(Theme theme) {
        current = theme;
        HudStyle.TEXT = theme.text;
        HudStyle.TEXT_DIM = theme.textDim;
        HudStyle.BG = theme.bg;
        HudStyle.BG_SOFT = theme.bgSoft;
        HudStyle.PANEL = theme.panel;
        HudStyle.BORDER = theme.border;
        HudStyle.SHADOW = theme.shadow;
        HudStyle.FILL = theme.fill;
        HudStyle.EMPTY = theme.empty;
        HudStyle.ACCENT = theme.accent;
        HudStyle.BAR = theme.bar;
        HudStyle.DRAG_HIGHLIGHT = theme.dragHighlight;
        HudStyle.TOGGLE_ON = theme.toggleOn;
        HudStyle.TOGGLE_OFF = theme.toggleOff;
        Config.theme(theme.name);
    }

    public static Theme current() {
        return current;
    }
}
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

        public Theme(String name, int text, int textDim, int bg, int bgSoft, int panel, int border, int shadow,
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

    public static final Theme MONOCHROME = new Theme("Монохром",
            0xFFFFFFFF, 0xFF9CA3AF, 0xEE141519, 0x991C1E24, 0xF50E0F12,
            0x33FFFFFF, 0x80000000, 0xFFFFFFFF, 0x33FFFFFF, 0xFFFFFFFF,
            0x66FFFFFF, 0x80FFFFFF, 0xFFFFFFFF, 0x4D27272A);

    public static final Theme GRAPHITE = new Theme("Графит",
            0xFFF4F4F5, 0xFFA1A1AA, 0xEE18181B, 0x9927272A, 0xF5121214,
            0x33FFFFFF, 0x80000000, 0xFFE4E4E7, 0x33E4E4E7, 0xFFFAFAFA,
            0x66FAFAFA, 0x80FAFAFA, 0xFFFAFAFA, 0x4D27272A);

    public static final Theme DARK = new Theme("Тёмный Космос",
            0xFFF8FAFC, 0xFF94A3B8, 0xEE0F172A, 0x991E293B, 0xF50B132B,
            0x4D6EE7FF, 0x80000000, 0xFF6EE7FF, 0x336EE7FF, 0xFF38BDF8,
            0x6638BDF8, 0x8038BDF8, 0xFF38BDF8, 0x4D1E293B);

    public static final Theme LIGHT = new Theme("Светлый Иней",
            0xFF0F172A, 0xFF64748B, 0xE6FFFFFF, 0xCCF1F5F9, 0xF8F8FAFC,
            0x330F172A, 0x26000000, 0xFF0284C7, 0x260284C7, 0xFF0284C7,
            0x4D0284C7, 0x660284C7, 0xFF0284C7, 0x33CBD5E1);

    public static final Theme CYBERPUNK = new Theme("Киберпанк",
            0xFFFDF4FF, 0xFFC084FC, 0xEE180B2E, 0x992E1065, 0xF5130724,
            0x4DF43F5E, 0x80000000, 0xFFF43F5E, 0x33F43F5E, 0xFFE879F9,
            0x66E879F9, 0x80E879F9, 0xFFE879F9, 0x4D2E1065);

    public static final Theme[] ALL = {MONOCHROME, GRAPHITE, DARK, LIGHT, CYBERPUNK};

    private static Theme current = MONOCHROME;

    private ThemeManager() {
    }

    public static void init() {
        String saved = Config.theme();
        for (Theme theme : ALL) {
            if (theme.name.equalsIgnoreCase(saved)) {
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
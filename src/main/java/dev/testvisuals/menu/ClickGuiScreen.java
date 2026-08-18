package dev.testvisuals.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import dev.testvisuals.font.CustomFontRenderer;
import dev.testvisuals.hud.Config;
import dev.testvisuals.hud.HudManager;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.render.Renderer2D;
import dev.testvisuals.util.AnimationUtils;
import dev.testvisuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ClickGuiScreen extends Screen {

    private static final float PADDING = 16f;
    private static final float NAVBAR_H = 46f;
    private static final float TAB_W = 104f;
    private static final float TAB_H = 28f;
    private static final float ROW_H = 34f;
    private static final float TEXT_SCALE = 0.24f;
    private static final float SMALL_SCALE = 0.17f;

    private static final class Area {
        final String id;
        float x, y, w, h;

        Area(String id, float x, float y, float w, float h) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        boolean contains(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    private final List<Area> areas = new ArrayList<>();
    private final Map<String, Float> hoverT = new HashMap<>();
    private final Map<String, Float> knobT = new HashMap<>();

    private int tab;
    private TextFieldWidget altField;

    private float windowX;
    private float windowY;
    private float windowW;
    private float windowH;
    private float contentY;

    public ClickGuiScreen() {
        super(Text.literal("TestVisuals"));
    }

    @Override
    public void init() {
        MinecraftClient client = MinecraftClient.getInstance();
        float sw = client.getWindow().getScaledWidth();
        float sh = client.getWindow().getScaledHeight();
        windowW = Math.min(sw * 0.62f, 880f);
        windowH = Math.min(sh * 0.66f, 560f);
        windowX = (sw - windowW) / 2f;
        windowY = (sh - windowH) / 2f;

        if (tab == 2) {
            float fieldW = windowW - PADDING * 2f - 130f;
            altField = new TextFieldWidget(client.textRenderer, (int) (windowX + PADDING),
                    (int) (windowY + NAVBAR_H + PADDING), (int) fieldW, 20, Text.literal("Никнейм"));
            altField.setMaxLength(32);
            addSelectableChild(altField);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.applyBlur();

        MinecraftClient client = MinecraftClient.getInstance();
        float sw = client.getWindow().getScaledWidth();
        float sh = client.getWindow().getScaledHeight();

        Renderer2D renderer = HudManager.get().renderer();
        CustomFontRenderer font = HudManager.get().font();
        dev.testvisuals.gl.GLUtil.enableBlend();
        dev.testvisuals.gl.GLUtil.disableDepth();
        renderer.begin(sw, sh);

        int overlay = ThemeManager.current() == ThemeManager.LIGHT ? 0x73FFFFFF : 0x8C000000;
        renderer.quad(0f, 0f, sw, sh, overlay);

        drawWindow(renderer, font, mouseX, mouseY, delta);
        drawContent(renderer, font, mouseX, mouseY, delta);

        renderer.flush();
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawWindow(Renderer2D renderer, CustomFontRenderer font, float mouseX, float mouseY, float delta) {
        renderer.dropShadow(windowX, windowY, windowW, windowH, 10f, 14f, HudStyle.SHADOW);
        renderer.roundedRect(windowX, windowY, windowW, windowH, 10f, HudStyle.PANEL);
        renderer.roundedOutline(windowX, windowY, windowW, windowH, 10f, 1f, HudStyle.BORDER);

        float titleY = windowY + (NAVBAR_H - font.lineHeight(0.42f)) / 2f;
        font.draw(renderer, "TestVisuals", windowX + PADDING, titleY, 0.42f, HudStyle.TEXT);

        renderer.quad(windowX + PADDING, windowY + NAVBAR_H - 1f, windowW - PADDING * 2f, 1f, HudStyle.BORDER);

        String[] tabs = {"HUD", "Темы", "Альты"};
        float tabX = windowX + windowW - PADDING - TAB_W * 3f - 8f * 2f;
        for (int i = 0; i < tabs.length; i++) {
            boolean active = i == tab;
            float tx = tabX + i * (TAB_W + 8f);
            int color = active ? HudStyle.ACCENT : HudStyle.TOGGLE_OFF;
            renderer.roundedRect(tx, windowY + 9f, TAB_W, TAB_H, 6f, color);
            int textColor = active ? HudStyle.TOGGLE_ON : HudStyle.TEXT_DIM;
            font.drawCentered(renderer, tabs[i], tx + TAB_W / 2f,
                    windowY + 9f + (TAB_H - font.lineHeight(SMALL_SCALE)) / 2f, SMALL_SCALE, textColor);
            areas.add(new Area("tab_" + i, tx, windowY + 9f, TAB_W, TAB_H));
        }
    }

    private void drawContent(Renderer2D renderer, CustomFontRenderer font, float mouseX, float mouseY, float delta) {
        contentY = windowY + NAVBAR_H + PADDING;
        float contentW = windowW - PADDING * 2f;
        switch (tab) {
            case 0 -> drawHudTab(renderer, font, delta);
            case 1 -> drawThemesTab(renderer, font, mouseX, mouseY, delta);
            case 2 -> drawAltsTab(renderer, font, delta);
        }
    }

    private void drawHudTab(Renderer2D renderer, CustomFontRenderer font, float delta) {
        float y = contentY;
        for (dev.testvisuals.hud.HudComponent component : HudManager.get().components()) {
            float rowY = y;
            renderer.roundedRect(windowX + PADDING, rowY, windowW - PADDING * 2f, ROW_H, 6f, HudStyle.BG_SOFT);

            float nameY = rowY + (ROW_H - font.lineHeight(TEXT_SCALE)) / 2f;
            font.draw(renderer, component.getDisplayName(), windowX + PADDING + 12f, nameY, TEXT_SCALE, HudStyle.TEXT);

            drawToggle(renderer, font, component.getId(), component.enabled,
                    windowX + windowW - PADDING - 46f, rowY + (ROW_H - 18f) / 2f, delta);

            areas.add(new Area("toggle_" + component.getId(), windowX + windowW - PADDING - 46f,
                    rowY + (ROW_H - 18f) / 2f, 34f, 18f));
            y += ROW_H + 6f;
        }

        float btnY = y + 8f;
        float btnW = 170f;
        float btnH = 26f;
        renderer.roundedRect(windowX + PADDING, btnY, btnW, btnH, 6f, HudStyle.TOGGLE_OFF);
        font.drawCentered(renderer, "Сбросить позиции", windowX + PADDING + btnW / 2f,
                btnY + (btnH - font.lineHeight(SMALL_SCALE)) / 2f, SMALL_SCALE, HudStyle.TEXT);
        areas.add(new Area("reset", windowX + PADDING, btnY, btnW, btnH));

        font.draw(renderer, "Откройте чат (T), чтобы перетаскивать элементы HUD", windowX + PADDING,
                btnY + btnH + 12f, SMALL_SCALE * 0.85f, HudStyle.TEXT_DIM);
    }

    private void drawToggle(Renderer2D renderer, CustomFontRenderer font, String id, boolean enabled,
                            float x, float y, float delta) {
        float target = enabled ? 1f : 0f;
        knobT.put(id, AnimationUtils.approach(knobT.getOrDefault(id, enabled ? 1f : 0f), target, delta, 14f));
        float t = knobT.get(id);

        int track = ColorUtils.lerp(HudStyle.TOGGLE_OFF, HudStyle.ACCENT, t);
        renderer.roundedRect(x, y, 34f, 18f, 9f, track);
        renderer.roundedOutline(x, y, 34f, 18f, 9f, 1f, HudStyle.BORDER);
        renderer.circle(x + 9f + t * 16f, y + 9f, 6f, enabled ? HudStyle.TOGGLE_ON : HudStyle.FILL);
    }

    private void drawThemesTab(Renderer2D renderer, CustomFontRenderer font, float mouseX, float mouseY, float delta) {
        float contentW = windowW - PADDING * 2f;
        float cardW = (contentW - 12f * 2f) / 3f;
        float cardH = 96f;
        for (int i = 0; i < ThemeManager.ALL.length; i++) {
            ThemeManager.Theme theme = ThemeManager.ALL[i];
            float x = windowX + PADDING + i * (cardW + 12f);
            boolean active = ThemeManager.current() == theme;
            renderer.roundedRect(x, contentY, cardW, cardH, 8f, active ? HudStyle.ACCENT : HudStyle.BG_SOFT);
            renderer.roundedRect(x + 1.5f, contentY + 1.5f, cardW - 3f, cardH - 3f, 7f,
                    active ? HudStyle.PANEL : theme.panel);

            font.drawCentered(renderer, theme.name, x + cardW / 2f, contentY + 10f, 0.24f,
                    active ? HudStyle.TEXT : HudStyle.TEXT_DIM);

            float swX = x + cardW / 2f - 34f;
            float swY = contentY + 44f;
            renderer.roundedRect(swX, swY, 22f, 34f, 4f, theme.bg);
            renderer.roundedRect(swX + 26f, swY, 22f, 34f, 4f, theme.panel);
            renderer.roundedRect(swX + 52f, swY, 16f, 34f, 4f, theme.text);
            renderer.roundedOutline(swX, swY, 68f, 34f, 4f, 1f, theme.border);

            areas.add(new Area("theme_" + i, x, contentY, cardW, cardH));
        }
    }

    private void drawAltsTab(Renderer2D renderer, CustomFontRenderer font, float delta) {
        float fieldW = windowW - PADDING * 2f - 130f;
        if (altField == null) {
            return;
        }
        renderer.roundedRect(windowX + PADDING - 2f, contentY - 4f, fieldW + 4f, 28f, 6f, HudStyle.BG_SOFT);
        altField.setPosition((int) (windowX + PADDING), (int) contentY);

        float addX = windowX + PADDING + fieldW + 8f;
        float addW = 122f;
        float addH = 20f;
        renderer.roundedRect(addX, contentY, addW, addH, 5f, HudStyle.TOGGLE_OFF);
        font.drawCentered(renderer, "Добавить", addX + addW / 2f,
                contentY + (addH - font.lineHeight(SMALL_SCALE)) / 2f, SMALL_SCALE, HudStyle.TEXT);
        areas.add(new Area("alt_add", addX, contentY, addW, addH));

        float y = contentY + 42f;
        List<Config.AltData> alts = Config.alts();
        float rowW = windowW - PADDING * 2f;
        for (int i = 0; i < alts.size(); i++) {
            Config.AltData alt = alts.get(i);
            float rowY = y;
            renderer.roundedRect(windowX + PADDING, rowY, rowW, ROW_H, 6f, HudStyle.BG_SOFT);

            String label = alt.name;
            if (label != null && alt.name.equals(clientName())) {
                label = label + " (текущий)";
            }
            font.draw(renderer, label == null ? "?" : label, windowX + PADDING + 12f,
                    rowY + (ROW_H - font.lineHeight(TEXT_SCALE)) / 2f, TEXT_SCALE, HudStyle.TEXT);

            float useW = 74f;
            float rmW = 34f;
            float useX = windowX + windowW - PADDING - rmW - 8f - useW;
            renderer.roundedRect(useX, rowY + 6f, useW, ROW_H - 12f, 5f, HudStyle.TOGGLE_OFF);
            font.drawCentered(renderer, "Войти", useX + useW / 2f,
                    rowY + 6f + (ROW_H - 12f - font.lineHeight(SMALL_SCALE)) / 2f, SMALL_SCALE, HudStyle.TEXT);
            areas.add(new Area("alt_use_" + i, useX, rowY + 6f, useW, ROW_H - 12f));

            float rmX = useX + useW + 8f;
            renderer.roundedRect(rmX, rowY + 6f, rmW, ROW_H - 12f, 5f, HudStyle.TOGGLE_OFF);
            font.drawCentered(renderer, "✕", rmX + rmW / 2f,
                    rowY + 6f + (ROW_H - 12f - font.lineHeight(SMALL_SCALE)) / 2f, SMALL_SCALE, HudStyle.TEXT);
            areas.add(new Area("alt_remove_" + i, rmX, rowY + 6f, rmW, ROW_H - 12f));

            y += ROW_H + 6f;
        }
    }

    private String clientName() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Area area : areas) {
                if (area.contains(mouseX, mouseY)) {
                    handleClick(area.id);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleClick(String id) {
        if (id.startsWith("tab_")) {
            tab = Integer.parseInt(id.substring(4));
            init();
            return;
        }
        if (id.startsWith("toggle_")) {
            HudManager.get().toggle(id.substring(7));
            return;
        }
        if (id.equals("reset")) {
            HudManager.get().resetPositions();
            HudManager.get().notify("Позиции HUD сброшены");
            return;
        }
        if (id.startsWith("theme_")) {
            ThemeManager.apply(ThemeManager.ALL[Integer.parseInt(id.substring(6))]);
            HudManager.get().notify("Тема: " + ThemeManager.current().name);
            return;
        }
        if (id.equals("alt_add")) {
            if (altField != null && !altField.getText().isBlank()) {
                AltManager.add(altField.getText());
                altField.setText("");
            }
            return;
        }
        if (id.startsWith("alt_use_")) {
            int index = Integer.parseInt(id.substring(8));
            Config.AltData alt = Config.alts().get(index);
            AltManager.use(index);
            HudManager.get().notify("Альт применён: " + alt.name);
            return;
        }
        if (id.startsWith("alt_remove_")) {
            AltManager.remove(Integer.parseInt(id.substring(11)));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (altField != null && altField.isFocused() && !altField.getText().isBlank()) {
                AltManager.add(altField.getText());
                altField.setText("");
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
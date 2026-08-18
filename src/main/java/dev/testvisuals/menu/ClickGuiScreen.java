package dev.testvisuals.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import dev.testvisuals.font.CustomFontRenderer;
import dev.testvisuals.gl.GLUtil;
import dev.testvisuals.hud.Config;
import dev.testvisuals.hud.HudComponent;
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

public final class ClickGuiScreen extends Screen {

    private static final float PADDING = 18f;
    private static final float NAVBAR_H = 50f;
    private static final float TAB_W = 110f;
    private static final float TAB_H = 30f;
    private static final float ROW_H = 38f;

    private static final class ClickArea {
        final String id;
        float x, y, w, h;

        ClickArea(String id, float x, float y, float w, float h) {
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

    private final List<ClickArea> clickAreas = new ArrayList<>();
    private final Map<String, Float> toggleAnims = new HashMap<>();
    private final Map<String, Float> hoverAnims = new HashMap<>();

    private int currentTab = 0;
    private TextFieldWidget altField;
    private float openProgress = 0f;

    private float winX;
    private float winY;
    private float winW;
    private float winH;

    public ClickGuiScreen() {
        super(Text.literal("TestVisuals ClickGUI"));
    }

    @Override
    protected void init() {
        MinecraftClient client = MinecraftClient.getInstance();
        float sw = client.getWindow().getScaledWidth();
        float sh = client.getWindow().getScaledHeight();

        winW = Math.min(sw * 0.72f, 680f);
        winH = Math.min(sh * 0.76f, 460f);
        winX = (sw - winW) / 2f;
        winY = (sh - winH) / 2f;

        if (currentTab == 2) {
            float fieldW = winW - PADDING * 2f - 110f;
            altField = new TextFieldWidget(client.textRenderer, (int) (winX + PADDING),
                    (int) (winY + NAVBAR_H + PADDING + 42f), (int) fieldW, 24, Text.literal("Никнейм"));
            altField.setMaxLength(32);
            addSelectableChild(altField);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.draw();
        clickAreas.clear();

        openProgress = AnimationUtils.approach(openProgress, 1.0f, delta, 12f);

        MinecraftClient client = MinecraftClient.getInstance();
        float sw = client.getWindow().getScaledWidth();
        float sh = client.getWindow().getScaledHeight();

        Renderer2D renderer = HudManager.get().renderer();
        CustomFontRenderer font = HudManager.get().font();

        GLUtil.enableBlend();
        GLUtil.disableDepth();
        renderer.begin(sw, sh);

        // Dim background overlay
        int dimColor = ThemeManager.current() == ThemeManager.LIGHT
                ? ColorUtils.rgba(240, 245, 250, (int) (160 * openProgress))
                : ColorUtils.rgba(5, 10, 20, (int) (180 * openProgress));
        renderer.quad(0f, 0f, sw, sh, dimColor);

        // Scale transform from center on open
        renderer.pushMatrix();
        float scale = 0.92f + 0.08f * AnimationUtils.easeOutCubic(openProgress);
        renderer.scale(scale, scale, sw / 2f, sh / 2f);

        drawMainWindow(renderer, font, mouseX, mouseY, delta);
        drawTabContent(renderer, font, mouseX, mouseY, delta);

        renderer.popMatrix();
        renderer.flush();
        GLUtil.restoreState();

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawMainWindow(Renderer2D renderer, CustomFontRenderer font, float mouseX, float mouseY, float delta) {
        // Window Outer Drop Shadow
        renderer.dropShadow(winX, winY, winW, winH, 12f, 16f, ColorUtils.rgba(0, 0, 0, 160));

        // Window Glass Surface
        int panelBg = HudStyle.PANEL;
        int panelBorder = HudStyle.BORDER;
        renderer.roundedRect(winX, winY, winW, winH, 12f, panelBg);
        renderer.roundedOutline(winX, winY, winW, winH, 12f, 1.2f, panelBorder);

        // Header Title
        float titleX = winX + PADDING;
        float titleY = winY + (NAVBAR_H - font.lineHeight(0.36f)) / 2f;
        font.drawGlow(renderer, "TESTVISUALS", titleX, titleY, 0.36f, HudStyle.ACCENT, 0xFFFFFFFF);

        // Navigation Tabs (HUD, Themes, Alts)
        String[] tabNames = {"HUD Модули", "Темы", "Аккаунты"};
        float tabsTotalW = tabNames.length * TAB_W + (tabNames.length - 1) * 8f;
        float tabStartX = winX + winW - PADDING - tabsTotalW - 28f;
        float tabY = winY + (NAVBAR_H - TAB_H) / 2f;

        for (int i = 0; i < tabNames.length; i++) {
            float tx = tabStartX + i * (TAB_W + 8f);
            boolean active = (i == currentTab);
            boolean hovered = mouseX >= tx && mouseX <= tx + TAB_W && mouseY >= tabY && mouseY <= tabY + TAB_H;

            String hoverKey = "tab_hover_" + i;
            hoverAnims.put(hoverKey, AnimationUtils.approach(hoverAnims.getOrDefault(hoverKey, 0f),
                    hovered ? 1f : 0f, delta, 12f));
            float hAnim = hoverAnims.get(hoverKey);

            int tabBg = active
                    ? HudStyle.ACCENT
                    : ColorUtils.lerp(HudStyle.BG_SOFT, HudStyle.BAR, hAnim);
            int tabTextColor = active
                    ? 0xFFFFFFFF
                    : ColorUtils.lerp(HudStyle.TEXT_DIM, HudStyle.TEXT, hAnim);

            renderer.roundedRect(tx, tabY, TAB_W, TAB_H, 6f, tabBg);
            if (active) {
                renderer.glow(tx, tabY, TAB_W, TAB_H, 6f, 6f, ColorUtils.withAlpha(HudStyle.ACCENT, 0.5f));
            }
            font.drawCentered(renderer, tabNames[i], tx + TAB_W / 2f,
                    tabY + (TAB_H - font.lineHeight(0.19f)) / 2f, 0.19f, tabTextColor);

            clickAreas.add(new ClickArea("tab_" + i, tx, tabY, TAB_W, TAB_H));
        }

        // Close Button (✕)
        float closeX = winX + winW - PADDING - 20f;
        float closeY = winY + (NAVBAR_H - 20f) / 2f;
        boolean closeHovered = mouseX >= closeX && mouseX <= closeX + 20f && mouseY >= closeY && mouseY <= closeY + 20f;
        int closeColor = closeHovered ? 0xFFFF5555 : HudStyle.TEXT_DIM;
        renderer.roundedRect(closeX, closeY, 20f, 20f, 4f, closeHovered ? 0x33FF5555 : HudStyle.BG_SOFT);
        font.drawCentered(renderer, "✕", closeX + 10f, closeY + (20f - font.lineHeight(0.18f)) / 2f, 0.18f, closeColor);
        clickAreas.add(new ClickArea("close", closeX, closeY, 20f, 20f));

        // Header Divider Line
        renderer.gradientQuadH(winX + PADDING, winY + NAVBAR_H, winW - PADDING * 2f, 1f,
                ColorUtils.withAlpha(HudStyle.BORDER, 0.8f), ColorUtils.withAlpha(HudStyle.BORDER, 0.1f));
    }

    private void drawTabContent(Renderer2D renderer, CustomFontRenderer font, float mouseX, float mouseY, float delta) {
        float contentY = winY + NAVBAR_H + PADDING;
        switch (currentTab) {
            case 0 -> drawModulesTab(renderer, font, mouseX, mouseY, contentY, delta);
            case 1 -> drawThemesTab(renderer, font, mouseX, mouseY, contentY, delta);
            case 2 -> drawAltsTab(renderer, font, mouseX, mouseY, contentY, delta);
        }
    }

    private void drawModulesTab(Renderer2D renderer, CustomFontRenderer font, float mouseX, float mouseY,
                                float startY, float delta) {
        float y = startY;
        float rowW = winW - PADDING * 2f;

        List<HudComponent> components = HudManager.get().components();
        for (HudComponent comp : components) {
            boolean hovered = mouseX >= winX + PADDING && mouseX <= winX + PADDING + rowW
                    && mouseY >= y && mouseY <= y + ROW_H;

            String hKey = "comp_h_" + comp.getId();
            hoverAnims.put(hKey, AnimationUtils.approach(hoverAnims.getOrDefault(hKey, 0f),
                    hovered ? 1f : 0f, delta, 12f));
            float hAnim = hoverAnims.get(hKey);

            int rowBg = ColorUtils.lerp(HudStyle.BG_SOFT, ColorUtils.rgba(30, 41, 59, 210), hAnim);
            renderer.roundedRect(winX + PADDING, y, rowW, ROW_H, 8f, rowBg);
            renderer.roundedOutline(winX + PADDING, y, rowW, ROW_H, 8f, 1f,
                    comp.enabled ? ColorUtils.withAlpha(HudStyle.ACCENT, 0.35f) : HudStyle.BORDER);

            // Component Icon & Name
            float textY = y + (ROW_H - font.lineHeight(0.24f)) / 2f;
            font.draw(renderer, comp.getDisplayName(), winX + PADDING + 14f, textY, 0.24f,
                    comp.enabled ? HudStyle.TEXT : HudStyle.TEXT_DIM);

            // Animated Switch
            float swW = 38f;
            float swH = 20f;
            float swX = winX + winW - PADDING - swW - 12f;
            float swY = y + (ROW_H - swH) / 2f;

            drawSwitch(renderer, comp.getId(), comp.enabled, swX, swY, delta);
            clickAreas.add(new ClickArea("toggle_" + comp.getId(), winX + PADDING, y, rowW, ROW_H));

            y += ROW_H + 8f;
        }

        // Bottom Actions Bar
        float bottomY = winY + winH - PADDING - 28f;
        float btnW = 160f;
        float btnH = 28f;

        boolean resetHover = mouseX >= winX + PADDING && mouseX <= winX + PADDING + btnW
                && mouseY >= bottomY && mouseY <= bottomY + btnH;
        int resetBg = resetHover ? ColorUtils.withAlpha(HudStyle.ACCENT, 0.3f) : HudStyle.BG_SOFT;

        renderer.roundedRect(winX + PADDING, bottomY, btnW, btnH, 6f, resetBg);
        renderer.roundedOutline(winX + PADDING, bottomY, btnW, btnH, 6f, 1f, HudStyle.BORDER);
        font.drawCentered(renderer, "Сбросить позиции", winX + PADDING + btnW / 2f,
                bottomY + (btnH - font.lineHeight(0.18f)) / 2f, 0.18f, HudStyle.TEXT);
        clickAreas.add(new ClickArea("reset_hud", winX + PADDING, bottomY, btnW, btnH));

        String tip = "Нажмите T (чат) в игре для перетаскивания панелей HUD";
        font.drawRight(renderer, tip, winX + winW - PADDING,
                bottomY + (btnH - font.lineHeight(0.16f)) / 2f, 0.16f, HudStyle.TEXT_DIM);
    }

    private void drawSwitch(Renderer2D renderer, String id, boolean enabled, float x, float y, float delta) {
        float target = enabled ? 1f : 0f;
        toggleAnims.put(id, AnimationUtils.approach(toggleAnims.getOrDefault(id, enabled ? 1f : 0f), target, delta, 14f));
        float t = toggleAnims.get(id);

        int trackColor = ColorUtils.lerp(HudStyle.TOGGLE_OFF, HudStyle.ACCENT, t);
        renderer.roundedRect(x, y, 38f, 20f, 10f, trackColor);
        renderer.roundedOutline(x, y, 38f, 20f, 10f, 1f, ColorUtils.withAlpha(HudStyle.BORDER, 0.6f));

        if (t > 0.05f) {
            renderer.glow(x, y, 38f, 20f, 10f, 4f, ColorUtils.withAlpha(HudStyle.ACCENT, t * 0.4f));
        }

        float knobX = x + 3f + t * 18f;
        float knobY = y + 3f;
        renderer.circle(knobX + 7f, knobY + 7f, 7f, 0xFFFFFFFF);
    }

    private void drawThemesTab(Renderer2D renderer, CustomFontRenderer font, float mouseX, float mouseY,
                               float startY, float delta) {
        float contentW = winW - PADDING * 2f;
        float cardW = (contentW - 12f * 2f) / 3f;
        float cardH = 92f;

        for (int i = 0; i < ThemeManager.ALL.length; i++) {
            ThemeManager.Theme theme = ThemeManager.ALL[i];
            int col = i % 3;
            int row = i / 3;

            float cx = winX + PADDING + col * (cardW + 12f);
            float cy = startY + row * (cardH + 12f);

            boolean active = (ThemeManager.current() == theme);
            boolean hovered = mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + cardH;

            int cardBg = active
                    ? ColorUtils.withAlpha(HudStyle.ACCENT, 0.22f)
                    : (hovered ? ColorUtils.withAlpha(HudStyle.BG_SOFT, 0.9f) : HudStyle.BG_SOFT);

            renderer.roundedRect(cx, cy, cardW, cardH, 8f, cardBg);
            renderer.roundedOutline(cx, cy, cardW, cardH, 8f, active ? 1.6f : 1f,
                    active ? HudStyle.ACCENT : HudStyle.BORDER);

            if (active) {
                renderer.glow(cx, cy, cardW, cardH, 8f, 8f, ColorUtils.withAlpha(HudStyle.ACCENT, 0.35f));
            }

            font.draw(renderer, theme.name, cx + 12f, cy + 12f, 0.22f, active ? HudStyle.TEXT : HudStyle.TEXT_DIM);
            if (active) {
                font.drawRight(renderer, "Активно", cx + cardW - 12f, cy + 12f, 0.17f, HudStyle.ACCENT);
            }

            // Swatch Color Bars
            float barY = cy + 46f;
            float barW = (cardW - 24f - 18f) / 4f;
            float barH = 22f;

            renderer.roundedRect(cx + 12f, barY, barW, barH, 4f, theme.bg);
            renderer.roundedRect(cx + 12f + barW + 6f, barY, barW, barH, 4f, theme.panel);
            renderer.roundedRect(cx + 12f + (barW + 6f) * 2f, barY, barW, barH, 4f, theme.accent);
            renderer.roundedRect(cx + 12f + (barW + 6f) * 3f, barY, barW, barH, 4f, theme.text);

            clickAreas.add(new ClickArea("theme_" + i, cx, cy, cardW, cardH));
        }
    }

    private void drawAltsTab(Renderer2D renderer, CustomFontRenderer font, float mouseX, float mouseY,
                             float startY, float delta) {
        float rowW = winW - PADDING * 2f;

        // Current Session Status Card
        String currentName = MinecraftClient.getInstance().getSession().getUsername();
        renderer.roundedRect(winX + PADDING, startY, rowW, 36f, 6f, HudStyle.BG_SOFT);
        renderer.roundedOutline(winX + PADDING, startY, rowW, 36f, 6f, 1f, ColorUtils.withAlpha(HudStyle.ACCENT, 0.4f));
        font.draw(renderer, "Текущий аккаунт: " + currentName, winX + PADDING + 12f,
                startY + (36f - font.lineHeight(0.22f)) / 2f, 0.22f, HudStyle.TEXT);

        // Input Field & Add Button
        float addBtnW = 100f;
        float addBtnH = 24f;
        float addBtnX = winX + winW - PADDING - addBtnW;
        float addBtnY = startY + 44f;

        boolean addHover = mouseX >= addBtnX && mouseX <= addBtnX + addBtnW
                && mouseY >= addBtnY && mouseY <= addBtnY + addBtnH;
        int addBg = addHover ? ColorUtils.withAlpha(HudStyle.ACCENT, 0.35f) : HudStyle.BG_SOFT;

        renderer.roundedRect(addBtnX, addBtnY, addBtnW, addBtnH, 5f, addBg);
        renderer.roundedOutline(addBtnX, addBtnY, addBtnW, addBtnH, 5f, 1f, HudStyle.BORDER);
        font.drawCentered(renderer, "Добавить", addBtnX + addBtnW / 2f,
                addBtnY + (addBtnH - font.lineHeight(0.18f)) / 2f, 0.18f, HudStyle.TEXT);
        clickAreas.add(new ClickArea("alt_add", addBtnX, addBtnY, addBtnW, addBtnH));

        // Saved Accounts List
        float listY = addBtnY + 36f;
        List<Config.AltData> alts = Config.alts();

        if (alts.isEmpty()) {
            font.drawCentered(renderer, "Нет сохранённых аккаунтов. Введите ник и нажмите Добавить.",
                    winX + winW / 2f, listY + 30f, 0.20f, HudStyle.TEXT_DIM);
            return;
        }

        for (int i = 0; i < Math.min(alts.size(), 6); i++) {
            Config.AltData alt = alts.get(i);
            float rowY = listY + i * 36f;
            boolean isCur = alt.name != null && alt.name.equalsIgnoreCase(currentName);

            renderer.roundedRect(winX + PADDING, rowY, rowW, 30f, 5f, HudStyle.BG_SOFT);
            font.draw(renderer, alt.name + (isCur ? " (текущий)" : ""), winX + PADDING + 12f,
                    rowY + (30f - font.lineHeight(0.20f)) / 2f, 0.20f, isCur ? HudStyle.ACCENT : HudStyle.TEXT);

            // Use button
            float useW = 60f;
            float useX = winX + winW - PADDING - 74f;
            renderer.roundedRect(useX, rowY + 3f, useW, 24f, 4f, HudStyle.BG_SOFT);
            renderer.roundedOutline(useX, rowY + 3f, useW, 24f, 4f, 1f, HudStyle.BORDER);
            font.drawCentered(renderer, "Войти", useX + useW / 2f,
                    rowY + 3f + (24f - font.lineHeight(0.16f)) / 2f, 0.16f, HudStyle.TEXT);
            clickAreas.add(new ClickArea("alt_use_" + i, useX, rowY + 3f, useW, 24f));

            // Delete button
            float delX = winX + winW - PADDING - 24f;
            renderer.roundedRect(delX, rowY + 3f, 24f, 24f, 4f, 0x33FF5555);
            font.drawCentered(renderer, "✕", delX + 12f,
                    rowY + 3f + (24f - font.lineHeight(0.16f)) / 2f, 0.16f, 0xFFFF7777);
            clickAreas.add(new ClickArea("alt_del_" + i, delX, rowY + 3f, 24f, 24f));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (ClickArea area : clickAreas) {
                if (area.contains(mouseX, mouseY)) {
                    processClick(area.id);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void processClick(String id) {
        if (id.equals("close")) {
            close();
            return;
        }
        if (id.startsWith("tab_")) {
            currentTab = Integer.parseInt(id.substring(4));
            init();
            return;
        }
        if (id.startsWith("toggle_")) {
            HudManager.get().toggle(id.substring(7));
            return;
        }
        if (id.equals("reset_hud")) {
            HudManager.get().resetPositions();
            HudManager.get().notify("Позиции HUD сброшены");
            return;
        }
        if (id.startsWith("theme_")) {
            int idx = Integer.parseInt(id.substring(6));
            ThemeManager.apply(ThemeManager.ALL[idx]);
            HudManager.get().notify("Тема: " + ThemeManager.current().name);
            return;
        }
        if (id.equals("alt_add")) {
            if (altField != null && !altField.getText().isBlank()) {
                AltManager.add(altField.getText());
                altField.setText("");
                HudManager.get().notify("Аккаунт добавлен");
            }
            return;
        }
        if (id.startsWith("alt_use_")) {
            int idx = Integer.parseInt(id.substring(8));
            Config.AltData alt = Config.alts().get(idx);
            AltManager.use(idx);
            HudManager.get().notify("Вход: " + alt.name);
            return;
        }
        if (id.startsWith("alt_del_")) {
            int idx = Integer.parseInt(id.substring(8));
            AltManager.remove(idx);
            HudManager.get().notify("Аккаунт удалён");
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (altField != null && altField.isFocused() && !altField.getText().isBlank()) {
                AltManager.add(altField.getText());
                altField.setText("");
                HudManager.get().notify("Аккаунт добавлен");
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
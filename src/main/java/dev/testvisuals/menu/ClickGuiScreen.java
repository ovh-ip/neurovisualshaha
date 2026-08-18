package dev.testvisuals.menu;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import dev.testvisuals.font.CustomFontRenderer;
import dev.testvisuals.font.GlyphAtlas;
import dev.testvisuals.hud.HudComponent;
import dev.testvisuals.hud.HudManager;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.render.Renderer2D;
import dev.testvisuals.util.AnimationUtils;
import dev.testvisuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class ClickGuiScreen extends Screen {

    public static final class GuiModule {
        public final String id;
        public final String name;
        public final String description;
        public final String category;
        public boolean enabled;
        public float toggleAnim;
        public boolean expanded;
        public float expandAnim;
        public String keybind;

        public GuiModule(String id, String name, String description, String category, boolean enabled, String keybind) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.enabled = enabled;
            this.toggleAnim = enabled ? 1f : 0f;
            this.keybind = keybind;
        }
    }

    public static final class CategoryItem {
        public final String name;
        public final String section;
        public final char icon;

        public CategoryItem(String name, String section, char icon) {
            this.name = name;
            this.section = section;
            this.icon = icon;
        }
    }

    private final List<CategoryItem> categories = new ArrayList<>();
    private final List<GuiModule> modules = new ArrayList<>();
    private int selectedCategory = 0;

    private float openProgress = 0f;
    private float scrollY = 0f;
    private float targetScrollY = 0f;

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));

        // Categories
        categories.add(new CategoryItem("Combat", "Combat", GlyphAtlas.ICON_SWORD));
        categories.add(new CategoryItem("Render", "Visuals", GlyphAtlas.ICON_RENDER));
        categories.add(new CategoryItem("Display", "Visuals", GlyphAtlas.ICON_FPS));
        categories.add(new CategoryItem("ESP", "Visuals", GlyphAtlas.ICON_EYE));
        categories.add(new CategoryItem("Player", "Player", GlyphAtlas.ICON_USER));
        categories.add(new CategoryItem("Movement", "Player", GlyphAtlas.ICON_SPEED));
        categories.add(new CategoryItem("Themes", "Settings", GlyphAtlas.ICON_BOOST));
        categories.add(new CategoryItem("Bindings", "Settings", GlyphAtlas.ICON_KEYBOARD));

        // Modules
        // Combat
        modules.add(new GuiModule("auto_gapple", "Auto GApple", "Автоматически ест яблоки", "Combat", true, "R"));
        modules.add(new GuiModule("velocity", "Velocity", "Уменьшает отталкивание от ударов", "Combat", false, "NONE"));
        modules.add(new GuiModule("fast_bow", "Fast Bow", "Флудит выстрелами из лука", "Combat", false, "NONE"));
        modules.add(new GuiModule("auto_armor", "Auto Armor", "Автоматически надевает броню", "Combat", true, "NONE"));
        modules.add(new GuiModule("weapon_spam", "Weapon Spam", "Быстрое переключение оружия", "Combat", false, "NONE"));
        modules.add(new GuiModule("no_entity_trace", "No Entity Trace", "Взаимодействие сквозь сущностей", "Combat", false, "NONE"));
        modules.add(new GuiModule("hit_boxes", "Hit Boxes", "Расширяет хит-бокс игрока", "Combat", false, "NONE"));
        modules.add(new GuiModule("auto_swap", "Auto Swap", "Авто-свап тотема и предметов", "Combat", true, "X"));

        // Render / Display / Visuals
        modules.add(new GuiModule("top_info", "Watermark", "Верхняя плашка со статусом", "Render", true, "NONE"));
        modules.add(new GuiModule("keybinds", "Keybinds", "Список активных биндов", "Render", true, "END"));
        modules.add(new GuiModule("target", "Target HUD", "Индикатор выбранной цели", "Render", true, "V"));
        modules.add(new GuiModule("effects", "Potion Effects", "Отображение активных зелий", "Render", true, "NONE"));
        modules.add(new GuiModule("notifications", "Notifications", "Всплывающие уведомления", "Render", true, "NONE"));
        modules.add(new GuiModule("fullbright", "Fullbright", "Максимальная яркость в пещерах", "Display", true, "C"));
        modules.add(new GuiModule("no_hurt_cam", "No Hurt Cam", "Отключает тряску при уроне", "Display", true, "NONE"));
        modules.add(new GuiModule("player_esp", "Player ESP", "Подсветка игроков сквозь стены", "ESP", true, "NONE"));
        modules.add(new GuiModule("chest_esp", "Chest ESP", "Подсветка сундуков и лута", "ESP", false, "NONE"));

        // Player / Movement
        modules.add(new GuiModule("auto_sprint", "Auto Sprint", "Постоянный автоматический бег", "Player", true, "NONE"));
        modules.add(new GuiModule("fast_place", "Fast Place", "Быстрая установка блоков", "Player", false, "NONE"));
        modules.add(new GuiModule("elytra_helper", "Elytra Helper", "Умный взлет на элитрах", "Movement", true, "END"));
        modules.add(new GuiModule("no_slow", "No Slowdown", "Снятие замедления от еды", "Movement", false, "NONE"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        openProgress = AnimationUtils.approach(openProgress, 1.0f, delta, 12f);
        scrollY = AnimationUtils.approach(scrollY, targetScrollY, delta, 14f);

        float sw = width;
        float sh = height;

        Renderer2D renderer = HudManager.get().renderer();
        CustomFontRenderer font = HudManager.get().font();

        renderer.begin(sw, sh);

        // Dim background overlay
        renderer.quad(0f, 0f, sw, sh, ColorUtils.rgba(8, 10, 14, (int) (160 * openProgress)));

        float winW = 460f;
        float winH = 290f;
        float winX = (sw - winW) / 2f;
        float winY = (sh - winH) / 2f;

        renderer.pushMatrix();
        float scale = 0.95f + 0.05f * AnimationUtils.easeOutCubic(openProgress);
        renderer.scale(scale, scale, sw / 2f, sh / 2f);

        // 1. Window Body (Dark translucent glass)
        renderer.roundedBordered(winX, winY, winW, winH, 8f, 1f, 0xEE121317, 0x33FFFFFF);

        // 2. Left Sidebar (115px)
        float sideW = 115f;
        renderer.roundedGradient(winX, winY, sideW, winH, 8f, 0f, 0f, 8f, 0xF50E0F13, 0xF50E0F13, 0xF50E0F13, 0xF50E0F13);
        renderer.line(winX + sideW, winY, winX + sideW, winY + winH, 1f, 0x22FFFFFF);

        // Sidebar Header
        font.draw(renderer, String.valueOf(GlyphAtlas.ICON_LOGO), winX + 14f, winY + 12f, 0.32f, 0xFFFFFFFF);
        String name = client.getSession() != null ? client.getSession().getUsername() : "Player";
        if (name.length() > 6) name = name.substring(0, 5) + "…";
        font.draw(renderer, name, winX + 32f, winY + 10f, 0.18f, 0xFFE2E8F0);
        font.draw(renderer, "Till: lifetime", winX + 32f, winY + 19f, 0.14f, 0xFF64748B);

        // Sidebar Categories
        float catY = winY + 36f;
        String lastSection = "";
        for (int i = 0; i < categories.size(); i++) {
            CategoryItem cat = categories.get(i);
            if (!cat.section.equals(lastSection)) {
                lastSection = cat.section;
                font.draw(renderer, lastSection, winX + 12f, catY + 2f, 0.15f, 0xFF475569);
                catY += 13f;
            }

            boolean sel = i == selectedCategory;
            boolean hov = mouseX >= winX + 6f && mouseX <= winX + sideW - 6f && mouseY >= catY && mouseY <= catY + 18f;

            if (sel) {
                renderer.roundedRect(winX + 6f, catY, sideW - 12f, 18f, 4f, 0xDD252830);
                renderer.roundedRect(winX + 6f, catY + 3f, 2f, 12f, 1f, 0xFFFFFFFF);
            } else if (hov) {
                renderer.roundedRect(winX + 6f, catY, sideW - 12f, 18f, 4f, 0x551C1D24);
            }

            int textCol = sel ? 0xFFFFFFFF : (hov ? 0xFFCBD5E1 : 0xFF8E9BAE);
            font.draw(renderer, String.valueOf(cat.icon), winX + 12f, catY + 4f, 0.18f, textCol);
            font.draw(renderer, cat.name, winX + 26f, catY + 4f, 0.18f, textCol);

            catY += 20f;
        }

        // 3. Main Area (Right, 345px)
        float mainX = winX + sideW + 12f;
        float mainY = winY + 10f;
        float mainW = winW - sideW - 24f;
        float mainH = winH - 20f;

        // Top Breadcrumb
        CategoryItem activeCat = categories.get(selectedCategory);
        String breadcrumb = activeCat.section + " / " + activeCat.name;
        font.draw(renderer, breadcrumb, mainX, mainY + 2f, 0.19f, 0xFF94A3B8);

        // Content: Themes tab vs Module cards
        if (activeCat.name.equals("Themes")) {
            renderThemesTab(renderer, font, mainX, mainY + 22f, mainW, mouseX, mouseY);
        } else if (activeCat.name.equals("Bindings")) {
            renderBindingsTab(renderer, font, mainX, mainY + 22f, mainW, mouseX, mouseY);
        } else {
            renderModulesTab(renderer, font, mainX, mainY + 22f, mainW, mainH - 22f, activeCat.name, mouseX, mouseY, delta);
        }

        renderer.popMatrix();
        renderer.flush();

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderModulesTab(Renderer2D renderer, CustomFontRenderer font,
                                  float x, float y, float w, float h,
                                  String categoryName, int mouseX, int mouseY, float delta) {
        List<GuiModule> filtered = new ArrayList<>();
        for (GuiModule m : modules) {
            if (m.category.equalsIgnoreCase(categoryName)) {
                filtered.add(m);
            }
        }

        float colW = (w - 8f) / 2f;
        float cardH = 38f;
        float gap = 6f;

        int rows = (int) Math.ceil(filtered.size() / 2.0);
        float totalH = rows * (cardH + gap);

        targetScrollY = Math.clamp(targetScrollY, -Math.max(0f, totalH - h), 0f);

        renderer.pushScissor(x, y, w, h);

        for (int i = 0; i < filtered.size(); i++) {
            GuiModule mod = filtered.get(i);
            int col = i % 2;
            int row = i / 2;

            float cx = x + col * (colW + 8f);
            float cy = y + scrollY + row * (cardH + gap);

            mod.toggleAnim = AnimationUtils.approach(mod.toggleAnim, mod.enabled ? 1f : 0f, delta, 14f);

            // Card body
            boolean hov = mouseX >= cx && mouseX <= cx + colW && mouseY >= cy && mouseY <= cy + cardH;
            int bg = hov ? 0xDD1C1D24 : 0xCC15161A;
            renderer.roundedBordered(cx, cy, colW, cardH, 5f, 1f, bg, 0x22FFFFFF);

            // Module Title & Subtitle
            font.draw(renderer, mod.name, cx + 8f, cy + 6f, 0.19f, mod.enabled ? 0xFFFFFFFF : 0xFFD1D5DB);
            font.draw(renderer, mod.description, cx + 8f, cy + 19f, 0.14f, 0xFF64748B);

            // Toggle Switch (Right)
            float swW = 20f;
            float swH = 11f;
            float swX = cx + colW - swW - 8f;
            float swY = cy + (cardH - swH) / 2f;

            int trackCol = ColorUtils.lerp(0xFF27272A, 0xFFFFFFFF, mod.toggleAnim);
            renderer.roundedRect(swX, swY, swW, swH, 5.5f, trackCol);

            float knobR = 4f;
            float knobX = swX + 2f + (swW - knobR * 2f - 4f) * mod.toggleAnim + knobR;
            float knobY = swY + swH / 2f;
            int knobCol = mod.toggleAnim > 0.5f ? 0xFF121316 : 0xFFFFFFFF;
            renderer.circle(knobX, knobY, knobR, knobCol);
        }

        renderer.popScissor();
    }

    private void renderThemesTab(Renderer2D renderer, CustomFontRenderer font,
                                 float x, float y, float w, int mouseX, int mouseY) {
        float cardW = (w - 8f) / 2f;
        float cardH = 42f;
        float gap = 8f;

        for (int i = 0; i < ThemeManager.ALL.length; i++) {
            ThemeManager.Theme theme = ThemeManager.ALL[i];
            int col = i % 2;
            int row = i / 2;
            float cx = x + col * (cardW + 8f);
            float cy = y + row * (cardH + gap);

            boolean active = ThemeManager.current() == theme;
            boolean hov = mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + cardH;

            int bg = active ? 0xDD252830 : (hov ? 0xDD1C1D24 : 0xCC15161A);
            int border = active ? 0xFFFFFFFF : 0x22FFFFFF;
            renderer.roundedBordered(cx, cy, cardW, cardH, 5f, 1f, bg, border);

            // Name
            font.draw(renderer, theme.name, cx + 8f, cy + 8f, 0.20f, active ? 0xFFFFFFFF : 0xFFCBD5E1);

            // Color swatches
            renderer.circle(cx + cardW - 32f, cy + 20f, 5f, theme.accent);
            renderer.circle(cx + cardW - 18f, cy + 20f, 5f, theme.panel);

            if (active) {
                font.draw(renderer, "✓ Активна", cx + 8f, cy + 22f, 0.16f, 0xFF22C55E);
            }
        }
    }

    private void renderBindingsTab(Renderer2D renderer, CustomFontRenderer font,
                                   float x, float y, float w, int mouseX, int mouseY) {
        float rowH = 22f;
        float curY = y;

        for (GuiModule mod : modules) {
            renderer.roundedBordered(x, curY, w, rowH, 4f, 1f, 0xCC15161A, 0x22FFFFFF);
            font.draw(renderer, mod.name, x + 8f, curY + 5f, 0.19f, 0xFFFFFFFF);
            font.drawRight(renderer, "[" + mod.keybind + "]", x + w - 8f, curY + 5f, 0.18f, 0xFF94A3B8);
            curY += rowH + 4f;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        float sw = width;
        float sh = height;
        float winW = 460f;
        float winH = 290f;
        float winX = (sw - winW) / 2f;
        float winY = (sh - winH) / 2f;
        float sideW = 115f;

        // 1. Sidebar clicks
        float catY = winY + 36f;
        String lastSection = "";
        for (int i = 0; i < categories.size(); i++) {
            CategoryItem cat = categories.get(i);
            if (!cat.section.equals(lastSection)) {
                lastSection = cat.section;
                catY += 13f;
            }
            if (mouseX >= winX + 6f && mouseX <= winX + sideW - 6f && mouseY >= catY && mouseY <= catY + 18f) {
                selectedCategory = i;
                targetScrollY = 0f;
                scrollY = 0f;
                return true;
            }
            catY += 20f;
        }

        // 2. Main Area clicks
        float mainX = winX + sideW + 12f;
        float mainY = winY + 32f;
        float mainW = winW - sideW - 24f;
        CategoryItem activeCat = categories.get(selectedCategory);

        if (activeCat.name.equals("Themes")) {
            float cardW = (mainW - 8f) / 2f;
            float cardH = 42f;
            float gap = 8f;
            for (int i = 0; i < ThemeManager.ALL.length; i++) {
                int col = i % 2;
                int row = i / 2;
                float cx = mainX + col * (cardW + 8f);
                float cy = mainY + row * (cardH + gap);
                if (mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + cardH) {
                    ThemeManager.apply(ThemeManager.ALL[i]);
                    HudManager.get().notify("Тема: " + ThemeManager.ALL[i].name);
                    return true;
                }
            }
        } else {
            List<GuiModule> filtered = new ArrayList<>();
            for (GuiModule m : modules) {
                if (m.category.equalsIgnoreCase(activeCat.name)) {
                    filtered.add(m);
                }
            }
            float colW = (mainW - 8f) / 2f;
            float cardH = 38f;
            float gap = 6f;
            for (int i = 0; i < filtered.size(); i++) {
                GuiModule mod = filtered.get(i);
                int col = i % 2;
                int row = i / 2;
                float cx = mainX + col * (colW + 8f);
                float cy = mainY + scrollY + row * (cardH + gap);
                if (mouseX >= cx && mouseX <= cx + colW && mouseY >= cy && mouseY <= cy + cardH) {
                    mod.enabled = !mod.enabled;
                    HudManager.get().toggle(mod.id);
                    HudManager.get().notify((mod.enabled ? "Включено: " : "Выключено: ") + mod.name);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        targetScrollY += (float) (verticalAmount * 24.0);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
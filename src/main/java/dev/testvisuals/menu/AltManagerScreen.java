package dev.testvisuals.menu;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import dev.testvisuals.font.CustomFontRenderer;
import dev.testvisuals.font.GlyphAtlas;
import dev.testvisuals.hud.Config;
import dev.testvisuals.hud.HudManager;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.render.Renderer2D;
import dev.testvisuals.util.AnimationUtils;
import dev.testvisuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;

public final class AltManagerScreen extends Screen {

    private final Screen parent;
    private final List<String> accounts = new ArrayList<>();
    private String inputName = "";
    private float openProgress = 0f;

    public AltManagerScreen(Screen parent) {
        super(Text.literal("Alt Manager"));
        this.parent = parent;
        accounts.add("clody1337");
        accounts.add("Player");
        accounts.add("WexUser");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        openProgress = AnimationUtils.approach(openProgress, 1.0f, delta, 12f);

        float sw = width;
        float sh = height;

        Renderer2D renderer = HudManager.get().renderer();
        CustomFontRenderer font = HudManager.get().font();

        renderer.begin(sw, sh);

        // Dark backdrop
        renderer.quad(0f, 0f, sw, sh, ColorUtils.rgba(10, 12, 16, (int) (180 * openProgress)));

        float winW = 340f;
        float winH = 240f;
        float winX = (sw - winW) / 2f;
        float winY = (sh - winH) / 2f;

        renderer.pushMatrix();
        float scale = 0.94f + 0.06f * AnimationUtils.easeOutCubic(openProgress);
        renderer.scale(scale, scale, sw / 2f, sh / 2f);

        // Window Body
        renderer.roundedBordered(winX, winY, winW, winH, 8f, 1f, HudStyle.PANEL, HudStyle.BORDER);

        // Header
        font.draw(renderer, GlyphAtlas.ICON_USER + "  Менеджер аккаунтов", winX + 16f, winY + 12f, 0.25f, HudStyle.TEXT);
        font.drawRight(renderer, "✕", winX + winW - 16f, winY + 12f, 0.25f, HudStyle.TEXT_DIM);

        // Current user
        String cur = "Текущий ник: " + client.getSession().getUsername();
        font.draw(renderer, cur, winX + 16f, winY + 34f, 0.20f, HudStyle.ACCENT);

        // Account list
        float listY = winY + 54f;
        for (int i = 0; i < accounts.size(); i++) {
            String acc = accounts.get(i);
            boolean hov = mouseX >= winX + 16f && mouseX <= winX + winW - 16f && mouseY >= listY && mouseY <= listY + 22f;
            int bg = hov ? 0xDD252830 : 0xAA181920;
            renderer.roundedBordered(winX + 16f, listY, winW - 32f, 22f, 4f, 1f, bg, HudStyle.BORDER);
            font.draw(renderer, acc, winX + 26f, listY + 5f, 0.20f, HudStyle.TEXT);
            if (acc.equals(client.getSession().getUsername())) {
                font.drawRight(renderer, "✓ Активен", winX + winW - 26f, listY + 5f, 0.18f, 0xFF22C55E);
            }
            listY += 26f;
        }

        // Input Box
        float inY = winY + winH - 46f;
        renderer.roundedBordered(winX + 16f, inY, winW - 32f, 28f, 5f, 1f, 0xDD121316, HudStyle.BORDER);
        String displayInput = inputName.isEmpty() ? "Введите ник и нажмите Enter..." : inputName + "_";
        int inputColor = inputName.isEmpty() ? HudStyle.TEXT_DIM : HudStyle.TEXT;
        font.draw(renderer, displayInput, winX + 24f, inY + 8f, 0.20f, inputColor);

        renderer.popMatrix();
        renderer.flush();

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float sw = width;
        float sh = height;
        float winW = 340f;
        float winH = 240f;
        float winX = (sw - winW) / 2f;
        float winY = (sh - winH) / 2f;

        if (mouseX >= winX + winW - 26f && mouseX <= winX + winW - 6f && mouseY >= winY + 6f && mouseY <= winY + 26f) {
            close();
            return true;
        }

        float listY = winY + 54f;
        for (int i = 0; i < accounts.size(); i++) {
            if (mouseX >= winX + 16f && mouseX <= winX + winW - 16f && mouseY >= listY && mouseY <= listY + 22f) {
                switchAccount(accounts.get(i));
                return true;
            }
            listY += 26f;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= 32 && chr <= 126 && inputName.length() < 16) {
            inputName += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !inputName.isEmpty()) {
            inputName = inputName.substring(0, inputName.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER && !inputName.trim().isEmpty()) {
            String name = inputName.trim();
            if (!accounts.contains(name)) {
                accounts.add(name);
            }
            switchAccount(name);
            inputName = "";
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void switchAccount(String username) {
        try {
            Session session = new Session(username, java.util.UUID.randomUUID(), "", java.util.Optional.empty(),
                    java.util.Optional.empty(), Session.AccountType.MOJANG);
            ((dev.testvisuals.mixin.SessionAccessor) client).setSession(session);
            HudManager.get().notify("Аккаунт: " + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}

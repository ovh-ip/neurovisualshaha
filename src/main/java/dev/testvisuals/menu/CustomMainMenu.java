package dev.testvisuals.menu;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.testvisuals.font.CustomFontRenderer;
import dev.testvisuals.gl.GLUtil;
import dev.testvisuals.render.Renderer2D;
import dev.testvisuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;

public final class CustomMainMenu {

    private static boolean initialized;
    private static Renderer2D renderer2d;
    private static BackgroundRenderer background;
    private static CustomFontRenderer font;
    private static int glowTexture;

    private static float time;
    private static Screen parentScreen;
    private static final List<MenuButton> buttons = new ArrayList<>();

    private CustomMainMenu() {
    }

    public static void setParentScreen(Screen screen) {
        parentScreen = screen;
    }

    public static void render(int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!initialized) {
            init();
        }

        time += Math.max(0.001f, Math.min(delta, 0.1f));

        float width = client.getWindow().getScaledWidth();
        float height = client.getWindow().getScaledHeight();
        int fbWidth = client.getWindow().getFramebufferWidth();
        int fbHeight = client.getWindow().getFramebufferHeight();

        layout(width, height);

        GLUtil.saveState();

        if (client.getFramebuffer() != null) {
            client.getFramebuffer().beginWrite(true);
        }

        RenderSystem.viewport(0, 0, fbWidth, fbHeight);
        RenderSystem.clearColor(0.04f, 0.06f, 0.10f, 1f);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // 1. Render atmospheric Minecraft background artwork
        background.render(time, fbWidth, fbHeight, mouseX, mouseY);

        // 2. Render 2D UI overlay (Center Title + Center Buttons + Minimalist aesthetic)
        GLUtil.enableBlend();
        GLUtil.disableDepth();
        renderer2d.begin(width, height);

        // Subtle dark vignette overlay over background for maximum contrast
        renderer2d.quad(0f, 0f, width, height, ColorUtils.rgba(10, 15, 25, 110));

        // Center Title & Subtitle
        float titleScale = 0.55f;
        String title = "TESTVISUALS";
        float titleY = height * 0.28f;
        font.drawGlow(renderer2d, title, (width - font.measure(title, titleScale)) / 2f, titleY, titleScale, 0xFFFFFFFF, 0xFFE2E8F0);

        String subtitle = "MINECRAFT CLIENT • 1.21.4";
        float subScale = 0.20f;
        font.drawCentered(renderer2d, subtitle, width / 2f, titleY + font.lineHeight(titleScale) + 6f, subScale, 0xFF94A3B8);

        // Center Buttons
        for (MenuButton button : buttons) {
            button.tick(mouseX, mouseY, delta);
            button.render(renderer2d, font, time, glowTexture);
        }

        // Bottom Footer Bar
        float footerY = height - 20f;
        font.draw(renderer2d, "TestVisuals Client", 16f, footerY, 0.19f, 0xFF64748B);

        String user = "Игрок: " + client.getSession().getUsername();
        font.drawRight(renderer2d, user, width - 16f, footerY, 0.19f, 0xFF64748B);

        renderer2d.flush();
        GLUtil.restoreState();
    }

    public static boolean onClick(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        if (parentScreen == null || MinecraftClient.getInstance().currentScreen != parentScreen) {
            return false;
        }
        for (MenuButton b : buttons) {
            if (b.contains((float) mouseX, (float) mouseY)) {
                b.activate();
                return true;
            }
        }
        return false;
    }

    private static void init() {
        GLUtil.ensureCapabilities();
        renderer2d = new Renderer2D();
        background = new BackgroundRenderer();
        font = CustomFontRenderer.get();
        glowTexture = Textures.createRadialGlow(64);
        initialized = true;
    }

    private static void layout(float width, float height) {
        float bw = Math.min(260f, width * 0.45f);
        float bh = 38f;
        float bx = (width - bw) / 2f;
        float startY = height * 0.44f;

        if (buttons.isEmpty()) {
            buttons.add(new MenuButton(0f, 0f, bw, bh, "Одиночная игра",
                    () -> open(new SelectWorldScreen(parentScreen))));
            buttons.add(new MenuButton(0f, 0f, bw, bh, "Сетевая игра",
                    () -> open(new MultiplayerScreen(parentScreen))));
            buttons.add(new MenuButton(0f, 0f, bw, bh, "Настройки",
                    () -> open(new OptionsScreen(parentScreen, MinecraftClient.getInstance().options))));
            buttons.add(new MenuButton(0f, 0f, bw, bh, "Выйти из игры",
                    () -> MinecraftClient.getInstance().scheduleStop()));
        }

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).position(bx, startY + i * (bh + 10f), bw, bh);
        }
    }

    private static void open(Screen screen) {
        MinecraftClient.getInstance().setScreen(screen);
    }
}
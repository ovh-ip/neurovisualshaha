package dev.testvisuals.menu;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.testvisuals.font.CustomFontRenderer;
import dev.testvisuals.font.GlyphAtlas;
import dev.testvisuals.gl.GLUtil;
import dev.testvisuals.render.Renderer2D;
import dev.testvisuals.util.AnimationUtils;
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
    private static long startTime = System.currentTimeMillis();
    private static Screen parentScreen;
    private static final List<MenuButton> buttons = new ArrayList<>();
    private static final List<SocialIcon> socials = new ArrayList<>();

    public static final class SocialIcon {
        char icon;
        float x, y, size;
        float hover;
        String url;

        SocialIcon(char icon, String url) {
            this.icon = icon;
            this.url = url;
        }

        boolean contains(float mx, float my) {
            return mx >= x && mx <= x + size && my >= y && my <= y + size;
        }
    }

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

        if (client.getFramebuffer() != null) {
            client.getFramebuffer().beginWrite(true);
        }

        RenderSystem.viewport(0, 0, fbWidth, fbHeight);
        RenderSystem.clearColor(0.04f, 0.05f, 0.08f, 1f);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // 1. Render atmospheric Minecraft background artwork
        background.render(time, fbWidth, fbHeight, mouseX, mouseY);

        // 2. Render 2D UI overlay
        renderer2d.begin(width, height);

        // Dark vignette overlay
        renderer2d.quad(0f, 0f, width, height, ColorUtils.rgba(8, 10, 14, 160));

        // Center Logo & Greeting
        float centerY = height * 0.32f;
        String logoStr = String.valueOf(GlyphAtlas.ICON_BIOHAZARD);
        float logoScale = 0.55f;
        font.drawCentered(renderer2d, logoStr, width / 2f, centerY - 46f, logoScale, 0xFFE2E8F0);

        String username = client.getSession() != null ? client.getSession().getUsername() : "Player";
        String welcome = "Добро пожаловать, " + username;
        font.drawCentered(renderer2d, welcome, width / 2f, centerY - 10f, 0.22f, 0xFF94A3B8);

        // Menu Buttons
        for (MenuButton button : buttons) {
            button.tick(mouseX, mouseY, delta);
            button.render(renderer2d, font, time, glowTexture);
        }

        // Stats underneath
        float statsY = height * 0.36f + 4 * (34f + 8f) + 12f;
        font.drawCentered(renderer2d, "Всего наиграно: 116h 35m", width / 2f, statsY, 0.18f, 0xFF64748B);

        long sessionSecs = (System.currentTimeMillis() - startTime) / 1000L;
        long m = sessionSecs / 60L;
        long s = sessionSecs % 60L;
        font.drawCentered(renderer2d, String.format("Сессия: %dm %02ds", m, s), width / 2f, statsY + 12f, 0.18f, 0xFF64748B);

        // Footer Social Icons
        for (SocialIcon soc : socials) {
            soc.hover = AnimationUtils.approach(soc.hover, soc.contains(mouseX, mouseY) ? 1f : 0f, delta, 12f);
            int col = ColorUtils.lerp(0xFF64748B, 0xFFFFFFFF, soc.hover);
            renderer2d.pushMatrix();
            float sc = 1.0f + 0.1f * soc.hover;
            renderer2d.scale(sc, sc, soc.x + soc.size / 2f, soc.y + soc.size / 2f);
            font.drawCentered(renderer2d, String.valueOf(soc.icon), soc.x + soc.size / 2f, soc.y + 2f, 0.26f, col);
            renderer2d.popMatrix();
        }

        renderer2d.flush();
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
        for (SocialIcon soc : socials) {
            if (soc.contains((float) mouseX, (float) mouseY)) {
                // Social click feedback
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

        if (socials.isEmpty()) {
            socials.add(new SocialIcon(GlyphAtlas.ICON_DISCORD, "https://discord.gg"));
            socials.add(new SocialIcon(GlyphAtlas.ICON_TELEGRAM, "https://t.me"));
            socials.add(new SocialIcon(GlyphAtlas.ICON_YOUTUBE, "https://youtube.com"));
            socials.add(new SocialIcon(GlyphAtlas.ICON_VK, "https://vk.com"));
        }

        initialized = true;
    }

    private static void layout(float width, float height) {
        float bw = Math.min(270f, width * 0.5f);
        float bh = 34f;
        float gap = 8f;
        float bx = (width - bw) / 2f;
        float startY = height * 0.36f;

        if (buttons.isEmpty()) {
            // 1. Singleplayer
            buttons.add(new MenuButton(0f, 0f, bw, bh, GlyphAtlas.ICON_COMPASS + "   Одиночная игра",
                    () -> open(new SelectWorldScreen(parentScreen))));
            // 2. Multiplayer
            buttons.add(new MenuButton(0f, 0f, bw, bh, GlyphAtlas.ICON_GLOBE + "   Сетевая игра",
                    () -> open(new MultiplayerScreen(parentScreen))));
            // 3. Alt Manager
            buttons.add(new MenuButton(0f, 0f, bw, bh, GlyphAtlas.ICON_USER + "   Менеджер аккаунтов",
                    () -> open(new AltManagerScreen(parentScreen))));
            // 4. Options (Half)
            buttons.add(new MenuButton(0f, 0f, (bw - gap) / 2f, bh, GlyphAtlas.ICON_GEAR + "   Настройки",
                    () -> open(new OptionsScreen(parentScreen, MinecraftClient.getInstance().options))));
            // 5. Quit (Half)
            buttons.add(new MenuButton(0f, 0f, (bw - gap) / 2f, bh, GlyphAtlas.ICON_CROSS + "   Выход",
                    () -> MinecraftClient.getInstance().scheduleStop()));
        }

        buttons.get(0).position(bx, startY, bw, bh);
        buttons.get(1).position(bx, startY + (bh + gap), bw, bh);
        buttons.get(2).position(bx, startY + 2f * (bh + gap), bw, bh);

        float halfW = (bw - gap) / 2f;
        buttons.get(3).position(bx, startY + 3f * (bh + gap), halfW, bh);
        buttons.get(4).position(bx + halfW + gap, startY + 3f * (bh + gap), halfW, bh);

        // Social icons at bottom
        float socSize = 22f;
        float socGap = 18f;
        float totalSocW = socials.size() * socSize + (socials.size() - 1) * socGap;
        float socX = (width - totalSocW) / 2f;
        float socY = height - 32f;
        for (int i = 0; i < socials.size(); i++) {
            SocialIcon s = socials.get(i);
            s.x = socX + i * (socSize + socGap);
            s.y = socY;
            s.size = socSize;
        }
    }

    private static void open(Screen screen) {
        MinecraftClient.getInstance().setScreen(screen);
    }
}
package dev.testvisuals.menu;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.testvisuals.font.CustomFontRenderer;
import dev.testvisuals.gl.GLUtil;
import dev.testvisuals.render.Camera3D;
import dev.testvisuals.render.Renderer2D;
import dev.testvisuals.render.Renderer3D;
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
    private static Renderer3D renderer3d;
    private static BackgroundRenderer background;
    private static CustomFontRenderer font;
    private static ParticleSystem particles;
    private static int glowTexture;

    private static float time;
    private static float mouseCamX;
    private static float mouseCamY;
    private static Screen parentScreen;
    private static final List<MenuButton> buttons = new ArrayList<>();

    private static final Camera3D camera = new Camera3D();
    private static final Vector3f origin = new Vector3f(0f, 0f, -2.2f);

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
        float aspect = fbWidth / (float) Math.max(1, fbHeight);

        layout(width, height);

        // Ensure Minecraft's framebuffer is active and properly sized
        if (client.getFramebuffer() != null) {
            client.getFramebuffer().beginWrite(true);
        }

        RenderSystem.viewport(0, 0, fbWidth, fbHeight);
        RenderSystem.clearColor(0.004f, 0.006f, 0.02f, 1f);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // Render dynamic background shader
        background.render(time, fbWidth, fbHeight, mouseX, mouseY);

        // 3D Scene Rendering
        GLUtil.enableDepth();
        GLUtil.depthMask(true);
        GLUtil.enableCull();
        GLUtil.enableBlend();

        renderScene3D(aspect, mouseX, mouseY, width, height, delta);

        GLUtil.disableDepth();
        GLUtil.disableCull();

        // 2D Scene Rendering
        renderer2d.begin(width, height);
        renderScene2D(mouseX, mouseY, width, height, delta);
        renderer2d.flush();

        // Restore clean OpenGL and Blaze3D state for Minecraft
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
        renderer3d = new Renderer3D();
        background = new BackgroundRenderer();
        font = CustomFontRenderer.get();
        particles = new ParticleSystem();
        glowTexture = Textures.createRadialGlow(64);
        initialized = true;
    }

    private static void layout(float width, float height) {
        float bw = Math.min(240f, width * 0.28f);
        float bh = 42f;
        float bx = width - bw - Math.max(24f, width * 0.05f);
        float startY = Math.max(70f, height * 0.32f);

        if (buttons.isEmpty()) {
            buttons.add(new MenuButton(0f, 0f, bw, bh, "Одиночная игра",
                    () -> open(new SelectWorldScreen(parentScreen))));
            buttons.add(new MenuButton(0f, 0f, bw, bh, "Сетевая игра",
                    () -> open(new MultiplayerScreen(parentScreen))));
            buttons.add(new MenuButton(0f, 0f, bw, bh, "Настройки",
                    () -> open(new OptionsScreen(parentScreen, MinecraftClient.getInstance().options))));
            buttons.add(new MenuButton(0f, 0f, bw, bh, "Выйти",
                    () -> MinecraftClient.getInstance().scheduleStop()));
        }

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).position(bx, startY + i * (bh + 12f), bw, bh);
        }
    }

    private static void open(Screen screen) {
        MinecraftClient.getInstance().setScreen(screen);
    }

    private static void renderScene3D(float aspect, int mouseX, int mouseY, float width, float height, float delta) {
        float targetMx = (mouseX / width - 0.5f) * 2f;
        float targetMy = ((height - mouseY) / height - 0.5f) * 2f;

        mouseCamX = AnimationUtils.approach(mouseCamX, targetMx, delta, 4f);
        mouseCamY = AnimationUtils.approach(mouseCamY, targetMy, delta, 4f);

        camera.lookAt(origin);
        camera.orbit(0.35f + mouseCamX * 0.22f, 0.18f - mouseCamY * 0.18f, 7.2f);
        renderer3d.begin(camera.projection(aspect), camera.view());

        // Central floating crystal core
        renderer3d.push();
        renderer3d.translate(1.4f, 0.15f, -1.2f);
        renderer3d.rotateY(time * 0.45f);
        renderer3d.rotateX(0.25f);
        renderer3d.crystal(1.3f, 2.4f, 0xFF4EB8FF, 0xFFA55CFF);

        // Gyroscope Torus Rings
        renderer3d.push();
        renderer3d.rotateX(0.75f + (float) Math.sin(time * 0.3f) * 0.15f);
        renderer3d.rotateY(time * 0.25f);
        renderer3d.torus(2.3f, 0.045f, 48, 12, 0xFF4AE8FF);
        renderer3d.pop();

        renderer3d.push();
        renderer3d.rotateZ(0.65f);
        renderer3d.rotateY(-time * 0.35f);
        renderer3d.torus(2.8f, 0.035f, 54, 10, 0xFFB57CFF);
        renderer3d.pop();

        // Orbiting Satellite Cubes & Beams
        int[] cubeColors = {0xFF7FB8FF, 0xFFC9A2FF, 0xFF56E5D0, 0xFFFF8EBA};
        for (int i = 0; i < 6; i++) {
            float angle = time * 0.6f + (float) (Math.PI * 2.0 * i / 6.0);
            float radius = 2.4f + 0.2f * (float) Math.sin(time * 0.8f + i * 1.5f);
            float px = (float) Math.cos(angle) * radius;
            float pz = (float) Math.sin(angle) * radius;
            float py = 0.35f * (float) Math.sin(time * 1.5f + i * 2f);

            renderer3d.push();
            renderer3d.translate(px, py, pz);
            renderer3d.rotateY(time * 2f + i);
            renderer3d.rotateX(time * 1.5f);
            renderer3d.cube(0.24f, cubeColors[i % cubeColors.length]);
            renderer3d.wireCube(0.28f, 0xFFFFFFFF);
            renderer3d.pop();
        }

        // Left background wireframe sphere
        renderer3d.pop();

        renderer3d.push();
        renderer3d.translate(-3.6f, 0.6f, -3.8f);
        renderer3d.rotateX(0.45f);
        renderer3d.rotateY(time * 0.1f);
        renderer3d.wireSphere(1.3f, 16, 22, 0x3344AAFF);
        renderer3d.pop();

        // Neon Grid Horizon
        renderer3d.push();
        renderer3d.translate(0f, -3.4f, -2.5f);
        renderer3d.grid(8f, 20, 0x1E4499FF);
        renderer3d.pop();

        renderer3d.flush();
    }

    private static void renderScene2D(int mouseX, int mouseY, float width, float height, float delta) {
        // Particles & Constellation lines
        particles.update(delta, width, height, mouseX, mouseY);
        particles.render(renderer2d, time, width, height, glowTexture);

        // Header Title Section
        float titleScale = Math.min(1.08f, width / 950f);
        String title = "TESTVISUALS";
        float titleWidth = font.measure(title, titleScale);
        float titleX = Math.max(32f, width * 0.06f);
        float titleY = height * 0.12f;

        // Title Glow & Text
        font.drawGlow(renderer2d, title, titleX, titleY, titleScale, 0xFF6EE7FF, 0xFFC084FC);

        String subtitle = "NEXT-GEN 2D / 3D CLIENT RENDERER • 1.21.4";
        font.drawWithShadow(renderer2d, subtitle, titleX + 2f,
                titleY + 68f * titleScale + 12f, 0.28f, 0xFF94A3B8, 0x88000000);

        // Title Decorative Shimmer Line
        float ulW = titleWidth * 0.85f;
        float ulY = titleY + 84f * titleScale + 14f;
        float shimmer = 0.5f + 0.5f * (float) Math.sin(time * 2f);
        renderer2d.gradientQuadH(titleX, ulY, ulW, 2.0f, 0x556EE7FF, 0x55C084FC);
        renderer2d.gradientQuadH(titleX, ulY, ulW * (0.3f + 0.7f * shimmer), 2.0f, 0xCC6EE7FF, 0xCCC084FC);

        // Info / Stats Glass Card (Left Side)
        float cardX = titleX;
        float cardY = ulY + 24f;
        float cardW = Math.min(260f, width * 0.3f);
        float cardH = 100f;

        // Card Drop Shadow and Glass Surface
        renderer2d.dropShadow(cardX, cardY, cardW, cardH, 10f, 10f, ColorUtils.rgba(0, 0, 0, 120));
        renderer2d.roundedGradient(cardX, cardY, cardW, cardH, 10f,
                ColorUtils.rgba(15, 23, 42, 180), ColorUtils.rgba(15, 23, 42, 180),
                ColorUtils.rgba(10, 15, 30, 210), ColorUtils.rgba(10, 15, 30, 210));
        renderer2d.roundedOutline(cardX, cardY, cardW, cardH, 10f, 1.2f, ColorUtils.rgba(110, 231, 255, 45));

        // Card Text / Engine Stats
        font.draw(renderer2d, "ENGINE DIAGNOSTICS", cardX + 14f, cardY + 12f, 0.26f, 0xFF6EE7FF);
        int fps = MinecraftClient.getInstance().getCurrentFps();
        font.draw(renderer2d, "Framerate: " + fps + " FPS", cardX + 14f, cardY + 34f, 0.25f, 0xFFE2E8F0);
        font.draw(renderer2d, "Pipeline: SDF 2D + Blinn 3D", cardX + 14f, cardY + 54f, 0.25f, 0xFF94A3B8);
        font.draw(renderer2d, "Canvas: " + (int) width + "x" + (int) height, cardX + 14f, cardY + 74f, 0.25f, 0xFF94A3B8);

        // Right Menu Buttons
        for (MenuButton button : buttons) {
            button.tick(mouseX, mouseY, delta);
            button.render(renderer2d, font, time, glowTexture);
        }

        // Bottom Footer Bar
        float footerY = height - 24f;
        font.draw(renderer2d, "TestVisuals v1.0.0 (Optimized Fabric)", 18f, footerY, 0.26f, 0xFF64748B);
        String rightText = "OpenGL 3.3 Core Profile • Ready";
        font.draw(renderer2d, rightText, width - font.measure(rightText, 0.26f) - 18f, footerY, 0.26f, 0xFF64748B);
    }
}
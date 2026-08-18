package dev.testvisuals.gl;

import java.util.ArrayDeque;
import java.util.Deque;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public final class GLUtil {

    private static boolean initialized;
    private static final Deque<ScissorBounds> scissorStack = new ArrayDeque<>();

    public record ScissorBounds(int x, int y, int width, int height) {}

    private GLUtil() {
    }

    public static void ensureCapabilities() {
        if (initialized) {
            return;
        }
        if (GL.getCapabilities() == null) {
            GL.createCapabilities();
        }
        initialized = true;
    }

    public static void bindVertexArray(int vao) {
        GlStateManager._glBindVertexArray(vao);
    }

    public static void useProgram(int program) {
        GlStateManager._glUseProgram(program);
    }

    public static void activeTexture(int texture) {
        GlStateManager._activeTexture(texture);
    }

    public static void bindTexture(int texture) {
        GlStateManager._bindTexture(texture);
    }

    public static void pushScissor(float x, float y, float width, float height) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        if (window == null) return;

        double scale = window.getScaleFactor();

        int sx = (int) Math.floor(x * scale);
        int sy = (int) Math.floor((window.getScaledHeight() - (y + height)) * scale);
        int sw = (int) Math.ceil(width * scale);
        int sh = (int) Math.ceil(height * scale);

        if (!scissorStack.isEmpty()) {
            ScissorBounds parent = scissorStack.peek();
            int nx = Math.max(sx, parent.x);
            int ny = Math.max(sy, parent.y);
            int nx2 = Math.min(sx + sw, parent.x + parent.width);
            int ny2 = Math.min(sy + sh, parent.y + parent.height);

            sw = Math.max(0, nx2 - nx);
            sh = Math.max(0, ny2 - ny);
            sx = nx;
            sy = ny;
        }

        ScissorBounds bounds = new ScissorBounds(sx, sy, sw, sh);
        scissorStack.push(bounds);

        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(bounds.x, bounds.y, Math.max(0, bounds.width), Math.max(0, bounds.height));
    }

    public static void popScissor() {
        if (scissorStack.isEmpty()) {
            return;
        }
        scissorStack.pop();
        if (scissorStack.isEmpty()) {
            GlStateManager._disableScissorTest();
        } else {
            ScissorBounds top = scissorStack.peek();
            GlStateManager._scissorBox(top.x, top.y, Math.max(0, top.width), Math.max(0, top.height));
        }
    }

    public static void enableBlend() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void enableAdditiveBlend() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
    }

    public static void disableBlend() {
        RenderSystem.disableBlend();
    }

    public static void enableDepth() {
        RenderSystem.enableDepthTest();
    }

    public static void disableDepth() {
        RenderSystem.disableDepthTest();
    }

    public static void depthMask(boolean flag) {
        RenderSystem.depthMask(flag);
    }

    public static void enableCull() {
        RenderSystem.enableCull();
    }

    public static void disableCull() {
        RenderSystem.disableCull();
    }

    public static void restoreState() {
        bindVertexArray(0);
        useProgram(0);
        activeTexture(GL13.GL_TEXTURE0);
        bindTexture(0);
        enableBlend();
        RenderSystem.defaultBlendFunc();
        disableDepth();
        depthMask(true);
        enableCull();
        if (!scissorStack.isEmpty()) {
            scissorStack.clear();
            GlStateManager._disableScissorTest();
        }
    }
}
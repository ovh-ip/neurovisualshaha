package dev.testvisuals.gl;

import java.util.ArrayDeque;
import java.util.Deque;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

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

    public static void pushScissor(float x, float y, float width, float height) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        if (window == null) return;

        double scale = window.getScaleFactor();
        int fbHeight = window.getFramebufferHeight();

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

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(bounds.x, bounds.y, Math.max(0, bounds.width), Math.max(0, bounds.height));
    }

    public static void popScissor() {
        if (scissorStack.isEmpty()) {
            return;
        }
        scissorStack.pop();
        if (scissorStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            ScissorBounds top = scissorStack.peek();
            GL11.glScissor(top.x, top.y, Math.max(0, top.width), Math.max(0, top.height));
        }
    }

    public static void enableBlend() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void enableAdditiveBlend() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
    }

    public static void disableBlend() {
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void enableDepth() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void disableDepth() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public static void depthMask(boolean flag) {
        GL11.glDepthMask(flag);
    }

    public static void enableCull() {
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public static void disableCull() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }
}
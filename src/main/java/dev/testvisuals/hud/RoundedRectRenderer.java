package dev.testvisuals.hud;

import dev.testvisuals.render.Renderer2D;

public final class RoundedRectRenderer {

    private RoundedRectRenderer() {
    }

    public static void box(Renderer2D renderer, float x, float y, float w, float h, float radius) {
        renderer.dropShadow(x, y, w, h, radius, 6f, HudStyle.SHADOW);
        renderer.roundedRect(x, y, w, h, radius, HudStyle.BG);
        renderer.roundedOutline(x, y, w, h, radius, 1f, HudStyle.BORDER);
    }

    public static void chip(Renderer2D renderer, float x, float y, float w, float h, float radius, int color) {
        renderer.roundedRect(x, y, w, h, radius, color);
    }

    public static void progressBar(Renderer2D renderer, float x, float y, float w, float h,
                                   float radius, float progress, int fill, int empty) {
        renderer.roundedRect(x, y, w, h, radius, empty);
        if (progress > 0.001f) {
            renderer.roundedRect(x, y, w * Math.clamp(progress, 0f, 1f), h, radius, fill);
        }
    }
}
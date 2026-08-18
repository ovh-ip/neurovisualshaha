package dev.testvisuals.menu;

import dev.testvisuals.font.CustomFontRenderer;
import dev.testvisuals.render.Renderer2D;
import dev.testvisuals.util.AnimationUtils;
import dev.testvisuals.util.ColorUtils;

public final class MenuButton {

    public float x;
    public float y;
    public float width;
    public float height;

    private final String label;
    private final Runnable action;
    private boolean hovered;
    private float hoverProgress;
    private float clickRipple;

    public MenuButton(float x, float y, float width, float height, String label, Runnable action) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
        this.action = action;
    }

    public void position(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(float mx, float my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public void activate() {
        clickRipple = 1.0f;
        action.run();
    }

    public void tick(float mouseX, float mouseY, float delta) {
        boolean now = contains(mouseX, mouseY);
        hovered = now;

        float target = hovered ? 1f : 0f;
        float speed = hovered ? 14f : 8f;
        hoverProgress = AnimationUtils.approach(hoverProgress, target, delta, speed);

        if (clickRipple > 0f) {
            clickRipple = Math.max(0f, clickRipple - delta * 4f);
        }
    }

    public void render(Renderer2D renderer, CustomFontRenderer font, float time, int glowTexture) {
        float h = AnimationUtils.easeOutCubic(hoverProgress);
        float radius = 8f;

        // Outer Glow / Drop Shadow on hover
        if (h > 0.01f) {
            int glowColor = ColorUtils.rgba(110, 231, 255, (int) (65 * h));
            renderer.glow(x, y, width, height, radius, 12f * h, glowColor);
        }

        // Glassmorphic Body Background
        int bgTop = ColorUtils.lerp(ColorUtils.rgba(14, 20, 36, 175), ColorUtils.rgba(24, 38, 68, 220), h);
        int bgBottom = ColorUtils.lerp(ColorUtils.rgba(10, 14, 26, 195), ColorUtils.rgba(18, 28, 52, 235), h);
        renderer.roundedGradient(x, y, width, height, radius, bgTop, bgTop, bgBottom, bgBottom);

        // Animated Glass Border
        int borderTL = ColorUtils.lerp(ColorUtils.rgba(110, 231, 255, 30), ColorUtils.rgba(110, 231, 255, 180), h);
        int borderBR = ColorUtils.lerp(ColorUtils.rgba(181, 140, 255, 20), ColorUtils.rgba(181, 140, 255, 140), h);
        renderer.roundedGradient(x, y, width, height, radius, radius, radius, radius,
                0, 0, 0, 0, 1.2f, borderTL);

        // Left accent bar
        float barHeight = height * (0.35f + 0.45f * h);
        float barY = y + (height - barHeight) * 0.5f;
        int barTop = ColorUtils.rgba(110, 231, 255, (int) (120 + 135 * h));
        int barBottom = ColorUtils.rgba(181, 140, 255, (int) (120 + 135 * h));
        renderer.roundedGradient(x + 2.5f, barY, 3.0f, barHeight, 1.5f, barTop, barTop, barBottom, barBottom);

        // Bottom hover shimmer line
        if (h > 0.01f) {
            float ulW = width * 0.7f * h;
            float ulX = x + (width - ulW) * 0.5f;
            int ulColor = ColorUtils.rgba(110, 231, 255, (int) (180 * h));
            renderer.gradientQuadH(ulX, y + height - 2.5f, ulW, 1.5f, 0x006EE7FF, ulColor);
        }

        // Text rendering
        float textScale = 0.32f;
        float textY = y + (height - font.lineHeight(textScale)) * 0.5f + 1f;
        int textColor = ColorUtils.lerp(0xFFB4C6E2, 0xFFFFFFFF, h);
        font.drawCentered(renderer, label, x + width * 0.5f + (h * 2f), textY, textScale, textColor);
    }
}
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
        action.run();
    }

    public void tick(float mouseX, float mouseY, float delta) {
        hovered = contains(mouseX, mouseY);
        float target = hovered ? 1.0f : 0.0f;
        hoverProgress = AnimationUtils.approach(hoverProgress, target, delta, 12f);
    }

    public void render(Renderer2D renderer, CustomFontRenderer font, float time, int glowTexture) {
        float h = AnimationUtils.easeOutCubic(hoverProgress);
        float radius = 6f;

        renderer.pushMatrix();

        // Smooth scale down effect on hover (1.0 -> 0.975)
        float scale = 1.0f - 0.025f * h;
        renderer.scale(scale, scale, x + width / 2f, y + height / 2f);

        // Dark glassmorphic button body
        int bg = ColorUtils.lerp(0xD9121317, 0xF01C1D24, h);
        int border = ColorUtils.lerp(0x33FFFFFF, 0x88FFFFFF, h);
        renderer.roundedBordered(x, y, width, height, radius, 1f, bg, border);

        // Crisp centered text
        float textScale = 0.25f;
        float textY = y + (height - font.lineHeight(textScale)) / 2f + 1f;
        int textColor = ColorUtils.lerp(0xFFD1D5DB, 0xFFFFFFFF, h);
        font.drawCentered(renderer, label, x + width / 2f, textY, textScale, textColor);

        renderer.popMatrix();
    }
}
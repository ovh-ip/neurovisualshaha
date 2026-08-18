package dev.testvisuals.hud;

public enum Anchor {

    TOP_LEFT(0f, 0f),
    TOP_CENTER(0.5f, 0f),
    TOP_RIGHT(1f, 0f),
    MIDDLE_LEFT(0f, 0.5f),
    CENTER(0.5f, 0.5f),
    MIDDLE_RIGHT(1f, 0.5f),
    BOTTOM_LEFT(0f, 1f),
    BOTTOM_CENTER(0.5f, 1f),
    BOTTOM_RIGHT(1f, 1f);

    private final float hx;
    private final float hy;

    Anchor(float hx, float hy) {
        this.hx = hx;
        this.hy = hy;
    }

    public float resolveX(float screenWidth, float width, float offsetX) {
        return screenWidth * hx - width * hx + offsetX;
    }

    public float resolveY(float screenHeight, float height, float offsetY) {
        return screenHeight * hy - height * hy + offsetY;
    }
}
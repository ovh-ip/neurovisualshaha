package dev.testvisuals.hud;

public final class HudPosition {

    public Anchor anchor = Anchor.TOP_LEFT;
    public float offsetX = 10f;
    public float offsetY = 10f;

    public HudPosition() {
    }

    public HudPosition(Anchor anchor, float offsetX, float offsetY) {
        this.anchor = anchor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public float resolveX(float screenWidth, float width) {
        return anchor.resolveX(screenWidth, width, offsetX);
    }

    public float resolveY(float screenHeight, float height) {
        return anchor.resolveY(screenHeight, height, offsetY);
    }
}
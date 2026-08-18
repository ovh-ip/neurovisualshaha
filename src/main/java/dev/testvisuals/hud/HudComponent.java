package dev.testvisuals.hud;

import dev.testvisuals.font.CustomFontRenderer;
import dev.testvisuals.render.Renderer2D;

public abstract class HudComponent {

    protected final String id;
    protected final String displayName;

    public HudPosition position = new HudPosition();
    public boolean enabled = true;

    protected float screenX;
    protected float screenY;

    protected HudComponent(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract float getWidth();

    public abstract float getHeight();

    protected abstract void renderContent(Renderer2D renderer, float delta, boolean editMode);

    public final void render(Renderer2D renderer, float screenWidth, float screenHeight, float delta, boolean editMode) {
        screenX = position.resolveX(screenWidth, getWidth());
        screenY = position.resolveY(screenHeight, getHeight());
        renderContent(renderer, delta, editMode);
    }

    public final boolean contains(float x, float y) {
        return x >= screenX && x <= screenX + getWidth() && y >= screenY && y <= screenY + getHeight();
    }

    protected final CustomFontRenderer font() {
        return HudManager.get().font();
    }

    public final void dragTo(float mouseX, float mouseY, float screenWidth, float screenHeight) {
        position.offsetX = mouseX - position.anchor.resolveX(screenWidth, 0f, 0f);
        position.offsetY = mouseY - position.anchor.resolveY(screenHeight, 0f, 0f);
        screenX = position.resolveX(screenWidth, getWidth());
        screenY = position.resolveY(screenHeight, getHeight());
    }
}
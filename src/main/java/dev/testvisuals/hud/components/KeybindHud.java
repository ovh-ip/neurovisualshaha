package dev.testvisuals.hud.components;

import java.util.ArrayList;
import java.util.List;

import dev.testvisuals.font.GlyphAtlas;
import dev.testvisuals.hud.Anchor;
import dev.testvisuals.hud.HudComponent;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.render.Renderer2D;

public final class KeybindHud extends HudComponent {

    private static final float WIDTH = 135f;
    private static final float HEADER_HEIGHT = 18f;
    private static final float ROW_HEIGHT = 14f;
    private static final float SCALE = 0.22f;

    public record KeyEntry(String name, String bind) {}

    public KeybindHud() {
        super("keybinds", "Keybinds");
        position.anchor = Anchor.MIDDLE_LEFT;
        position.offsetX = 8f;
        position.offsetY = -20f;
    }

    private List<KeyEntry> resolveKeybinds(boolean editMode) {
        List<KeyEntry> list = new ArrayList<>();
        list.add(new KeyEntry("ElytraHelper", "END"));
        list.add(new KeyEntry("TargetHUD", "V"));
        if (editMode) {
            list.add(new KeyEntry("AutoTotem", "R"));
        }
        return list;
    }

    @Override
    public float getWidth() {
        return WIDTH;
    }

    @Override
    public float getHeight() {
        List<KeyEntry> keys = resolveKeybinds(false);
        int rows = Math.max(1, keys.size());
        return HEADER_HEIGHT + rows * ROW_HEIGHT + 4f;
    }

    @Override
    protected void renderContent(Renderer2D renderer, float delta, boolean editMode) {
        List<KeyEntry> keys = resolveKeybinds(editMode);
        float h = HEADER_HEIGHT + keys.size() * ROW_HEIGHT + 4f;

        // Dark card background & border
        renderer.roundedBordered(screenX, screenY, WIDTH, h, 6f, 1f, HudStyle.BG, HudStyle.BORDER);

        // Header: Keybinds + Keyboard icon
        float headerTextY = screenY + 4f;
        font().draw(renderer, "Keybinds", screenX + 8f, headerTextY, SCALE, HudStyle.TEXT);
        font().drawRight(renderer, String.valueOf(GlyphAtlas.ICON_KEYBOARD), screenX + WIDTH - 8f, headerTextY, SCALE, HudStyle.TEXT_DIM);

        // Rows
        float rowY = screenY + HEADER_HEIGHT + 2f;
        for (KeyEntry entry : keys) {
            font().draw(renderer, entry.name(), screenX + 8f, rowY, SCALE, HudStyle.TEXT);
            font().drawRight(renderer, entry.bind(), screenX + WIDTH - 8f, rowY, SCALE, HudStyle.TEXT_DIM);
            rowY += ROW_HEIGHT;
        }
    }
}
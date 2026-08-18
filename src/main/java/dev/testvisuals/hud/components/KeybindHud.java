package dev.testvisuals.hud.components;

import java.util.ArrayList;
import java.util.List;

import dev.testvisuals.font.CustomFontRenderer;
import dev.testvisuals.hud.HudComponent;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.hud.RoundedRectRenderer;
import dev.testvisuals.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public final class KeybindHud extends HudComponent {

    private static final float ROW_HEIGHT = 17f;
    private static final float PADDING = 8f;
    private static final float GAP = 2f;
    private static final float TEXT_SCALE = 0.15f;

    private static final class Row {
        String name;
        String key;

        Row(String name, String key) {
            this.name = name;
            this.key = key;
        }
    }

    public KeybindHud() {
        super("keybind", "Keybinds");
        position.anchor = dev.testvisuals.hud.Anchor.MIDDLE_RIGHT;
        position.offsetX = -12f;
        position.offsetY = 0f;
    }

    private List<Row> collectRows() {
        List<Row> rows = new ArrayList<>();
        int count = 0;
        for (KeyBinding keyBinding : MinecraftClient.getInstance().options.allKeys) {
            if (keyBinding.isUnbound()) {
                continue;
            }
            String translationKey = keyBinding.getTranslationKey();
            if (translationKey.startsWith("key.hotbar.")) {
                continue;
            }
            String name = KeyBinding.getLocalizedName(translationKey).get().getString();
            String key = keyBinding.getBoundKeyLocalizedText().getString();
            rows.add(new Row(name, key));
            count++;
            if (count >= 10) {
                break;
            }
        }
        return rows;
    }

    @Override
    public float getWidth() {
        return 150f;
    }

    @Override
    public float getHeight() {
        List<Row> rows = collectRows();
        if (rows.isEmpty()) {
            return 0f;
        }
        return PADDING * 2f + rows.size() * ROW_HEIGHT + (rows.size() - 1) * GAP;
    }

    @Override
    protected void renderContent(Renderer2D renderer, float delta, boolean editMode) {
        List<Row> rows = collectRows();
        if (rows.isEmpty()) {
            return;
        }
        float h = getHeight();
        RoundedRectRenderer.box(renderer, screenX, screenY, getWidth(), h, 6f);

        CustomFontRenderer font = font();
        float y = screenY + PADDING;
        for (Row row : rows) {
            float textY = y + (ROW_HEIGHT - font.lineHeight(TEXT_SCALE)) / 2f;
            font.draw(renderer, row.name, screenX + PADDING, textY, TEXT_SCALE, HudStyle.TEXT_DIM);
            float keyW = font.measure(row.key, TEXT_SCALE) + 12f;
            RoundedRectRenderer.chip(renderer, screenX + getWidth() - PADDING - keyW, y + 2f,
                    keyW, ROW_HEIGHT - 4f, 4f, HudStyle.BG_SOFT);
            font.draw(renderer, row.key, screenX + getWidth() - PADDING - keyW + 6f, textY, TEXT_SCALE, HudStyle.TEXT);
            y += ROW_HEIGHT + GAP;
        }
    }
}
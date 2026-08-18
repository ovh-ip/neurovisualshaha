package dev.testvisuals.hud.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.testvisuals.hud.HudComponent;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.hud.RoundedRectRenderer;
import dev.testvisuals.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

public final class EffectsHud extends HudComponent {

    private static final float ROW_HEIGHT = 22f;
    private static final float PADDING = 8f;
    private static final float GAP = 2f;
    private static final float ICON_SIZE = 14f;
    private static final float TEXT_SCALE = 0.16f;
    private static final float DOTS = 5f;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Map<String, Long> startedAt = new HashMap<>();

    public EffectsHud() {
        super("effects", "Effects");
        position.anchor = dev.testvisuals.hud.Anchor.TOP_RIGHT;
        position.offsetX = -12f;
        position.offsetY = 40f;
    }

    private List<StatusEffectInstance> collect() {
        if (client.player == null) {
            return List.of();
        }
        List<StatusEffectInstance> effects = new ArrayList<>(client.player.getStatusEffects());
        effects.sort((a, b) -> {
            if (a.isAmbient() != b.isAmbient()) {
                return a.isAmbient() ? 1 : -1;
            }
            return Integer.compare(b.getDuration(), a.getDuration());
        });
        return effects;
    }

    @Override
    public float getWidth() {
        return 196f;
    }

    @Override
    public float getHeight() {
        List<StatusEffectInstance> effects = collect();
        if (effects.isEmpty()) {
            return 0f;
        }
        return PADDING * 2f + effects.size() * ROW_HEIGHT + (effects.size() - 1) * GAP;
    }

    @Override
    protected void renderContent(Renderer2D renderer, float delta, boolean editMode) {
        List<StatusEffectInstance> effects = collect();
        if (effects.isEmpty()) {
            return;
        }
        float h = getHeight();
        RoundedRectRenderer.box(renderer, screenX, screenY, getWidth(), h, 6f);

        float y = screenY + PADDING;
        for (StatusEffectInstance effect : effects) {
            drawIcon(renderer, effect, y);
            drawRowText(renderer, effect, y);
            drawProgressDots(renderer, effect, y);
            y += ROW_HEIGHT + GAP;
        }
    }

    private void drawIcon(Renderer2D renderer, StatusEffectInstance effect, float rowY) {
        try {
            Sprite sprite = client.getStatusEffectSpriteManager().getSprite(effect.getEffectType());
            SpriteAtlasTexture atlas = (SpriteAtlasTexture) client.getTextureManager()
                    .getTexture(Identifier.ofVanilla("textures/atlas/mob_effects.png"));
            float x = screenX + PADDING;
            float y = rowY + (ROW_HEIGHT - ICON_SIZE) / 2f;
            renderer.setSaturate(0f);
            renderer.texturedQuad(atlas.getGlId(), x, y, ICON_SIZE, ICON_SIZE,
                    sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), 0xE6FFFFFF);
            renderer.setSaturate(1f);
        } catch (Exception e) {
            font().draw(renderer, "✦", screenX + PADDING + 1f, rowY + 1f, TEXT_SCALE * 1.4f, HudStyle.TEXT_DIM);
        }
    }

    private void drawRowText(Renderer2D renderer, StatusEffectInstance effect, float rowY) {
        String name = effect.getEffectType().value().getName().getString();
        String roman = roman(effect.getAmplifier() + 1);
        String line = roman.isEmpty() ? name : name + " " + roman;
        String duration = duration(effect);

        float dotsW = DOTS * 5f;
        float maxTextW = getWidth() - PADDING * 2f - ICON_SIZE - 8f - dotsW - 4f;
        float scale = TEXT_SCALE;

        float textX = screenX + PADDING + ICON_SIZE + 8f;
        float textY = rowY + 1f;

        if (font().measure(line, scale) > maxTextW) {
            line = truncateByWidth(line, maxTextW, scale);
        }
        font().draw(renderer, line, textX, textY, scale, HudStyle.TEXT);
        font().draw(renderer, duration, textX, textY + font().lineHeight(scale) + 1f,
                scale * 0.75f, HudStyle.TEXT_DIM);
    }

    private void drawProgressDots(Renderer2D renderer, StatusEffectInstance effect, float rowY) {
        if (effect.isInfinite()) {
            return;
        }
        float fraction = fractionRemaining(effect);
        int filled = (int) Math.round(fraction * DOTS);

        float dotSize = 3f;
        float gap = 2f;
        float startX = screenX + getWidth() - PADDING - (DOTS * (dotSize + gap) - gap);
        float dotY = rowY + (ROW_HEIGHT - dotSize) / 2f;
        for (int i = 0; i < (int) DOTS; i++) {
            int color = i < filled ? HudStyle.FILL : HudStyle.EMPTY;
            renderer.circle(startX + i * (dotSize + gap) + dotSize / 2f, dotY + dotSize / 2f,
                    dotSize / 2f, color);
        }
    }

    private float fractionRemaining(StatusEffectInstance effect) {
        long now = client.world != null ? client.world.getTime() : 0L;
        String key = effect.getEffectType().getKey().map(Object::toString).orElse("?") + ":" + effect.getAmplifier();
        startedAt.computeIfAbsent(key, k -> now);
        long started = startedAt.get(key);
        int elapsed = (int) Math.max(0L, now - started);
        int duration = effect.getDuration();
        int remaining = Math.max(0, duration - elapsed);
        return Math.clamp(remaining / (float) duration, 0f, 1f);
    }

    private String roman(int value) {
        if (value <= 1) {
            return "";
        }
        return switch (value) {
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> "V+";
        };
    }

    private String duration(StatusEffectInstance effect) {
        if (effect.isInfinite()) {
            return "∞";
        }
        int seconds = effect.getDuration() / 20;
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    private String truncateByWidth(String text, float maxWidth, float scale) {
        if (font().measure(text, scale) <= maxWidth) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i));
            if (font().measure(sb.toString() + "…", scale) > maxWidth) {
                sb.setLength(sb.length() - 1);
                break;
            }
        }
        return sb + "…";
    }
}
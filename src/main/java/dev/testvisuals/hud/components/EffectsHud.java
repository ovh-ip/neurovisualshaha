package dev.testvisuals.hud.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.testvisuals.font.GlyphAtlas;
import dev.testvisuals.hud.Anchor;
import dev.testvisuals.hud.HudComponent;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;

public final class EffectsHud extends HudComponent {

    private static final float CARD_WIDTH = 115f;
    private static final float CARD_HEIGHT = 26f;
    private static final float CARD_GAP = 4f;

    private final MinecraftClient client = MinecraftClient.getInstance();

    public EffectsHud() {
        super("effects", "Potions");
        position.anchor = Anchor.MIDDLE_RIGHT;
        position.offsetX = -8f;
        position.offsetY = -30f;
    }

    public record EffectDisplay(String name, String duration, char icon, int iconColor) {}

    private List<EffectDisplay> resolveEffects(boolean editMode) {
        List<EffectDisplay> list = new ArrayList<>();
        if (client.player != null) {
            Collection<StatusEffectInstance> active = client.player.getStatusEffects();
            for (StatusEffectInstance inst : active) {
                RegistryEntry<StatusEffect> effect = inst.getEffectType();
                String name = effect.value().getName().getString();
                if (inst.getAmplifier() > 0) {
                    name += " " + roman(inst.getAmplifier() + 1);
                }
                String dur = formatDuration(inst.getDuration());
                char icon = iconFor(effect);
                int color = colorFor(effect);
                list.add(new EffectDisplay(name, dur, icon, color));
            }
        }
        if (list.isEmpty() && editMode) {
            list.add(new EffectDisplay("Regeneration III", "**:**", GlyphAtlas.ICON_HEART, 0xFFEF4444));
            list.add(new EffectDisplay("Health Boost III", "**:**", GlyphAtlas.ICON_BOOST, 0xFFEF4444));
            list.add(new EffectDisplay("Resistance", "**:**", GlyphAtlas.ICON_SHIELD, 0xFF94A3B8));
            list.add(new EffectDisplay("Strength III", "**:**", GlyphAtlas.ICON_SWORD, 0xFFF59E0B));
        }
        return list;
    }

    @Override
    public float getWidth() {
        return CARD_WIDTH;
    }

    @Override
    public float getHeight() {
        List<EffectDisplay> effects = resolveEffects(false);
        int count = Math.max(1, effects.size());
        return count * CARD_HEIGHT + (count - 1) * CARD_GAP;
    }

    @Override
    protected void renderContent(Renderer2D renderer, float delta, boolean editMode) {
        List<EffectDisplay> effects = resolveEffects(editMode);
        float curY = screenY;

        for (EffectDisplay eff : effects) {
            // Dark card pill
            renderer.roundedBordered(screenX, curY, CARD_WIDTH, CARD_HEIGHT, 5f, 1f, HudStyle.BG, HudStyle.BORDER);

            // Left Icon
            font().draw(renderer, String.valueOf(eff.icon()), screenX + 7f, curY + 6f, 0.22f, eff.iconColor());

            // Right: Name + Duration
            font().draw(renderer, eff.name(), screenX + 24f, curY + 4f, 0.19f, HudStyle.TEXT);
            font().draw(renderer, eff.duration(), screenX + 24f, curY + 14f, 0.16f, HudStyle.TEXT_DIM);

            curY += CARD_HEIGHT + CARD_GAP;
        }
    }

    private char iconFor(RegistryEntry<StatusEffect> effect) {
        if (effect.matches(StatusEffects.REGENERATION)) return GlyphAtlas.ICON_HEART;
        if (effect.matches(StatusEffects.HEALTH_BOOST) || effect.matches(StatusEffects.ABSORPTION)) return GlyphAtlas.ICON_BOOST;
        if (effect.matches(StatusEffects.RESISTANCE)) return GlyphAtlas.ICON_SHIELD;
        if (effect.matches(StatusEffects.STRENGTH)) return GlyphAtlas.ICON_SWORD;
        if (effect.matches(StatusEffects.SPEED) || effect.matches(StatusEffects.HASTE)) return GlyphAtlas.ICON_SPEED;
        return GlyphAtlas.ICON_BOOST;
    }

    private int colorFor(RegistryEntry<StatusEffect> effect) {
        if (effect.matches(StatusEffects.REGENERATION)) return 0xFFEF4444;
        if (effect.matches(StatusEffects.HEALTH_BOOST) || effect.matches(StatusEffects.ABSORPTION)) return 0xFFF59E0B;
        if (effect.matches(StatusEffects.RESISTANCE)) return 0xFF94A3B8;
        if (effect.matches(StatusEffects.STRENGTH)) return 0xFFEF4444;
        if (effect.matches(StatusEffects.SPEED)) return 0xFF38BDF8;
        return 0xFFE2E8F0;
    }

    private String formatDuration(int ticks) {
        int secs = ticks / 20;
        int m = secs / 60;
        int s = secs % 60;
        return String.format("%02d:%02d", m, s);
    }

    private String roman(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }
}
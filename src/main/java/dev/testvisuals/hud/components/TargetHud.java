package dev.testvisuals.hud.components;

import com.mojang.authlib.GameProfile;

import dev.testvisuals.hud.HudComponent;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.hud.RoundedRectRenderer;
import dev.testvisuals.render.Renderer2D;
import dev.testvisuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class TargetHud extends HudComponent {

    private static final float WIDTH = 180f;
    private static final float HEIGHT = 52f;
    private static final float AVATAR_SIZE = 34f;
    private static final float TEXT_SCALE = 0.22f;
    private static final float HEART_SCALE = 0.45f;

    private final MinecraftClient client = MinecraftClient.getInstance();

    public TargetHud() {
        super("target", "Target HUD");
        position.anchor = dev.testvisuals.hud.Anchor.BOTTOM_LEFT;
        position.offsetX = 16f;
        position.offsetY = 60f;
    }

    private LivingEntity resolveTarget() {
        if (client.player == null) {
            return null;
        }
        LivingEntity attacking = client.player.getAttacking();
        if (attacking != null && attacking.isAlive() && !attacking.isRemoved()) {
            return attacking;
        }
        Entity targeted = client.targetedEntity;
        if (targeted instanceof LivingEntity living && living.isAlive() && living != client.player) {
            return living;
        }
        return null;
    }

    @Override
    public float getWidth() {
        return WIDTH;
    }

    @Override
    public float getHeight() {
        return HEIGHT;
    }

    @Override
    protected void renderContent(Renderer2D renderer, float delta, boolean editMode) {
        LivingEntity target = resolveTarget();
        if (target == null && !editMode) {
            return;
        }

        RoundedRectRenderer.box(renderer, screenX, screenY, WIDTH, HEIGHT, 8f);

        float avatarY = screenY + (HEIGHT - AVATAR_SIZE) / 2f;
        int skinGlId = (target != null) ? skinGlId(target) : (client.player != null ? skinGlId(client.player) : 0);

        if (skinGlId > 0) {
            renderer.texturedQuad(skinGlId, screenX + 8f, avatarY, AVATAR_SIZE, AVATAR_SIZE,
                    8f / 64f, 8f / 64f, 16f / 64f, 16f / 64f, 0xFFFFFFFF);
        } else {
            renderer.roundedRect(screenX + 8f, avatarY, AVATAR_SIZE, AVATAR_SIZE, 4f, HudStyle.BG_SOFT);
            font().drawCentered(renderer, "★", screenX + 8f + AVATAR_SIZE / 2f,
                    avatarY + (AVATAR_SIZE - font().lineHeight(0.24f)) / 2f, 0.24f, HudStyle.ACCENT);
        }
        renderer.roundedOutline(screenX + 8f, avatarY, AVATAR_SIZE, AVATAR_SIZE, 4f, 1f, HudStyle.BORDER);

        String name = "Target";
        if (target != null) {
            name = target.getDisplayName() != null ? target.getDisplayName().getString() : target.getName().getString();
        }
        name = truncate(name, 12);

        float textX = screenX + 48f;
        float nameY = screenY + 8f;
        font().draw(renderer, name, textX, nameY, TEXT_SCALE, HudStyle.TEXT);

        float health = target != null ? target.getHealth() : 20f;
        float maxHealth = target != null ? target.getMaxHealth() : 20f;

        // Health Bar
        float barX = textX;
        float barY = screenY + 28f;
        float barW = WIDTH - 56f;
        float barH = 6f;
        float progress = Math.clamp(health / Math.max(1f, maxHealth), 0f, 1f);

        renderer.roundedRect(barX, barY, barW, barH, 3f, HudStyle.EMPTY);
        if (progress > 0.01f) {
            renderer.roundedGradient(barX, barY, barW * progress, barH, 3f,
                    HudStyle.ACCENT, HudStyle.ACCENT, HudStyle.FILL, HudStyle.FILL);
        }

        // Health text counter
        font().draw(renderer, String.format("%.1f HP", health), barX, screenY + 38f, 0.16f, HudStyle.TEXT_DIM);

        // Level
        int level = (target instanceof PlayerEntity player) ? player.experienceLevel : 0;
        if (level > 0 || editMode) {
            font().drawRight(renderer, "Lvl " + level, screenX + WIDTH - 10f, screenY + 38f, 0.16f, HudStyle.ACCENT);
        }
    }

    private int skinGlId(LivingEntity target) {
        try {
            GameProfile profile = null;
            if (target instanceof PlayerEntity player) {
                profile = player.getGameProfile();
            }
            if (profile == null) {
                return 0;
            }
            SkinTextures textures = client.getSkinProvider().getSkinTextures(profile);
            Identifier textureId = textures.texture();
            AbstractTexture texture = client.getTextureManager().getTexture(textureId);
            return texture.getGlId();
        } catch (Exception e) {
            return 0;
        }
    }

    private String truncate(String text, int maxChars) {
        return text.length() <= maxChars ? text : text.substring(0, maxChars) + "…";
    }
}
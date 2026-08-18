package dev.testvisuals.hud.components;

import com.mojang.authlib.GameProfile;

import dev.testvisuals.font.GlyphAtlas;
import dev.testvisuals.hud.Anchor;
import dev.testvisuals.hud.HudComponent;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class TargetHud extends HudComponent {

    private static final float WIDTH = 160f;
    private static final float HEIGHT = 46f;
    private static final float AVATAR_SIZE = 30f;

    private final MinecraftClient client = MinecraftClient.getInstance();

    public TargetHud() {
        super("target", "Target HUD");
        position.anchor = Anchor.BOTTOM_LEFT;
        position.offsetX = 8f;
        position.offsetY = 46f;
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

        // Dark card body & border
        renderer.roundedBordered(screenX, screenY, WIDTH, HEIGHT, 6f, 1f, HudStyle.BG, HudStyle.BORDER);

        // 1. Avatar (Left)
        float avatarY = screenY + (HEIGHT - AVATAR_SIZE) / 2f;
        int skinGlId = (target != null) ? skinGlId(target) : (client.player != null ? skinGlId(client.player) : 0);
        if (skinGlId > 0) {
            renderer.texturedQuad(skinGlId, screenX + 8f, avatarY, AVATAR_SIZE, AVATAR_SIZE,
                    8f / 64f, 8f / 64f, 16f / 64f, 16f / 64f, 0xFFFFFFFF);
        } else {
            renderer.roundedRect(screenX + 8f, avatarY, AVATAR_SIZE, AVATAR_SIZE, 4f, HudStyle.BG_SOFT);
            font().drawCentered(renderer, String.valueOf(GlyphAtlas.ICON_USER),
                    screenX + 8f + AVATAR_SIZE / 2f, avatarY + 8f, 0.22f, HudStyle.TEXT_DIM);
        }
        renderer.roundedOutline(screenX + 8f, avatarY, AVATAR_SIZE, AVATAR_SIZE, 4f, 1f, HudStyle.BORDER);

        // 2. Middle Text & Armor
        float textX = screenX + 44f;
        font().draw(renderer, "wexside", textX, screenY + 6f, 0.16f, HudStyle.TEXT_DIM);

        String name = target != null
                ? (target.getDisplayName() != null ? target.getDisplayName().getString() : target.getName().getString())
                : (client.player != null ? client.player.getName().getString() : "Player");
        name = truncate(name, 10);
        font().draw(renderer, name, textX, screenY + 16f, 0.22f, HudStyle.TEXT);

        // Equipment icon row
        String armorIcons = String.valueOf(GlyphAtlas.ICON_HELMET) + ' '
                + GlyphAtlas.ICON_CHEST + ' '
                + GlyphAtlas.ICON_LEGS + ' '
                + GlyphAtlas.ICON_BOOTS + ' '
                + GlyphAtlas.ICON_TOTEM;
        font().draw(renderer, armorIcons, textX, screenY + 30f, 0.18f, 0xFFEF4444);

        // 3. Right HP Circle
        float health = target != null ? target.getHealth() : 20f;
        float maxHealth = target != null ? target.getMaxHealth() : 20f;
        int hpInt = (int) Math.ceil(health);

        float cx = screenX + WIDTH - 20f;
        float cy = screenY + HEIGHT / 2f;
        float radius = 13f;

        // Dark filled circle + outline ring
        renderer.circle(cx, cy, radius, 0xEE1A1B20);
        renderer.circleOutline(cx, cy, radius, 1.5f, HudStyle.BORDER);

        // Ring progress arc
        float progress = Math.clamp(health / Math.max(1f, maxHealth), 0f, 1f);
        if (progress > 0.01f) {
            renderer.arc(cx, cy, radius, -(float) (Math.PI / 2.0),
                    -(float) (Math.PI / 2.0) + (float) (Math.PI * 2.0 * progress), 1.5f, 0xFFFFFFFF);
        }

        // HP number in center
        String hpStr = String.valueOf(hpInt);
        float hpScale = 0.22f;
        font().drawCentered(renderer, hpStr, cx, cy - font().lineHeight(hpScale) / 2f + 1f, hpScale, HudStyle.TEXT);
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
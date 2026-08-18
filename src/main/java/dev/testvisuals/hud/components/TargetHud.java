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

    private static final float WIDTH = 172f;
    private static final float HEIGHT = 52f;
    private static final float AVATAR_SIZE = 32f;
    private static final float TEXT_SCALE = 0.19f;
    private static final float HEART_SCALE = 0.55f;

    private final MinecraftClient client = MinecraftClient.getInstance();

    public TargetHud() {
        super("target", "Target");
        position.anchor = dev.testvisuals.hud.Anchor.BOTTOM_LEFT;
        position.offsetX = 12f;
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
        if (target == null) {
            return;
        }
        RoundedRectRenderer.box(renderer, screenX, screenY, WIDTH, HEIGHT, 6f);

        float avatarY = screenY + (HEIGHT - AVATAR_SIZE) / 2f;
        int skinGlId = skinGlId(target);
        renderer.setSaturate(0f);
        renderer.texturedQuad(skinGlId, screenX + 10f, avatarY, AVATAR_SIZE, AVATAR_SIZE,
                8f / 64f, 8f / 64f, 16f / 64f, 16f / 64f, 0xFFFFFFFF);
        renderer.setSaturate(1f);
        renderer.roundedOutline(screenX + 10f, avatarY, AVATAR_SIZE, AVATAR_SIZE, 4f, 1f, HudStyle.BORDER);

        String name = truncate(target.getDisplayName() != null
                ? target.getDisplayName().getString() : target.getName().getString(), 13);

        float textX = screenX + 52f;
        float nameY = screenY + 8f;
        font().draw(renderer, name, textX, nameY, TEXT_SCALE, HudStyle.TEXT);

        float heartsY = screenY + 34f;
        drawHearts(renderer, target, textX, heartsY);

        drawLevelCircle(renderer, target);
    }

    private void drawHearts(Renderer2D renderer, LivingEntity target, float x, float y) {
        float maxHealth = target.getMaxHealth();
        float health = target.getHealth();
        int total = (int) Math.ceil(maxHealth / 2f);
        total = Math.max(total, 10);
        int full = (int) (health / 2f);
        boolean half = health % 2f >= 0.99f && full < total;

        float heartW = font().measure("♥", HEART_SCALE);
        for (int i = 0; i < total; i++) {
            int color = i < full ? HudStyle.FILL : HudStyle.EMPTY;
            if (i == full && half) {
                float uMid = (font().atlas().glyph('♥').u0() + font().atlas().glyph('♥').u1()) / 2f;
                renderer.texturedQuad(font().atlas().textureId(), x + i * (heartW + 2f), y, heartW / 2f,
                        font().lineHeight(HEART_SCALE), font().atlas().glyph('♥').u0(),
                        font().atlas().glyph('♥').v0(), uMid, font().atlas().glyph('♥').v1(), HudStyle.FILL);
            } else {
                font().draw(renderer, "♥", x + i * (heartW + 2f), y, HEART_SCALE, color);
            }
        }
        font().draw(renderer, formatHealth(health), x + total * (heartW + 2f) + 8f, y,
                HEART_SCALE * 0.7f, HudStyle.TEXT_DIM);
    }

    private void drawLevelCircle(Renderer2D renderer, LivingEntity target) {
        int level = 0;
        if (target instanceof PlayerEntity player) {
            level = player.experienceLevel;
        }
        float cx = screenX + WIDTH - 28f;
        float cy = screenY + HEIGHT / 2f;
        renderer.ring(cx, cy, 8f, 10f, HudStyle.BORDER);
        String text = String.valueOf(level);
        font().drawCentered(renderer, text, cx, cy - font().lineHeight(0.32f) / 2f, 0.32f,
                ColorUtils.withAlpha(HudStyle.TEXT, 0.9f));
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

    private String formatHealth(float health) {
        return String.format("%.1f", health);
    }

    private String truncate(String text, int maxChars) {
        return text.length() <= maxChars ? text : text.substring(0, maxChars) + "…";
    }
}
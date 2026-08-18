package dev.testvisuals.hud.components;

import dev.testvisuals.hud.HudComponent;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.hud.RoundedRectRenderer;
import dev.testvisuals.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

public final class TopInfoHud extends HudComponent {

    private static final float HEIGHT = 24f;
    private static final float PADDING = 12f;

    private final MinecraftClient client = MinecraftClient.getInstance();

    public TopInfoHud() {
        super("topinfo", "Top Info");
        position.anchor = dev.testvisuals.hud.Anchor.TOP_LEFT;
        position.offsetX = 10f;
        position.offsetY = 4f;
    }

    @Override
    public float getWidth() {
        return client.getWindow().getScaledWidth() - 20f;
    }

    @Override
    public float getHeight() {
        return HEIGHT;
    }

    @Override
    protected void renderContent(Renderer2D renderer, float delta, boolean editMode) {
        float w = getWidth();
        RoundedRectRenderer.box(renderer, screenX, screenY, w, HEIGHT, 6f);

        float logoScale = 0.22f;
        float centerY = screenY + (HEIGHT - fontLine(logoScale)) / 2f;
        font().draw(renderer, "FlugerWexside", screenX + PADDING, centerY, logoScale, HudStyle.TEXT);

        float sepX = screenX + PADDING + font().measure("FlugerWexside", logoScale) + 10f;
        font().drawCentered(renderer, "•", sepX, centerY, logoScale * 0.8f, HudStyle.TEXT_DIM);

        String fps = "FPS " + client.getCurrentFps();
        String ping = "Ping " + formatPing();
        float textScale = 0.18f;
        float right = screenX + w - PADDING;
        font().drawRight(renderer, ping, right, centerY, textScale, HudStyle.TEXT_DIM);
        float pingW = font().measure(ping, textScale);
        font().drawRight(renderer, fps, right - pingW - 18f, centerY, textScale, HudStyle.TEXT);
    }

    private String formatPing() {
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null) {
            return "--";
        }
        PlayerListEntry entry = networkHandler.getPlayerListEntry(client.getSession().getUuidOrNull());
        if (entry == null) {
            return "--";
        }
        return entry.getLatency() + "ms";
    }

    private float fontLine(float scale) {
        return font().lineHeight(scale);
    }
}
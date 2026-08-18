package dev.testvisuals.hud.components;

import dev.testvisuals.font.GlyphAtlas;
import dev.testvisuals.hud.Anchor;
import dev.testvisuals.hud.HudComponent;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

public final class TopInfoHud extends HudComponent {

    private static final float HEIGHT = 20f;
    private static final float SCALE = 0.23f;
    private static final float PADDING_H = 8f;
    private static final float GAP = 12f;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private float width = 180f;

    public TopInfoHud() {
        super("top_info", "Top Info");
        position.anchor = Anchor.TOP_LEFT;
        position.offsetX = 8f;
        position.offsetY = 8f;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return HEIGHT;
    }

    @Override
    protected void renderContent(Renderer2D renderer, float delta, boolean editMode) {
        String clientName = GlyphAtlas.ICON_LOGO + " wexside";
        String userName = client.getSession() != null ? client.getSession().getUsername() : "Player";
        int fps = client.getCurrentFps();
        String fpsText = GlyphAtlas.ICON_FPS + " " + fps + "fps";

        int ping = 0;
        if (client.getNetworkHandler() != null && client.player != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) {
                ping = Math.max(0, entry.getLatency());
            }
        }
        String pingText = GlyphAtlas.ICON_PING + " " + ping + "ms";

        float wClient = font().measure(clientName, SCALE);
        float wUser = font().measure(userName, SCALE);
        float wFps = font().measure(fpsText, SCALE);
        float wPing = font().measure(pingText, SCALE);

        width = PADDING_H * 2f + wClient + GAP + wUser + GAP + wFps + GAP + wPing;

        // Dark rounded capsule
        renderer.roundedBordered(screenX, screenY, width, HEIGHT, 5f, 1f, HudStyle.BG, HudStyle.BORDER);

        float textY = screenY + (HEIGHT - font().lineHeight(SCALE)) / 2f + 1f;
        float curX = screenX + PADDING_H;

        // 1. Client Name
        font().draw(renderer, clientName, curX, textY, SCALE, HudStyle.TEXT);
        curX += wClient + GAP;

        // 2. User Name
        font().draw(renderer, userName, curX, textY, SCALE, HudStyle.TEXT_DIM);
        curX += wUser + GAP;

        // 3. FPS
        font().draw(renderer, fpsText, curX, textY, SCALE, HudStyle.TEXT);
        curX += wFps + GAP;

        // 4. Ping
        font().draw(renderer, pingText, curX, textY, SCALE, HudStyle.TEXT_DIM);
    }
}
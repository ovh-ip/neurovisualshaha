package dev.testvisuals.hud.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dev.testvisuals.font.GlyphAtlas;
import dev.testvisuals.hud.Anchor;
import dev.testvisuals.hud.HudComponent;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.render.Renderer2D;
import dev.testvisuals.util.AnimationUtils;

public final class NotificationHud extends HudComponent {

    private static final float HEIGHT = 20f;
    private static final float SCALE = 0.22f;
    private static final float MAX_LIFETIME = 4.0f;

    private static final class Notification {
        final String text;
        float time;
        float alpha;

        Notification(String text) {
            this.text = text;
        }
    }

    private final List<Notification> queue = new ArrayList<>();

    public NotificationHud() {
        super("notifications", "Notifications");
        position.anchor = Anchor.TOP_CENTER;
        position.offsetX = 0f;
        position.offsetY = 32f;
    }

    public void push(String text) {
        queue.add(new Notification(text));
    }

    @Override
    public float getWidth() {
        if (queue.isEmpty()) {
            return 140f;
        }
        return font().measure(GlyphAtlas.ICON_HEART + "  " + queue.get(0).text, SCALE) + 20f;
    }

    @Override
    public float getHeight() {
        return Math.max(HEIGHT, queue.size() * (HEIGHT + 4f));
    }

    @Override
    protected void renderContent(Renderer2D renderer, float delta, boolean editMode) {
        if (queue.isEmpty() && editMode) {
            queue.add(new Notification("Пример уведомления"));
        }

        Iterator<Notification> it = queue.iterator();
        float curY = screenY;

        while (it.hasNext()) {
            Notification n = it.next();
            n.time += delta;
            if (n.time > MAX_LIFETIME && !editMode) {
                it.remove();
                continue;
            }

            n.alpha = AnimationUtils.approach(n.alpha, 1.0f, delta, 8f);
            if (n.time > MAX_LIFETIME - 0.5f && !editMode) {
                n.alpha = Math.max(0f, (MAX_LIFETIME - n.time) * 2.0f);
            }

            String fullText = GlyphAtlas.ICON_HEART + "  " + n.text;
            float w = font().measure(fullText, SCALE) + 18f;
            float x = screenX - w / 2f;

            // Dark pill
            renderer.roundedBordered(x, curY, w, HEIGHT, 5f, 1f, HudStyle.BG, HudStyle.BORDER);

            // Red heart icon
            font().draw(renderer, String.valueOf(GlyphAtlas.ICON_HEART), x + 8f, curY + 4f, SCALE, 0xFFEF4444);

            // White text
            font().draw(renderer, n.text, x + 22f, curY + 4f, SCALE, HudStyle.TEXT);

            curY += HEIGHT + 4f;
        }
    }
}
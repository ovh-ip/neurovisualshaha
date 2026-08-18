package dev.testvisuals.hud.components;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import dev.testvisuals.hud.HudComponent;
import dev.testvisuals.hud.HudStyle;
import dev.testvisuals.hud.RoundedRectRenderer;
import dev.testvisuals.render.Renderer2D;
import dev.testvisuals.util.AnimationUtils;

public final class NotificationHud extends HudComponent {

    private static final long IN_MS = 200L;
    private static final long OUT_MS = 200L;
    private static final long HOLD_MS = 2000L;
    private static final float WIDTH = 210f;
    private static final float HEIGHT = 24f;
    private static final float GAP = 4f;
    private static final float TEXT_SCALE = 0.17f;
    private static final int MAX_ENTRIES = 4;

    private static final class Entry {
        final String text;
        final long start;

        Entry(String text, long start) {
            this.text = text;
            this.start = start;
        }
    }

    private final Deque<Entry> entries = new ArrayDeque<>();

    public NotificationHud() {
        super("notification", "Notifications");
        position.anchor = dev.testvisuals.hud.Anchor.BOTTOM_RIGHT;
        position.offsetX = -12f;
        position.offsetY = -44f;
    }

    public void push(String text) {
        entries.addLast(new Entry(text, System.nanoTime()));
        while (entries.size() > MAX_ENTRIES) {
            entries.removeFirst();
        }
    }

    @Override
    public float getWidth() {
        return WIDTH;
    }

    @Override
    public float getHeight() {
        return entries.isEmpty() ? 0f : entries.size() * (HEIGHT + GAP) - GAP;
    }

    @Override
    protected void renderContent(Renderer2D renderer, float delta, boolean editMode) {
        long now = System.nanoTime();
        List<Entry> visible = new ArrayList<>();
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            float age = (now - entry.start) / 1_000_000f;
            if (age > HOLD_MS + OUT_MS) {
                it.remove();
                continue;
            }
            visible.add(entry);
        }

        float y = screenY;
        for (Entry entry : visible) {
            float age = (now - entry.start) / 1_000_000f;
            float inP = AnimationUtils.clamp01(age / IN_MS);
            float outP = AnimationUtils.clamp01((HOLD_MS + OUT_MS - age) / OUT_MS);
            float alpha = Math.min(AnimationUtils.easeOutCubic(inP), AnimationUtils.easeOutCubic(outP));
            float slide = (1f - AnimationUtils.easeOutCubic(inP)) * -18f;

            float drawY = y + slide;
            RoundedRectRenderer.box(renderer, screenX, drawY, WIDTH, HEIGHT, 6f);
            renderer.roundedRect(screenX, drawY + 5f, 2f, HEIGHT - 10f, 1f, HudStyle.text(alpha));
            float textY = drawY + (HEIGHT - font().lineHeight(TEXT_SCALE)) / 2f;
            font().draw(renderer, entry.text, screenX + 12f, textY, TEXT_SCALE, HudStyle.text(alpha));
            y += HEIGHT + GAP;
        }
    }
}
package dev.testvisuals.menu;

import java.util.Random;

import dev.testvisuals.render.Renderer2D;
import dev.testvisuals.util.ColorUtils;

public final class ParticleSystem {

    private static final int COUNT = 80;
    private static final float CONNECT_DIST = 75f;

    private final Random random = new Random(1337L);
    private final float[] px = new float[COUNT];
    private final float[] py = new float[COUNT];
    private final float[] vx = new float[COUNT];
    private final float[] vy = new float[COUNT];
    private final float[] size = new float[COUNT];
    private final float[] phase = new float[COUNT];
    private final float[] speed = new float[COUNT];
    private final int[] colors = new int[COUNT];

    public ParticleSystem() {
        for (int i = 0; i < COUNT; i++) {
            px[i] = random.nextFloat();
            py[i] = random.nextFloat();
            vx[i] = (random.nextFloat() - 0.5f) * 0.025f;
            vy[i] = -(0.012f + random.nextFloat() * 0.035f);
            size[i] = 2.0f + random.nextFloat() * 3.5f;
            phase[i] = random.nextFloat() * (float) Math.PI * 2f;
            speed[i] = 0.8f + random.nextFloat() * 1.6f;
            colors[i] = pickPalette(random);
        }
    }

    private static int pickPalette(Random random) {
        int[] palette = {
                0xFF7FB8FF, // Azure
                0xFFC9A2FF, // Lavender / Violet
                0xFF56E5D0, // Mint cyan
                0xFFE0ECFF, // Soft white-blue
                0xFFFF8EBA  // Rose neon
        };
        int rgb = palette[random.nextInt(palette.length)];
        int alpha = 0x55 + random.nextInt(0x55);
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    public void update(float dt, float width, float height, float mouseX, float mouseY) {
        float mxNorm = mouseX / Math.max(1f, width);
        float myNorm = mouseY / Math.max(1f, height);

        for (int i = 0; i < COUNT; i++) {
            // Mouse repulsion force
            float dx = px[i] - mxNorm;
            float dy = py[i] - myNorm;
            float distSq = dx * dx + dy * dy;
            if (distSq < 0.025f && distSq > 0.00001f) {
                float force = (0.025f - distSq) * 0.4f * dt;
                float d = (float) Math.sqrt(distSq);
                px[i] += (dx / d) * force;
                py[i] += (dy / d) * force;
            }

            px[i] += vx[i] * dt * 2.5f;
            py[i] += vy[i] * dt * 2.5f;

            if (py[i] < -0.05f) {
                py[i] = 1.05f;
                px[i] = random.nextFloat();
            }
            if (px[i] < -0.05f) {
                px[i] = 1.05f;
            } else if (px[i] > 1.05f) {
                px[i] = -0.05f;
            }
        }
    }

    public void render(Renderer2D renderer, float time, float width, float height, int glowTexture) {
        // Draw constellation connection lines between close particles
        for (int i = 0; i < COUNT; i++) {
            float x1 = px[i] * width;
            float y1 = py[i] * height;

            for (int j = i + 1; j < COUNT; j++) {
                float x2 = px[j] * width;
                float y2 = py[j] * height;

                float dx = x2 - x1;
                float dy = y2 - y1;
                float d2 = dx * dx + dy * dy;

                if (d2 < CONNECT_DIST * CONNECT_DIST) {
                    float dist = (float) Math.sqrt(d2);
                    float alphaFactor = 1f - (dist / CONNECT_DIST);
                    alphaFactor *= alphaFactor; // quadratic falloff

                    int c1 = ColorUtils.withAlpha(colors[i], alphaFactor * 0.45f);
                    int c2 = ColorUtils.withAlpha(colors[j], alphaFactor * 0.45f);
                    renderer.gradientLine(x1, y1, x2, y2, 1.0f, c1, c2);
                }
            }
        }

        // Draw particle cores & glows
        for (int i = 0; i < COUNT; i++) {
            float twinkle = 0.7f + 0.3f * (float) Math.sin(time * speed[i] + phase[i]);
            float s = size[i] * twinkle;
            float cx = px[i] * width;
            float cy = py[i] * height;

            int col = colors[i];
            int glowAlpha = (int) (((col >>> 24) & 0xFF) * twinkle * 0.6f);
            int glowCol = (glowAlpha << 24) | (col & 0x00FFFFFF);

            renderer.texture(glowTexture, cx - s * 2.5f, cy - s * 2.5f, s * 5f, s * 5f, glowCol);
            renderer.circle(cx, cy, s * 0.75f, ColorUtils.withAlpha(col, twinkle));
        }
    }
}
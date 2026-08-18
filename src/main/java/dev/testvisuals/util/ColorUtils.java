package dev.testvisuals.util;

public final class ColorUtils {

    private ColorUtils() {
    }

    public static int rgba(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    public static int rgba(float r, float g, float b, float a) {
        return rgba((int) (r * 255f + 0.5f), (int) (g * 255f + 0.5f), (int) (b * 255f + 0.5f), (int) (a * 255f + 0.5f));
    }

    public static int getRed(int argb) {
        return (argb >>> 16) & 0xFF;
    }

    public static int getGreen(int argb) {
        return (argb >>> 8) & 0xFF;
    }

    public static int getBlue(int argb) {
        return argb & 0xFF;
    }

    public static int getAlpha(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    public static int withAlpha(int argb, int alpha) {
        return (Math.clamp(alpha, 0, 255) << 24) | (argb & 0x00FFFFFF);
    }

    public static int withAlpha(int argb, float alphaFactor) {
        int currentAlpha = (argb >>> 24) & 0xFF;
        int newAlpha = (int) (currentAlpha * Math.clamp(alphaFactor, 0f, 1f) + 0.5f);
        return (newAlpha << 24) | (argb & 0x00FFFFFF);
    }

    public static int lerp(int c1, int c2, float t) {
        float factor = Math.clamp(t, 0f, 1f);
        int a1 = (c1 >>> 24) & 0xFF, r1 = (c1 >>> 16) & 0xFF, g1 = (c1 >>> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF, r2 = (c2 >>> 16) & 0xFF, g2 = (c2 >>> 8) & 0xFF, b2 = c2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * factor + 0.5f);
        int r = (int) (r1 + (r2 - r1) * factor + 0.5f);
        int g = (int) (g1 + (g2 - g1) * factor + 0.5f);
        int b = (int) (b1 + (b2 - b1) * factor + 0.5f);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int hslToRgb(float h, float s, float l, float a) {
        h = h % 360f;
        if (h < 0f) h += 360f;
        s = Math.clamp(s, 0f, 1f);
        l = Math.clamp(l, 0f, 1f);

        float c = (1f - Math.abs(2f * l - 1f)) * s;
        float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m = l - c / 2f;

        float r = 0, g = 0, b = 0;
        if (h < 60f) {
            r = c; g = x;
        } else if (h < 120f) {
            r = x; g = c;
        } else if (h < 180f) {
            g = c; b = x;
        } else if (h < 240f) {
            g = x; b = c;
        } else if (h < 300f) {
            r = x; b = c;
        } else {
            r = c; b = x;
        }

        return rgba(r + m, g + m, b + m, a);
    }

    public static int rainbow(float speed, float offset, float saturation, float brightness) {
        float hue = (System.currentTimeMillis() * speed * 0.05f + offset) % 360f;
        return hslToRgb(hue, saturation, brightness, 1f);
    }

    public static int wave(int color1, int color2, float speed, float offset) {
        float time = (float) (Math.sin((System.currentTimeMillis() * 0.001f * speed) + offset) * 0.5 + 0.5);
        return lerp(color1, color2, time);
    }
}

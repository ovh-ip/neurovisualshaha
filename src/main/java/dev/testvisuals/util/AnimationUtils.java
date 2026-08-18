package dev.testvisuals.util;

public final class AnimationUtils {

    private AnimationUtils() {
    }

    public static float clamp01(float value) {
        return Math.clamp(value, 0f, 1f);
    }

    public static float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    public static float easeInQuad(float t) {
        t = clamp01(t);
        return t * t;
    }

    public static float easeOutQuad(float t) {
        t = clamp01(t);
        return 1f - (1f - t) * (1f - t);
    }

    public static float easeInOutQuad(float t) {
        t = clamp01(t);
        return t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2) / 2f;
    }

    public static float easeInCubic(float t) {
        t = clamp01(t);
        return t * t * t;
    }

    public static float easeOutCubic(float t) {
        t = clamp01(t);
        return 1f - (float) Math.pow(1f - t, 3);
    }

    public static float easeInOutCubic(float t) {
        t = clamp01(t);
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }

    public static float easeOutBack(float t) {
        t = clamp01(t);
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(t - 1f, 3) + c1 * (float) Math.pow(t - 1f, 2);
    }

    public static float easeOutElastic(float t) {
        t = clamp01(t);
        float c4 = (2f * (float) Math.PI) / 3f;
        return t == 0f ? 0f : (t == 1f ? 1f : (float) Math.pow(2, -10 * t) * (float) Math.sin((t * 10f - 0.75f) * c4) + 1f);
    }

    public static float approach(float current, float target, float delta, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.0001f) {
            return target;
        }
        return current + diff * Math.min(1f, delta * speed);
    }
}

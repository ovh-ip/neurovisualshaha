package dev.testvisuals.font;

import dev.testvisuals.render.Renderer2D;

public final class CustomFontRenderer {

    private static CustomFontRenderer instance;

    private final GlyphAtlas atlas;

    private CustomFontRenderer(GlyphAtlas atlas) {
        this.atlas = atlas;
    }

    public static CustomFontRenderer get() {
        if (instance == null) {
            instance = new CustomFontRenderer(GlyphAtlas.generate());
        }
        return instance;
    }

    public GlyphAtlas atlas() {
        return atlas;
    }

    public float lineHeight(float scale) {
        return atlas.lineHeight() * scale;
    }

    public float measure(String text, float scale) {
        float width = 0f;
        for (int i = 0; i < text.length(); i++) {
            width += glyphFor(text.charAt(i)).advance() * scale;
        }
        return width;
    }

    public void draw(Renderer2D renderer, String text, float x, float y, float scale, int color) {
        float penX = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                continue;
            }
            GlyphAtlas.Glyph glyph = glyphFor(c);
            renderer.texturedQuad(atlas.textureId(),
                    penX + glyph.offsetX() * scale, y + glyph.offsetY() * scale,
                    glyph.width() * scale, glyph.height() * scale,
                    glyph.u0(), glyph.v0(), glyph.u1(), glyph.v1(), color);
            penX += glyph.advance() * scale;
        }
    }

    public void drawCentered(Renderer2D renderer, String text, float centerX, float y, float scale, int color) {
        draw(renderer, text, centerX - measure(text, scale) / 2f, y, scale, color);
    }

    public void drawRight(Renderer2D renderer, String text, float rightX, float y, float scale, int color) {
        draw(renderer, text, rightX - measure(text, scale), y, scale, color);
    }

    public void drawGradient(Renderer2D renderer, String text, float x, float y, float scale,
                             int topColor, int bottomColor) {
        int n = Math.max(1, text.length());
        float penX = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                continue;
            }
            GlyphAtlas.Glyph glyph = glyphFor(c);
            int color = lerpColor(topColor, bottomColor, (float) i / (n - 1));
            renderer.texturedQuad(atlas.textureId(),
                    penX + glyph.offsetX() * scale, y + glyph.offsetY() * scale,
                    glyph.width() * scale, glyph.height() * scale,
                    glyph.u0(), glyph.v0(), glyph.u1(), glyph.v1(), color);
            penX += glyph.advance() * scale;
        }
    }

    public void drawWithShadow(Renderer2D renderer, String text, float x, float y, float scale,
                               int color, int shadowColor) {
        draw(renderer, text, x + 1.5f * scale, y + 1.5f * scale, scale, shadowColor);
        draw(renderer, text, x, y, scale, color);
    }

    public void drawGlow(Renderer2D renderer, String text, float x, float y, float scale,
                         int topColor, int bottomColor) {
        int glow = 0x30FFFFFF;
        float s = 2.5f;
        draw(renderer, text, x - s, y - s, scale, glow);
        draw(renderer, text, x + s, y - s, scale, glow);
        draw(renderer, text, x - s, y + s, scale, glow);
        draw(renderer, text, x + s, y + s, scale, glow);
        drawGradient(renderer, text, x, y, scale, topColor, bottomColor);
    }

    private GlyphAtlas.Glyph glyphFor(char c) {
        GlyphAtlas.Glyph glyph = atlas.glyph(c);
        return glyph != null ? glyph : atlas.glyph('?');
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a >>> 16) & 0xFF, ag = (a >>> 8) & 0xFF, ab = a & 0xFF, aa = (a >>> 24) & 0xFF;
        int br = (b >>> 16) & 0xFF, bg = (b >>> 8) & 0xFF, bb = b & 0xFF, ba = (b >>> 24) & 0xFF;
        return ((int) (aa + (ba - aa) * t + 0.5f) << 24)
                | ((int) (ar + (br - ar) * t + 0.5f) << 16)
                | ((int) (ag + (bg - ag) * t + 0.5f) << 8)
                | (int) (ab + (bb - ab) * t + 0.5f);
    }
}
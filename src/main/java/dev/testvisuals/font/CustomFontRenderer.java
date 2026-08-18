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
        if (text == null || text.isEmpty()) {
            return 0f;
        }
        float width = 0f;
        for (int i = 0; i < text.length(); i++) {
            width += glyphFor(text.charAt(i)).advance() * scale;
        }
        return width;
    }

    public void draw(Renderer2D renderer, String text, float x, float y, float scale, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        float penX = x;
        float yOffset = -GlyphAtlas.PADDING * scale;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                continue;
            }
            GlyphAtlas.Glyph glyph = glyphFor(c);
            renderer.texturedQuad(atlas.textureId(),
                    penX - GlyphAtlas.PADDING * scale, y + yOffset,
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

    public void drawWithShadow(Renderer2D renderer, String text, float x, float y, float scale,
                               int color, int shadowColor) {
        draw(renderer, text, x + 1.0f, y + 1.0f, scale, shadowColor);
        draw(renderer, text, x, y, scale, color);
    }

    public void drawGlow(Renderer2D renderer, String text, float x, float y, float scale,
                         int topColor, int bottomColor) {
        int glow = 0x22FFFFFF;
        draw(renderer, text, x - 1f, y, scale, glow);
        draw(renderer, text, x + 1f, y, scale, glow);
        draw(renderer, text, x, y - 1f, scale, glow);
        draw(renderer, text, x, y + 1f, scale, glow);
        draw(renderer, text, x, y, scale, topColor);
    }

    private GlyphAtlas.Glyph glyphFor(char c) {
        GlyphAtlas.Glyph glyph = atlas.glyph(c);
        return glyph != null ? glyph : atlas.glyph('?');
    }
}
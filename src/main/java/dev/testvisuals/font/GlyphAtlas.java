package dev.testvisuals.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public final class GlyphAtlas {

    public static final int ATLAS_SIZE = 2048;
    public static final int FONT_SIZE = 64;
    public static final int PADDING = 6;

    private final int textureId;
    private final Map<Character, Glyph> glyphs;
    private final float maxAscent;
    private final float maxDescent;

    public record Glyph(float u0, float v0, float u1, float v1,
                        float width, float height, float offsetX, float offsetY, float advance) {
    }

    private GlyphAtlas(int textureId, Map<Character, Glyph> glyphs, float maxAscent, float maxDescent) {
        this.textureId = textureId;
        this.glyphs = glyphs;
        this.maxAscent = maxAscent;
        this.maxDescent = maxDescent;
    }

    public static GlyphAtlas generate() {
        StringBuilder charset = new StringBuilder();
        for (int c = 0x20; c <= 0x7E; c++) {
            charset.append((char) c);
        }
        for (int c = 0x400; c <= 0x45F; c++) {
            charset.append((char) c);
        }
        charset.append("ёЁ«»—–…“”‘’\u00A0");
        String chars = charset.toString();

        Font font = pickFont();
        BufferedImage image = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setFont(font);
            g.setColor(Color.WHITE);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

            FontRenderContext frc = g.getFontRenderContext();
            FontMetrics fm = g.getFontMetrics(font);
            float maxAscent = fm.getMaxAscent();
            float maxDescent = fm.getMaxDescent();

            Map<Character, Glyph> glyphs = new HashMap<>();
            int x = PADDING;
            int y = PADDING;
            int rowHeight = 0;

            for (int i = 0; i < chars.length(); i++) {
                char c = chars.charAt(i);
                GlyphVector gv = font.createGlyphVector(frc, new char[] {c});
                Rectangle2D bounds = gv.getVisualBounds();
                int gw = (int) Math.ceil(bounds.getWidth()) + PADDING * 2;
                int gh = (int) Math.ceil(bounds.getHeight()) + PADDING * 2;
                if (x + gw > ATLAS_SIZE) {
                    x = PADDING;
                    y += rowHeight;
                    rowHeight = 0;
                }
                g.drawGlyphVector(gv, x + PADDING - (float) bounds.getX(), y + PADDING - (float) bounds.getY());
                glyphs.put(c, new Glyph(
                        x / (float) ATLAS_SIZE, y / (float) ATLAS_SIZE,
                        (x + gw) / (float) ATLAS_SIZE, (y + gh) / (float) ATLAS_SIZE,
                        gw, gh,
                        (float) bounds.getX(), (float) bounds.getY(),
                        fm.charWidth(c)));
                x += gw;
                rowHeight = Math.max(rowHeight, gh);
            }

            return new GlyphAtlas(upload(image), glyphs, maxAscent, maxDescent);
        } finally {
            g.dispose();
        }
    }

    private static Font pickFont() {
        String[] names = {"DejaVu Sans", "Liberation Sans", "Noto Sans", "Arial", "SansSerif"};
        for (String name : names) {
            Font font = new Font(name, Font.BOLD, FONT_SIZE);
            if (font.canDisplay('А') && font.canDisplay('я') && font.canDisplay('ё')) {
                return font;
            }
        }
        return new Font(Font.SANS_SERIF, Font.BOLD, FONT_SIZE);
    }

    private static int upload(BufferedImage image) {
        int[] pixels = image.getRGB(0, 0, ATLAS_SIZE, ATLAS_SIZE, null, 0, ATLAS_SIZE);
        ByteBuffer data = BufferUtils.createByteBuffer(ATLAS_SIZE * ATLAS_SIZE * 4);
        for (int pixel : pixels) {
            data.put((byte) ((pixel >>> 16) & 0xFF));
            data.put((byte) ((pixel >>> 8) & 0xFF));
            data.put((byte) (pixel & 0xFF));
            data.put((byte) ((pixel >>> 24) & 0xFF));
        }
        data.flip();
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, ATLAS_SIZE, ATLAS_SIZE, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
        return texture;
    }

    public int textureId() {
        return textureId;
    }

    public Glyph glyph(char c) {
        return glyphs.get(c);
    }

    public float maxAscent() {
        return maxAscent;
    }

    public float maxDescent() {
        return maxDescent;
    }

    public float lineHeight() {
        return maxAscent + maxDescent;
    }
}
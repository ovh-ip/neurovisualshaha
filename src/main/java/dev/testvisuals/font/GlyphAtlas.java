package dev.testvisuals.font;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import dev.testvisuals.gl.GLUtil;

public final class GlyphAtlas {

    public static final int ATLAS_SIZE = 2048;
    public static final int FONT_SIZE = 52;
    public static final int PADDING = 6;

    // Vector icons
    public static final char ICON_LOGO = '\uE000';
    public static final char ICON_USER = '\uE001';
    public static final char ICON_FPS = '\uE002';
    public static final char ICON_PING = '\uE003';
    public static final char ICON_KEYBOARD = '\uE004';
    public static final char ICON_SHIELD = '\uE005';
    public static final char ICON_SWORD = '\uE006';
    public static final char ICON_SPEED = '\uE007';
    public static final char ICON_HEART = '\uE008';
    public static final char ICON_BOOST = '\uE009';
    public static final char ICON_HELMET = '\uE00A';
    public static final char ICON_CHEST = '\uE00B';
    public static final char ICON_LEGS = '\uE00C';
    public static final char ICON_BOOTS = '\uE00D';
    public static final char ICON_TOTEM = '\uE00E';
    public static final char ICON_BELL = '\uE00F';
    public static final char ICON_GLOBE = '\uE010';
    public static final char ICON_COMPASS = '\uE011';
    public static final char ICON_GEAR = '\uE012';
    public static final char ICON_CROSS = '\uE013';
    public static final char ICON_DISCORD = '\uE014';
    public static final char ICON_TELEGRAM = '\uE015';
    public static final char ICON_YOUTUBE = '\uE016';
    public static final char ICON_VK = '\uE017';
    public static final char ICON_EYE = '\uE018';
    public static final char ICON_RENDER = '\uE019';
    public static final char ICON_SEARCH = '\uE01A';
    public static final char ICON_ARROW_RIGHT = '\uE01B';
    public static final char ICON_BIOHAZARD = '\uE01C';

    private final int textureId;
    private final Map<Character, Glyph> glyphs;
    private final float maxAscent;
    private final float maxDescent;

    public record Glyph(float u0, float v0, float u1, float v1,
                        float width, float height, float advance) {
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
        charset.append("ёЁ«»—–…“”‘’♥✦★✓✗•>\u00A0");
        String chars = charset.toString();

        Font font = pickFont();
        Font fallbackFont = pickFallbackFont(font);
        BufferedImage image = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setFont(font);
            g.setColor(Color.WHITE);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            FontMetrics fm = g.getFontMetrics(font);
            FontMetrics fmFallback = g.getFontMetrics(fallbackFont);
            float maxAscent = fm.getAscent();
            float maxDescent = fm.getDescent();
            int cellH = (int) (maxAscent + maxDescent) + PADDING * 2;

            Map<Character, Glyph> glyphs = new HashMap<>();
            int x = PADDING;
            int y = PADDING;

            // 1. Render standard typography characters
            for (int i = 0; i < chars.length(); i++) {
                char c = chars.charAt(i);
                Font activeFont = font.canDisplay(c) ? font : fallbackFont;
                FontMetrics activeFm = activeFont == font ? fm : fmFallback;
                g.setFont(activeFont);

                int adv = Math.max(1, activeFm.charWidth(c));
                int cellW = adv + PADDING * 2;

                if (x + cellW > ATLAS_SIZE - PADDING) {
                    x = PADDING;
                    y += cellH + PADDING;
                }

                g.drawString(String.valueOf(c), x + PADDING, y + PADDING + (int) maxAscent);

                glyphs.put(c, new Glyph(
                        x / (float) ATLAS_SIZE, y / (float) ATLAS_SIZE,
                        (x + cellW) / (float) ATLAS_SIZE, (y + cellH) / (float) ATLAS_SIZE,
                        cellW, cellH, adv));

                x += cellW + PADDING;
            }

            // 2. Render crisp vector icons
            char[] iconChars = {
                ICON_LOGO, ICON_USER, ICON_FPS, ICON_PING, ICON_KEYBOARD,
                ICON_SHIELD, ICON_SWORD, ICON_SPEED, ICON_HEART, ICON_BOOST,
                ICON_HELMET, ICON_CHEST, ICON_LEGS, ICON_BOOTS, ICON_TOTEM, ICON_BELL,
                ICON_GLOBE, ICON_COMPASS, ICON_GEAR, ICON_CROSS, ICON_DISCORD,
                ICON_TELEGRAM, ICON_YOUTUBE, ICON_VK, ICON_EYE, ICON_RENDER,
                ICON_SEARCH, ICON_ARROW_RIGHT, ICON_BIOHAZARD
            };

            int iconSize = (int) (maxAscent * 1.05f);
            int iconCellW = iconSize + PADDING * 2;

            for (char icon : iconChars) {
                if (x + iconCellW > ATLAS_SIZE - PADDING) {
                    x = PADDING;
                    y += cellH + PADDING;
                }

                drawVectorIcon(g, icon, x + PADDING, y + PADDING, iconSize, (int) maxAscent);

                glyphs.put(icon, new Glyph(
                        x / (float) ATLAS_SIZE, y / (float) ATLAS_SIZE,
                        (x + iconCellW) / (float) ATLAS_SIZE, (y + cellH) / (float) ATLAS_SIZE,
                        iconCellW, cellH, iconSize + 4));

                x += iconCellW + PADDING;
            }

            return new GlyphAtlas(upload(image), glyphs, maxAscent, maxDescent);
        } finally {
            g.dispose();
        }
    }

    private static void drawVectorIcon(Graphics2D g, char icon, int x, int y, int w, int h) {
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        float pad = w * 0.08f;
        float ix = x + pad;
        float iy = y + pad;
        float iw = w - pad * 2f;
        float ih = h - pad * 2f;

        switch (icon) {
            case ICON_BIOHAZARD, ICON_LOGO -> {
                // Precision Crown / Biohazard emblem
                float cx = ix + iw * 0.5f;
                float cy = iy + ih * 0.5f;
                Path2D.Float crown = new Path2D.Float();
                crown.moveTo(ix + iw * 0.12f, iy + ih * 0.78f);
                crown.lineTo(ix + iw * 0.88f, iy + ih * 0.78f);
                crown.lineTo(ix + iw * 0.95f, iy + ih * 0.28f);
                crown.lineTo(ix + iw * 0.68f, iy + ih * 0.52f);
                crown.lineTo(cx, iy + ih * 0.16f);
                crown.lineTo(ix + iw * 0.32f, iy + ih * 0.52f);
                crown.lineTo(ix + iw * 0.05f, iy + ih * 0.28f);
                crown.closePath();
                g.fill(crown);
                g.setColor(new Color(20, 20, 20));
                g.fill(new Ellipse2D.Float(cx - 3f, iy + ih * 0.62f, 6f, 6f));
                g.fill(new Ellipse2D.Float(ix + iw * 0.28f, iy + ih * 0.62f, 5f, 5f));
                g.fill(new Ellipse2D.Float(ix + iw * 0.64f, iy + ih * 0.62f, 5f, 5f));
                g.setColor(Color.WHITE);
            }
            case ICON_SWORD -> {
                // Sharp Katana / Sword
                Path2D.Float blade = new Path2D.Float();
                blade.moveTo(ix + iw * 0.85f, iy + ih * 0.05f);
                blade.lineTo(ix + iw * 0.95f, iy + ih * 0.15f);
                blade.lineTo(ix + iw * 0.42f, iy + ih * 0.68f);
                blade.lineTo(ix + iw * 0.32f, iy + ih * 0.58f);
                blade.closePath();
                g.fill(blade);

                // Guard
                g.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) (ix + iw * 0.26f), (int) (iy + ih * 0.74f), (int) (ix + iw * 0.48f), (int) (iy + ih * 0.52f));

                // Handle & Pommel
                g.drawLine((int) (ix + iw * 0.35f), (int) (iy + ih * 0.65f), (int) (ix + iw * 0.12f), (int) (iy + ih * 0.88f));
                g.fill(new Ellipse2D.Float(ix + iw * 0.08f, iy + ih * 0.84f, iw * 0.12f, ih * 0.12f));
            }
            case ICON_RENDER -> {
                // Modern 3D Folded Paperplane
                Path2D.Float plane = new Path2D.Float();
                plane.moveTo(ix + iw * 0.95f, iy + ih * 0.08f);
                plane.lineTo(ix + iw * 0.05f, iy + ih * 0.48f);
                plane.lineTo(ix + iw * 0.42f, iy + ih * 0.62f);
                plane.lineTo(ix + iw * 0.55f, iy + ih * 0.95f);
                plane.lineTo(ix + iw * 0.68f, iy + ih * 0.70f);
                plane.closePath();
                g.fill(plane);
                g.setColor(new Color(30, 30, 35));
                g.drawLine((int) (ix + iw * 0.42f), (int) (iy + ih * 0.62f), (int) (ix + iw * 0.95f), (int) (iy + ih * 0.08f));
                g.setColor(Color.WHITE);
            }
            case ICON_FPS -> {
                // Modern Display Monitor
                g.draw(new RoundRectangle2D.Float(ix, iy + ih * 0.10f, iw, ih * 0.62f, 6, 6));
                g.drawLine((int) (ix + iw * 0.5f), (int) (iy + ih * 0.72f), (int) (ix + iw * 0.5f), (int) (iy + ih * 0.90f));
                g.drawLine((int) (ix + iw * 0.28f), (int) (iy + ih * 0.90f), (int) (ix + iw * 0.72f), (int) (iy + ih * 0.90f));
            }
            case ICON_EYE -> {
                // Crisp Vision ESP Eye
                Path2D.Float eye = new Path2D.Float();
                eye.moveTo(ix, iy + ih * 0.5f);
                eye.curveTo(ix + iw * 0.25f, iy + ih * 0.15f, ix + iw * 0.75f, iy + ih * 0.15f, ix + iw, iy + ih * 0.5f);
                eye.curveTo(ix + iw * 0.75f, iy + ih * 0.85f, ix + iw * 0.25f, iy + ih * 0.85f, ix, iy + ih * 0.5f);
                eye.closePath();
                g.draw(eye);
                g.fill(new Ellipse2D.Float(ix + iw * 0.38f, iy + ih * 0.38f, iw * 0.24f, ih * 0.24f));
            }
            case ICON_USER -> {
                // Sleek Silhouette Avatar
                g.fill(new Ellipse2D.Float(ix + iw * 0.28f, iy + ih * 0.06f, iw * 0.44f, ih * 0.44f));
                Path2D.Float body = new Path2D.Float();
                body.moveTo(ix + iw * 0.08f, iy + ih * 0.94f);
                body.curveTo(ix + iw * 0.12f, iy + ih * 0.58f, ix + iw * 0.88f, iy + ih * 0.58f, ix + iw * 0.92f, iy + ih * 0.94f);
                body.closePath();
                g.fill(body);
            }
            case ICON_SPEED -> {
                // Lightning Speed Bolt
                Path2D.Float bolt = new Path2D.Float();
                bolt.moveTo(ix + iw * 0.62f, iy);
                bolt.lineTo(ix + iw * 0.15f, iy + ih * 0.52f);
                bolt.lineTo(ix + iw * 0.52f, iy + ih * 0.52f);
                bolt.lineTo(ix + iw * 0.38f, iy + ih);
                bolt.lineTo(ix + iw * 0.88f, iy + ih * 0.44f);
                bolt.lineTo(ix + iw * 0.52f, iy + ih * 0.44f);
                bolt.closePath();
                g.fill(bolt);
            }
            case ICON_GEAR -> {
                // Precision 6-tooth Cogwheel
                float cx = ix + iw * 0.5f;
                float cy = iy + ih * 0.5f;
                float rOut = iw * 0.44f;
                float rIn = iw * 0.32f;
                Path2D.Float gear = new Path2D.Float();
                int teeth = 6;
                for (int i = 0; i < teeth; i++) {
                    double a0 = (i * 2 - 0.5) * Math.PI / teeth;
                    double a1 = (i * 2 + 0.5) * Math.PI / teeth;
                    double a2 = (i * 2 + 0.8) * Math.PI / teeth;
                    double a3 = (i * 2 + 1.2) * Math.PI / teeth;
                    float x0 = cx + (float) Math.cos(a0) * rIn;
                    float y0 = cy + (float) Math.sin(a0) * rIn;
                    float x1 = cx + (float) Math.cos(a1) * rIn;
                    float y1 = cy + (float) Math.sin(a1) * rIn;
                    float x2 = cx + (float) Math.cos(a2) * rOut;
                    float y2 = cy + (float) Math.sin(a2) * rOut;
                    float x3 = cx + (float) Math.cos(a3) * rOut;
                    float y3 = cy + (float) Math.sin(a3) * rOut;
                    if (i == 0) gear.moveTo(x0, y0);
                    else gear.lineTo(x0, y0);
                    gear.lineTo(x1, y1);
                    gear.lineTo(x2, y2);
                    gear.lineTo(x3, y3);
                }
                gear.closePath();
                g.fill(gear);
                g.setColor(new Color(20, 20, 20));
                g.fill(new Ellipse2D.Float(cx - iw * 0.16f, cy - ih * 0.16f, iw * 0.32f, ih * 0.32f));
                g.setColor(Color.WHITE);
            }
            case ICON_KEYBOARD -> {
                // Clean Keycap / Keyboard
                g.fill(new RoundRectangle2D.Float(ix, iy + ih * 0.12f, iw, ih * 0.76f, 6, 6));
                g.setColor(new Color(25, 25, 25));
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) (ih * 0.46f)));
                g.drawString("K", ix + iw * 0.30f, iy + ih * 0.65f);
                g.setColor(Color.WHITE);
            }
            case ICON_BOOST -> {
                // Artist Palette (Themes)
                Path2D.Float palette = new Path2D.Float();
                palette.moveTo(ix + iw * 0.5f, iy + ih * 0.05f);
                palette.curveTo(ix + iw * 0.95f, iy + ih * 0.05f, ix + iw * 0.95f, iy + ih * 0.90f, ix + iw * 0.65f, iy + ih * 0.90f);
                palette.curveTo(ix + iw * 0.50f, iy + ih * 0.90f, ix + iw * 0.45f, iy + ih * 0.70f, ix + iw * 0.30f, iy + ih * 0.70f);
                palette.curveTo(ix + iw * 0.15f, iy + ih * 0.70f, ix + iw * 0.05f, iy + ih * 0.85f, ix + iw * 0.05f, iy + ih * 0.50f);
                palette.curveTo(ix + iw * 0.05f, iy + ih * 0.15f, ix + iw * 0.25f, iy + ih * 0.05f, ix + iw * 0.50f, iy + ih * 0.05f);
                palette.closePath();
                g.fill(palette);
                g.setColor(new Color(25, 25, 25));
                g.fill(new Ellipse2D.Float(ix + iw * 0.32f, iy + ih * 0.22f, 5f, 5f));
                g.fill(new Ellipse2D.Float(ix + iw * 0.58f, iy + ih * 0.22f, 5f, 5f));
                g.fill(new Ellipse2D.Float(ix + iw * 0.72f, iy + ih * 0.44f, 5f, 5f));
                g.setColor(Color.WHITE);
            }
            case ICON_GLOBE, ICON_COMPASS -> {
                // Minimalist Globe with Latitude & Meridian
                g.draw(new Ellipse2D.Float(ix, iy, iw, ih));
                g.draw(new java.awt.geom.Line2D.Float(ix, iy + ih * 0.5f, ix + iw, iy + ih * 0.5f));
                g.draw(new Ellipse2D.Float(ix + iw * 0.25f, iy, iw * 0.50f, ih));
            }
            case ICON_CROSS -> {
                g.setStroke(new BasicStroke(3.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) (ix + iw * 0.18f), (int) (iy + ih * 0.18f), (int) (ix + iw * 0.82f), (int) (iy + ih * 0.82f));
                g.drawLine((int) (ix + iw * 0.82f), (int) (iy + ih * 0.18f), (int) (ix + iw * 0.18f), (int) (iy + ih * 0.82f));
            }
            case ICON_DISCORD -> {
                Path2D.Float p = new Path2D.Float();
                p.moveTo(ix + iw * 0.15f, iy + ih * 0.20f);
                p.lineTo(ix + iw * 0.85f, iy + ih * 0.20f);
                p.lineTo(ix + iw * 0.82f, iy + ih * 0.78f);
                p.lineTo(ix + iw * 0.65f, iy + ih * 0.72f);
                p.lineTo(ix + iw * 0.50f, iy + ih * 0.88f);
                p.lineTo(ix + iw * 0.35f, iy + ih * 0.72f);
                p.lineTo(ix + iw * 0.18f, iy + ih * 0.78f);
                p.closePath();
                g.fill(p);
                g.setColor(new Color(25, 25, 25));
                g.fill(new Ellipse2D.Float(ix + iw * 0.32f, iy + ih * 0.42f, iw * 0.14f, ih * 0.14f));
                g.fill(new Ellipse2D.Float(ix + iw * 0.54f, iy + ih * 0.42f, iw * 0.14f, ih * 0.14f));
                g.setColor(Color.WHITE);
            }
            case ICON_TELEGRAM -> {
                Path2D.Float p = new Path2D.Float();
                p.moveTo(ix + iw * 0.95f, iy + ih * 0.10f);
                p.lineTo(ix + iw * 0.10f, iy + ih * 0.50f);
                p.lineTo(ix + iw * 0.40f, iy + ih * 0.64f);
                p.lineTo(ix + iw * 0.55f, iy + ih * 0.95f);
                p.lineTo(ix + iw * 0.65f, iy + ih * 0.70f);
                p.lineTo(ix + iw * 0.95f, iy + ih * 0.10f);
                p.closePath();
                g.fill(p);
            }
            case ICON_YOUTUBE -> {
                g.fill(new RoundRectangle2D.Float(ix, iy + ih * 0.16f, iw, ih * 0.68f, 10, 10));
                g.setColor(new Color(25, 25, 25));
                Path2D.Float p = new Path2D.Float();
                p.moveTo(ix + iw * 0.38f, iy + ih * 0.32f);
                p.lineTo(ix + iw * 0.70f, iy + ih * 0.50f);
                p.lineTo(ix + iw * 0.38f, iy + ih * 0.68f);
                p.closePath();
                g.fill(p);
                g.setColor(Color.WHITE);
            }
            case ICON_VK -> {
                g.fill(new RoundRectangle2D.Float(ix, iy + ih * 0.12f, iw, ih * 0.76f, 8, 8));
                g.setColor(new Color(25, 25, 25));
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) (ih * 0.42f)));
                g.drawString("VK", ix + iw * 0.18f, iy + ih * 0.64f);
                g.setColor(Color.WHITE);
            }
            case ICON_PING -> {
                g.fill(new RoundRectangle2D.Float(ix + iw * 0.08f, iy + ih * 0.68f, iw * 0.18f, ih * 0.32f, 3, 3));
                g.fill(new RoundRectangle2D.Float(ix + iw * 0.41f, iy + ih * 0.38f, iw * 0.18f, ih * 0.62f, 3, 3));
                g.fill(new RoundRectangle2D.Float(ix + iw * 0.74f, iy + ih * 0.08f, iw * 0.18f, ih * 0.92f, 3, 3));
            }
            case ICON_HEART -> {
                Path2D.Float p = new Path2D.Float();
                p.moveTo(ix + iw * 0.5f, iy + ih * 0.25f);
                p.curveTo(ix + iw * 0.5f, iy, ix, iy, ix, iy + ih * 0.35f);
                p.curveTo(ix, iy + ih * 0.65f, ix + iw * 0.5f, iy + ih, ix + iw * 0.5f, iy + ih);
                p.curveTo(ix + iw * 0.5f, iy + ih, ix + iw, iy + ih * 0.65f, ix + iw, iy + ih * 0.35f);
                p.curveTo(ix + iw, iy, ix + iw * 0.5f, iy, ix + iw * 0.5f, iy + ih * 0.25f);
                p.closePath();
                g.fill(p);
            }
            case ICON_HELMET -> {
                g.fill(new RoundRectangle2D.Float(ix + iw * 0.10f, iy + ih * 0.12f, iw * 0.80f, ih * 0.72f, 8, 8));
                g.setColor(new Color(20, 20, 20));
                g.fill(new Rectangle2D.Float(ix + iw * 0.25f, iy + ih * 0.42f, iw * 0.50f, ih * 0.25f));
                g.setColor(Color.WHITE);
            }
            case ICON_CHEST -> {
                g.fill(new RoundRectangle2D.Float(ix + iw * 0.12f, iy + ih * 0.12f, iw * 0.76f, ih * 0.76f, 6, 6));
                g.fill(new RoundRectangle2D.Float(ix, iy + ih * 0.18f, iw, ih * 0.32f, 4, 4));
            }
            case ICON_LEGS -> {
                g.fill(new Rectangle2D.Float(ix + iw * 0.12f, iy + ih * 0.10f, iw * 0.76f, ih * 0.35f));
                g.fill(new Rectangle2D.Float(ix + iw * 0.12f, iy + ih * 0.45f, iw * 0.32f, ih * 0.48f));
                g.fill(new Rectangle2D.Float(ix + iw * 0.56f, iy + ih * 0.45f, iw * 0.32f, ih * 0.48f));
            }
            case ICON_BOOTS -> {
                g.fill(new RoundRectangle2D.Float(ix + iw * 0.08f, iy + ih * 0.28f, iw * 0.38f, ih * 0.62f, 4, 4));
                g.fill(new RoundRectangle2D.Float(ix + iw * 0.54f, iy + ih * 0.28f, iw * 0.38f, ih * 0.62f, 4, 4));
            }
            case ICON_TOTEM -> {
                g.fill(new Ellipse2D.Float(ix + iw * 0.30f, iy + ih * 0.05f, iw * 0.40f, ih * 0.35f));
                g.fill(new RoundRectangle2D.Float(ix + iw * 0.10f, iy + ih * 0.35f, iw * 0.80f, ih * 0.55f, 4, 4));
            }
            default -> {
                g.fill(new Rectangle2D.Float(ix, iy, iw, ih));
            }
        }
    }

    private static Font pickFont() {
        String[] preferred = {
                "Inter",
                "Segoe UI",
                "SF Pro Display",
                "Roboto",
                "Montserrat",
                "DejaVu Sans",
                "Liberation Sans",
                "Arial",
                "SansSerif"
        };
        for (String name : preferred) {
            Font font = new Font(name, Font.PLAIN, FONT_SIZE);
            if (font.canDisplay('A') && font.canDisplay('a')) {
                return font;
            }
        }
        return new Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE);
    }

    private static Font pickFallbackFont(Font primary) {
        return pickFirst(new String[] {"Segoe UI", "DejaVu Sans", "Noto Sans", "SansSerif"});
    }

    private static Font pickFirst(String[] names) {
        for (String name : names) {
            Font font = new Font(name, Font.PLAIN, FONT_SIZE);
            if (font.canDisplay('А') && font.canDisplay('я') && font.canDisplay('ё')) {
                return font;
            }
        }
        return new Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE);
    }

    private static int upload(BufferedImage image) {
        int[] pixels = image.getRGB(0, 0, ATLAS_SIZE, ATLAS_SIZE, null, 0, ATLAS_SIZE);
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(ATLAS_SIZE * ATLAS_SIZE * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();

        int total = ATLAS_SIZE * ATLAS_SIZE;
        int[] formatted = new int[total];
        for (int i = 0; i < total; i++) {
            int p = pixels[i];
            int a = (p >>> 24) & 0xFF;
            int r = (p >>> 16) & 0xFF;
            int g = (p >>> 8) & 0xFF;
            int b = p & 0xFF;
            formatted[i] = (a << 24) | (b << 16) | (g << 8) | r;
        }
        intBuffer.put(formatted);
        byteBuffer.position(0);

        int texture = GL11.glGenTextures();
        GLUtil.bindTexture(texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, ATLAS_SIZE, ATLAS_SIZE, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);
        GLUtil.bindTexture(0);
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
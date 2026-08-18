package dev.testvisuals.render;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import dev.testvisuals.gl.GLUtil;
import dev.testvisuals.gl.MatrixStack2D;
import dev.testvisuals.gl.ShaderProgram;
import dev.testvisuals.util.ColorUtils;

public final class Renderer2D {

    private static final int MODE_NONE = -1;
    private static final int MODE_FLAT = 0;
    private static final int MODE_TEXTURED = 1;
    private static final int FLOATS_PER_VERTEX = 8;
    private static final int INITIAL_CAPACITY = 1 << 18;

    private FloatBuffer buffer;
    private int vertexCount;
    private int mode = MODE_NONE;
    private int boundTexture = -1;
    private float saturate = 1.0f;
    private int vao;
    private int vbo;

    private final ShaderProgram colorShader;
    private final ShaderProgram textureShader;

    private final Matrix4f orthoMatrix = new Matrix4f();
    private final Matrix4f combinedMatrix = new Matrix4f();
    private final MatrixStack2D matrixStack = new MatrixStack2D();
    private final Vector4f tempPos = new Vector4f();

    public Renderer2D() {
        GLUtil.ensureCapabilities();
        buffer = BufferUtils.createFloatBuffer(INITIAL_CAPACITY);
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();

        GLUtil.bindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) buffer.capacity() * 4L, GL15.GL_DYNAMIC_DRAW);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, FLOATS_PER_VERTEX * 4, 0L);

        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, FLOATS_PER_VERTEX * 4, 8L);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, FLOATS_PER_VERTEX * 4, 16L);

        colorShader = ShaderProgram.load("/assets/testvisuals/shaders/quad.vert",
                "/assets/testvisuals/shaders/quad_flat.frag", new String[]{"aPos", "aUV", "aColor"});
        textureShader = ShaderProgram.load("/assets/testvisuals/shaders/quad.vert",
                "/assets/testvisuals/shaders/quad_tex.frag", new String[]{"aPos", "aUV", "aColor"});
    }

    public void begin(float width, float height) {
        flush();
        orthoMatrix.setOrtho(0f, width, height, 0f, 1000f, -1000f);
        matrixStack.reset();

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setSaturate(float s) {
        if (Math.abs(this.saturate - s) > 0.001f) {
            flush();
            this.saturate = s;
        }
    }

    public void pushMatrix() {
        matrixStack.push();
    }

    public void popMatrix() {
        matrixStack.pop();
    }

    public void translate(float x, float y) {
        matrixStack.translate(x, y);
    }

    public void scale(float sx, float sy) {
        matrixStack.scale(sx, sy);
    }

    public void scale(float sx, float sy, float cx, float cy) {
        matrixStack.scale(sx, sy, cx, cy);
    }

    public void rotate(float radians, float cx, float cy) {
        matrixStack.rotate(radians, cx, cy);
    }

    public void pushScissor(float x, float y, float width, float height) {
        flush();
        GLUtil.pushScissor(x, y, width, height);
    }

    public void popScissor() {
        flush();
        GLUtil.popScissor();
    }

    // ==================== Quads & Gradients ====================

    public void quad(float x, float y, float w, float h, int color) {
        setMode(MODE_FLAT);
        ensureCapacity(6);
        vertex(x, y, color);
        vertex(x + w, y, color);
        vertex(x + w, y + h, color);
        vertex(x, y, color);
        vertex(x + w, y + h, color);
        vertex(x, y + h, color);
    }

    public void gradientQuad(float x, float y, float w, float h, int top, int bottom) {
        gradientQuad4(x, y, w, h, top, top, bottom, bottom);
    }

    public void gradientQuadH(float x, float y, float w, float h, int left, int right) {
        gradientQuad4(x, y, w, h, left, right, right, left);
    }

    public void gradientQuad4(float x, float y, float w, float h, int cTL, int cTR, int cBR, int cBL) {
        setMode(MODE_FLAT);
        ensureCapacity(6);
        vertex(x, y, cTL);
        vertex(x + w, y, cTR);
        vertex(x + w, y + h, cBR);
        vertex(x, y, cTL);
        vertex(x + w, y + h, cBR);
        vertex(x, y + h, cBL);
    }

    // ==================== Rect Outline ====================

    public void rectOutline(float x, float y, float w, float h, float thickness, int color) {
        quad(x, y, w, thickness, color);
        quad(x, y + h - thickness, w, thickness, color);
        quad(x, y + thickness, thickness, h - thickness * 2f, color);
        quad(x + w - thickness, y + thickness, thickness, h - thickness * 2f, color);
    }

    // ==================== Rounded Rectangles ====================

    public void roundedRect(float x, float y, float w, float h, float radius, int color) {
        roundedGradient(x, y, w, h, radius, radius, radius, radius, color, color, color, color);
    }

    public void roundedRect(float x, float y, float w, float h, float tl, float tr, float br, float bl, int color) {
        roundedGradient(x, y, w, h, tl, tr, br, bl, color, color, color, color);
    }

    public void roundedGradient(float x, float y, float w, float h, float radius, int cTL, int cTR, int cBR, int cBL) {
        roundedGradient(x, y, w, h, radius, radius, radius, radius, cTL, cTR, cBR, cBL);
    }

    public void roundedGradient(float x, float y, float w, float h,
                                float tl, float tr, float br, float bl,
                                int cTL, int cTR, int cBR, int cBL) {
        float maxR = Math.min(w, h) * 0.5f;
        tl = Math.min(tl, maxR);
        tr = Math.min(tr, maxR);
        br = Math.min(br, maxR);
        bl = Math.min(bl, maxR);

        if (tl <= 0.5f && tr <= 0.5f && br <= 0.5f && bl <= 0.5f) {
            gradientQuad4(x, y, w, h, cTL, cTR, cBR, cBL);
            return;
        }

        setMode(MODE_FLAT);

        // Center body
        gradientQuad4(x + bl, y + tl, w - bl - tr, h - tl - br, cTL, cTR, cBR, cBL);
        // Top rect
        gradientQuad4(x + tl, y, w - tl - tr, tl, cTL, cTR, cTR, cTL);
        // Bottom rect
        gradientQuad4(x + bl, y + h - br, w - bl - br, br, cBL, cBR, cBR, cBL);
        // Left rect
        gradientQuad4(x, y + tl, bl, h - tl - bl, cTL, cTL, cBL, cBL);
        // Right rect
        gradientQuad4(x + w - tr, y + tr, tr, h - tr - br, cTR, cTR, cBR, cBR);

        // Corner Fans
        drawCornerFan(x + tl, y + tl, tl, (float) Math.PI, (float) (Math.PI * 1.5), cTL, 8);
        drawCornerFan(x + w - tr, y + tr, tr, (float) (Math.PI * 1.5), (float) (Math.PI * 2.0), cTR, 8);
        drawCornerFan(x + w - br, y + h - br, br, 0f, (float) (Math.PI * 0.5), cBR, 8);
        drawCornerFan(x + bl, y + h - bl, bl, (float) (Math.PI * 0.5), (float) Math.PI, cBL, 8);
    }

    public void roundedOutline(float x, float y, float w, float h, float radius, float thickness, int color) {
        float maxR = Math.min(w, h) * 0.5f;
        radius = Math.min(radius, maxR);

        // Straight segments
        quad(x + radius, y, w - radius * 2f, thickness, color);
        quad(x + radius, y + h - thickness, w - radius * 2f, thickness, color);
        quad(x, y + radius, thickness, h - radius * 2f, color);
        quad(x + w - thickness, y + radius, thickness, h - radius * 2f, color);

        // Curved corner arcs
        if (radius > 0.5f) {
            drawCornerArc(x + radius, y + radius, radius, thickness, (float) Math.PI, (float) (Math.PI * 1.5), color, 8);
            drawCornerArc(x + w - radius, y + radius, radius, thickness, (float) (Math.PI * 1.5), (float) (Math.PI * 2.0), color, 8);
            drawCornerArc(x + w - radius, y + h - radius, radius, thickness, 0f, (float) (Math.PI * 0.5), color, 8);
            drawCornerArc(x + radius, y + h - radius, radius, thickness, (float) (Math.PI * 0.5), (float) Math.PI, color, 8);
        }
    }

    public void roundedBordered(float x, float y, float w, float h, float radius, float borderWidth, int fillColor, int borderColor) {
        roundedRect(x, y, w, h, radius, fillColor);
        if (borderWidth > 0.01f) {
            roundedOutline(x, y, w, h, radius, borderWidth, borderColor);
        }
    }

    // ==================== Drop Shadow & Glow ====================

    public void dropShadow(float x, float y, float w, float h, float radius, float blur, int shadowColor) {
        glow(x, y + 2f, w, h, radius, blur, shadowColor);
    }

    public void glow(float x, float y, float w, float h, float radius, float blur, int glowColor) {
        int steps = 4;
        int alpha = (glowColor >>> 24) & 0xFF;
        int rgb = glowColor & 0x00FFFFFF;

        for (int i = steps; i >= 1; i--) {
            float spread = (blur * i) / steps;
            int stepAlpha = (int) (alpha * (1.0f - (float) i / (steps + 1)) * 0.35f);
            int color = (stepAlpha << 24) | rgb;
            roundedOutline(x - spread, y - spread, w + spread * 2f, h + spread * 2f, radius + spread, spread * 0.5f + 1f, color);
        }
    }

    // ==================== Circles & Rings ====================

    public void circle(float cx, float cy, float radius, int color) {
        int segments = Math.max(16, (int) (radius * 3.0f));
        setMode(MODE_FLAT);
        ensureCapacity(segments * 3);

        float angleStep = (float) (Math.PI * 2.0 / segments);
        for (int i = 0; i < segments; i++) {
            float a1 = i * angleStep;
            float a2 = (i + 1) * angleStep;
            vertex(cx, cy, color);
            vertex(cx + (float) Math.cos(a1) * radius, cy + (float) Math.sin(a1) * radius, color);
            vertex(cx + (float) Math.cos(a2) * radius, cy + (float) Math.sin(a2) * radius, color);
        }
    }

    public void circleOutline(float cx, float cy, float radius, float thickness, int color) {
        ring(cx, cy, Math.max(0f, radius - thickness), radius, color);
    }

    public void arc(float cx, float cy, float radius, float startAngle, float endAngle, float thickness, int color) {
        float innerRadius = Math.max(0f, radius - thickness);
        float sweep = endAngle - startAngle;
        if (Math.abs(sweep) < 0.001f) return;

        int segments = Math.max(8, (int) (Math.abs(sweep) / (Math.PI * 2.0) * (radius * 3.0f)));
        setMode(MODE_FLAT);
        ensureCapacity(segments * 6);

        float angleStep = sweep / segments;
        for (int i = 0; i < segments; i++) {
            float a1 = startAngle + i * angleStep;
            float a2 = startAngle + (i + 1) * angleStep;

            float x1Out = cx + (float) Math.cos(a1) * radius;
            float y1Out = cy + (float) Math.sin(a1) * radius;
            float x2Out = cx + (float) Math.cos(a2) * radius;
            float y2Out = cy + (float) Math.sin(a2) * radius;

            float x1In = cx + (float) Math.cos(a1) * innerRadius;
            float y1In = cy + (float) Math.sin(a1) * innerRadius;
            float x2In = cx + (float) Math.cos(a2) * innerRadius;
            float y2In = cy + (float) Math.sin(a2) * innerRadius;

            vertex(x1In, y1In, color);
            vertex(x1Out, y1Out, color);
            vertex(x2Out, y2Out, color);

            vertex(x1In, y1In, color);
            vertex(x2Out, y2Out, color);
            vertex(x2In, y2In, color);
        }
    }

    public void ring(float cx, float cy, float innerRadius, float outerRadius, int color) {
        int segments = Math.max(16, (int) (outerRadius * 3.0f));
        setMode(MODE_FLAT);
        ensureCapacity(segments * 6);

        float angleStep = (float) (Math.PI * 2.0 / segments);
        for (int i = 0; i < segments; i++) {
            float a1 = i * angleStep;
            float a2 = (i + 1) * angleStep;

            float x1Out = cx + (float) Math.cos(a1) * outerRadius;
            float y1Out = cy + (float) Math.sin(a1) * outerRadius;
            float x2Out = cx + (float) Math.cos(a2) * outerRadius;
            float y2Out = cy + (float) Math.sin(a2) * outerRadius;

            float x1In = cx + (float) Math.cos(a1) * innerRadius;
            float y1In = cy + (float) Math.sin(a1) * innerRadius;
            float x2In = cx + (float) Math.cos(a2) * innerRadius;
            float y2In = cy + (float) Math.sin(a2) * innerRadius;

            vertex(x1In, y1In, color);
            vertex(x1Out, y1Out, color);
            vertex(x2Out, y2Out, color);

            vertex(x1In, y1In, color);
            vertex(x2Out, y2Out, color);
            vertex(x2In, y2In, color);
        }
    }

    // ==================== Corner Helpers ====================

    private void drawCornerFan(float cx, float cy, float radius, float startAngle, float endAngle, int color, int segments) {
        if (radius <= 0.01f) return;
        ensureCapacity(segments * 3);
        float step = (endAngle - startAngle) / segments;
        for (int i = 0; i < segments; i++) {
            float a1 = startAngle + i * step;
            float a2 = startAngle + (i + 1) * step;
            vertex(cx, cy, color);
            vertex(cx + (float) Math.cos(a1) * radius, cy + (float) Math.sin(a1) * radius, color);
            vertex(cx + (float) Math.cos(a2) * radius, cy + (float) Math.sin(a2) * radius, color);
        }
    }

    private void drawCornerArc(float cx, float cy, float radius, float thickness, float startAngle, float endAngle, int color, int segments) {
        if (radius <= 0.01f) return;
        float inner = Math.max(0f, radius - thickness);
        ensureCapacity(segments * 6);
        float step = (endAngle - startAngle) / segments;
        for (int i = 0; i < segments; i++) {
            float a1 = startAngle + i * step;
            float a2 = startAngle + (i + 1) * step;

            float x1Out = cx + (float) Math.cos(a1) * radius;
            float y1Out = cy + (float) Math.sin(a1) * radius;
            float x2Out = cx + (float) Math.cos(a2) * radius;
            float y2Out = cy + (float) Math.sin(a2) * radius;

            float x1In = cx + (float) Math.cos(a1) * inner;
            float y1In = cy + (float) Math.sin(a1) * inner;
            float x2In = cx + (float) Math.cos(a2) * inner;
            float y2In = cy + (float) Math.sin(a2) * inner;

            vertex(x1In, y1In, color);
            vertex(x1Out, y1Out, color);
            vertex(x2Out, y2Out, color);

            vertex(x1In, y1In, color);
            vertex(x2Out, y2Out, color);
            vertex(x2In, y2In, color);
        }
    }

    // ==================== Lines & Polygons ====================

    public void line(float x1, float y1, float x2, float y2, float thickness, int color) {
        gradientLine(x1, y1, x2, y2, thickness, color, color);
    }

    public void gradientLine(float x1, float y1, float x2, float y2, float thickness, int c1, int c2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.0001f) return;

        float nx = -dy / len * (thickness * 0.5f);
        float ny = dx / len * (thickness * 0.5f);

        setMode(MODE_FLAT);
        ensureCapacity(6);
        vertex(x1 + nx, y1 + ny, c1);
        vertex(x2 + nx, y2 + ny, c2);
        vertex(x2 - nx, y2 - ny, c2);
        vertex(x1 + nx, y1 + ny, c1);
        vertex(x2 - nx, y2 - ny, c2);
        vertex(x1 - nx, y1 - ny, c1);
    }

    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        setMode(MODE_FLAT);
        ensureCapacity(3);
        vertex(x1, y1, color);
        vertex(x2, y2, color);
        vertex(x3, y3, color);
    }

    // ==================== Textured Quads ====================

    public void texture(int texture, float x, float y, float w, float h, int color) {
        texturedQuad(texture, x, y, w, h, 0f, 0f, 1f, 1f, color);
    }

    public void texturedQuad(int texture, float x, float y, float w, float h,
                             float u0, float v0, float u1, float v1, int color) {
        if (boundTexture != texture) {
            flush();
            boundTexture = texture;
        }
        setMode(MODE_TEXTURED);
        ensureCapacity(6);
        vertexTex(x, y, u0, v0, color);
        vertexTex(x + w, y, u1, v0, color);
        vertexTex(x + w, y + h, u1, v1, color);
        vertexTex(x, y, u0, v0, color);
        vertexTex(x + w, y + h, u1, v1, color);
        vertexTex(x, y + h, u0, v1, color);
    }

    // ==================== Flush & Internal Pipeline (800+ FPS Stream) ====================

    public void flush() {
        if (vertexCount == 0) {
            mode = MODE_NONE;
            return;
        }

        ShaderProgram shader = (mode == MODE_TEXTURED) ? textureShader : colorShader;
        shader.use();

        combinedMatrix.set(orthoMatrix);
        shader.setMat4("uMVP", combinedMatrix);

        if (mode == MODE_TEXTURED) {
            GLUtil.activeTexture(GL13.GL_TEXTURE0);
            GLUtil.bindTexture(boundTexture);
            shader.setInt("uTex", 0);
            shader.setFloat("uSaturate", saturate);
        }

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GLUtil.bindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        buffer.flip();
        // Buffer Orphaning: prevents GPU driver pipeline stalls -> 800+ FPS!
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) buffer.capacity() * 4L, GL15.GL_STREAM_DRAW);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, buffer);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);

        buffer.clear();
        vertexCount = 0;
        mode = MODE_NONE;
        boundTexture = -1;

        GLUtil.bindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void setMode(int newMode) {
        if (mode != newMode) {
            flush();
            mode = newMode;
        }
    }

    private void vertex(float x, float y, int color) {
        transformPos(x, y);
        buffer.put(tempPos.x).put(tempPos.y);
        buffer.put(0f).put(0f);
        putColor(color);
        vertexCount++;
    }

    private void vertexTex(float x, float y, float u, float v, int color) {
        transformPos(x, y);
        buffer.put(tempPos.x).put(tempPos.y);
        buffer.put(u).put(v);
        putColor(color);
        vertexCount++;
    }

    private void transformPos(float x, float y) {
        tempPos.set(x, y, 0f, 1f);
        tempPos.mul(matrixStack.current());
    }

    private void putColor(int argb) {
        float r = ((argb >>> 16) & 0xFF) / 255f;
        float g = ((argb >>> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        float a = ((argb >>> 24) & 0xFF) / 255f;
        buffer.put(r).put(g).put(b).put(a);
    }

    private void ensureCapacity(int verticesToAdd) {
        int floatsNeeded = verticesToAdd * FLOATS_PER_VERTEX;
        if (buffer.remaining() < floatsNeeded) {
            flush();
            if (buffer.remaining() < floatsNeeded) {
                int newCap = Math.max(buffer.capacity() * 2, floatsNeeded * 2);
                buffer = BufferUtils.createFloatBuffer(newCap);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) newCap * 4L, GL15.GL_STREAM_DRAW);
            }
        }
    }
}
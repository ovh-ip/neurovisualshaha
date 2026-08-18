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
    private static final int INITIAL_CAPACITY = 1 << 16;

    private FloatBuffer buffer;
    private int vertexCount;
    private int mode = MODE_NONE;
    private int boundTexture = -1;
    private int vao;
    private int vbo;

    private final ShaderProgram colorShader;
    private final ShaderProgram textureShader;
    private final ShaderProgram roundedShader;
    private final ShaderProgram circleShader;

    private final Matrix4f orthoMatrix = new Matrix4f();
    private final Matrix4f combinedMatrix = new Matrix4f();
    private final MatrixStack2D matrixStack = new MatrixStack2D();
    private final Vector4f tempPos = new Vector4f();

    public Renderer2D() {
        GLUtil.ensureCapabilities();
        buffer = BufferUtils.createFloatBuffer(INITIAL_CAPACITY);
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) buffer.capacity() * 4L, GL15.GL_DYNAMIC_DRAW);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, FLOATS_PER_VERTEX * 4, 0L);

        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, FLOATS_PER_VERTEX * 4, 8L);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, FLOATS_PER_VERTEX * 4, 16L);

        GL30.glBindVertexArray(0);

        colorShader = ShaderProgram.load("/assets/testvisuals/shaders/quad.vert",
                "/assets/testvisuals/shaders/quad_flat.frag", new String[]{"aPos", "aUV", "aColor"});
        textureShader = ShaderProgram.load("/assets/testvisuals/shaders/quad.vert",
                "/assets/testvisuals/shaders/quad_tex.frag", new String[]{"aPos", "aUV", "aColor"});
        roundedShader = ShaderProgram.load("/assets/testvisuals/shaders/quad.vert",
                "/assets/testvisuals/shaders/rounded_rect.frag", new String[]{"aPos", "aUV", "aColor"});
        circleShader = ShaderProgram.load("/assets/testvisuals/shaders/quad.vert",
                "/assets/testvisuals/shaders/circle.frag", new String[]{"aPos", "aUV", "aColor"});
    }

    public void begin(float width, float height) {
        flush();
        orthoMatrix.setOrtho(0f, width, height, 0f, -100f, 100f);
        matrixStack.reset();
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

    // ==================== SDF Rounded Rectangles ====================

    public void roundedRect(float x, float y, float w, float h, float radius, int color) {
        roundedRect(x, y, w, h, radius, radius, radius, radius, color);
    }

    public void roundedRect(float x, float y, float w, float h, float tl, float tr, float br, float bl, int color) {
        roundedGradient(x, y, w, h, tl, tr, br, bl, color, color, color, color, 0f, 0);
    }

    public void roundedGradient(float x, float y, float w, float h, float radius, int cTL, int cTR, int cBR, int cBL) {
        roundedGradient(x, y, w, h, radius, radius, radius, radius, cTL, cTR, cBR, cBL, 0f, 0);
    }

    public void roundedOutline(float x, float y, float w, float h, float radius, float thickness, int color) {
        roundedGradient(x, y, w, h, radius, radius, radius, radius, 0, 0, 0, 0, thickness, color);
    }

    public void roundedBordered(float x, float y, float w, float h, float radius, float borderWidth, int fillColor, int borderColor) {
        roundedGradient(x, y, w, h, radius, radius, radius, radius, fillColor, fillColor, fillColor, fillColor, borderWidth, borderColor);
    }

    public void roundedGradient(float x, float y, float w, float h,
                                float tl, float tr, float br, float bl,
                                int cTL, int cTR, int cBR, int cBL,
                                float borderWidth, int borderColor) {
        flush();
        prepareSdfShader(roundedShader);

        roundedShader.setVec4("uRect", x, y, w, h);
        roundedShader.setVec4("uRadius", tl, tr, br, bl);
        setShaderColor(roundedShader, "uColorTL", cTL);
        setShaderColor(roundedShader, "uColorTR", cTR);
        setShaderColor(roundedShader, "uColorBR", cBR);
        setShaderColor(roundedShader, "uColorBL", cBL);
        setShaderColor(roundedShader, "uBorderColor", borderColor);
        roundedShader.setFloat("uBorderWidth", borderWidth);
        roundedShader.setFloat("uSoftness", 1.0f);
        roundedShader.setFloat("uShadow", 0.0f);

        drawSdfQuad(x, y, w, h);
    }

    // ==================== Drop Shadow & Glow ====================

    public void dropShadow(float x, float y, float w, float h, float radius, float blur, int shadowColor) {
        drawGlowInternal(x, y, w, h, radius, blur, shadowColor, 0f, 3f);
    }

    public void glow(float x, float y, float w, float h, float radius, float blur, int glowColor) {
        drawGlowInternal(x, y, w, h, radius, blur, glowColor, 0f, 0f);
    }

    private void drawGlowInternal(float x, float y, float w, float h, float radius, float blur, int color, float offsetX, float offsetY) {
        flush();
        prepareSdfShader(roundedShader);

        float pad = blur * 2.0f;
        float gx = x - pad + offsetX;
        float gy = y - pad + offsetY;
        float gw = w + pad * 2f;
        float gh = h + pad * 2f;

        roundedShader.setVec4("uRect", 0f, 0f, gw, gh);
        roundedShader.setVec4("uRadius", radius, radius, radius, radius);
        roundedShader.setFloat("uBorderWidth", 0f);
        roundedShader.setFloat("uSoftness", blur);
        roundedShader.setFloat("uShadow", 1.0f);
        setShaderColor(roundedShader, "uShadowColor", color);

        drawSdfQuad(gx, gy, gw, gh);
    }

    // ==================== Circles & Rings ====================

    public void circle(float cx, float cy, float radius, int color) {
        circleGradient(cx, cy, radius, 0f, color, color);
    }

    public void circleOutline(float cx, float cy, float radius, float thickness, int color) {
        ring(cx, cy, Math.max(0f, radius - thickness), radius, color);
    }

    public void ring(float cx, float cy, float innerRadius, float outerRadius, int color) {
        circleGradient(cx, cy, outerRadius, innerRadius, color, color);
    }

    public void arc(float cx, float cy, float radius, float startAngle, float endAngle, float thickness, int color) {
        flush();
        prepareSdfShader(circleShader);

        float dia = radius * 2f + 4f;
        circleShader.setVec2("uCenter", cx, cy);
        circleShader.setFloat("uRadius", radius);
        circleShader.setFloat("uInnerRadius", Math.max(0f, radius - thickness));
        circleShader.setVec2("uAngles", startAngle, endAngle);
        setShaderColor(circleShader, "uColor1", color);
        setShaderColor(circleShader, "uColor2", color);
        circleShader.setFloat("uSoftness", 1.0f);

        drawSdfQuad(cx - dia / 2f, cy - dia / 2f, dia, dia);
    }

    public void circleGradient(float cx, float cy, float outerRadius, float innerRadius, int cCenter, int cOuter) {
        flush();
        prepareSdfShader(circleShader);

        float dia = outerRadius * 2f + 4f;
        circleShader.setVec2("uCenter", cx, cy);
        circleShader.setFloat("uRadius", outerRadius);
        circleShader.setFloat("uInnerRadius", innerRadius);
        circleShader.setVec2("uAngles", 0f, 6.2831853f);
        setShaderColor(circleShader, "uColor1", cCenter);
        setShaderColor(circleShader, "uColor2", cOuter);
        circleShader.setFloat("uSoftness", 1.0f);

        drawSdfQuad(cx - dia / 2f, cy - dia / 2f, dia, dia);
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

    public void gradientTriangle(float x1, float y1, int c1, float x2, float y2, int c2, float x3, float y3, int c3) {
        setMode(MODE_FLAT);
        ensureCapacity(3);
        vertex(x1, y1, c1);
        vertex(x2, y2, c2);
        vertex(x3, y3, c3);
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

    // ==================== Flush & Internal Pipeline ====================

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
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, boundTexture);
            shader.setInt("uTex", 0);
        }

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        buffer.flip();
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, buffer);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);

        buffer.clear();
        vertexCount = 0;
        mode = MODE_NONE;
        boundTexture = -1;
        GL30.glBindVertexArray(0);
    }

    private void prepareSdfShader(ShaderProgram shader) {
        shader.use();
        combinedMatrix.set(orthoMatrix);
        shader.setMat4("uMVP", combinedMatrix);
    }

    private void drawSdfQuad(float x, float y, float w, float h) {
        setMode(MODE_FLAT);
        ensureCapacity(6);
        vertexRaw(x, y, 0f, 0f, 0xFFFFFFFF);
        vertexRaw(x + w, y, 1f, 0f, 0xFFFFFFFF);
        vertexRaw(x + w, y + h, 1f, 1f, 0xFFFFFFFF);
        vertexRaw(x, y, 0f, 0f, 0xFFFFFFFF);
        vertexRaw(x + w, y + h, 1f, 1f, 0xFFFFFFFF);
        vertexRaw(x, y + h, 0f, 1f, 0xFFFFFFFF);

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        buffer.flip();
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, buffer);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);

        buffer.clear();
        vertexCount = 0;
        mode = MODE_NONE;
        GL30.glBindVertexArray(0);
    }

    private void setShaderColor(ShaderProgram shader, String name, int argb) {
        shader.setVec4(name,
                ((argb >>> 16) & 0xFF) / 255f,
                ((argb >>> 8) & 0xFF) / 255f,
                (argb & 0xFF) / 255f,
                ((argb >>> 24) & 0xFF) / 255f);
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

    private void vertexRaw(float x, float y, float u, float v, int color) {
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
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) newCap * 4L, GL15.GL_DYNAMIC_DRAW);
            }
        }
    }
}
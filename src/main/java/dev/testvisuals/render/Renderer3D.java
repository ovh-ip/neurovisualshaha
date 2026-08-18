package dev.testvisuals.render;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import dev.testvisuals.gl.GLUtil;
import dev.testvisuals.gl.MatrixStack3D;
import dev.testvisuals.gl.ShaderProgram;
import dev.testvisuals.util.ColorUtils;

public final class Renderer3D {

    private static final int FLOATS_PER_VERTEX = 12; // pos(3), normal(3), color(4), uv(2)
    private static final int INITIAL_CAPACITY = 1 << 17;

    private FloatBuffer buffer;
    private int vertexCount;
    private int primitive = GL11.GL_TRIANGLES;
    private int vao;
    private int vbo;

    private final ShaderProgram shader;
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f view = new Matrix4f();
    private final Matrix4f mvp = new Matrix4f();
    private final MatrixStack3D matrixStack = new MatrixStack3D();

    private final Vector4f tempPos = new Vector4f();
    private final Vector4f tempNorm = new Vector4f();

    public Renderer3D() {
        GLUtil.ensureCapabilities();
        buffer = BufferUtils.createFloatBuffer(INITIAL_CAPACITY);
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();

        GLUtil.bindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) buffer.capacity() * 4L, GL15.GL_DYNAMIC_DRAW);

        int stride = FLOATS_PER_VERTEX * 4;
        GL20.glEnableVertexAttribArray(0); // Pos
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0L);

        GL20.glEnableVertexAttribArray(1); // Normal
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, stride, 12L);

        GL20.glEnableVertexAttribArray(2); // Color
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, stride, 24L);

        GL20.glEnableVertexAttribArray(3); // UV
        GL20.glVertexAttribPointer(3, 2, GL11.GL_FLOAT, false, stride, 40L);

        GLUtil.bindVertexArray(0);

        shader = ShaderProgram.load("/assets/testvisuals/shaders/3d.vert",
                "/assets/testvisuals/shaders/3d.frag", new String[]{"aPos", "aNormal", "aColor", "aUV"});
    }

    public void begin(Matrix4f proj, Matrix4f v) {
        flush();
        this.projection.set(proj);
        this.view.set(v);
        this.mvp.set(proj).mul(v);
        matrixStack.reset();
    }

    public void push() {
        matrixStack.push();
    }

    public void pop() {
        matrixStack.pop();
    }

    public void translate(float x, float y, float z) {
        matrixStack.translate(x, y, z);
    }

    public void translate(Vector3f vec) {
        matrixStack.translate(vec);
    }

    public void scale(float sx, float sy, float sz) {
        matrixStack.scale(sx, sy, sz);
    }

    public void scale(float s) {
        matrixStack.scale(s);
    }

    public void rotateX(float radians) {
        matrixStack.rotateX(radians);
    }

    public void rotateY(float radians) {
        matrixStack.rotateY(radians);
    }

    public void rotateZ(float radians) {
        matrixStack.rotateZ(radians);
    }

    public void rotate(float radians, float x, float y, float z) {
        matrixStack.rotate(radians, x, y, z);
    }

    public MatrixStack3D getMatrixStack() {
        return matrixStack;
    }

    // ==================== Cubes & Boxes ====================

    public void cube(int color) {
        cube(1f, color);
    }

    public void cube(float size, int color) {
        float h = size * 0.5f;
        box(-h, -h, -h, h, h, h, color);
    }

    public void box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color) {
        gradientBox(minX, minY, minZ, maxX, maxY, maxZ, color, color);
    }

    public void gradientBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int topColor, int bottomColor) {
        setPrimitive(GL11.GL_TRIANGLES);
        // Front (+Z)
        quad(minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, 0f, 0f, 1f, bottomColor, topColor);
        // Back (-Z)
        quad(maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, 0f, 0f, -1f, bottomColor, topColor);
        // Right (+X)
        quad(maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, 1f, 0f, 0f, bottomColor, topColor);
        // Left (-X)
        quad(minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, -1f, 0f, 0f, bottomColor, topColor);
        // Top (+Y)
        quad(minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, 0f, 1f, 0f, topColor, topColor);
        // Bottom (-Y)
        quad(minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, 0f, -1f, 0f, bottomColor, bottomColor);
    }

    private void quad(float x0, float y0, float z0, float x1, float y1, float z1,
                      float x2, float y2, float z2, float x3, float y3, float z3,
                      float nx, float ny, float nz, int cBottom, int cTop) {
        ensureCapacity(6);
        vertex(x0, y0, z0, nx, ny, nz, cBottom);
        vertex(x1, y1, z1, nx, ny, nz, cBottom);
        vertex(x2, y2, z2, nx, ny, nz, cTop);

        vertex(x0, y0, z0, nx, ny, nz, cBottom);
        vertex(x2, y2, z2, nx, ny, nz, cTop);
        vertex(x3, y3, z3, nx, ny, nz, cTop);
    }

    // ==================== Wireframe Box & ESP ====================

    public void wireCube(float size, int color) {
        float h = size * 0.5f;
        wireBox(-h, -h, -h, h, h, h, color);
    }

    public void wireBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color) {
        setPrimitive(GL11.GL_LINES);
        ensureCapacity(24);
        // Bottom 4 lines
        lineV(minX, minY, minZ, maxX, minY, minZ, color);
        lineV(maxX, minY, minZ, maxX, minY, maxZ, color);
        lineV(maxX, minY, maxZ, minX, minY, maxZ, color);
        lineV(minX, minY, maxZ, minX, minY, minZ, color);
        // Top 4 lines
        lineV(minX, maxY, minZ, maxX, maxY, minZ, color);
        lineV(maxX, maxY, minZ, maxX, maxY, maxZ, color);
        lineV(maxX, maxY, maxZ, minX, maxY, maxZ, color);
        lineV(minX, maxY, maxZ, minX, maxY, minZ, color);
        // Pillars 4 lines
        lineV(minX, minY, minZ, minX, maxY, minZ, color);
        lineV(maxX, minY, minZ, maxX, maxY, minZ, color);
        lineV(maxX, minY, maxZ, maxX, maxY, maxZ, color);
        lineV(minX, minY, maxZ, minX, maxY, maxZ, color);
    }

    public void boundingBoxESP(float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                               int fillColor, int outlineColor) {
        box(minX, minY, minZ, maxX, maxY, maxZ, fillColor);
        wireBox(minX, minY, minZ, maxX, maxY, maxZ, outlineColor);
    }

    // ==================== Spheres & Crystals ====================

    public void sphere(float radius, int stacks, int slices, int colorTop, int colorBottom) {
        setPrimitive(GL11.GL_TRIANGLES);
        for (int i = 0; i < stacks; i++) {
            double phi0 = Math.PI * i / stacks;
            double phi1 = Math.PI * (i + 1) / stacks;
            float sinPhi0 = (float) Math.sin(phi0), cosPhi0 = (float) Math.cos(phi0);
            float sinPhi1 = (float) Math.sin(phi1), cosPhi1 = (float) Math.cos(phi1);

            for (int j = 0; j < slices; j++) {
                double th0 = Math.PI * 2.0 * j / slices;
                double th1 = Math.PI * 2.0 * (j + 1) / slices;
                float sinTh0 = (float) Math.sin(th0), cosTh0 = (float) Math.cos(th0);
                float sinTh1 = (float) Math.sin(th1), cosTh1 = (float) Math.cos(th1);

                float x00 = radius * sinPhi0 * cosTh0, y00 = radius * cosPhi0, z00 = radius * sinPhi0 * sinTh0;
                float x01 = radius * sinPhi0 * cosTh1, y01 = radius * cosPhi0, z01 = radius * sinPhi0 * sinTh1;
                float x10 = radius * sinPhi1 * cosTh0, y10 = radius * cosPhi1, z10 = radius * sinPhi1 * sinTh0;
                float x11 = radius * sinPhi1 * cosTh1, y11 = radius * cosPhi1, z11 = radius * sinPhi1 * sinTh1;

                int c0 = ColorUtils.lerp(colorBottom, colorTop, (y00 / radius + 1f) * 0.5f);
                int c1 = ColorUtils.lerp(colorBottom, colorTop, (y10 / radius + 1f) * 0.5f);

                ensureCapacity(6);
                vertex(x00, y00, z00, x00 / radius, y00 / radius, z00 / radius, c0);
                vertex(x10, y10, z10, x10 / radius, y10 / radius, z10 / radius, c1);
                vertex(x11, y11, z11, x11 / radius, y11 / radius, z11 / radius, c1);

                vertex(x00, y00, z00, x00 / radius, y00 / radius, z00 / radius, c0);
                vertex(x11, y11, z11, x11 / radius, y11 / radius, z11 / radius, c1);
                vertex(x01, y01, z01, x01 / radius, y01 / radius, z01 / radius, c0);
            }
        }
    }

    public void wireSphere(float radius, int rings, int meridians, int color) {
        setPrimitive(GL11.GL_LINES);
        for (int i = 0; i < rings; i++) {
            double phi = Math.PI * (i + 1) / (rings + 1);
            float y = (float) (radius * Math.cos(phi));
            float r = (float) (radius * Math.sin(phi));
            for (int j = 0; j < meridians; j++) {
                double a0 = Math.PI * 2.0 * j / meridians;
                double a1 = Math.PI * 2.0 * (j + 1) / meridians;
                lineV((float) (r * Math.cos(a0)), y, (float) (r * Math.sin(a0)),
                      (float) (r * Math.cos(a1)), y, (float) (r * Math.sin(a1)), color);
            }
        }
        for (int j = 0; j < meridians; j++) {
            double a = Math.PI * 2.0 * j / meridians;
            float cosA = (float) Math.cos(a), sinA = (float) Math.sin(a);
            for (int i = 0; i < rings; i++) {
                double phi0 = Math.PI * i / rings;
                double phi1 = Math.PI * (i + 1) / rings;
                lineV((float) (radius * Math.sin(phi0) * cosA), (float) (radius * Math.cos(phi0)), (float) (radius * Math.sin(phi0) * sinA),
                      (float) (radius * Math.sin(phi1) * cosA), (float) (radius * Math.cos(phi1)), (float) (radius * Math.sin(phi1) * sinA), color);
            }
        }
    }

    public void crystal(float radius, float height, int colorTop, int colorBottom) {
        setPrimitive(GL11.GL_TRIANGLES);
        int slices = 6;
        float h = height * 0.5f;
        for (int i = 0; i < slices; i++) {
            double a0 = Math.PI * 2.0 * i / slices;
            double a1 = Math.PI * 2.0 * (i + 1) / slices;
            float x0 = (float) Math.cos(a0) * radius;
            float z0 = (float) Math.sin(a0) * radius;
            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;

            // Top pyramid
            ensureCapacity(3);
            vertex(0f, h, 0f, 0f, 1f, 0f, colorTop);
            vertex(x0, 0f, z0, x0 / radius, 0.5f, z0 / radius, colorBottom);
            vertex(x1, 0f, z1, x1 / radius, 0.5f, z1 / radius, colorBottom);

            // Bottom pyramid
            ensureCapacity(3);
            vertex(0f, -h, 0f, 0f, -1f, 0f, colorBottom);
            vertex(x1, 0f, z1, x1 / radius, -0.5f, z1 / radius, colorBottom);
            vertex(x0, 0f, z0, x0 / radius, -0.5f, z0 / radius, colorBottom);
        }
    }

    // ==================== Cylinders, Cones & Torus ====================

    public void cylinder(float baseRadius, float topRadius, float height, int slices, int stacks, int color) {
        setPrimitive(GL11.GL_TRIANGLES);
        float halfH = height * 0.5f;
        for (int i = 0; i < stacks; i++) {
            float y0 = -halfH + height * i / stacks;
            float y1 = -halfH + height * (i + 1) / stacks;
            float r0 = baseRadius + (topRadius - baseRadius) * i / stacks;
            float r1 = baseRadius + (topRadius - baseRadius) * (i + 1) / stacks;

            for (int j = 0; j < slices; j++) {
                double a0 = Math.PI * 2.0 * j / slices;
                double a1 = Math.PI * 2.0 * (j + 1) / slices;
                float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
                float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);

                ensureCapacity(6);
                vertex(r0 * c0, y0, r0 * s0, c0, 0f, s0, color);
                vertex(r0 * c1, y0, r0 * s1, c1, 0f, s1, color);
                vertex(r1 * c1, y1, r1 * s1, c1, 0f, s1, color);

                vertex(r0 * c0, y0, r0 * s0, c0, 0f, s0, color);
                vertex(r1 * c1, y1, r1 * s1, c1, 0f, s1, color);
                vertex(r1 * c0, y1, r1 * s0, c0, 0f, s0, color);
            }
        }
    }

    public void torus(float majorRadius, float minorRadius, int majorSegments, int minorSegments, int color) {
        setPrimitive(GL11.GL_TRIANGLES);
        for (int i = 0; i < majorSegments; i++) {
            double u0 = Math.PI * 2.0 * i / majorSegments;
            double u1 = Math.PI * 2.0 * (i + 1) / majorSegments;
            float cu0 = (float) Math.cos(u0), su0 = (float) Math.sin(u0);
            float cu1 = (float) Math.cos(u1), su1 = (float) Math.sin(u1);

            for (int j = 0; j < minorSegments; j++) {
                double v0 = Math.PI * 2.0 * j / minorSegments;
                double v1 = Math.PI * 2.0 * (j + 1) / minorSegments;
                float cv0 = (float) Math.cos(v0), sv0 = (float) Math.sin(v0);
                float cv1 = (float) Math.cos(v1), sv1 = (float) Math.sin(v1);

                float x00 = (majorRadius + minorRadius * cv0) * cu0;
                float y00 = minorRadius * sv0;
                float z00 = (majorRadius + minorRadius * cv0) * su0;

                float x01 = (majorRadius + minorRadius * cv1) * cu0;
                float y01 = minorRadius * sv1;
                float z01 = (majorRadius + minorRadius * cv1) * su0;

                float x10 = (majorRadius + minorRadius * cv0) * cu1;
                float y10 = minorRadius * sv0;
                float z10 = (majorRadius + minorRadius * cv0) * su1;

                float x11 = (majorRadius + minorRadius * cv1) * cu1;
                float y11 = minorRadius * sv1;
                float z11 = (majorRadius + minorRadius * cv1) * su1;

                ensureCapacity(6);
                vertex(x00, y00, z00, cv0 * cu0, sv0, cv0 * su0, color);
                vertex(x10, y10, z10, cv0 * cu1, sv0, cv0 * su1, color);
                vertex(x11, y11, z11, cv1 * cu1, sv1, cv1 * su1, color);

                vertex(x00, y00, z00, cv0 * cu0, sv0, cv0 * su0, color);
                vertex(x11, y11, z11, cv1 * cu1, sv1, cv1 * su1, color);
                vertex(x01, y01, z01, cv1 * cu0, sv1, cv1 * su0, color);
            }
        }
    }

    // ==================== Lines & Grids ====================

    public void line(float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        setPrimitive(GL11.GL_LINES);
        lineV(x1, y1, z1, x2, y2, z2, color);
    }

    public void lineLoop(float radius, int segments, int color) {
        setPrimitive(GL11.GL_LINES);
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i + 1) / segments;
            lineV((float) (radius * Math.cos(a0)), 0f, (float) (radius * Math.sin(a0)),
                  (float) (radius * Math.cos(a1)), 0f, (float) (radius * Math.sin(a1)), color);
        }
    }

    public void grid(float halfExtent, int lines, int color) {
        setPrimitive(GL11.GL_LINES);
        float step = halfExtent * 2f / lines;
        for (int i = 0; i <= lines; i++) {
            float v = -halfExtent + step * i;
            lineV(v, 0f, -halfExtent, v, 0f, halfExtent, color);
            lineV(-halfExtent, 0f, v, halfExtent, 0f, v, color);
        }
    }

    public void beaconBeam(float height, float innerRadius, float outerRadius, int innerColor, int outerColor, float time) {
        push();
        rotateY(time * 0.4f);
        cylinder(innerRadius, innerRadius, height, 16, 1, innerColor);
        cylinder(outerRadius, outerRadius, height, 20, 1, outerColor);
        pop();
    }

    public void billboard(float x, float y, float z, float width, float height, int color) {
        setPrimitive(GL11.GL_TRIANGLES);
        float hw = width * 0.5f;
        float hh = height * 0.5f;

        // Extract camera right & up vectors from view matrix
        float rx = view.m00(), ry = view.m10(), rz = view.m20();
        float ux = view.m01(), uy = view.m11(), uz = view.m21();

        float x0 = x - rx * hw - ux * hh, y0 = y - ry * hw - uy * hh, z0 = z - rz * hw - uz * hh;
        float x1 = x + rx * hw - ux * hh, y1 = y + ry * hw - uy * hh, z1 = z + rz * hw - uz * hh;
        float x2 = x + rx * hw + ux * hh, y2 = y + ry * hw + uy * hh, z2 = z + rz * hw + uz * hh;
        float x3 = x - rx * hw + ux * hh, y3 = y - ry * hw + uy * hh, z3 = z - rz * hw + uz * hh;

        ensureCapacity(6);
        vertex(x0, y0, z0, 0f, 0f, 0f, color);
        vertex(x1, y1, z1, 0f, 0f, 0f, color);
        vertex(x2, y2, z2, 0f, 0f, 0f, color);

        vertex(x0, y0, z0, 0f, 0f, 0f, color);
        vertex(x2, y2, z2, 0f, 0f, 0f, color);
        vertex(x3, y3, z3, 0f, 0f, 0f, color);
    }

    // ==================== Vertex & Buffer Streaming ====================

    public void flush() {
        if (vertexCount == 0) {
            return;
        }

        shader.use();
        shader.setMat4("uMVP", mvp);
        shader.setMat4("uView", view);

        GLUtil.bindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        buffer.flip();
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, buffer);

        GL11.glDrawArrays(primitive, 0, vertexCount);

        buffer.clear();
        vertexCount = 0;
        GLUtil.bindVertexArray(0);
    }

    private void setPrimitive(int p) {
        if (p != primitive) {
            flush();
            primitive = p;
        }
    }

    private void lineV(float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        ensureCapacity(2);
        vertex(x1, y1, z1, 0f, 0f, 0f, color);
        vertex(x2, y2, z2, 0f, 0f, 0f, color);
    }

    private void vertex(float px, float py, float pz, float nx, float ny, float nz, int color) {
        Matrix4f cur = matrixStack.current();
        tempPos.set(px, py, pz, 1f).mul(cur);
        tempNorm.set(nx, ny, nz, 0f).mul(cur);

        buffer.put(tempPos.x).put(tempPos.y).put(tempPos.z);
        buffer.put(tempNorm.x).put(tempNorm.y).put(tempNorm.z);
        putColor(color);
        buffer.put(0f).put(0f); // UV
        vertexCount++;
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
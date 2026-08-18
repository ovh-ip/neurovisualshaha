package dev.testvisuals.gl;

import org.joml.Matrix4f;

public final class MatrixStack2D {

    private static final int MAX_DEPTH = 32;
    private final Matrix4f[] stack = new Matrix4f[MAX_DEPTH];
    private int depth = 0;

    public MatrixStack2D() {
        for (int i = 0; i < MAX_DEPTH; i++) {
            stack[i] = new Matrix4f();
        }
    }

    public void reset() {
        depth = 0;
        stack[0].identity();
    }

    public void push() {
        if (depth >= MAX_DEPTH - 1) {
            throw new IllegalStateException("MatrixStack2D overflow (max depth is " + MAX_DEPTH + ")");
        }
        stack[depth + 1].set(stack[depth]);
        depth++;
    }

    public void pop() {
        if (depth <= 0) {
            throw new IllegalStateException("MatrixStack2D underflow");
        }
        depth--;
    }

    public Matrix4f current() {
        return stack[depth];
    }

    public void translate(float x, float y) {
        stack[depth].translate(x, y, 0f);
    }

    public void scale(float sx, float sy) {
        stack[depth].scale(sx, sy, 1f);
    }

    public void scale(float sx, float sy, float cx, float cy) {
        translate(cx, cy);
        scale(sx, sy);
        translate(-cx, -cy);
    }

    public void rotate(float radians, float cx, float cy) {
        translate(cx, cy);
        stack[depth].rotateZ(radians);
        translate(-cx, -cy);
    }
}

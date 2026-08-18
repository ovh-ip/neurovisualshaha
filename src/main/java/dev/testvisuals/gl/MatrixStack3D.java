package dev.testvisuals.gl;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class MatrixStack3D {

    private static final int MAX_DEPTH = 32;
    private final Matrix4f[] stack = new Matrix4f[MAX_DEPTH];
    private int depth = 0;

    public MatrixStack3D() {
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
            throw new IllegalStateException("MatrixStack3D overflow (max depth is " + MAX_DEPTH + ")");
        }
        stack[depth + 1].set(stack[depth]);
        depth++;
    }

    public void pop() {
        if (depth <= 0) {
            throw new IllegalStateException("MatrixStack3D underflow");
        }
        depth--;
    }

    public Matrix4f current() {
        return stack[depth];
    }

    public void set(Matrix4f matrix) {
        stack[depth].set(matrix);
    }

    public void translate(float x, float y, float z) {
        stack[depth].translate(x, y, z);
    }

    public void translate(Vector3f offset) {
        stack[depth].translate(offset);
    }

    public void scale(float sx, float sy, float sz) {
        stack[depth].scale(sx, sy, sz);
    }

    public void scale(float s) {
        stack[depth].scale(s, s, s);
    }

    public void rotateX(float radians) {
        stack[depth].rotateX(radians);
    }

    public void rotateY(float radians) {
        stack[depth].rotateY(radians);
    }

    public void rotateZ(float radians) {
        stack[depth].rotateZ(radians);
    }

    public void rotate(float radians, float x, float y, float z) {
        stack[depth].rotate(radians, x, y, z);
    }

    public void rotate(Quaternionf quaternion) {
        stack[depth].rotate(quaternion);
    }

    public void mul(Matrix4f matrix) {
        stack[depth].mul(matrix);
    }
}

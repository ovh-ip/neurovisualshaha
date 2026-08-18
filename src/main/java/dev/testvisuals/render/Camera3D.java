package dev.testvisuals.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Camera3D {

    private final Vector3f position = new Vector3f();
    private final Vector3f target = new Vector3f();
    private final Vector3f up = new Vector3f(0f, 1f, 0f);
    private float distance = 8f;
    private float yaw;
    private float pitch;
    private float fov = (float) Math.toRadians(55);
    private float near = 0.05f;
    private float far = 200f;

    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projMatrix = new Matrix4f();

    public void orbit(float yaw, float pitch, float distance) {
        this.yaw = yaw;
        this.pitch = Math.clamp(pitch, -1.5f, 1.5f);
        this.distance = Math.max(0.1f, distance);
    }

    public void setFov(float fovDegrees) {
        this.fov = (float) Math.toRadians(fovDegrees);
    }

    public void lookAt(Vector3f target) {
        this.target.set(target);
    }

    public void lookAt(float x, float y, float z) {
        this.target.set(x, y, z);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getTarget() {
        return target;
    }

    public Matrix4f view() {
        float cp = (float) Math.cos(pitch);
        position.set(
                (float) (Math.sin(yaw) * cp) * distance,
                (float) Math.sin(pitch) * distance,
                (float) (Math.cos(yaw) * cp) * distance);
        position.add(target);
        return viewMatrix.identity().lookAt(position, target, up);
    }

    public Matrix4f projection(float aspect) {
        return projMatrix.identity().perspective(fov, aspect, near, far);
    }
}
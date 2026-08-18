package dev.testvisuals.menu;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import dev.testvisuals.gl.GLUtil;
import dev.testvisuals.gl.ShaderProgram;

public final class BackgroundRenderer {

    private static final float[] QUAD = {
            -1f, -1f, 1f, -1f, 1f, 1f,
            -1f, -1f, 1f, 1f, -1f, 1f
    };

    private final ShaderProgram shader;
    private final int vao;
    private final int vbo;
    private final FloatBuffer vertices;

    public BackgroundRenderer() {
        GLUtil.ensureCapabilities();
        shader = ShaderProgram.load("/assets/testvisuals/shaders/background.vert",
                "/assets/testvisuals/shaders/background.frag", new String[]{"aPos"});
        vertices = BufferUtils.createFloatBuffer(QUAD.length);
        vertices.put(QUAD).flip();
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 8, 0L);
        GL30.glBindVertexArray(0);
    }

    public void render(float time, int width, int height, float mouseX, float mouseY) {
        shader.use();
        shader.setVec2("u_res", width, height);
        shader.setFloat("u_time", time);
        shader.setVec2("u_mouse", mouseX, mouseY);
        GL30.glBindVertexArray(vao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(0);
    }
}
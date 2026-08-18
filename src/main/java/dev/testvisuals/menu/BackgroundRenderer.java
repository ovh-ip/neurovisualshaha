package dev.testvisuals.menu;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import dev.testvisuals.gl.GLUtil;
import dev.testvisuals.gl.ShaderProgram;

public final class BackgroundRenderer {

    private int textureId = -1;
    private int vao;
    private int vbo;
    private ShaderProgram shader;

    public BackgroundRenderer() {
        GLUtil.ensureCapabilities();
        loadTexture();
        shader = ShaderProgram.load("/assets/testvisuals/shaders/quad.vert",
                "/assets/testvisuals/shaders/quad_tex.frag", new String[]{"aPos", "aUV", "aColor"});

        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GLUtil.bindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        float[] vertices = {
            -1f, -1f,  0f, 1f,  1f, 1f, 1f, 1f,
             1f, -1f,  1f, 1f,  1f, 1f, 1f, 1f,
             1f,  1f,  1f, 0f,  1f, 1f, 1f, 1f,
            -1f, -1f,  0f, 1f,  1f, 1f, 1f, 1f,
             1f,  1f,  1f, 0f,  1f, 1f, 1f, 1f,
            -1f,  1f,  0f, 0f,  1f, 1f, 1f, 1f
        };
        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.length);
        fb.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fb, GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 8 * 4, 0L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 8 * 4, 8L);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 8 * 4, 16L);
        GLUtil.bindVertexArray(0);
    }

    private void loadTexture() {
        try (InputStream in = getClass().getResourceAsStream("/assets/testvisuals/textures/background.jpg")) {
            if (in == null) {
                return;
            }
            BufferedImage img = ImageIO.read(in);
            int w = img.getWidth();
            int h = img.getHeight();
            int[] pixels = img.getRGB(0, 0, w, h, null, 0, w);
            ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 4);
            IntBuffer ib = buf.asIntBuffer();
            int[] formatted = new int[w * h];
            for (int i = 0; i < pixels.length; i++) {
                int p = pixels[i];
                int a = (p >>> 24) & 0xFF;
                int r = (p >>> 16) & 0xFF;
                int g = (p >>> 8) & 0xFF;
                int b = p & 0xFF;
                formatted[i] = (a << 24) | (b << 16) | (g << 8) | r;
            }
            ib.put(formatted);
            buf.position(0);

            textureId = GL11.glGenTextures();
            GLUtil.bindTexture(textureId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
            GLUtil.bindTexture(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void render(float time, int width, int height, float mouseX, float mouseY) {
        if (textureId == -1) {
            return;
        }
        shader.use();
        Matrix4f identity = new Matrix4f();
        float px = (mouseX / (float) Math.max(1, width) - 0.5f) * 0.015f;
        float py = (mouseY / (float) Math.max(1, height) - 0.5f) * 0.015f;
        identity.scale(1.03f, 1.03f, 1.0f);
        identity.translate(-px, py, 0f);

        shader.setMat4("uMVP", identity);
        shader.setInt("uTex", 0);
        shader.setFloat("uSaturate", 0.90f);

        GLUtil.activeTexture(GL13.GL_TEXTURE0);
        GLUtil.bindTexture(textureId);
        GLUtil.bindVertexArray(vao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GLUtil.bindVertexArray(0);
    }
}
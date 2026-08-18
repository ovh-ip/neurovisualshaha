package dev.testvisuals.menu;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public final class Textures {

    private Textures() {
    }

    public static int createRadialGlow(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        float half = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - half) / half;
                float dy = (y - half) / half;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float alpha = 1f - d;
                alpha *= alpha;
                int a = (int) (Math.max(0f, alpha) * 255f);
                image.setRGB(x, y, (a << 24) | 0xFFFFFF);
            }
        }
        int[] pixels = image.getRGB(0, 0, size, size, null, 0, size);
        ByteBuffer data = BufferUtils.createByteBuffer(size * size * 4);
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
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, size, size, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
        return texture;
    }
}
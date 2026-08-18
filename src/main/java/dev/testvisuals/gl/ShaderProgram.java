package dev.testvisuals.gl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

public final class ShaderProgram {

    private final int programId;
    private final Map<String, Integer> uniformLocations = new HashMap<>();
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    private ShaderProgram(int programId) {
        this.programId = programId;
    }

    public static ShaderProgram load(String vertexPath, String fragmentPath, String[] attributeNames) {
        int vertex = compile(GL20.GL_VERTEX_SHADER, readResource(vertexPath));
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, readResource(fragmentPath));
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertex);
        GL20.glAttachShader(program, fragment);
        if (attributeNames != null) {
            for (int i = 0; i < attributeNames.length; i++) {
                if (attributeNames[i] != null && !attributeNames[i].isEmpty()) {
                    GL20.glBindAttribLocation(program, i, attributeNames[i]);
                }
            }
        }
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(program);
            GL20.glDeleteProgram(program);
            throw new IllegalStateException("Failed to link shader program: " + log);
        }
        GL20.glDetachShader(program, vertex);
        GL20.glDetachShader(program, fragment);
        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
        return new ShaderProgram(program);
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException("Shader compile error: " + log);
        }
        return shader;
    }

    private static String readResource(String path) {
        try (InputStream in = ShaderProgram.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read shader resource " + path, e);
        }
    }

    public void use() {
        GL20.glUseProgram(programId);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public int getProgramId() {
        return programId;
    }

    public void setInt(String name, int value) {
        int loc = location(name);
        if (loc != -1) {
            GL20.glUniform1i(loc, value);
        }
    }

    public void setFloat(String name, float value) {
        int loc = location(name);
        if (loc != -1) {
            GL20.glUniform1f(loc, value);
        }
    }

    public void setVec2(String name, float x, float y) {
        int loc = location(name);
        if (loc != -1) {
            GL20.glUniform2f(loc, x, y);
        }
    }

    public void setVec2(String name, Vector2f vec) {
        setVec2(name, vec.x, vec.y);
    }

    public void setVec3(String name, float x, float y, float z) {
        int loc = location(name);
        if (loc != -1) {
            GL20.glUniform3f(loc, x, y, z);
        }
    }

    public void setVec3(String name, Vector3f vec) {
        setVec3(name, vec.x, vec.y, vec.z);
    }

    public void setVec4(String name, float x, float y, float z, float w) {
        int loc = location(name);
        if (loc != -1) {
            GL20.glUniform4f(loc, x, y, z, w);
        }
    }

    public void setVec4(String name, Vector4f vec) {
        setVec4(name, vec.x, vec.y, vec.z, vec.w);
    }

    public void setMat3(String name, Matrix3f matrix) {
        int loc = location(name);
        if (loc != -1) {
            matrixBuffer.clear();
            matrix.get(matrixBuffer);
            GL20.glUniformMatrix3fv(loc, false, matrixBuffer);
        }
    }

    public void setMat4(String name, Matrix4f matrix) {
        int loc = location(name);
        if (loc != -1) {
            matrixBuffer.clear();
            matrix.get(matrixBuffer);
            GL20.glUniformMatrix4fv(loc, false, matrixBuffer);
        }
    }

    private int location(String name) {
        Integer loc = uniformLocations.get(name);
        if (loc == null) {
            loc = GL20.glGetUniformLocation(programId, name);
            uniformLocations.put(name, loc);
        }
        return loc;
    }

    public void delete() {
        GL20.glDeleteProgram(programId);
    }
}
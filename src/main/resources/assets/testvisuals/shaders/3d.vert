#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aUV;

uniform mat4 uMVP;
uniform mat4 uView;

out vec3 vNormal;
out vec4 vColor;
out vec3 vViewPos;
out vec2 vUV;

void main() {
    gl_Position = uMVP * vec4(aPos, 1.0);
    vNormal = mat3(uView) * aNormal;
    vViewPos = (uView * vec4(aPos, 1.0)).xyz;
    vColor = aColor;
    vUV = aUV;
}
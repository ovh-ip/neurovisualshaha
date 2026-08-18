#version 330 core

uniform sampler2D uTex;

in vec2 vUV;
in vec4 vColor;

out vec4 fragColor;

void main() {
    vec4 tex = texture(uTex, vUV);
    fragColor = tex * vColor;
}
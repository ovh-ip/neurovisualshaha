#version 330 core

uniform sampler2D uTex;
uniform float uSaturate;

in vec2 vUV;
in vec4 vColor;

out vec4 fragColor;

void main() {
    vec4 tex = texture(uTex, vUV);
    vec4 c = tex * vColor;
    float l = dot(c.rgb, vec3(0.2126, 0.7152, 0.0722));
    fragColor = vec4(mix(vec3(l), c.rgb, uSaturate), c.a);
}
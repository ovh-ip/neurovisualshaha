#version 330 core

in vec3 vNormal;
in vec4 vColor;
in vec3 vViewPos;
in vec2 vUV;

out vec4 fragColor;

void main() {
    float nlen = length(vNormal);
    vec3 base = vColor.rgb;

    // If no normal provided (e.g. lines, glowing particles, flat shapes)
    if (nlen < 0.001) {
        fragColor = vColor;
        return;
    }

    vec3 N = normalize(vNormal);
    vec3 V = normalize(-vViewPos);

    // Primary directional key light
    vec3 L1 = normalize(vec3(0.5, 0.8, 0.6));
    float diff1 = max(dot(N, L1), 0.0);
    vec3 H1 = normalize(L1 + V);
    float spec1 = pow(max(dot(N, H1), 0.0), 32.0);

    // Secondary fill light (ambient cool tone)
    vec3 L2 = normalize(vec3(-0.6, -0.3, -0.4));
    float diff2 = max(dot(N, L2), 0.0) * 0.35;

    // Ambient baseline
    float ambient = 0.28;

    // Combine diffuse & ambient
    vec3 litColor = base * (ambient + 0.72 * diff1 + diff2);

    // Specular highlight
    litColor += vec3(1.0, 1.0, 1.0) * spec1 * 0.45;

    // Fresnel rim glow
    float NdotV = max(dot(N, V), 0.0);
    float rim = pow(1.0 - NdotV, 2.5);
    litColor += mix(vec3(0.3, 0.7, 1.0), vec3(0.7, 0.4, 1.0), rim) * rim * 0.45;

    fragColor = vec4(litColor, vColor.a);
}
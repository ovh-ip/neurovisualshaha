#version 330 core

uniform vec2 u_res;
uniform float u_time;
uniform vec2 u_mouse;

in vec2 vUV;
out vec4 fragColor;

// Fast hash
float hash21(vec2 p) {
    p = fract(p * vec2(234.34, 435.345));
    p += dot(p, p + 34.23);
    return fract(p.x * p.y);
}

// 2D Noise
float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash21(i), hash21(i + vec2(1.0, 0.0)), u.x),
               mix(hash21(i + vec2(0.0, 1.0)), hash21(i + vec2(1.0, 1.0)), u.x), u.y);
}

// Fast FBM
float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    mat2 rot = mat2(0.8, 0.6, -0.6, 0.8);
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p = rot * p * 2.02 + vec2(1.7, 9.2);
        a *= 0.5;
    }
    return v;
}

void main() {
    vec2 p = (gl_FragCoord.xy - 0.5 * u_res) / u_res.y;
    vec2 mouseOffset = (u_mouse / u_res - 0.5) * 0.15;
    p -= mouseOffset;

    float t = u_time * 0.35;

    // Base cosmic color gradient
    vec3 col = mix(vec3(0.008, 0.012, 0.028), vec3(0.015, 0.008, 0.035), clamp(p.y + 0.5, 0.0, 1.0));

    // Twinkling stars
    vec2 starGrid = gl_FragCoord.xy / 48.0;
    vec2 starCell = floor(starGrid);
    float starRnd = hash21(starCell);
    if (starRnd > 0.82) {
        vec2 starPos = starCell + vec2(hash21(starCell + 0.1), hash21(starCell + 0.2));
        float d = length(starGrid - starPos);
        float twinkle = 0.5 + 0.5 * sin(t * 3.0 + starRnd * 6.28);
        float starIntensity = smoothstep(0.12, 0.0, d) * twinkle * (0.4 + 0.6 * starRnd);
        vec3 starColor = mix(vec3(0.6, 0.8, 1.0), vec3(0.9, 0.7, 1.0), starRnd);
        col += starColor * starIntensity * 0.85;
    }

    // Dynamic Aurora & Nebula waves
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        vec2 waveP = vec2(p.x * (1.2 + fi * 0.3) + t * 0.08, p.y * (0.8 + fi * 0.2));
        float w = fbm(waveP + vec2(fi * 3.1, t * 0.05));
        float waveY = 0.25 + 0.2 * sin(t * 0.4 + fi * 2.1 + p.x * 1.5) + (w - 0.5) * 0.35;
        float band = exp(-pow(abs(p.y - waveY) * (4.5 + fi * 1.2), 1.6));

        vec3 auroraCol1 = vec3(0.12, 0.55, 0.95); // Azure cyan
        vec3 auroraCol2 = vec3(0.65, 0.25, 0.95); // Neon purple
        vec3 auroraCol3 = vec3(0.15, 0.85, 0.75); // Mint emerald
        vec3 c = mix(auroraCol1, auroraCol2, 0.5 + 0.5 * sin(t * 0.3 + fi * 1.5 + p.x));
        c = mix(c, auroraCol3, w * 0.5);

        col += c * band * (0.35 + 0.25 * w);
    }

    // Horizon glow
    float horizon = exp(-abs(p.y + 0.45) * 6.0) * 0.22;
    col += vec3(0.2, 0.6, 1.0) * horizon;

    // Perspective neon grid floor
    if (p.y < -0.35) {
        float depth = 1.0 / abs(p.y + 0.35);
        vec2 gridUV = vec2(p.x * depth * 0.8, depth + t * 0.6);
        vec2 grid = abs(fract(gridUV - 0.5) - 0.5) / fwidth(gridUV);
        float line = min(grid.x, grid.y);
        float gridAlpha = 1.0 - min(line, 1.0);
        float fade = exp(-depth * 0.25);
        col += vec3(0.2, 0.5, 0.9) * gridAlpha * fade * 0.3;
    }

    // Vignette
    float vig = 1.0 - smoothstep(0.4, 1.4, length(p * vec2(1.0, 1.2)));
    col *= vig;

    fragColor = vec4(col, 1.0);
}
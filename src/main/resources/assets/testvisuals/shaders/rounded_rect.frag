#version 330 core

uniform vec4 uRect;           // x, y, width, height
uniform vec4 uRadius;         // top-left, top-right, bottom-right, bottom-left
uniform vec4 uColorTL;
uniform vec4 uColorTR;
uniform vec4 uColorBR;
uniform vec4 uColorBL;
uniform vec4 uBorderColor;
uniform float uBorderWidth;
uniform float uSoftness;      // typically 1.0 for standard AA, or larger for glow/shadow
uniform float uShadow;        // 1.0 for shadow/glow rendering, 0.0 for normal
uniform vec4 uShadowColor;

in vec2 vUV;
in vec4 vColor;

out vec4 fragColor;

float sdRoundBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + vec2(r);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec2 size = uRect.zw;
    vec2 localPos = vUV * size;
    vec2 center = size * 0.5;
    vec2 p = localPos - center;

    // Pick corner radius based on quadrant
    float r = (p.x > 0.0)
        ? ((p.y > 0.0) ? uRadius.z : uRadius.y)
        : ((p.y > 0.0) ? uRadius.w : uRadius.x);
    r = min(r, min(size.x, size.y) * 0.5);

    float d = sdRoundBox(p, center, r);
    float softness = max(0.5, uSoftness);

    if (uShadow > 0.5) {
        float shadowFactor = 1.0 - smoothstep(-softness, softness * 2.0, d);
        fragColor = vec4(uShadowColor.rgb, uShadowColor.a * shadowFactor);
        return;
    }

    // Bilinear gradient interpolation
    vec2 uv = clamp(localPos / max(size, vec2(1.0)), 0.0, 1.0);
    vec4 topCol = mix(uColorTL, uColorTR, uv.x);
    vec4 btmCol = mix(uColorBL, uColorBR, uv.x);
    vec4 fillCol = mix(topCol, btmCol, uv.y) * vColor;

    float innerAlpha = 1.0 - smoothstep(-softness, 0.0, d);

    if (uBorderWidth > 0.0) {
        float innerD = d + uBorderWidth;
        float borderAlpha = (1.0 - smoothstep(-softness, 0.0, d)) * smoothstep(-softness, 0.0, innerD);
        vec4 border = uBorderColor;
        fragColor = mix(fillCol * (1.0 - borderAlpha) * innerAlpha, border, borderAlpha * border.a);
    } else {
        fragColor = fillCol * innerAlpha;
    }
}

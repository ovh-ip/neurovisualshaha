#version 330 core

uniform vec2 uCenter;
uniform float uRadius;
uniform float uInnerRadius;
uniform vec2 uAngles; // startAngle, endAngle in radians
uniform vec4 uColor1;
uniform vec4 uColor2;
uniform float uSoftness;

in vec2 vUV;
in vec4 vColor;

out vec4 fragColor;

void main() {
    vec2 p = (vUV - vec2(0.5)) * (uRadius * 2.0);
    float dist = length(p);
    float softness = max(0.5, uSoftness);

    // Outer edge AA
    float outerAlpha = 1.0 - smoothstep(uRadius - softness, uRadius, dist);
    
    // Inner edge AA (for rings)
    float innerAlpha = 1.0;
    if (uInnerRadius > 0.0) {
        innerAlpha = smoothstep(uInnerRadius - softness, uInnerRadius, dist);
    }

    // Arc angular check
    float angleAlpha = 1.0;
    if (uAngles.y - uAngles.x < 6.28) {
        float angle = atan(p.y, p.x);
        if (angle < 0.0) angle += 6.2831853;
        float aStart = uAngles.x;
        float aEnd = uAngles.y;
        if (angle < aStart || angle > aEnd) {
            angleAlpha = 0.0;
        }
    }

    float totalAlpha = outerAlpha * innerAlpha * angleAlpha;
    float t = clamp(dist / max(uRadius, 0.001), 0.0, 1.0);
    vec4 col = mix(uColor1, uColor2, t) * vColor;
    fragColor = vec4(col.rgb, col.a * totalAlpha);
}

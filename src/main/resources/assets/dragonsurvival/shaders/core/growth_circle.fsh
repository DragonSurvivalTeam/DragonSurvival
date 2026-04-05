#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

in vec2 texCoord0;

out vec4 fragColor;

#define DISTORTION_STRENGTH 0.05
#define DISTORTION_SPEED 0.5
#define OUTLINE_THRESHOLD 0.05
#define PI 3.14159265359

vec4 growthCircleParam(int row) {
    return texelFetch(Sampler2, ivec2(0, row), 0);
}

float sdNGon(vec2 position, float radius, int sideCount) {
    float angle = (2.0 * PI) / float(sideCount);
    float halfExtent = radius * tan(0.5 * angle);
    float sector = angle * floor((atan(position.y, position.x) + 0.5 * angle) / angle);
    vec2 rotation = vec2(cos(sector), sin(sector));

    position = mat2(rotation.x, -rotation.y, rotation.y, rotation.x) * position;

    return length(position - vec2(radius, clamp(position.y, -halfExtent, halfExtent))) * sign(position.x - radius);
}

float clampRadians(float angle) {
    return mod(angle + 2.0 * PI, 2.0 * PI);
}

vec2 pixelate(vec2 uv, float pixelSize) {
    return floor((uv / pixelSize) * 100.0) * (pixelSize / 100.0);
}

void main() {
    vec4 innerColor = growthCircleParam(0);
    vec4 outlineColor = growthCircleParam(1);
    vec4 addColor = growthCircleParam(2);
    vec4 subtractColor = growthCircleParam(3);
    vec4 info = growthCircleParam(4);
    int sideCount = max(int(round(growthCircleParam(5).r * 255.0)), 3);
    float animationTime = growthCircleParam(6).r * 64.0;
    vec4 shaderTint = growthCircleParam(7);
    float lineWidth = info.r;
    float fillPercent = clamp(info.g, 0.0, 1.0);
    float targetFillPercent = clamp(info.b, 0.0, 1.0);
    float safeFillPercent = max(fillPercent, 0.0001);
    float distortionSpeed = DISTORTION_SPEED;
    vec2 pixelatedTexCoord = pixelate(texCoord0, 4.0);
    float angle = atan(pixelatedTexCoord.y - 0.5, pixelatedTexCoord.x - 0.5) - PI / 2.0;
    float clampedAngle = clampRadians(angle);
    float sampledPercent = clampedAngle / (2.0 * PI);
    vec4 borderColor = vec4(0.25);
    float targetPercentDiff = targetFillPercent - sampledPercent;
    float percentDiff = fillPercent - sampledPercent;
    float gradient = 1.0 - (fillPercent - sampledPercent) / safeFillPercent;

    if (percentDiff > 0.0) {
        if (targetPercentDiff < 0.0) {
            borderColor = subtractColor;
        } else {
            borderColor = texture(Sampler1, vec2(gradient, 0.5));
            distortionSpeed *= 2.0;
        }
    } else if (targetPercentDiff > 0.0) {
        borderColor = addColor;
    }

    vec2 distortedTexCoord = pixelatedTexCoord;
    distortedTexCoord.x += DISTORTION_STRENGTH * sin(animationTime * distortionSpeed + pixelatedTexCoord.y * 10.0)
        + DISTORTION_STRENGTH * cos(animationTime * distortionSpeed * 0.2 + pixelatedTexCoord.x * 10.0);
    distortedTexCoord.y += DISTORTION_STRENGTH * cos(animationTime * distortionSpeed + pixelatedTexCoord.x * 10.0)
        + DISTORTION_STRENGTH * sin(animationTime * distortionSpeed * 0.2 + pixelatedTexCoord.y * 10.0);

    vec4 texColor = texture(Sampler0, distortedTexCoord) / 2.0 + 0.5;
    texColor *= borderColor;

    float innerRadius = 0.43 - clamp(lineWidth, 0.0, 0.43);
    float signedDistanceInner = sdNGon(pixelatedTexCoord - vec2(0.5, 0.5), innerRadius, sideCount);
    float signedDistanceOuter = sdNGon(pixelatedTexCoord - vec2(0.5, 0.5), 0.43, sideCount);
    float border = (signedDistanceInner > 0.0 && signedDistanceOuter < 0.0) ? 1.0 : 0.0;
    float inside = signedDistanceInner <= 0.0 ? 1.0 : 0.0;
    float outline = (signedDistanceOuter > -OUTLINE_THRESHOLD && signedDistanceOuter < 0.0) ? 1.0 : 0.0;

    vec4 finalColor = mix(mix(vec4(texColor.rgb, border), innerColor, inside), outlineColor, outline);
    if (finalColor.a == 0.0) {
        discard;
    }

    fragColor = finalColor * shaderTint * ColorModulator;
}

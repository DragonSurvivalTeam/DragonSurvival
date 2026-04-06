#version 330

uniform sampler2D ArmorTexture;
uniform sampler2D MaskTexture;
uniform sampler2D TrimTexture;

layout(std140) uniform ArmorGenerationInfo {
    float HasMask;
    float ApplyDye;
    float HasTrim;
    float DyeHue;
    float DyeSaturation;
    float TrimHue;
    float TrimSaturation;
} armorGenerationInfo;

in vec2 texCoord;

out vec4 fragColor;

#moj_import <minecraft:hsb.glsl>

void main() {
    vec4 armorPixel = texture(ArmorTexture, texCoord);

    if (armorPixel.a == 0.0) {
        fragColor = vec4(0.0);
        return;
    }

    if (armorGenerationInfo.HasMask > 0.5 && texture(MaskTexture, texCoord).a == 0.0) {
        fragColor = vec4(0.0);
        return;
    }

    if (armorGenerationInfo.HasTrim > 0.5) {
        vec4 trimPixel = texture(TrimTexture, texCoord);

        if (trimPixel.a != 0.0) {
            vec3 trimHSB = getHSB(trimPixel.rgb);

            if (trimHSB.g == 0.0) {
                fragColor = vec4(getRGB(vec3(armorGenerationInfo.TrimHue, armorGenerationInfo.TrimSaturation, trimHSB.b)), 1.0);
            } else {
                fragColor = vec4(0.0);
            }

            return;
        }
    }

    vec3 armorHSB = getHSB(armorPixel.rgb);

    if (armorGenerationInfo.ApplyDye > 0.5 && armorHSB.b != 0.0) {
        fragColor = vec4(getRGB(vec3(armorGenerationInfo.DyeHue, armorGenerationInfo.DyeSaturation, armorHSB.b)), armorPixel.a);
        return;
    }

    fragColor = armorPixel;
}

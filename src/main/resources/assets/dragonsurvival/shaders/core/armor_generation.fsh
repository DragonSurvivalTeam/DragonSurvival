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
    vec4 TrimPalette[8];
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
            float brightness = dot(trimPixel.rgb, vec3(0.299, 0.587, 0.114));
            int paletteIndex = int(clamp(round((224.0 - brightness * 255.0) / 32.0), 0.0, 7.0));
            vec4 palettePixel = armorGenerationInfo.TrimPalette[paletteIndex];
            fragColor = vec4(palettePixel.rgb, trimPixel.a * palettePixel.a);
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

#version 150

uniform sampler2D ArmorTexture;
uniform sampler2D MaskTexture;
uniform sampler2D TrimTexture;
uniform sampler2D TrimPalette;
uniform float HasMask;
uniform float ApplyDye;
uniform float HasTrim;
uniform float DyeHue;
uniform float DyeSaturation;

in vec2 texCoord;

out vec4 fragColor;

#moj_import <hsb.glsl>

void main() {
    vec4 armorPixel = texture(ArmorTexture, texCoord);

    if (armorPixel.a == 0.0) {
        fragColor = vec4(0.0);
        return;
    }

    if (HasMask > 0.5 && texture(MaskTexture, texCoord).a == 0.0) {
        fragColor = vec4(0.0);
        return;
    }

    if (HasTrim > 0.5) {
        vec4 trimPixel = texture(TrimTexture, texCoord);

        if (trimPixel.a != 0.0) {
            float brightness = dot(trimPixel.rgb, vec3(0.299, 0.587, 0.114));
            int paletteIndex = int(clamp(round((224.0 - brightness * 255.0) / 32.0), 0.0, 7.0));
            vec4 palettePixel = texelFetch(TrimPalette, ivec2(paletteIndex, 0), 0);
            fragColor = vec4(palettePixel.rgb, trimPixel.a * palettePixel.a);
            return;
        }
    }

    vec3 armorHSB = getHSB(armorPixel.rgb);

    if (ApplyDye > 0.5 && armorHSB.b != 0.0) {
        fragColor = vec4(getRGB(vec3(DyeHue, DyeSaturation, armorHSB.b)), armorPixel.a);
        return;
    }

    fragColor = armorPixel;
}

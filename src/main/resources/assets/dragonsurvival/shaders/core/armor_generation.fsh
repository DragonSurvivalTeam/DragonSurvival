#version 150

uniform sampler2D ArmorTexture;
uniform sampler2D MaskTexture;
uniform sampler2D TrimTexture;
uniform float HasMask;
uniform float ApplyDye;
uniform float HasTrim;
uniform float DyeHue;
uniform float DyeSaturation;
uniform float TrimHue;
uniform float TrimSaturation;

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
            vec3 trimHSB = getHSB(trimPixel.rgb);

            if (trimHSB.g == 0.0) {
                fragColor = vec4(getRGB(vec3(TrimHue, TrimSaturation, trimHSB.b)), 1.0);
            } else {
                fragColor = vec4(0.0);
            }

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

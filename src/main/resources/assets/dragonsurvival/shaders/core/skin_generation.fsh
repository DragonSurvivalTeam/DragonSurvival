#version 330

uniform sampler2D SkinTexture;

layout(std140) uniform SkinGenerationInfo {
    float HueVal;
    float SatVal;
    float BrightVal;
    float Colorable;
    float Glowing;
} skinGenerationInfo;

in vec2 texCoord;

out vec4 fragColor;

#moj_import <minecraft:hsb.glsl>

vec3 getHueAdjustedColor(vec4 texColor) {
    if (texColor.a == 0.0 || skinGenerationInfo.Colorable < 0.5) {
        return texColor.rgb;
    }

    vec3 hsb = getHSB(texColor.rgb);

    if (skinGenerationInfo.Glowing > 0.5 && hsb.r == 0.5 && hsb.g == 0.5) {
        return texColor.rgb;
    }

    hsb.r += skinGenerationInfo.HueVal;
    hsb.g = mix(hsb.g, skinGenerationInfo.SatVal > 0.5 ? 1.0 : 0.0, abs(skinGenerationInfo.SatVal - 0.5) * 2.0);
    hsb.b = mix(hsb.b, skinGenerationInfo.BrightVal > 0.5 ? 1.0 : 0.0, abs(skinGenerationInfo.BrightVal - 0.5) * 2.0);
    return getRGB(hsb);
}

void main() {
    vec4 texColor = texture(SkinTexture, texCoord);
    fragColor = vec4(getHueAdjustedColor(texColor), texColor.a);
}

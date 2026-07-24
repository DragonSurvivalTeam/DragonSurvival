#version 150

uniform sampler2D SkinTexture;
uniform float HueVal;
uniform float SatVal;
uniform float BrightVal;
uniform float Colorable;
uniform float Glowing;

in vec2 texCoord;

out vec4 fragColor;

#moj_import <hsb.glsl>

vec3 getHueAdjustedColor(vec4 texColor) {
    if(texColor.a == 0.0 || Colorable < 0.5) {
        return texColor.rgb;
    }

    vec3 hsb = getHSB(texColor.rgb);

    if(Glowing > 0.5 && hsb.r == 0.5 && hsb.g == 0.5){
        return texColor.rgb;
    }

    hsb.r = hsb.r + HueVal;
    hsb.g = mix(hsb.g, SatVal > 0.5 ? 1.0 : 0.0, abs(SatVal - 0.5) * 2.0);
    hsb.b = mix(hsb.b, BrightVal > 0.5 ? 1.0 : 0.0, abs(BrightVal - 0.5) * 2.0);

    return getRGB(hsb);
}

void main() {
    vec4 texColor = texture(SkinTexture, texCoord);
    fragColor = vec4(getHueAdjustedColor(texColor), texColor.a);
}

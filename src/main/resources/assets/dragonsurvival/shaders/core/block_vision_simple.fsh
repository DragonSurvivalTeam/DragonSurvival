#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0; // block texture atlas

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 baseSample = texture(Sampler0, texCoord0);

    if (baseSample.a < 0.1) {
        discard;
    }

    // Only the shape of the texture is used, the color is a flat shade on top of the block
    vec4 color = vertexColor * ColorModulator;
    fragColor = vec4(color.rgb, color.a * baseSample.a);
}

#version 150

in vec2 texCoord;
in vec4 vertColor;

uniform sampler2D Sampler0; // block texture atlas

out vec4 fragColor;

void main() {
    vec4 baseSample = texture(Sampler0, texCoord);

    if (baseSample.a < 0.1) {
        discard;
    }

    fragColor = vec4(vertColor.rgb, vertColor.a * baseSample.a);
}

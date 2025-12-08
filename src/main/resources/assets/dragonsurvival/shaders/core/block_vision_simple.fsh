#version 150

in vec2 texCoord;
in vec4 vertColor;

uniform sampler2D Sampler0; // block texture atlas

out vec4 fragColor;

void main() {
    // Sample alpha from block atlas to match cutout silhouettes
    vec4 baseSample = texture(Sampler0, texCoord, -8.0);

    if (baseSample.a < 0.1) {
        discard;
    }

    // Use per-vertex color; modulate alpha by atlas alpha
    fragColor = vec4(vertColor.rgb, vertColor.a * baseSample.a);
}

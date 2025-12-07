#version 150

// Vertex shader for the block vision treasure aura (pixelated sparkles)
// Matches DefaultVertexFormat.POSITION_TEX

in vec3 Position;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord;

void main() {
    texCoord = UV0;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}

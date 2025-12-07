#version 150

// Vertex shader for the block vision treasure aura (pixelated sparkles)
// Matches DefaultVertexFormat.POSITION_TEX

in vec3 Position;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float ZBias; // tiny view-space depth bias to avoid z-fighting without visible offset

out vec2 texCoord;

void main() {
    texCoord = UV0;
    vec4 clip = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // Nudge slightly toward the camera in clip space to reduce z-fighting while staying visually coplanar
    clip.z -= ZBias * clip.w;
    gl_Position = clip;
}

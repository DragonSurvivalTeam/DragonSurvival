#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vColor;
out vec2 vUV;

void main() {
    vColor = Color;
    vUV = UV0;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}

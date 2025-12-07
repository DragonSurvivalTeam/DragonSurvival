#version 150

// Pixelated treasure aura — small popping sparkles
// Matches DefaultVertexFormat.POSITION_TEX and mirrors the structure of growth_circle.

// Inputs
in vec2 texCoord;        // face-local UV in [0,1]

// Uniforms
uniform float Time;      // animation time in seconds
uniform float BlockSeed; // per-block phase/variation
uniform vec3 OreColor;   // ore RGB color (from visions.getColor)
uniform sampler2D Sampler0; // bound block texture atlas (unit 0)

// Standard pipeline outputs
out vec4 fragColor;

// Tunable constants kept inside the shader (pack-friendly and minimal Java uniforms)
// Keep the pixel grid aligned with vanilla texels (16x16 per block face) to avoid "mixels".
const float PIXEL_GRID = 16.0;                     // vanilla pixel grid alignment
const float BASE_ALPHA = 0.50;                     // overall transparency multiplier
const float COVERAGE_FLOOR = 0.35;                 // minimum coverage to avoid empty patches
const float EDGE_NEIGHBOR_FEATHER = 0.6 / PIXEL_GRID; // neighbor stitch width near cell borders
// Make the effect less busy by lowering density and lengthening each sparkle slightly.
const float SPARKLE_DENSITY = 0.15;                // fraction of cells that can sparkle (0..1)
const float SPARKLE_LIFE = 1.80;                   // seconds per sparkle cycle
const float EDGE_FEATHER = 0.06;                   // softening near edges inside the cell (0..~0.25)
const float SPARKLE_BRIGHTNESS = 0.65;             // brightness scaling for sparkle contribution
// Temporal bucketing to prevent too many cells popping at once
const float ACTIVE_BUCKETS = 4.0;                  // 4 alternating groups
const float BUCKET_SPEED = 0.35;                   // how fast we cycle buckets

// Hash utilities for stable cell randomness
float hash12(vec2 p) {
    // Stable 2D -> 1D hash in 0..1; incorporate BlockSeed
    vec3 p3 = fract(vec3(p.x, p.y, p.x + p.y) * 0.1031 + BlockSeed);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec2 hash22(vec2 p) {
    // Two-component hash in 0..1 for per-cell variation
    float n = sin(dot(p, vec2(127.1, 311.7)) + BlockSeed * 31.7);
    float s = sin(dot(p, vec2(269.5, 183.3)) + BlockSeed * 17.3);
    return fract(vec2(n, s) * 43758.5453);
}

// Soft box: returns 1.0 inside a centered rectangle and fades out at the edges
float softBox(vec2 uv, vec2 center, vec2 halfSize, float feather) {
    vec2 d = abs(uv - center) - halfSize;
    // Negative d means inside; apply smoothstep across the feather band
    vec2 edge = 1.0 - smoothstep(0.0, feather, d);
    return clamp(min(edge.x, edge.y), 0.0, 1.0);
}

// Compute raw sparkle alpha (without coverage floor) for a specific cell index and local cell UV
float sparkleAlphaRaw(vec2 cellIndex, vec2 cellUV) {
    // Decide if this cell participates
    float participation = step(1.0 - SPARKLE_DENSITY, hash12(cellIndex + 13.79));

    // Smooth activity gate per cell:
    // Instead of discrete buckets (which can end a sparkle abruptly at high intensity),
    // use a per-cell phased pulse that rises and falls smoothly over time.
    float gatePhase = fract(Time * BUCKET_SPEED + hash12(cellIndex + 5.123) + BlockSeed * 0.13);
    float gateIn  = smoothstep(0.00, 0.20, gatePhase);
    float gateOut = 1.0 - smoothstep(0.80, 1.00, gatePhase);
    float gate    = gateIn * gateOut; // bell-shaped 0..1..0 window
    participation *= gate;

    // Per-cell timing: randomized phase to desynchronize sparkles across the face
    float phaseJitter = hash12(cellIndex + 91.17);
    float localTime = Time + phaseJitter * SPARKLE_LIFE + BlockSeed * 0.37;
    float cyclePhase = mod(localTime, SPARKLE_LIFE) / SPARKLE_LIFE; // 0..1

    // Sparkle intensity envelope: quick pop to peak, then fade
    float bell = 1.0 - abs(cyclePhase * 2.0 - 1.0);
    float intensityEnvelope = smoothstep(0.0, 1.0, bell);

    // Small core square size over life (in cell UV units)
    float minHalfSize = 0.03;  // tiny at birth
    float maxHalfSize = 0.12;  // still small to keep sparkle feel
    float coreHalf = mix(minHalfSize, maxHalfSize, intensityEnvelope);
    float coreAlpha = softBox(cellUV, vec2(0.5), vec2(coreHalf), EDGE_FEATHER);

    // Cross-shaped rays: short, thin rectangles extending from the center
    vec2 rand2 = hash22(cellIndex + 7.31);
    float rayThicknessX = mix(0.03, 0.05, rand2.x); // vertical bar half-width in X
    float rayThicknessY = mix(0.03, 0.05, rand2.y); // horizontal bar half-width in Y

    float maxReach = 0.25; // slightly shorter rays for a subtler look
    float reach = maxReach * intensityEnvelope;

    // Horizontal and vertical rays
    float rayHorizontal = softBox(cellUV, vec2(0.5, 0.5), vec2(reach, rayThicknessY), EDGE_FEATHER);
    float rayVertical   = softBox(cellUV, vec2(0.5, 0.5), vec2(rayThicknessX, reach), EDGE_FEATHER);

    float sparkleShape = clamp(coreAlpha + 0.9 * (rayHorizontal + rayVertical), 0.0, 1.0);
    float sparkleAlpha = SPARKLE_BRIGHTNESS * intensityEnvelope * sparkleShape;
    return participation * sparkleAlpha;
}

// Stitched sparkle for UV in 0..1, sampling neighbor cells near borders to avoid thin seams
float sparkleStitched(vec2 uv) {
    vec2 gridPos = uv * PIXEL_GRID;
    vec2 cellIndex = floor(gridPos);
    vec2 cellUV = fract(gridPos);

    float maxSparkle = sparkleAlphaRaw(cellIndex, cellUV);

    bool nearLeft   = (cellUV.x < EDGE_NEIGHBOR_FEATHER);
    bool nearRight  = (cellUV.x > 1.0 - EDGE_NEIGHBOR_FEATHER);
    bool nearBottom = (cellUV.y < EDGE_NEIGHBOR_FEATHER);
    bool nearTop    = (cellUV.y > 1.0 - EDGE_NEIGHBOR_FEATHER);

    if (nearLeft)  maxSparkle = max(maxSparkle, sparkleAlphaRaw(cellIndex + vec2(-1.0, 0.0), cellUV + vec2(1.0, 0.0)));
    if (nearRight) maxSparkle = max(maxSparkle, sparkleAlphaRaw(cellIndex + vec2( 1.0, 0.0), cellUV + vec2(-1.0, 0.0)));
    if (nearBottom)maxSparkle = max(maxSparkle, sparkleAlphaRaw(cellIndex + vec2(0.0, -1.0), cellUV + vec2(0.0, 1.0)));
    if (nearTop)   maxSparkle = max(maxSparkle, sparkleAlphaRaw(cellIndex + vec2(0.0,  1.0), cellUV + vec2(0.0, -1.0)));

    if (nearLeft && nearBottom)
        maxSparkle = max(maxSparkle, sparkleAlphaRaw(cellIndex + vec2(-1.0, -1.0), cellUV + vec2(1.0, 1.0)));
    if (nearLeft && nearTop)
        maxSparkle = max(maxSparkle, sparkleAlphaRaw(cellIndex + vec2(-1.0,  1.0), cellUV + vec2(1.0, -1.0)));
    if (nearRight && nearBottom)
        maxSparkle = max(maxSparkle, sparkleAlphaRaw(cellIndex + vec2( 1.0, -1.0), cellUV + vec2(-1.0, 1.0)));
    if (nearRight && nearTop)
        maxSparkle = max(maxSparkle, sparkleAlphaRaw(cellIndex + vec2( 1.0,  1.0), cellUV + vec2(-1.0, -1.0)));

    return maxSparkle;
}

void main() {
    // Sample block atlas alpha for masking to the real block texels
    // Use explicit LOD 0 to avoid mipmap alpha bleeding that can make cutout quads
    // appear to fill as distance increases (common on cross-plant textures)
    vec4 baseSample = textureLod(Sampler0, texCoord, 0.0);

    // Early discard for cutout-style blocks to match vanilla silhouettes
    if (baseSample.a < 0.1) {
        discard;
    }

    // Compute stitched sparkle in current face UV space (UVs provided from baked quad)
    float combinedSparkle = sparkleStitched(texCoord);

    // Minimum coverage so the face is never empty
    float combined = COVERAGE_FLOOR + combinedSparkle * (1.0 - COVERAGE_FLOOR);
    combined = clamp(combined, 0.0, 1.0);

    // Final color: base ore RGB, animated alpha
    float finalAlpha = BASE_ALPHA * combined * baseSample.a;
    fragColor = vec4(OreColor, finalAlpha);
}

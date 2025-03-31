#version 450 core

uniform sampler2D u_CurrentSampler;
uniform sampler2D u_PrevSampler;

in vec2 texCoord;

uniform float u_Strength = 5.0f;

out vec4 fragColor;

void main() {
    vec4 currentTexel = texture(u_CurrentSampler, texCoord);
    vec4 prevTexel = texture(u_PrevSampler, texCoord);

    float factor = u_Strength / 10.0f;

    fragColor = vec4(mix(prevTexel.rgb, currentTexel.rgb, factor), 1.0);
}
#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MaskSampler;
uniform vec4 ColorModulator;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 baseColor = texture(DiffuseSampler, texCoord);
    vec4 maskColor = texture(MaskSampler, texCoord);
    
    // Use the red channel of the mask as alpha
    fragColor = baseColor * ColorModulator;
    fragColor.a *= maskColor.r;
}

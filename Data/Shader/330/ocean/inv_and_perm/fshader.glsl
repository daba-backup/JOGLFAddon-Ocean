#version 330

uniform int N;

uniform sampler2D input_texture;

layout(location=0) out vec4 heightmap;

float GetTexelR(ivec2 x){
    vec2 uv=(vec2(x)+0.5)/float(N);
    return texture(input_texture,uv).r;
}

void main(){
    ivec2 x=ivec2(gl_FragCoord.xy);

    float perms[]={1.0,-1.0};
    int index=int(mod(x.x+x.y,2));
    float perm=perms[index];

    float h=GetTexelR(x)*perm/float(N*N);
    heightmap=vec4(h,h,h,1.0);
}

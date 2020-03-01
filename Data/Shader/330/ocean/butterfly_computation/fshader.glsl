#version 330

uniform int N;
uniform int total_stage_num;//=log2(N)

uniform int stage;
uniform int direction;

uniform sampler2D butterfly_texture;
uniform sampler2D pingpong_in;

layout(location=0) out vec4 pingpong_out;

struct complex{
    float re;
    float im;
};

complex mul(complex c0,complex c1){
    complex c;

    c.re=c0.re*c1.re-c0.im*c1.im;
    c.im=c0.re*c1.im+c0.im*c1.re;

    return c;
}
complex add(complex c0,complex c1){
    complex c;

    c.re=c0.re+c1.re;
    c.im=c0.im+c1.im;

    return c;
}

vec4 GetTexelFromButterflyTexture(ivec2 p){
    vec2 uv;
    uv.x=(float(p.x)+0.5)/total_stage_num;
    uv.y=(float(p.y)+0.5)/N;

    return texture(butterfly_texture,uv);
}
vec4 GetTexelFromPingpongTexture(ivec2 p){
    vec2 uv;
    uv.x=(float(p.x)+0.5)/N;
    uv.y=(float(p.y)+0.5)/N;

    return texture(pingpong_in,uv);
}

vec4 HorizontalButterflies(){
    complex H;
    ivec2 x=ivec2(gl_FragCoord.xy);

    //Get the info for the current sample from the butterfly texture.
    vec4 data=GetTexelFromButterflyTexture(ivec2(stage,x.x));
    //Get the values of the top butterfly.
    vec2 p_=GetTexelFromPingpongTexture(ivec2(data.z,x.y)).rg;
    //Get the values of the bottom butterfly.
    vec2 q_=GetTexelFromPingpongTexture(ivec2(data.w,x.y)).rg;
    //Twiddle factor
    vec2 w_=vec2(data.x,data.y);

    complex p=complex(p_.x,p_.y);
    complex q=complex(q_.x,q_.y);
    complex w=complex(w_.x,w_.y);

    //(top butterfly)=(top_butterfly)+(twiddle factor)*(bottom_butterfly)
    H=add(p,mul(w,q));

    return vec4(H.re,H.im,0.0,1.0);
}
vec4 VerticalButterflies(){
    complex H;
    ivec2 x=ivec2(gl_FragCoord.xy);

    //Get the info for the current sample from the butterfly texture.
    vec4 data=GetTexelFromButterflyTexture(ivec2(stage,x.y));
    //Get the values of the top butterfly.
    vec2 p_=GetTexelFromPingpongTexture(ivec2(x.x,data.z)).rg;
    //Get the values of the bottom butterfly.
    vec2 q_=GetTexelFromPingpongTexture(ivec2(x.x,data.w)).rg;
    //Twiddle factor
    vec2 w_=vec2(data.x,data.y);

    complex p=complex(p_.x,p_.y);
    complex q=complex(q_.x,q_.y);
    complex w=complex(w_.x,w_.y);

    //(top butterfly)=(top_butterfly)+(twiddle factor)*(bottom_butterfly)
    H=add(p,mul(w,q));

    return vec4(H.re,H.im,0.0,1.0);
}

void main(){
    if(direction==0){
        pingpong_out=HorizontalButterflies();
    }
    else{
        pingpong_out=VerticalButterflies();
    }
}

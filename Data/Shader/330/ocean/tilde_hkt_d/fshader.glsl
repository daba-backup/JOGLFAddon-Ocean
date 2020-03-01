#version 330

uniform sampler2D tilde_h0k;
uniform sampler2D tilde_h0minusk;

uniform int N;
uniform int L;
uniform float t;

const float M_PI=3.1415926536;
const float g=9.81;//Gravitational constant

layout(location=0) out vec4 tilde_hkt_d;

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
complex conj(complex c){
    complex c_conj=complex(c.re,-c.im);
    return c_conj;
}

vec4 GetTexel(sampler2D tex,ivec2 x){
    vec2 uv=(vec2(x)+0.5)/float(N);
    return texture(tex,uv);
}

void main(){
    ivec2 ix=ivec2(gl_FragCoord.xy);
    vec2 x=gl_FragCoord.xy-float(N)/2.0;
    vec2 k=vec2(2.0*M_PI*x.x/L,2.0*M_PI*x.y/L);

    float mag=length(k);
    if(mag<0.00001)mag=0.00001;

    float w=sqrt(g*mag);

    vec2 tilde_h0k_values=GetTexel(tilde_h0k,ix).rg;
    complex fourier_cmp=complex(tilde_h0k_values.x,tilde_h0k_values.y);

    vec2 tilde_h0minusk_values=GetTexel(tilde_h0minusk,ix).rg;
    complex fourier_cmp_conj=conj(complex(tilde_h0minusk_values.x,tilde_h0minusk_values.y));

    float cos_wt=cos(w*t);
    float sin_wt=sin(w*t);

    complex exp_jwt=complex(cos_wt,sin_wt);
    complex exp_jwt_conj=complex(cos_wt,-sin_wt);

    complex hkt=add(mul(fourier_cmp,exp_jwt),mul(fourier_cmp_conj,exp_jwt_conj));

    vec2 normalized_k=normalize(k);
    tilde_hkt_d=vec4(hkt.im*normalized_k.y,-hkt.re*normalized_k.x,0.0,1.0);
}

#version 330

uniform sampler2D uniform_rnds;

uniform int N;
uniform float L;
uniform float A;
uniform float v;//Wind speed
uniform vec2 w;//Direction of the wind

layout(location=0) out vec4 tilde_h0k;
layout(location=1) out vec4 tilde_h0minusk;

const float M_PI=3.1415926536;
const float g=9.81;//Gravitational constant

vec4 GetUniformRnds(ivec2 x){
    vec2 uv=(vec2(x)+0.5)/float(N);
    return texture(uniform_rnds,uv);
}

//Box-Muller's method
vec4 GetGaussianRnds(){
    vec4 color=GetUniformRnds(ivec2(gl_FragCoord.xy));

    vec4 rnds;
    rnds.x=sqrt(-2.0*log(color.r))*cos(2.0*M_PI*color.g);
    rnds.y=sqrt(-2.0*log(color.r))*sin(2.0*M_PI*color.g);
    rnds.z=sqrt(-2.0*log(color.b))*cos(2.0*M_PI*color.a);
    rnds.w=sqrt(-2.0*log(color.b))*sin(2.0*M_PI*color.a);

    return rnds;
}

void main(){
    vec2 x=gl_FragCoord.xy-float(N)/2.0;
    vec2 k=vec2(2.0*M_PI*x.x/L,2.0*M_PI*x.y/L);

    float L_=v*v/g;
    float mag=length(k);
    if(mag<0.00001)mag=0.00001;
    float mag_sq=mag*mag;

    //h0(k)
    float temp0=exp(-1.0/(mag_sq*L_*L_));
    float temp1=pow(dot(normalize(k),normalize(w)),8.0);
    float temp2=mag_sq*mag_sq;
    float Phk=A*temp0*temp1/temp2;
    
    float h0k=(1.0/sqrt(2.0))*sqrt(Phk);

    //h0(-k)
    temp1=pow(dot(normalize(-k),normalize(w)),6.0);
    float Phminusk=A*temp0*temp1/temp2;

    float h0minusk=(1.0/sqrt(2.0))*sqrt(Phminusk);

    vec4 rnd=GetGaussianRnds();
    tilde_h0k=vec4(rnd.xy*h0k,0.0,1.0);
    tilde_h0minusk=vec4(rnd.zw*h0minusk,0.0,1.0);
}

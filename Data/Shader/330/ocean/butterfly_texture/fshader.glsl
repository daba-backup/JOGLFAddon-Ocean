#version 330

uniform int N;
uniform isampler2D bit_reversed_indices;//size:N x 1

layout(location=0) out vec4 fs_out_color;

const float M_PI=3.1415926536;

struct complex{
    float re;
    float im;
};

int GetTexelR(int index){
    float u=(float(index)+0.5)/float(N);
    int r=texture(bit_reversed_indices,vec2(u,0.5)).r;

    return r;
}

void main(){
    ivec2 x=ivec2(gl_FragCoord.xy);
    float k=mod(x.y*N/pow(2,x.x+1),N);
    complex twiddle=complex(cos(2.0*M_PI*k/float(N)),sin(2.0*M_PI*k/float(N)));

    int butterfly_span=int(pow(2,x.x));

    int butterfly_wing;
    if(mod(x.y,pow(2,x.x+1))<pow(2,x.x)){
        butterfly_wing=1;
    }
    else{
        butterfly_wing=0;
    }

    //First stage
    if(x.x==0){
        //Top butterfly wing
        if(butterfly_wing==1){
            fs_out_color=vec4(twiddle.re,twiddle.im,GetTexelR(x.y),GetTexelR(x.y+1));
        }
        //Bottom butterfly wing
        else{
            fs_out_color=vec4(twiddle.re,twiddle.im,GetTexelR(x.y-1),GetTexelR(x.y));
        }
    }
    //From the second to the log2(N)th stage
    else{
        //Top butterfly wing
        if(butterfly_wing==1){
            fs_out_color=vec4(twiddle.re,twiddle.im,x.y,x.y+butterfly_span);
        }
        //Bottom buttefly wing
        else{
            fs_out_color=vec4(twiddle.re,twiddle.im,x.y-butterfly_span,x.y);
        }
    }
}
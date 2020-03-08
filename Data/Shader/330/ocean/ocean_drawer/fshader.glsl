#version 330

uniform vec3 camera_position;
uniform vec3 camera_target;
uniform float camera_near;
uniform float camera_far;

uniform vec3 light_direction;
uniform vec4 ambient_color;
uniform float diffuse_power;
uniform float specular_power;
//Additional uniform variables
uniform vec4 water_diffuse_color;
uniform vec4 water_specular_color;
uniform float water_refractive_index;
//==========

uniform float fog_start;
uniform float fog_end;

in vec3 vs_out_position;
in vec3 vs_out_normal;

uniform vec4 fog_color;

out vec4 fs_out_color;

vec4 Lighting(){
    vec3 camera_direction=normalize(camera_target-camera_position);
    vec3 half_le=-normalize(camera_direction+light_direction);

    float diffuse=clamp(dot(vs_out_normal,-light_direction),0.0,1.0);

    float sin_th_i=length(cross(-light_direction,vs_out_normal));
    float sin_th_t=sin_th_i/water_refractive_index;
    float th_i=asin(sin_th_i);
    float th_t=asin(sin_th_t);
    float specular=pow(sin(th_t-th_i),2.0)/pow(sin(th_t+th_i),2.0)+pow(tan(th_t-th_i),2.0)/pow(tan(th_t+th_i),2.0);

    vec4 diffuse_color=vec4(water_diffuse_color*diffuse*diffuse_power);
    vec4 specular_color=vec4(water_specular_color*specular*specular_power);

    vec4 color=ambient_color+diffuse_color+specular_color;
    color.a=1.0;

    return color;
}
float Fog(){
    float linear_pos=length(camera_position-vs_out_position);
    float fog_factor=clamp((fog_end-linear_pos)/(fog_end-fog_start),0.0,1.0);

    return fog_factor;
}

void main(){
    vec4 color=Lighting();
    float fog_factor=Fog();

    fs_out_color=mix(fog_color,color,fog_factor);
}

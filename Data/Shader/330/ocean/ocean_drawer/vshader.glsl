#version 330

layout(location=0) in vec3 vs_in_position;
layout(location=1) in vec2 vs_in_uv;
layout(location=2) in vec3 vs_in_normal;

uniform mat4 projection;
uniform mat4 view_transformation;

//Additional uniform variables
uniform vec3 offset;
uniform vec3 scale;
//==========

out vec3 vs_out_position;
out vec3 vs_out_normal;

void main(){
    mat4 camera_matrix=projection*view_transformation;
    vec3 new_pos=(vs_in_position+offset)*scale;
    gl_Position=camera_matrix*vec4(new_pos,1.0);

    vs_out_position=new_pos;
    vs_out_normal=vs_in_normal;
}

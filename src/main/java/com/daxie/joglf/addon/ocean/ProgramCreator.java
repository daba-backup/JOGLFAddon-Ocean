package com.daxie.joglf.addon.ocean;

import com.daxie.joglf.gl.shader.GLShaderFunctions;

class ProgramCreator {
	public static void Create() {
		GLShaderFunctions.CreateProgram(
				"tilde_h0k", 
				"./Data/Shader/330/ocean/tilde_h0k/vshader.glsl",
				"./Data/Shader/330/ocean/tilde_h0k/fshader.glsl");
		GLShaderFunctions.CreateProgram(
				"tilde_hkt", 
				"./Data/Shader/330/ocean/tilde_hkt/vshader.glsl",
				"./Data/Shader/330/ocean/tilde_hkt/fshader.glsl");
		GLShaderFunctions.CreateProgram(
				"tilde_hkt_d", 
				"./Data/Shader/330/ocean/tilde_hkt_d/vshader.glsl",
				"./Data/Shader/330/ocean/tilde_hkt_d/fshader.glsl");
		GLShaderFunctions.CreateProgram(
				"butterfly_texture", 
				"./Data/Shader/330/ocean/butterfly_texture/vshader.glsl",
				"./Data/Shader/330/ocean/butterfly_texture/fshader.glsl");
		GLShaderFunctions.CreateProgram(
				"butterfly_computation", 
				"./Data/Shader/330/ocean/butterfly_computation/vshader.glsl",
				"./Data/Shader/330/ocean/butterfly_computation/fshader.glsl");
		GLShaderFunctions.CreateProgram(
				"inv_and_perm", 
				"./Data/Shader/330/ocean/inv_and_perm/vshader.glsl",
				"./Data/Shader/330/ocean/inv_and_perm/fshader.glsl");
		GLShaderFunctions.CreateProgram(
				"ocean_drawer", 
				"./Data/Shader/330/ocean/ocean_drawer/vshader.glsl",
				"./Data/Shader/330/ocean/ocean_drawer/fshader.glsl");
	}
}

package com.daxie.joglf.addon.ocean;

import java.nio.IntBuffer;

import com.daxie.joglf.gl.shader.GLShaderFunctions;
import com.daxie.joglf.gl.shader.ShaderProgram;
import com.daxie.joglf.gl.transferrer.FullscreenQuadTransferrer;
import com.daxie.joglf.gl.wrapper.GLWrapper;
import com.daxie.log.LogWriter;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

/**
 * Inversion and permutation
 * @author Daba
 *
 */
class InversionAndPermutation {
	private int N;
	
	private int fbo_id;
	//Input texture
	private int input_texture_id;
	//Output texture
	private int output_texture_id;
	
	private ShaderProgram program;
	private FullscreenQuadTransferrer transferrer;
	
	public InversionAndPermutation(int N) {
		this.N=N;
		
		this.SetupOutputTexture();
		this.SetupFramebuffer();
		this.SetupProgram();
		
		transferrer=new FullscreenQuadTransferrer();
	}
	private void SetupOutputTexture() {
		IntBuffer texture_ids=Buffers.newDirectIntBuffer(1);
		GLWrapper.glGenTextures(1, texture_ids);
		output_texture_id=texture_ids.get(0);
		
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, output_texture_id);
		GLWrapper.glTexImage2D(
				GL4.GL_TEXTURE_2D, 0,GL4.GL_RGBA32F, 
				N, N, 0, GL4.GL_RGBA, GL4.GL_FLOAT, null);
		GLWrapper.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_NEAREST);
		GLWrapper.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_NEAREST);
		GLWrapper.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_EDGE);
		GLWrapper.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_EDGE);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, 0);
	}
	private void SetupFramebuffer() {
		IntBuffer fbo_ids=Buffers.newDirectIntBuffer(1);
		GLWrapper.glGenFramebuffers(1, fbo_ids);
		fbo_id=fbo_ids.get(0);
		
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbo_id);
		GLWrapper.glFramebufferTexture2D(
				GL4.GL_FRAMEBUFFER, GL4.GL_COLOR_ATTACHMENT0, 
				GL4.GL_TEXTURE_2D, output_texture_id, 0);
		if(GLWrapper.glCheckFramebufferStatus(GL4.GL_FRAMEBUFFER)!=GL4.GL_FRAMEBUFFER_COMPLETE) {
			LogWriter.WriteWarn("[InversionAndPermutation-SetupFramebuffer] Incomplete framebuffer", true);
		}
		int[] draw_buffers=new int[] {GL4.GL_COLOR_ATTACHMENT0};
		GLWrapper.glDrawBuffers(1, Buffers.newDirectIntBuffer(draw_buffers));
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
	}
	private void SetupProgram() {
		GLShaderFunctions.CreateProgram(
				"inv_and_perm", 
				"./Data/Shader/330/ocean/inv_and_perm/vshader.glsl",
				"./Data/Shader/330/ocean/inv_and_perm/fshader.glsl");
		program=new ShaderProgram("inv_and_perm");
	}
	
	public void SetInputTexture(int input_texture_id) {
		this.input_texture_id=input_texture_id;
	}
	
	public void Compute() {
		program.Enable();
		program.SetUniform("N", N);
		
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbo_id);
		GLWrapper.glActiveTexture(GL4.GL_TEXTURE0);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, input_texture_id);
		program.SetUniform("input_texture", 0);
		transferrer.Transfer();
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
	}
	
	public int GetOutputTexture() {
		return output_texture_id;
	}
}

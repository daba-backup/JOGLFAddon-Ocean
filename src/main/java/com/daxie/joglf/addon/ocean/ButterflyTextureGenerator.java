package com.daxie.joglf.addon.ocean;

import java.nio.Buffer;
import java.nio.IntBuffer;

import com.daxie.joglf.gl.shader.GLShaderFunctions;
import com.daxie.joglf.gl.shader.ShaderProgram;
import com.daxie.joglf.gl.transferrer.FullscreenQuadTransferrer;
import com.daxie.joglf.gl.wrapper.GLWrapper;
import com.daxie.log.LogWriter;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

/**
 * Generates a butterfly texture.
 * @author Daba
 *
 */
class ButterflyTextureGenerator {
	private int N;
	
	private int fbo_id;
	//Input texture
	private int bit_reversed_indices_id;
	//Output texture
	private int out_color_id;
	
	private ShaderProgram program;
	private FullscreenQuadTransferrer transferrer;
	
	private int output_texture_width;
	
	public ButterflyTextureGenerator(int N) {
		this.N=N;
		output_texture_width=(int)Math.round(Math.log(N)/Math.log(2));
		
		this.SetupInputTexture();
		this.SetupOutputTexture();
		this.SetupFramebuffer();
		this.SetupProgram();
		
		transferrer=new FullscreenQuadTransferrer();
	}
	private void SetupInputTexture() {
		IntBuffer texture_ids=Buffers.newDirectIntBuffer(1);
		GLWrapper.glGenTextures(1, texture_ids);
		bit_reversed_indices_id=texture_ids.get(0);
		
		IntBuffer indices=Buffers.newDirectIntBuffer(N);
		int half_N=N/2;
		//Even indices
		for(int i=0;i<half_N;i+=2) {
			indices.put(i);
			indices.put(i+half_N);
		}
		//Odd indices
		for(int i=1;i<half_N;i+=2) {
			indices.put(i);
			indices.put(i+half_N);
		}
		((Buffer)indices).flip();
		
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, bit_reversed_indices_id);
		GLWrapper.glTexImage2D(
				GL4.GL_TEXTURE_2D, 0,GL4.GL_R32I, 
				N, 1, 0, GL4.GL_RED_INTEGER, GL4.GL_INT, indices);
		GLWrapper.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_NEAREST);
		GLWrapper.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_NEAREST);
		GLWrapper.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_EDGE);
		GLWrapper.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_EDGE);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, 0);
	}
	private void SetupOutputTexture() {
		IntBuffer texture_ids=Buffers.newDirectIntBuffer(1);
		GLWrapper.glGenTextures(1, texture_ids);
		out_color_id=texture_ids.get(0);
		
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, out_color_id);
		GLWrapper.glTexImage2D(
				GL4.GL_TEXTURE_2D, 0,GL4.GL_RGBA32F, 
				output_texture_width, N, 0, GL4.GL_RGBA, GL4.GL_FLOAT, null);
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
				GL4.GL_TEXTURE_2D, out_color_id, 0);
		if(GLWrapper.glCheckFramebufferStatus(GL4.GL_FRAMEBUFFER)!=GL4.GL_FRAMEBUFFER_COMPLETE) {
			LogWriter.WriteWarn("[ButterflyTextureGenerator-SetupFramebuffer] Incomplete framebuffer", true);
		}
		int[] draw_buffers=new int[] {GL4.GL_COLOR_ATTACHMENT0};
		GLWrapper.glDrawBuffers(1, Buffers.newDirectIntBuffer(draw_buffers));
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
	}
	private void SetupProgram() {
		GLShaderFunctions.CreateProgram(
				"butterfly_texture", 
				"./Data/Shader/330/ocean/butterfly_texture/vshader.glsl",
				"./Data/Shader/330/ocean/butterfly_texture/fshader.glsl");
		program=new ShaderProgram("butterfly_texture");
	}
	
	public void Compute() {
		program.Enable();
		program.SetUniform("N", N);
		
		GLWrapper.glViewport(0, 0, output_texture_width, N);
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbo_id);
		GLWrapper.glActiveTexture(GL4.GL_TEXTURE0);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, bit_reversed_indices_id);
		program.SetUniform("bit_reversed_indices", 0);
		transferrer.Transfer();
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
	}
	
	public int GetOutColor() {
		return out_color_id;
	}
}

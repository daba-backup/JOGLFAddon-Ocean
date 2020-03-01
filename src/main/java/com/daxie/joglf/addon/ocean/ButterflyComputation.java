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
 * Butterfly computation
 * @author Daba
 *
 */
class ButterflyComputation {
	private int N;
	private int total_stage_num;
	
	private int fbo_id;
	//Input texture
	private int butterfly_texture_id;
	//Pingpong textures
	private int[] pingpong_texture_ids;
	
	private ShaderProgram program;
	private FullscreenQuadTransferrer transferrer;
	
	private int pingpong;
	
	public ButterflyComputation(int N) {
		this.N=N;
		total_stage_num=(int)Math.round(Math.log(N)/Math.log(2));
		
		transferrer=new FullscreenQuadTransferrer();
		
		this.SetupPingpongTexture();
		this.SetupFramebuffer();
		this.SetupProgram();
		
		pingpong=0;
	}
	private void SetupPingpongTexture() {
		IntBuffer texture_ids=Buffers.newDirectIntBuffer(1);
		GLWrapper.glGenTextures(1, texture_ids);
		pingpong_texture_ids=new int[] {0,texture_ids.get(0)};
		
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, pingpong_texture_ids[1]);
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
		int[] draw_buffers=new int[] {GL4.GL_COLOR_ATTACHMENT0};
		GLWrapper.glDrawBuffers(1, Buffers.newDirectIntBuffer(draw_buffers));
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
	}
	private void SetupProgram() {
		GLShaderFunctions.CreateProgram(
				"butterfly_computation", 
				"./Data/Shader/330/ocean/butterfly_computation/vshader.glsl",
				"./Data/Shader/330/ocean/butterfly_computation/fshader.glsl");
		program=new ShaderProgram("butterfly_computation");
	}
	
	public void SetButterflyTexture(int butterfly_texture_id) {
		this.butterfly_texture_id=butterfly_texture_id;
	}
	public void SetPingpongIn(int pingpong_in_id) {
		this.pingpong_texture_ids[0]=pingpong_in_id;
	}
	
	public void Compute() {
		program.Enable();
		program.SetUniform("N", N);
		program.SetUniform("total_stage_num", total_stage_num);
		
		GLWrapper.glActiveTexture(GL4.GL_TEXTURE0);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, butterfly_texture_id);
		program.SetUniform("butterfly_texture", 0);
		
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbo_id);
		
		pingpong=0;
		
		//Horizontal
		program.SetUniform("direction", 0);
		for(int i=0;i<total_stage_num;i++) {
			program.SetUniform("stage", i);
			
			this.innerCompute(pingpong, 1-pingpong);
			transferrer.Transfer();
			
			pingpong=1-pingpong;
		}
		
		//Vertical
		program.SetUniform("direction", 1);
		for(int i=0;i<total_stage_num;i++) {
			program.SetUniform("stage", i);
			
			this.innerCompute(pingpong, 1-pingpong);
			transferrer.Transfer();
			
			pingpong=1-pingpong;
		}
		
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
	}
	private void innerCompute(int pingpong_in,int pingpong_out) {
		GLWrapper.glFramebufferTexture2D(
				GL4.GL_FRAMEBUFFER, GL4.GL_COLOR_ATTACHMENT0, 
				GL4.GL_TEXTURE_2D, pingpong_texture_ids[pingpong_out], 0);
		if(GLWrapper.glCheckFramebufferStatus(GL4.GL_FRAMEBUFFER)!=GL4.GL_FRAMEBUFFER_COMPLETE) {
			LogWriter.WriteWarn("[ButterflyComputation-innerCompute] Incomplete framebuffer", true);
		}
		
		GLWrapper.glActiveTexture(GL4.GL_TEXTURE1);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, pingpong_texture_ids[pingpong_in]);
		program.SetUniform("pingpong_in", 1);
	}
	
	public int GetComputationResult() {
		return pingpong_texture_ids[pingpong];
	}
}

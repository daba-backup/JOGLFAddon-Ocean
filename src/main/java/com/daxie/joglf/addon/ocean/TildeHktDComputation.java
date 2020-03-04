package com.daxie.joglf.addon.ocean;

import java.nio.IntBuffer;

import com.daxie.joglf.gl.shader.ShaderProgram;
import com.daxie.joglf.gl.transferrer.FullscreenQuadTransferrer;
import com.daxie.joglf.gl.wrapper.GLWrapper;
import com.daxie.log.LogWriter;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

/**
 * Computation of horizontal ~h(k,t)
 * @author Daba
 *
 */
class TildeHktDComputation {
	private int N;
	private float L;
	private float t;
	
	private int fbo_id;
	//Input textures
	private int tilde_h0k_id;
	private int tilde_h0minusk_id;
	//Output texture
	private int tilde_hkt_d_id;
	
	private ShaderProgram program;
	
	private FullscreenQuadTransferrer transferrer;
	
	public TildeHktDComputation(int N) {
		this.N=N;
		L=1000.0f;
		t=0.0f;
		
		this.SetupOutputTexture();
		this.SetupFramebuffer();
		this.SetupProgram();
		
		transferrer=new FullscreenQuadTransferrer();
	}
	private void SetupOutputTexture() {
		IntBuffer texture_ids=Buffers.newDirectIntBuffer(1);
		GLWrapper.glGenTextures(1, texture_ids);
		tilde_hkt_d_id=texture_ids.get(0);
		
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, tilde_hkt_d_id);
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
				GL4.GL_TEXTURE_2D, tilde_hkt_d_id, 0);
		if(GLWrapper.glCheckFramebufferStatus(GL4.GL_FRAMEBUFFER)!=GL4.GL_FRAMEBUFFER_COMPLETE) {
			LogWriter.WriteWarn("[TildeHktDComputation-SetupFramebuffer] Incomplete framebuffer", true);
		}
		int[] draw_buffers=new int[] {GL4.GL_COLOR_ATTACHMENT0};
		GLWrapper.glDrawBuffers(1, Buffers.newDirectIntBuffer(draw_buffers));
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
	}
	private void SetupProgram() {
		program=new ShaderProgram("tilde_hkt_d");
	}
	
	public void SetParameter(float L) {
		this.L=L;
	}
	public void AdvanceTime(float dt) {
		t+=dt;
	}
	public void SetTildeH0k(int tilde_h0k_id) {
		this.tilde_h0k_id=tilde_h0k_id;
	}
	public void SetTildeH0minusk(int tilde_h0minusk_id) {
		this.tilde_h0minusk_id=tilde_h0minusk_id;
	}
	
	public void Compute() {
		program.Enable();
		program.SetUniform("N", N);
		program.SetUniform("L", L);
		program.SetUniform("t", t);
		
		GLWrapper.glViewport(0, 0, N, N);
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbo_id);
		GLWrapper.glActiveTexture(GL4.GL_TEXTURE0);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, tilde_h0k_id);
		program.SetUniform("tilde_h0k", 0);
		GLWrapper.glActiveTexture(GL4.GL_TEXTURE1);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, tilde_h0minusk_id);
		program.SetUniform("tilde_h0minusk", 1);
		transferrer.Transfer();
		GLWrapper.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
	}
	
	public int GetTildeHktD() {
		return tilde_hkt_d_id;
	}
}

package com.daxie.joglf.addon.ocean;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.daxie.basis.vector.Vector;
import com.daxie.basis.vector.VectorFunctions;
import com.daxie.joglf.gl.wrapper.GLWrapper;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

/**
 * Generates a heightmap for ocean.
 * @author Daba
 *
 */
public class OceanHeightmapGenerator {
	private int N;
	
	private TildeHktComputation tilde_hkt_computation;
	private TildeHktDComputation[] tilde_hkt_d_computations;
	private ButterflyComputation butterfly_computation;
	private InversionAndPermutation inv_and_perm;
	
	private List<Vector> coords;
	
	/**
	 * 
	 * @param N Number of samples
	 * @param L Size of the patch
	 * @param A Amplitude of Ph(k)
	 * @param v Wind speed
	 * @param wx X-component of the wind direction
	 * @param wz Z-component of the wind direction
	 */
	public OceanHeightmapGenerator(int N,float L,float A,float v,float wx,float wz) {
		ProgramCreator.Create();
		
		this.N=N;
		this.Prepare(L, A, v, wx, wz);
		
		coords=new ArrayList<>((N+1)*(N+1));
	}
	public void Prepare(float L,float A,float v,float wx,float wz) {
		TildeH0kComputation[] tilde_h0k_computations=new TildeH0kComputation[3];
		for(int i=0;i<3;i++) {
			tilde_h0k_computations[i]=new TildeH0kComputation(N);
			tilde_h0k_computations[i].SetParameters(L, A, v, wx, wz);
			tilde_h0k_computations[i].Compute();
		}
		
		tilde_hkt_computation=new TildeHktComputation(N);
		ButterflyTextureGenerator butterfly_texture_generator=new ButterflyTextureGenerator(N);
		butterfly_computation=new ButterflyComputation(N);
		inv_and_perm=new InversionAndPermutation(N);
		
		tilde_hkt_computation.SetParameter(L);
		tilde_hkt_computation.SetTildeH0k(tilde_h0k_computations[0].GetTildeH0k());
		tilde_hkt_computation.SetTildeH0minusk(tilde_h0k_computations[0].GetTildeH0minusk());
		
		tilde_hkt_d_computations=new TildeHktDComputation[2];
		for(int i=0;i<2;i++) {
			tilde_hkt_d_computations[i]=new TildeHktDComputation(N);
			tilde_hkt_d_computations[i].SetParameter(L);
			tilde_hkt_d_computations[i].SetTildeH0k(tilde_h0k_computations[i+1].GetTildeH0k());
			tilde_hkt_d_computations[i].SetTildeH0minusk(tilde_h0k_computations[i+1].GetTildeH0minusk());
		}
		
		butterfly_texture_generator.Compute();
		
		butterfly_computation.SetButterflyTexture(butterfly_texture_generator.GetOutColor());
	}
	
	public void AdvanceTime(float t) {
		tilde_hkt_computation.AdvanceTime(t);
		for(int i=0;i<2;i++) {
			tilde_hkt_d_computations[i].AdvanceTime(t);
		}
	}
	
	public void Update() {
		tilde_hkt_computation.Compute();
		for(int i=0;i<2;i++) {
			tilde_hkt_d_computations[i].Compute();
		}
		
		//Y-component
		butterfly_computation.SetPingpongIn(tilde_hkt_computation.GetTildeHkt());
		butterfly_computation.Compute();
		inv_and_perm.SetInputTexture(butterfly_computation.GetComputationResult());
		inv_and_perm.Compute();
		
		int output_texture_id=inv_and_perm.GetOutputTexture();
		FloatBuffer y_component_buf=Buffers.newDirectFloatBuffer(N*N);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, output_texture_id);
		GLWrapper.glGetTexImage(GL4.GL_TEXTURE_2D, 0, GL4.GL_RED, GL4.GL_FLOAT, y_component_buf);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, 0);
		
		//X-component
		butterfly_computation.SetPingpongIn(tilde_hkt_d_computations[0].GetTildeHktD());
		butterfly_computation.Compute();
		inv_and_perm.SetInputTexture(butterfly_computation.GetComputationResult());
		inv_and_perm.Compute();
		
		FloatBuffer x_component_buf=Buffers.newDirectFloatBuffer(N*N);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, output_texture_id);
		GLWrapper.glGetTexImage(GL4.GL_TEXTURE_2D, 0, GL4.GL_RED, GL4.GL_FLOAT, x_component_buf);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, 0);
		
		//Z-component
		butterfly_computation.SetPingpongIn(tilde_hkt_d_computations[1].GetTildeHktD());
		butterfly_computation.Compute();
		inv_and_perm.SetInputTexture(butterfly_computation.GetComputationResult());
		inv_and_perm.Compute();
		
		FloatBuffer z_component_buf=Buffers.newDirectFloatBuffer(N*N);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, output_texture_id);
		GLWrapper.glGetTexImage(GL4.GL_TEXTURE_2D, 0, GL4.GL_RED, GL4.GL_FLOAT, z_component_buf);
		GLWrapper.glBindTexture(GL4.GL_TEXTURE_2D, 0);
		
		coords.clear();
		for(int z=0;z<=N;z++) {
			for(int x=0;x<=N;x++) {
				int index=(z%N)*N+(x%N);
				
				Vector vtemp=VectorFunctions.VGet(
						x_component_buf.get(index),
						y_component_buf.get(index),
						z_component_buf.get(index));
				vtemp=VectorFunctions.VAdd(vtemp, VectorFunctions.VGet(x, 0.0f, z));
				
				coords.add(vtemp);
			}
		}
		
		System.out.println(coords.size());
	}
	
	public List<Vector> GetCoords(){
		return new ArrayList<>(coords);
	}
}

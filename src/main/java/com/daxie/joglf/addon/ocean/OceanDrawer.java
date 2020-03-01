package com.daxie.joglf.addon.ocean;

import java.util.List;

import com.daxie.basis.coloru8.ColorU8;
import com.daxie.basis.coloru8.ColorU8Functions;
import com.daxie.basis.vector.Vector;
import com.daxie.basis.vector.VectorFunctions;
import com.daxie.joglf.gl.drawer.DynamicTrianglesDrawer;
import com.daxie.joglf.gl.front.CameraFront;
import com.daxie.joglf.gl.front.FogFront;
import com.daxie.joglf.gl.front.LightingFront;
import com.daxie.joglf.gl.shader.GLShaderFunctions;
import com.daxie.joglf.gl.shader.ShaderProgram;
import com.daxie.joglf.gl.shape.Triangle;

/**
 * Ocean drawer
 * @author Daba
 *
 */
public class OceanDrawer {
	private int N;
	
	private OceanHeightmapGenerator ohg;
	
	private ShaderProgram program;
	private DynamicTrianglesDrawer drawer;
	
	/**
	 * 
	 * @param N Number of samples
	 */
	public OceanDrawer(int N) {
		this.N=N;
		
		ohg=new OceanHeightmapGenerator(N, 1000.0f, 4.0f, 10.0f, 1.0f, 1.0f);
		
		GLShaderFunctions.CreateProgram(
				"ocean_drawer", 
				"./Data/Shader/330/ocean/ocean_drawer/vshader.glsl",
				"./Data/Shader/330/ocean/ocean_drawer/fshader.glsl");
		program=new ShaderProgram("ocean_drawer");
		program.Enable();
		program.SetUniform("water_diffuse_color", ColorU8Functions.GetColorU8(0.25f, 0.58f, 0.92f, 1.0f));
		program.SetUniform("water_specular_color", ColorU8Functions.GetColorU8(1.0f, 1.0f, 1.0f, 1.0f));
		program.SetUniform("refractive_index", 1.33f);
		program.SetUniform("specular_pow_y", 10.0f);
		
		CameraFront.AddProgram("ocean_drawer");
		FogFront.AddProgram("ocean_drawer");
		LightingFront.AddProgram("ocean_drawer");
		
		drawer=new DynamicTrianglesDrawer();
	}
	public void Dispose() {
		drawer.DeleteBuffers();
	}
	
	public void SetParameters(float L,float A,float v,float wx,float wz) {
		ohg.Prepare(L, A, v, wx, wz);
	}
	public void AdvanceTime(float t) {
		ohg.AdvanceTime(t);
	}
	
	public void SetWaterColor(ColorU8 water_diffuse_color,ColorU8 water_specular_color) {
		program.SetUniform("water_diffuse_color", water_diffuse_color);
		program.SetUniform("water_specular_color", water_specular_color);
	}
	public void SetRefractiveIndex(float refractive_index) {
		program.SetUniform("refractive_index", refractive_index);
	}
	public void SetSpecularPowY(float specular_pow_y) {
		program.SetUniform("specular_pow_y", specular_pow_y);
	}
	
	public void Update() {
		ohg.Update();
		
		List<Vector> coords=ohg.GetCoords();
		
		int size=N*N;
		Vector[] vertex_normals=new Vector[size];
		for(int i=0;i<size;i++) {
			vertex_normals[i]=VectorFunctions.VGet(0.0f, 0.0f, 0.0f);
		}
		
		int count=0;
		for(int z=0;z<N-1;z++) {
			for(int x=0;x<N-1;x++) {
				Triangle[] triangles=new Triangle[2];
				for(int k=0;k<2;k++)triangles[k]=new Triangle();
				
				Vector[] positions=new Vector[4];
				int[] indices=new int[] {z*N+x,(z+1)*N+x,(z+1)*N+(x+1),z*N+(x+1)};
				
				positions[0]=VectorFunctions.VAdd(VectorFunctions.VGet(x, 0.0f, z), coords.get(indices[0]));
				positions[1]=VectorFunctions.VAdd(VectorFunctions.VGet(x, 0.0f, z+1), coords.get(indices[1]));
				positions[2]=VectorFunctions.VAdd(VectorFunctions.VGet(x+1, 0.0f, z+1), coords.get(indices[2]));
				positions[3]=VectorFunctions.VAdd(VectorFunctions.VGet(x+1, 0.0f, z), coords.get(indices[3]));
				
				Vector edge1=VectorFunctions.VSub(positions[1], positions[0]);
				Vector edge2=VectorFunctions.VSub(positions[2], positions[0]);
				Vector edge3=VectorFunctions.VSub(positions[3], positions[2]);
				Vector edge4=VectorFunctions.VSub(positions[0], positions[2]);
				Vector n1=VectorFunctions.VCross(edge1, edge2);
				n1=VectorFunctions.VNorm(n1);
				Vector n2=VectorFunctions.VCross(edge3, edge4);
				n2=VectorFunctions.VNorm(n2);
				
				vertex_normals[z*N+x]=VectorFunctions.VAdd(vertex_normals[z*N+x], n1);
				vertex_normals[(z+1)*N+x]=VectorFunctions.VAdd(vertex_normals[(z+1)*N+x], n1);
				vertex_normals[(z+1)*N+(x+1)]=VectorFunctions.VAdd(vertex_normals[(z+1)*N+(x+1)], n1);
				vertex_normals[(z+1)*N+(x+1)]=VectorFunctions.VAdd(vertex_normals[(z+1)*N+(x+1)], n2);
				vertex_normals[z*N+(x+1)]=VectorFunctions.VAdd(vertex_normals[z*N+(x+1)], n2);
				vertex_normals[z*N+x]=VectorFunctions.VAdd(vertex_normals[z*N+x], n2);
				
				//First triangle
				for(int i=0;i<3;i++) {
					triangles[0].GetVertex(i).SetPos(positions[i]);
				}
				//Second triangle
				for(int i=0;i<3;i++) {
					triangles[1].GetVertex(i).SetPos(positions[(i+2)%4]);
				}
				
				drawer.AddTriangle(count, triangles[0]);
				drawer.AddTriangle(count+1, triangles[1]);
				
				count+=2;
			}
		}
		
		for(int i=0;i<size;i++) {
			vertex_normals[i]=VectorFunctions.VNorm(vertex_normals[i]);
		}
		
		count=0;
		for(int z=0;z<N-1;z++) {
			for(int x=0;x<N-1;x++) {
				drawer.GetTriangle(count).GetVertex(0).SetNorm(vertex_normals[z*N+x]);
				drawer.GetTriangle(count).GetVertex(1).SetNorm(vertex_normals[(z+1)*N+x]);
				drawer.GetTriangle(count).GetVertex(2).SetNorm(vertex_normals[(z+1)*N+(x+1)]);
				
				drawer.GetTriangle(count+1).GetVertex(0).SetNorm(vertex_normals[(z+1)*N+(x+1)]);
				drawer.GetTriangle(count+1).GetVertex(1).SetNorm(vertex_normals[z*N+(x+1)]);
				drawer.GetTriangle(count+1).GetVertex(2).SetNorm(vertex_normals[z*N+x]);
				
				count+=2;
			}
		}
		
		drawer.UpdateBuffers();
	}
	
	public void Draw() {
		program.Enable();
		drawer.Transfer();
	}
}

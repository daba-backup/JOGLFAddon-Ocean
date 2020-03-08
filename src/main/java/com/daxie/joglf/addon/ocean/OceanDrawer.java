package com.daxie.joglf.addon.ocean;

import java.util.List;

import com.daxie.basis.coloru8.ColorU8Functions;
import com.daxie.basis.vector.Vector;
import com.daxie.basis.vector.VectorFunctions;
import com.daxie.joglf.gl.drawer.DynamicTrianglesDrawer;
import com.daxie.joglf.gl.front.CameraFront;
import com.daxie.joglf.gl.front.FogFront;
import com.daxie.joglf.gl.shader.ShaderProgram;
import com.daxie.joglf.gl.shape.Triangle;

public class OceanDrawer {
	private int N;
	
	private OceanHeightmapGenerator ohg;
	private DynamicTrianglesDrawer drawer;
	private ShaderProgram program;
	
	public OceanDrawer(int N,float L,float A,float v,float wx,float wz) {
		this.N=N;
		
		ohg=new OceanHeightmapGenerator(N, L, A, v, wx, wz);
		drawer=new DynamicTrianglesDrawer();
		program=new ShaderProgram("ocean_drawer");
		
		CameraFront.AddProgram("ocean_drawer");
		FogFront.AddProgram("ocean_drawer");
	}
	
	public void SetParameters(float L,float A,float v,float wx,float wz) {
		ohg.Prepare(L, A, v, wx, wz);
	}
	
	public void AdvanceTime(float t) {
		ohg.AdvanceTime(t);
	}
	
	public void Update() {
		this.UpdateDrawer();
		this.UpdateUniformVariables();
	}
	private void UpdateDrawer() {
		ohg.Update();
		List<Vector> coords=ohg.GetCoords();
		
		int size=(N+1)*(N+1);
		Vector[] vertex_normals=new Vector[size];
		for(int i=0;i<size;i++) {
			vertex_normals[i]=VectorFunctions.VGet(0.0f, 0.0f, 0.0f);
		}
		
		int count=0;
		for(int z=0;z<N;z++) {
			for(int x=0;x<N;x++) {
				Triangle[] triangles=new Triangle[2];
				for(int k=0;k<2;k++)triangles[k]=new Triangle();
				
				Vector[] positions=new Vector[4];
				int[] indices=new int[] {
						z*(N+1)+x,
						(z+1)*(N+1)+x,
						(z+1)*(N+1)+(x+1),
						z*(N+1)+(x+1)};
				
				for(int i=0;i<4;i++) {
					positions[i]=coords.get(indices[i]);
				}
				
				Vector edge1=VectorFunctions.VSub(positions[1], positions[0]);
				Vector edge2=VectorFunctions.VSub(positions[2], positions[0]);
				Vector edge3=VectorFunctions.VSub(positions[3], positions[2]);
				Vector edge4=VectorFunctions.VSub(positions[0], positions[2]);
				Vector n1=VectorFunctions.VCross(edge1, edge2);
				n1=VectorFunctions.VNorm(n1);
				Vector n2=VectorFunctions.VCross(edge3, edge4);
				n2=VectorFunctions.VNorm(n2);
				
				vertex_normals[z*(N+1)+x]=VectorFunctions.VAdd(vertex_normals[z*(N+1)+x], n1);
				vertex_normals[(z+1)*(N+1)+x]=VectorFunctions.VAdd(vertex_normals[(z+1)*(N+1)+x], n1);
				vertex_normals[(z+1)*(N+1)+(x+1)]=VectorFunctions.VAdd(vertex_normals[(z+1)*(N+1)+(x+1)], n1);
				vertex_normals[(z+1)*(N+1)+(x+1)]=VectorFunctions.VAdd(vertex_normals[(z+1)*(N+1)+(x+1)], n2);
				vertex_normals[z*(N+1)+(x+1)]=VectorFunctions.VAdd(vertex_normals[z*(N+1)+(x+1)], n2);
				vertex_normals[z*(N+1)+x]=VectorFunctions.VAdd(vertex_normals[z*(N+1)+x], n2);
				
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
		for(int z=0;z<N;z++) {
			for(int x=0;x<N;x++) {
				drawer.GetTriangle(count).GetVertex(0).SetNorm(vertex_normals[z*(N+1)+x]);
				drawer.GetTriangle(count).GetVertex(1).SetNorm(vertex_normals[(z+1)*(N+1)+x]);
				drawer.GetTriangle(count).GetVertex(2).SetNorm(vertex_normals[(z+1)*(N+1)+(x+1)]);
				
				drawer.GetTriangle(count+1).GetVertex(0).SetNorm(vertex_normals[(z+1)*(N+1)+(x+1)]);
				drawer.GetTriangle(count+1).GetVertex(1).SetNorm(vertex_normals[z*(N+1)+(x+1)]);
				drawer.GetTriangle(count+1).GetVertex(2).SetNorm(vertex_normals[z*(N+1)+x]);
				
				count+=2;
			}
		}
		
		drawer.UpdateBuffers();
	}
	private void UpdateUniformVariables() {
		program.Enable();
		
		Vector light_direction=VectorFunctions.VGet(-1.0f, -1.0f, -1.0f);
		light_direction=VectorFunctions.VNorm(light_direction);
		program.SetUniform("light_direction", light_direction);
		
		program.SetUniform("ambient_color", ColorU8Functions.GetColorU8(0.0f, 0.0f, 0.0f, 0.0f));
		program.SetUniform("diffuse_power", 0.7f);
		program.SetUniform("specular_power", 4.0f);
		program.SetUniform("water_diffuse_color", ColorU8Functions.GetColorU8(0.25f, 0.58f, 0.92f, 1.0f));
		program.SetUniform("water_specular_color", ColorU8Functions.GetColorU8(1.0f, 1.0f, 1.0f, 1.0f));
		program.SetUniform("water_refractive_index", 1.33f);
	}
	
	public void Draw() {
		program.Enable();
		program.SetUniform("offset", VectorFunctions.VGet(0.0f, 0.0f, 0.0f));
		program.SetUniform("scale", VectorFunctions.VGet(1.0f, 1.0f, 1.0f));
		drawer.Transfer();
	}
}

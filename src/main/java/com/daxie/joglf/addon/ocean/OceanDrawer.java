package com.daxie.joglf.addon.ocean;

import java.util.List;

import com.daxie.basis.coloru8.ColorU8;
import com.daxie.basis.coloru8.ColorU8Functions;
import com.daxie.basis.vector.Vector;
import com.daxie.basis.vector.VectorFunctions;
import com.daxie.joglf.gl.drawer.DynamicTrianglesDrawer;
import com.daxie.joglf.gl.front.CameraFront;
import com.daxie.joglf.gl.shader.ShaderProgram;
import com.daxie.joglf.gl.shape.Triangle;

public class OceanDrawer {
	private int N;
	
	private OceanHeightmapGenerator ohg;
	private DynamicTrianglesDrawer drawer;
	private ShaderProgram program;
	
	private Vector light_direction;
	private ColorU8 ambient_color;
	private float diffuse_power;
	private float specular_power;
	private ColorU8 water_diffuse_color;
	private ColorU8 water_specular_color;
	private float water_refractive_index;
	
	private float fog_start;
	private float fog_end;
	private ColorU8 fog_color;
	
	public OceanDrawer(int N,float L,float A,float v,float wx,float wz) {
		this.N=N;
		
		ohg=new OceanHeightmapGenerator(N, L, A, v, wx, wz);
		drawer=new DynamicTrianglesDrawer();
		program=new ShaderProgram("ocean_drawer");
		
		CameraFront.AddProgram("ocean_drawer");
		
		light_direction=VectorFunctions.VGet(-1.0f, -1.0f, -1.0f);
		light_direction=VectorFunctions.VNorm(light_direction);
		
		ambient_color=ColorU8Functions.GetColorU8(0.0f, 0.0f, 0.0f, 1.0f);
		diffuse_power=1.2f;
		specular_power=3.0f;
		water_diffuse_color=ColorU8Functions.GetColorU8(47, 79, 79, 255);
		water_specular_color=ColorU8Functions.GetColorU8(1.0f, 1.0f, 1.0f, 1.0f);
		water_refractive_index=1.33f;
		
		fog_start=800.0f;
		fog_end=1000.0f;
		fog_color=ColorU8Functions.GetColorU8(0.0f, 0.0f, 0.0f, 1.0f);
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
				
				if(z==0) {
					vertex_normals[N*(N+1)+x]=VectorFunctions.VAdd(vertex_normals[N*(N+1)+x], n1);
					vertex_normals[N*(N+1)+(x+1)]=VectorFunctions.VAdd(vertex_normals[N*(N+1)+(x+1)], n1);
					vertex_normals[N*(N+1)+(x+1)]=VectorFunctions.VAdd(vertex_normals[N*(N+1)+(x+1)], n2);
				}
				if(z==N-1) {
					vertex_normals[x]=VectorFunctions.VAdd(vertex_normals[x], n1);
					vertex_normals[x+1]=VectorFunctions.VAdd(vertex_normals[x+1], n1);
					vertex_normals[x+1]=VectorFunctions.VAdd(vertex_normals[x+1], n2);
				}
				if(x==0) {
					vertex_normals[(z+1)*(N+1)+N]=VectorFunctions.VAdd(vertex_normals[(z+1)*(N+1)+N], n1);
					vertex_normals[(z+1)*(N+1)+N]=VectorFunctions.VAdd(vertex_normals[(z+1)*(N+1)+N], n2);
					vertex_normals[z*(N+1)+N]=VectorFunctions.VAdd(vertex_normals[z*(N+1)+N], n2);
				}
				if(x==N-1) {
					vertex_normals[(z+1)*(N+1)]=VectorFunctions.VAdd(vertex_normals[(z+1)*(N+1)], n1);
					vertex_normals[(z+1)*(N+1)]=VectorFunctions.VAdd(vertex_normals[(z+1)*(N+1)], n2);
					vertex_normals[z*(N+1)]=VectorFunctions.VAdd(vertex_normals[z*(N+1)], n2);
				}
				
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
		
		program.SetUniform("light_direction", light_direction);
		program.SetUniform("ambient_color", ambient_color);
		program.SetUniform("diffuse_power", diffuse_power);
		program.SetUniform("specular_power", specular_power);
		program.SetUniform("water_diffuse_color", water_diffuse_color);
		program.SetUniform("water_specular_color", water_specular_color);
		program.SetUniform("water_refractive_index", water_refractive_index);
		
		program.SetUniform("fog_start", fog_start);
		program.SetUniform("fog_end", fog_end);
		program.SetUniform("fog_color", fog_color);
	}
	
	public void SetAmbientColor(ColorU8 ambient_color) {
		this.ambient_color=ambient_color;
	}
	public void SetDiffusePower(float diffuse_power) {
		this.diffuse_power=diffuse_power;
	}
	public void SetSpecularPower(float specular_power) {
		this.specular_power=specular_power;
	}
	public void SetWaterDiffuseColor(ColorU8 water_diffuse_color) {
		this.water_diffuse_color=water_diffuse_color;
	}
	public void SetWaterSpecularColor(ColorU8 water_specular_color) {
		this.water_specular_color=water_specular_color;
	}
	public void SetWaterRefractiveIndex(float water_refractive_index) {
		this.water_refractive_index=water_refractive_index;
	}
	public void SetFogStartEnd(float fog_start,float fog_end) {
		this.fog_start=fog_start;
		this.fog_end=fog_end;
	}
	public void SetFogColor(ColorU8 fog_color) {
		this.fog_color=fog_color;
	}
	
	public void Draw(int x_repeat_num,int z_repeat_num,Vector center) {
		program.Enable();
		
		float x_length=(float)(N*x_repeat_num);
		float z_length=(float)(N*z_repeat_num);
		float x_center=x_length/2.0f;
		float z_center=z_length/2.0f;
		
		float x_init=center.GetX()-x_center;
		float y_init=center.GetY();
		float z_init=center.GetZ()-z_center;
		
		float offset_x=0.0f;
		float offset_z=0.0f;
		for(int i=0;i<z_repeat_num;i++) {
			for(int j=0;j<x_repeat_num;j++) {
				program.SetUniform("offset", VectorFunctions.VGet(x_init+offset_x, y_init, z_init+offset_z));
				program.SetUniform("scale", VectorFunctions.VGet(1.0f, 1.0f, 1.0f));
				drawer.Transfer();
				
				offset_x+=(float)N;
			}
			offset_x=0.0f;
			offset_z+=(float)N;
		}
	}
}

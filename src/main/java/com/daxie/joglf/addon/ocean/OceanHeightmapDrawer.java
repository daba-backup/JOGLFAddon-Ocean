package com.daxie.joglf.addon.ocean;

import java.util.List;

import com.daxie.basis.coloru8.ColorU8;
import com.daxie.basis.coloru8.ColorU8Functions;
import com.daxie.basis.vector.Vector;
import com.daxie.basis.vector.VectorFunctions;
import com.daxie.joglf.gl.drawer.DynamicSegmentsDrawer;
import com.daxie.joglf.gl.shape.Vertex3D;

/**
 * Ocean heightmap drawer
 * @author Daba
 *
 */
public class OceanHeightmapDrawer {
	private OceanHeightmapGenerator ohg;
	private DynamicSegmentsDrawer drawer;
	
	private ColorU8 color;
	
	public OceanHeightmapDrawer(int N,float L,float A,float v,float wx,float wz) {
		ohg=new OceanHeightmapGenerator(N, L, A, v, wx, wz);
		drawer=new DynamicSegmentsDrawer();
		
		color=ColorU8Functions.GetColorU8(1.0f, 1.0f, 1.0f, 1.0f);
	}
	
	public void SetParameters(float L,float A,float v,float wx,float wz) {
		ohg.Prepare(L, A, v, wx, wz);
	}
	public void SetColor(ColorU8 color) {
		this.color=color;
	}
	
	public void AdvanceTime(float t) {
		ohg.AdvanceTime(t);
	}
	
	public void Update() {
		ohg.Update();
		List<Vector> coords=ohg.GetCoords();
		
		int coords_num=coords.size();
		for(int i=0;i<coords_num;i++) {
			Vector pos=coords.get(i);
			
			Vector segment_pos_1=VectorFunctions.VAdd(pos, VectorFunctions.VGet(0.0f, -0.1f, 0.0f));
			Vector segment_pos_2=VectorFunctions.VAdd(pos, VectorFunctions.VGet(0.0f, 0.1f, 0.0f));
			
			Vertex3D v1=new Vertex3D();
			v1.SetPos(segment_pos_1);
			v1.SetDif(color);
			Vertex3D v2=new Vertex3D();
			v2.SetPos(segment_pos_2);
			v2.SetDif(color);
			
			drawer.AddSegment(i, v1, v2);
		}
		
		drawer.UpdateBuffers();
	}
	public void Draw() {
		drawer.Draw();
	}
}

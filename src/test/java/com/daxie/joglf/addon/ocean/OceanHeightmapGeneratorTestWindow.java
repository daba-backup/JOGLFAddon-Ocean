package com.daxie.joglf.addon.ocean;

import java.util.List;

import com.daxie.basis.coloru8.ColorU8Functions;
import com.daxie.basis.vector.Vector;
import com.daxie.basis.vector.VectorFunctions;
import com.daxie.joglf.gl.drawer.DynamicSegmentsDrawer;
import com.daxie.joglf.gl.front.CameraFront;
import com.daxie.joglf.gl.shape.Vertex3D;
import com.daxie.joglf.gl.window.JOGLFWindow;
import com.daxie.joglf.gl.wrapper.GLWrapper;

class OceanHeightmapGeneratorTestWindow extends JOGLFWindow{
	private OceanHeightmapGenerator ohg;
	private DynamicSegmentsDrawer drawer;
	
	private static final int N=128;
	
	@Override
	protected void Init() {
		ohg=new OceanHeightmapGenerator(N, 1000.0f, 4.0f, 40.0f, 1.0f, 1.0f);
		
		drawer=new DynamicSegmentsDrawer();
		drawer.SetDefaultProgram();
	}
	
	@Override
	protected void Update() {
		ohg.Update();
		ohg.AdvanceTime(1.0f/30.0f);
		
		List<Vector> coords=ohg.GetCoords();
		
		for(int i=0;i<N;i++) {
			for(int j=0;j<N;j++) {
				Vector pos=coords.get(i*N+j);
				
				Vertex3D[] vertices=new Vertex3D[2];
				vertices[0]=new Vertex3D();
				vertices[0].SetPos(VectorFunctions.VAdd(pos, VectorFunctions.VGet(0.0f, -0.1f, 0.0f)));
				vertices[0].SetDif(ColorU8Functions.GetColorU8(1.0f, 1.0f, 1.0f, 1.0f));
				vertices[1]=new Vertex3D();
				vertices[1].SetPos(VectorFunctions.VAdd(pos, VectorFunctions.VGet(0.0f, 0.1f, 0.0f)));
				vertices[1].SetDif(ColorU8Functions.GetColorU8(1.0f, 1.0f, 1.0f, 1.0f));
				
				drawer.AddSegment(i*N+j, vertices[0], vertices[1]);
			}
		}
		
		drawer.UpdateBuffers();
		
		CameraFront.SetCameraPositionAndTarget_UpVecY(
				VectorFunctions.VGet(-20.0f, 30.0f, -20.0f), 
				VectorFunctions.VGet(20.0f, 0.0f, 20.0f));
	}
	
	@Override
	protected void Draw() {
		GLWrapper.glViewport(0, 0, this.GetWidth(), this.GetHeight());
		drawer.Draw();
	}
}

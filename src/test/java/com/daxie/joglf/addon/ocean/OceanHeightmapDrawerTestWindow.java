package com.daxie.joglf.addon.ocean;

import com.daxie.basis.coloru8.ColorU8Functions;
import com.daxie.basis.vector.VectorFunctions;
import com.daxie.joglf.gl.front.CameraFront;
import com.daxie.joglf.gl.window.JOGLFWindow;
import com.daxie.joglf.gl.wrapper.GLWrapper;

class OceanHeightmapDrawerTestWindow extends JOGLFWindow{
	private OceanHeightmapDrawer drawer;

	@Override
	protected void Init() {
		drawer=new OceanHeightmapDrawer(128, 512.0f, 4.0f, 50.0f, 1.0f, 1.0f);
		drawer.SetColor(ColorU8Functions.GetColorU8(0.0f, 1.0f, 0.0f, 1.0f));
	}
	
	@Override
	protected void Update() {
		drawer.Update();
		drawer.AdvanceTime(1.0f/30.0f);
		
		CameraFront.SetCameraPositionAndTarget_UpVecY(
				VectorFunctions.VGet(-20.0f, 20.0f, -20.0f), 
				VectorFunctions.VGet(20.0f, 0.0f, 20.0f));
	}
	
	@Override
	protected void Draw() {
		GLWrapper.glViewport(0, 0, this.GetWidth(), this.GetHeight());
		drawer.Draw();
	}
}

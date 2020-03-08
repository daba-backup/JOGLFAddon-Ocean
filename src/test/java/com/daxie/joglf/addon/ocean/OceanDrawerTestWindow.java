package com.daxie.joglf.addon.ocean;

import com.daxie.basis.vector.VectorFunctions;
import com.daxie.joglf.gl.window.JOGLFWindow;
import com.daxie.joglf.gl.wrapper.GLWrapper;

class OceanDrawerTestWindow extends JOGLFWindow{
	private OceanDrawer drawer;
	
	@Override
	protected void Init() {
		drawer=new OceanDrawer(64, 256.0f, 4.0f, 20.0f, 1.0f, 1.0f);
	}
	
	@Override
	protected void Update() {
		drawer.Update();
		drawer.AdvanceTime(1.0f/30.0f);
	}
	
	@Override
	protected void Draw() {
		GLWrapper.glViewport(0, 0, this.GetWidth(), this.GetHeight());
		drawer.Draw(10, 10, VectorFunctions.VGet(0.0f, 0.0f, 0.0f));
	}
}

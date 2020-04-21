package com.daxie.joglf.addon.ocean;

import com.daxie.joglf.gl.front.GLFront;
import com.daxie.joglf.gl.window.JOGLFWindow;
import com.daxie.joglf.gl.wrapper.GLVersion;

public class Main {
	public static void main(String[] args) {
		GLFront.Setup(GLVersion.GL4);
		
		JOGLFWindow window=new OceanDrawerTestWindow();
		window.SetExitProcessWhenDestroyed();
	}
}

package com.daxie.joglf.addon.ocean;

import com.daxie.joglf.gl.front.GLFront;
import com.daxie.joglf.gl.window.JOGLFWindow;
import com.daxie.joglf.gl.wrapper.GLVersion;
import com.daxie.log.LogWriter;

public class Main {
	public static void main(String[] args) {
		new Main();
	}
	public Main() {
		LogWriter.SetLogLevelFlags(LogWriter.LOG_LEVEL_ALL);
		GLFront.Setup(GLVersion.GL4);
		
		JOGLFWindow window=new OceanDrawerTestWindow();
		window.SetExitProcessWhenDestroyed();
	}
}

package com.shilocity.pong.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.shilocity.pong.PongGame;

public class DesktopLauncher {
	public static final int VERSION_MAJOR = 0;
	public static final int VERSION_MINOR = 2;
	public static final int VERSION_REVISION = 0;
	
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Mr. Pong (v" +  VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_REVISION + ")";
		config.width = 1280;
		config.height = 768;
		config.fullscreen = false;
		new LwjglApplication(new PongGame(), config);
	}
}

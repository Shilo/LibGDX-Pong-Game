package com.shilocity.pong;

import com.badlogic.gdx.graphics.Color;

public class BodyUserData {
	public enum CollisionType {
		UNKNOWN,
		WALL,
		BALL,
		PADDLE
	}
	
	public CollisionType collisionType = CollisionType.UNKNOWN;
	public Color lineColor = Color.WHITE;
	public Color fillColor = Color.RED;
	
	private static int _targetIncrementColor = 0x000000;
	private static int _lastTargetIncrementColor = _targetIncrementColor;
	private static double _lastTargetIncrementColorTime = 0.0f;
	private static double _targetIncrementColorTime = 0.0f;
	
	public static void setTargetAndLastIncrementColor(int color) {
		_targetIncrementColor = _lastTargetIncrementColor = color;
	}
	
	public static void setTargetIncrementColor(int targetColor, double targetTime) {
		_lastTargetIncrementColor = _targetIncrementColor;
		_targetIncrementColor = targetColor;
		_lastTargetIncrementColorTime = _targetIncrementColorTime;
		_targetIncrementColorTime = targetTime;
	}
	
	public static Color incrementColor(double currentTime) {
		double maxTime = _targetIncrementColorTime-_lastTargetIncrementColorTime;
		double progressTime = currentTime-_lastTargetIncrementColorTime;
		double progress = Math.min(progressTime/maxTime, 1.0f);
		
		double lastTargetIncrementColorRed = ((_lastTargetIncrementColor & 0xFF0000) >> 16) / 255.0f;
		double lastTargetIncrementColorGreen = ((_lastTargetIncrementColor & 0xFF00) >> 8) / 255.0f;
		double lastTargetIncrementColorBlue = (_lastTargetIncrementColor & 0xFF) / 255.0f;
	    
		double targetIncrementColorRed = ((_targetIncrementColor & 0xFF0000) >> 16) / 255.0f;
		double targetIncrementColorGreen = ((_targetIncrementColor & 0xFF00) >> 8) / 255.0f;
		double targetIncrementColorBlue = (_targetIncrementColor & 0xFF) / 255.0f;
	    
		double red = (targetIncrementColorRed - lastTargetIncrementColorRed) * progress + lastTargetIncrementColorRed;
		double green = (targetIncrementColorGreen - lastTargetIncrementColorGreen) * progress + lastTargetIncrementColorGreen;
		double blue = (targetIncrementColorBlue - lastTargetIncrementColorBlue) * progress + lastTargetIncrementColorBlue;
		
		return new Color((float)red, (float)green, (float)blue, 1.0f);
	}
}

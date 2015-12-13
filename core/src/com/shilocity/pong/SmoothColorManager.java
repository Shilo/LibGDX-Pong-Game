package com.shilocity.pong;

import com.badlogic.gdx.graphics.Color;

public class SmoothColorManager {
	private int _targetIncrementColor = 0x000000;
	private int _lastTargetIncrementColor = _targetIncrementColor;
	private double _lastTargetIncrementColorTime = 0.0f;
	private double _targetIncrementColorTime = 0.0f;
	
	public void setTargetAndLastIncrementColor(int color) {
		_targetIncrementColor = _lastTargetIncrementColor = color;
	}
	
	public void setTargetIncrementColor(int targetColor, double targetTime) {
		_lastTargetIncrementColor = _targetIncrementColor;
		_targetIncrementColor = targetColor;
		_lastTargetIncrementColorTime = _targetIncrementColorTime;
		_targetIncrementColorTime = targetTime;
	}
	
	public Color incrementedColor(double currentTime) {
		return incrementedColor(currentTime, 1.0f);
	}
	
	public Color incrementedColor(double currentTime, float alpha) {
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
		
		return new Color((float)red, (float)green, (float)blue, alpha);
	}
}

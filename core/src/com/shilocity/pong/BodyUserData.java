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
}

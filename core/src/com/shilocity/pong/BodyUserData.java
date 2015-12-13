package com.shilocity.pong;

public class BodyUserData {
	public enum CollisionType {
		UNKNOWN,
		WALL,
		BALL,
		PADDLE
	}
	
	public CollisionType collisionType = CollisionType.UNKNOWN;
}

package com.shilocity.pong;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;

public class Utils {
	public static int randomHex() {
		Random rand = new Random();
		return rand.nextInt(0xffffff);
	}
	
	public static Color randomColor() {
		return new Color(randomHex());
	}
}

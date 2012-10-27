package jmar.games.tank;

import jmar.games.tank.LevelBuilder;

public class StandardLevels {
	public static void buildLevelOne(LevelBuilder builder) {
		//
		// Add Positions
		//
		builder.addMiddleStartPositionWithCheck(100, 300);
		builder.addMiddleStartPositionWithCheck(200, 300);
		builder.addMiddleStartPositionWithCheck(300, 300);
		builder.addMiddleStartPositionWithCheck(400, 300);
		builder.addMiddleStartPositionWithCheck(500, 300);
		builder.addMiddleStartPositionWithCheck(600, 300);
		builder.addMiddleStartPositionWithCheck(700, 300);
		
		//
		// Add Walls
		//
		builder.addWallWithCheck(0  , 40 , 500, 30);
		builder.addWallWithCheck(300, 530, 500, 30);
	}
}

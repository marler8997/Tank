package jmar.games.tank;

public class Level {
	
	//
	// Starting Positions
	//
	public final Position[] middleStartPositions;
	
	//
	// Walls
	//
	public final Wall[] walls;
	
	
	public Level(Position[] middleStartPositions, Wall[] walls) {
		this.middleStartPositions = middleStartPositions;
		this.walls = walls;
	}
	
	
	public void print() {
		System.out.println("Start Positions:");
		for(int i = 0; i < middleStartPositions.length; i++) {
			Position position = middleStartPositions[i];
			System.out.println(String.format("  %dx%d", position.x, position.y));
		}
		System.out.println("Walls:");
		for(int i = 0; i < walls.length; i++) {
			Wall wall = walls[i];
			System.out.println(String.format("  %s", wall.toString()));
		}
	}
}

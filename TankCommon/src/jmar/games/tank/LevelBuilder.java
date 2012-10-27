package jmar.games.tank;

import java.util.ArrayList;

public class LevelBuilder {
	public int arenaWidth, arenaHeight;
	private final ArrayList<Position> middleStartPositions;
	private final ArrayList<Wall> walls;
	
	private final Wall positionAreaChecker;
	
	public LevelBuilder() {
		this.arenaWidth = Settings.defaultArenaWidth;
		this.arenaHeight = Settings.defaultArenaHeight;
		this.middleStartPositions = new ArrayList<Position>();
		this.walls = new ArrayList<Wall>();
		
		this.positionAreaChecker = new Wall(0, 0, Settings.puckMaxHalfArenaSize, Settings.puckMaxHalfArenaSize);
	}
	
	public Level makeLevel() {
		Position[] middleStartPositionsArray = new Position[middleStartPositions.size()];
		Wall[] wallsArray = new Wall[walls.size()];
		
		middleStartPositions.toArray(middleStartPositionsArray);
		walls.toArray(wallsArray);
		
		return new Level(middleStartPositionsArray, wallsArray);
	}
	
	public void addMiddleStartPositionWithCheck(int middleX, int middleY) {
		positionAreaChecker.x = middleX - Settings.puckMaxHalfArenaSize;
		positionAreaChecker.y = middleX - Settings.puckMaxHalfArenaSize;
		for(int i = 0; i < walls.size(); i++) {
			Wall wall = walls.get(i);
			if(positionAreaChecker.intersects(wall)) {
				throw new IllegalStateException(String.format("Starting position %dx%d ('%s') intersects with wall '%s'",
						middleX, middleY, positionAreaChecker, wall));
			}
		}
		this.middleStartPositions.add(new Position(middleX, middleY));
	}
	
	// Returns false if wall could not be added, true otherwise
	public void addWallWithCheck(int x, int y, int width, int height) {
		addWallWithCheck(new Wall(x,y,width,height));
	}
	
	// Returns false if wall could not be added, true otherwise
	public void addWallWithCheck(Wall newWall) {
		//
		// Check that wall is within boundaries
		//
		if(newWall.x < 0 || newWall.y < 0 || newWall.x + newWall.width > arenaWidth ||
				newWall.y + newWall.height > arenaHeight) {
			throw new IllegalStateException(String.format("Wall '%s' is not within the arena boundaries %d %d", newWall, arenaWidth, arenaHeight));			
		}
		
		for(int i = 0; i < walls.size(); i++) {
			Wall wall = walls.get(i);
			if(newWall.intersects(wall)) {
				throw new IllegalStateException(String.format("Wall '%s' intersects with wall '%s'", newWall, wall));
			}
		}
		walls.add(newWall);
	}
}

package jmar.games.tank;

public class MovingArenaObject extends ArenaArea {

    public int inverseVelocityTimePerArenaUnit;
    
    int arenaUnitXOffset, arenaUnitYOffset;
	
	public MovingArenaObject(int middleX, int middleY, int oddHalfWidth, int oddHalfHeight, int inverseVelocityTimePerArenaUnit) {
		super(middleX, middleY, oddHalfWidth, oddHalfHeight);
		this.inverseVelocityTimePerArenaUnit = inverseVelocityTimePerArenaUnit;
		this.arenaUnitXOffset = 0;
		this.arenaUnitYOffset = 0;
	}
	
	// returns true if it moves
	public boolean oneTimestepMoveLeft() {
		arenaUnitXOffset--;
		if(arenaUnitXOffset < 0) {
			left--;
			right--;
			arenaUnitXOffset = inverseVelocityTimePerArenaUnit;
			return true;
		}
		return false;
	}
	public boolean oneTimestepMoveRight() {
		arenaUnitXOffset++;
		if(arenaUnitXOffset > inverseVelocityTimePerArenaUnit) {
			left++;
			right++;
			arenaUnitXOffset = 0;
			return true;
		}
		return false;
	}
	public boolean oneTimestepMoveDown() {
		arenaUnitYOffset--;
		if(arenaUnitYOffset < 0) {
			bottom--;
			top--;
			arenaUnitYOffset = inverseVelocityTimePerArenaUnit;
			return true;
		}
		return false;		
	}
	public boolean oneTimestepMoveUp() {
		arenaUnitYOffset++;
		if(arenaUnitYOffset > inverseVelocityTimePerArenaUnit) {
			bottom++;
			top++;
			arenaUnitYOffset = 0;
			return true;
		}
		return false;		
	}
	
}

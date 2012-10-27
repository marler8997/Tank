package jmar.games.tank;

public class ArenaArea {
	//    ---------------------------------------------------------------------
	// 11 |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	// 10 |   |   |   |   |   |   |      middleX =  8 |   |   |   |   |   |   |
	//    ------------------------|      middleY =  8 |------------------------
	// 9  |   |   |   |   |   |   | oddhalfWidth =  2 |   |   |   |   |   |   |
	//    ------------------------|oddhalfHeight =  2 |------------------------
	// 8  |   |   |   |   |   |   |                   |   |   |   |   |   |   |
	//    ------------------------|       leftX  =  6 |------------------------
	// 7  |   |   |   |   |   |   |      rightX  = 10 |   |   |   |   |   |   |
	//    ------------------------|      bottomY =  6 |------------------------ 
	// 6  |   |   |   |   |   |   |         topY = 10 |   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	// 5  |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | 
	//    ---------------------------------------------------------------------
	// 4  |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	// 3  |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	// 2  |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	// 1  |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	// 0  |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	//    0   1   2   3   4   5   6   7   8   9   10  11  12  13  14  15  16
	
	
	protected int left, right, bottom, top;
	// width = oddHalfWidth + 1
	// height = oddHalfHeight + 1
	
	public ArenaArea(int middleX, int middleY, int oddHalfWidth, int oddHalfHeight) {
		this.left   = middleX - oddHalfWidth;
		this.right  = middleX + oddHalfWidth;
		this.bottom = middleY - oddHalfHeight;
		this.top    = middleY + oddHalfHeight;
	}
	public int getLeft() { return left; }
	public int getRight() { return right; }
	public int getBottom() { return bottom; }
	public int getTop() { return top; }
	public int getMiddleX() { return left + ((right - left) / 2); }
	public int getMiddleY() { return bottom + ((top - bottom) / 2); }
	
	public void copyBottomLeftTo(ArenaPoint point) {
		point.x = this.left;
		point.y = this.bottom;
	}
	public void moveUsingBottomLeft(int left, int bottom) {
		int diffX   = this.right - this.left;
		int diffY   = this.top   - this.bottom;
		this.left   = left;
		this.bottom = bottom;
		this.right  = left + diffX;
		this.top    = bottom + diffY;
	}
	public boolean intersects(ArenaArea arenaArea) {
		if(this.left    > arenaArea.right ||
		   this.right   < arenaArea.left  ||
		   this.bottom  > arenaArea.top   ||
		   this.top     < arenaArea.bottom) return false;
		return true;
	}	
	public boolean bottomLeftEquals(int x, int y) {
		return (this.left == x && this.bottom == y);
	}
	public boolean bottomLeftEquals(ArenaPoint point) {
		return (this.left == point.x && this.bottom == point.y);
	}
}

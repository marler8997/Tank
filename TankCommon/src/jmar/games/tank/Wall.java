package jmar.games.tank;

public class Wall {
	public int x,y;
	public int width,height;
	public int rightLimit,topLimit;	
	public Wall(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.rightLimit = x + width - 1;
		this.topLimit = y + height - 1;
	}
	
	
	public boolean intersects(Wall wall) {
		if(this.x           >= wall.rightLimit) return false;
		if(this.rightLimit <= wall.x          ) return false;
		if(this.y           >= wall.topLimit  ) return false;
		if(this.topLimit   <= wall.y          ) return false;
		return true;
	}
	
	public String toString() {
		return String.format("Wall pos=%dx%d size=%dx%d", x, y, width, height);
	}
}

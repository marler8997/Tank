package jmar.games.tank;

public class TankGameSettings {	
	public int windowWidth;
	public int windowHeight;
	
	public String users[];
	public String servers[];
	
	public TankGameSettings() {
	}
	
	public void setDefaults() {
		this.windowWidth = 800;
		this.windowHeight = 600;		
	}
}

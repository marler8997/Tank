package jmar.games.tank;

public class ClientGameBuilderInfo {
	public final String userName;
	public int startPositionIndex;
	
	public ClientGameBuilderInfo(String userName) {
		this.userName = userName;
		this.startPositionIndex = -1;
	}
}

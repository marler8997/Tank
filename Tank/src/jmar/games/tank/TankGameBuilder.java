package jmar.games.tank;

import jmar.games.tank.Constants;
import jmar.games.tank.LevelBuilder;

public class TankGameBuilder {
	public final TankGameSettingsManager gameSettingsManager;
	public final ClientNetworkHandler networkHandler;

	public String myUserName;
	public String myPassword;
	public byte[] offlineKey;
	private byte myClientIndex;
	
	public ClientGameBuilderInfo[] clients;
	public final LevelBuilder levelBuilder;	
	
	public TankGameBuilder(TankGameSettingsManager gameSettingsManager) {
		this.gameSettingsManager        = gameSettingsManager;
		this.networkHandler             = null;
		this.clients                    = null;
		this.levelBuilder               = new LevelBuilder();
	}
	
	public void setMyClientIndex(byte clientIndex) {
		this.myClientIndex = clientIndex;
	}
	
	public boolean iAmInOfflineMode() {
		return offlineKey != null;
	}
	
	public String toString() {
		return String.format("{myUserName:'%s',myClientIndex:%d,arenaWidth:%d,arenaHeight:%d}",
				myUserName, myClientIndex, levelBuilder.arenaWidth, levelBuilder.arenaHeight);
	}
	
	public byte getMyClientIndex() {
		return myClientIndex;
	}
	
	public void refreshClients(ClientGameBuilderInfo[] clients) {
		this.clients = clients;
		for(byte i = 0; i < clients.length; i++) {
			if(myUserName.equals(clients[i].userName)) {
				this.myClientIndex = i;
				return;
			}
		}
		throw new IllegalStateException(String.format("Got a list of new clients, but my client name '%s' was not in the list?", myUserName));
	}
}

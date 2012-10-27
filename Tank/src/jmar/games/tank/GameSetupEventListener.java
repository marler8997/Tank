package jmar.games.tank;

import java.io.IOException;

public interface GameSetupEventListener {
	public void newClient(byte clientID, String userName);
	public void levelDownload(byte[] level);
	public void clientReady(byte clientID);
	public void clientLeft(byte clientID);
	public void gameSetupComplete(byte[] clientStartPositionIndices);
	
	public void badDataFromServer(String message);
	public void ioexception(IOException e);
}

package jmar.games.tank;

public interface TankGroupHostSetupManagerCallback {
	//void newClient(TankGroupHostSetupManager groupManager, RemoteTankClient newClient);
	//void closeClient(TankGroupHostSetupManager groupManager, RemoteTankClient closedClient);
	void change(TankGroupHostSetupManager groupManager);
	
	void exception(Exception e);
	void groupClosed();
}

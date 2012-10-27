package jmar.games.tank;

import java.io.IOException;

public interface JoinServerEventListener {
	void connectingOverTcp();
	void sendingCredentials();
	void waitingForCredentialResponse();
	void credentialsRejected();	
	void duplicateUser();
	void ioException(IOException e);	
	
	void done(boolean success); // This will always be called at the end
}

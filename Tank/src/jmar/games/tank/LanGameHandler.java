package jmar.games.tank;

import java.io.IOException;
import java.net.SocketException;

public interface LanGameHandler {
	void foundLanHost(String hostUserName);
	void ioException(IOException e);
	void socketException(SocketException e);
}

package jmar.games.tank;

import java.io.IOException;

import jmar.games.net.TcpSelectServerHandler;

public interface HostThreadCallback extends TcpSelectServerHandler {
	public void stopped();
	public void ioException(IOException e);
}

package jmar.games.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface TcpSelectServerHandler {
	void serverStopped(); // Always called if the server is stopped
	int listenSocketClosed(int clientCount);
	
	int clientNewCallback(int clientCount, SocketChannel channel) throws IOException;
	int clientCloseCallback(int clientCount, SocketChannel channel) throws IOException;
	int clientDataCallback(SocketChannel channel, byte[] bytes, int bytesRead) throws IOException;
}

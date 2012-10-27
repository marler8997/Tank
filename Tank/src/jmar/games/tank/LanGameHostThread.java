package jmar.games.tank;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import jmar.games.net.TcpSelectServer;
import jmar.games.net.TcpSelectServerHandler;
/*
public class LanGameHostThread implements IHostThread {
	
	private final HostThreadCallback callback;
	private TcpSelectServer tcpSelectServer;
	
	public LanGameHostThread(HostThreadCallback callback) {
		this.callback = callback;
		this.tcpSelectServer = new TcpSelectServer();
	}
	
	public void prepareToRun() {
		this.tcpSelectServer.prepareToRun();
	}
	
	public void stop() {
		this.tcpSelectServer.stop();
	}

	public void run() {
		final byte[] readBytes = new byte[512];

		try {
			this.tcpSelectServer.run(Settings.lanServerPort, readBytes, new TcpSelectServerHandler() {
				@Override
				public void serverStopped() {
					// TODO Auto-generated method stub
					
				}
				public int listenSocketClosed(int clientCount) {
					return TcpSelectServer.STOP_SERVER;
				}
				public int clientNewCallback(int clientCount, SocketChannel channel) throws IOException {
					return callback.clientNewCallback(clientCount, channel);
				}
				public int clientDataCallback(SocketChannel channel, byte[] readBytes, int bytesRead) throws IOException {
					return callback.clientDataCallback(channel, readBytes, bytesRead);
				}
				public int clientCloseCallback(int clientCount, SocketChannel channel) throws IOException {
					return callback.clientCloseCallback(clientCount, channel);
				}
			});
		} catch (IOException e) {
			callback.ioException(e);
		} finally {
			callback.stopped();
		}
	}

}
*/
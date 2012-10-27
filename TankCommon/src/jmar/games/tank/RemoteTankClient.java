package jmar.games.tank;

import java.nio.channels.SocketChannel;

import jmar.games.net.BytesChunker;

public class RemoteTankClient {
	public final SocketChannel socketChannel;
	public final TcpUdpAddressPortPair network;
	public final BytesChunker chunker;
	public final RemoteTankClientCredentials credentials;
	
	public RemoteTankClient(SocketChannel socketChannel, TcpUdpAddressPortPair network) {
		this.socketChannel = socketChannel;
		this.network = network;
		this.chunker = new BytesChunker(512);
		this.credentials = new RemoteTankClientCredentials();		
	}
	
	public String getIdString() {
		String userName = credentials.getUserName();
		if(userName != null) return userName;
		return network.tcpKeyString;
	}
}

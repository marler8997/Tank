package jmar.games.tank;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.IllegalSelectorException;

public class LanGameListener implements Runnable {
	private final LanGameHandler lanGameHandler;
	public final int listenPort, remotePort;
	private final DatagramSocket lanQuerySocket;
	private InetAddress broadcastAddress;
	
	public LanGameListener(LanGameHandler lanGameHandler, int listenPort, int remotePort) throws SocketException {
		this.lanGameHandler = lanGameHandler;
		this.listenPort = listenPort;
		this.remotePort = remotePort;
		this.lanQuerySocket = new DatagramSocket();
		try {
			this.broadcastAddress = InetAddress.getByAddress(new byte[]{(byte)255,(byte)255,(byte)255,(byte)255});
		} catch (Exception e) {
			throw new IllegalStateException("This should never happen");
		}
		
	}
	
	public void sendLanQuery() throws IOException {
		byte[] sendBytes = new byte[0]; // For now the query won't include any info		
		DatagramPacket sendPacket = new DatagramPacket(sendBytes, sendBytes.length, broadcastAddress, remotePort);
		lanQuerySocket.send(sendPacket);
	}
	
	public void run() {
		DatagramSocket serverSocket;
		try {
			serverSocket = new DatagramSocket(listenPort);
		} catch (SocketException e) {
			lanGameHandler.socketException(e);
			return;
		}

		byte[] receiveBytes = new byte[256];
		DatagramPacket receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);
		
		System.out.println("[Lan Game Listener] Listening for games on the LAN");
		
		while(true) {
			try {
				serverSocket.receive(receivePacket);
			} catch (IOException e) {
				lanGameHandler.ioException(e);
				serverSocket.close();
				return;
			}

		  if(receivePacket.getPort() != remotePort) {
			  InetAddress inetAddress = receivePacket.getAddress();
			  System.out.println(String.format("[LAN Listener] [Debug] Ignored packet from %s:%d", inetAddress.toString(), receivePacket.getPort()));
			  continue;
		  }
		  
		  int bytesReceived = receivePacket.getLength();
		  if(bytesReceived <= 0) {
			  InetAddress inetAddress = receivePacket.getAddress();
			  System.out.println(String.format("[LAN Listener] [Debug] Got empty packet from %s:%d", inetAddress.toString(), receivePacket.getPort()));
			  continue;
		  }
		  
		  int userLength = receiveBytes[0];
		  if(bytesReceived < userLength + 1) {
			  InetAddress inetAddress = receivePacket.getAddress();
			  System.out.println(String.format("[LAN Listener] [Debug] Got invalid packet from %s:%d", inetAddress.toString(), receivePacket.getPort()));
			  continue;
		  }
		  
		  String userLanGame = new String(receiveBytes, 1, userLength);
		  lanGameHandler.foundLanHost(userLanGame);
	   }
		
	}
	
}

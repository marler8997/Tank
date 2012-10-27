package jmar.udpforgames;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class SimpleUdpServer {
	public final DatagramChannel channel;
	
	public SimpleUdpServer(DatagramChannel channel) {
		this.channel = channel;
	}
	
	public void send(ByteBuffer sendBuffer, SocketAddress client) throws IOException {
		//System.out.println(String.format("[UDP] Sending %d bytes to %s", sendBuffer.remaining(), client.toString()));
		channel.send(sendBuffer, client);
	}
	
	/*
	public SocketAddress receive(ByteBuffer recvBuffer, int timeoutMillis) throws IOException {
		int sleepTime = timeoutMillis / 10;
		if(sleepTime < 0) sleepTime = 0;
		if(sleepTime > 500) sleepTime = 500;
	
		long timeoutNano = timeoutMillis * 1000000;
		
		long startTimeNano = System.nanoTime();
		
		SocketAddress from;
		while(true) {
			from = channel.receive(recvBuffer);
			if(from != null) {
				System.out.println(String.format("[UDP] Received %d bytes from %s", recvBuffer.position(), serverString));
				return from;
			}
			
			long nowNano = System.nanoTime();
			if(nowNano - startTimeNano > timeoutNano) {
				return null;
			}
			
			try { Thread.sleep(sleepTime); } catch(InterruptedException e) { }			
		}		
	}	
	*/
	
	public SocketAddress receiveNonBlocking(ByteBuffer recvBuffer) throws IOException {
		SocketAddress from = channel.receive(recvBuffer);
		if(from != null) {
			//System.out.println(String.format("[UDP] Received %d bytes from %s", recvBuffer.position(), from.toString()));
		}
		return from;
	}	

}

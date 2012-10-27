package jmar.udpforgames;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class SimpleUdpClient {
	public final DatagramChannel channel;
	public final String serverString;
	
	public SimpleUdpClient(DatagramChannel channel) {
		this.channel = channel;
		this.serverString = channel.socket().getInetAddress().toString();
	}
	
	public void send(ByteBuffer sendBuffer) throws IOException {
		//System.out.println(String.format("[UDP] Sending %d bytes to %s", sendBuffer.remaining(), serverString));
		channel.write(sendBuffer);
	}
	
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
				//System.out.println(String.format("[UDP] Received %d bytes from %s", recvBuffer.position(), serverString));
				return from;
			}
			
			long nowNano = System.nanoTime();
			if(nowNano - startTimeNano > timeoutNano) {
				return null;
			}
			
			try { Thread.sleep(sleepTime); } catch(InterruptedException e) { }			
		}		
	}	
	
	public SocketAddress receiveNonBlocking(ByteBuffer recvBuffer) throws IOException {
		SocketAddress from = channel.receive(recvBuffer);
		if(from != null) {
			//System.out.println(String.format("[UDP] Received %d bytes from %s", recvBuffer.position(), serverString));
		}
		return from;
	}	
	
}

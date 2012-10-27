package jmar.games.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import jmar.ShouldntHappenException;

public class TcpSelectServer {
	public static final int NO_INSTRUCTION = 0x00;
	public static final int CLOSE_CLIENT   = 0x01;
	public static final int STOP_SERVER    = 0x02;
	
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private boolean keepRunning;
	
	public TcpSelectServer() {
		this.selector = null;
		this.keepRunning = false;
	}
	
	public void stop() {
		this.keepRunning = false;
		
		ServerSocketChannel serverChannelCached = this.serverChannel;
		Selector selectorCached = this.selector;
		
		if(serverChannelCached != null) {
			try {serverChannelCached.close();} catch (IOException e) { }
		}
		if(selectorCached != null) {
			for (Iterator<SelectionKey> iterator = selectorCached.keys().iterator(); iterator.hasNext();) {
				SelectionKey key = iterator.next();
				if(key.channel() instanceof SocketChannel) {
					SocketChannel channel = ((SocketChannel)key.channel());
					Socket socket = channel.socket();
					
					try { socket.shutdownInput(); } catch(IOException e) { }
					try { socket.shutdownOutput(); } catch(IOException e) { }
					try { socket.close(); } catch(IOException e) { }
				}
			}
		}
	}
	
	public void prepareToRun() {
		this.keepRunning = true;
	}
	
	public void run(int serverPort, byte[] readBytes, TcpSelectServerHandler handler) throws IOException {
		ByteBuffer readBuffer = ByteBuffer.wrap(readBytes);
		int clientCount = 0;
		
		try {
			serverChannel = ServerSocketChannel.open();
			serverChannel.socket().bind(new InetSocketAddress(serverPort));
			serverChannel.configureBlocking(false);	
			
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			while(keepRunning) {
				System.out.println("[TcpSelectServer] select...");
				selector.select();
				for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext();) {
					SelectionKey key = iterator.next(); 
					iterator.remove(); 
					
					try {
						System.out.println(String.format("[TcpSelectServer] popped acceptable=%b readable=%b valid=%b",
							key.isAcceptable(), key.isReadable(), key.isValid()));
						/*
						if (key.isConnectable()) {
							SocketChannel client = (SocketChannel) key.channel();
							System.out.println(String.format("[TcpSelectServer] finishConnect() on %s", client.socket().getInetAddress().toString()));
							client.finishConnect();
						} 
						*/
					
						if (key.isAcceptable()) {
							// accept connection jav
							SocketChannel clientSocketChannel = serverChannel.accept(); 
							Socket clientSocket = clientSocketChannel.socket();
							
							clientCount++;
							int instruction = handler.clientNewCallback(clientCount, clientSocketChannel);
							
							if((instruction & CLOSE_CLIENT) != 0) {
								clientCount--;
								try {clientSocket.shutdownInput();} catch(IOException e) { }
								try {clientSocket.shutdownOutput();} catch(IOException e) { }
								try {clientSocket.close();} catch(IOException e) { }
								int newInstruction = handler.clientCloseCallback(clientCount, clientSocketChannel);
								if((newInstruction & STOP_SERVER) != 0) return;
							} else {
								clientSocketChannel.configureBlocking(false); 
								clientSocket.setTcpNoDelay(true);						
								clientSocketChannel.register(selector, SelectionKey.OP_READ);
								System.out.println(String.format("[TcpSelectServer] accepted %s", clientSocket.getInetAddress().toString()));				
							}
							
							if((instruction & STOP_SERVER) != 0) return;
						} else if (key.isReadable()) {
							
							SocketChannel channel = ((SocketChannel)key.channel());
							
							try {
								readBuffer.clear();
								int bytesRead = channel.read(readBuffer);
								if(bytesRead <= 0) {
									clientCount--;
									try {channel.socket().shutdownInput();} catch(IOException e2) { }
									try {channel.socket().shutdownOutput();} catch(IOException e2) { }
									try {channel.socket().close();} catch(IOException e2) { }
									
									int instruction = handler.clientCloseCallback(clientCount, channel);
									if((instruction & STOP_SERVER) != 0) return;
								} else {								
									int instruction = handler.clientDataCallback(channel, readBytes, readBuffer.position());
									
									if((instruction & CLOSE_CLIENT) != 0) {
										clientCount--;
										try {channel.socket().shutdownInput();} catch(IOException e2) { }
										try {channel.socket().shutdownOutput();} catch(IOException e2) { }
										try {channel.socket().close();} catch(IOException e2) { }
										
										int newInstruction = handler.clientCloseCallback(clientCount, channel);
										if((newInstruction & STOP_SERVER) != 0) return;								
									}
									
									if((instruction & STOP_SERVER) != 0) return;
								}
							} catch(IOException e) {
								System.out.println(String.format("[TcpSelectServer] IOException from %s: %s", channel.socket().getInetAddress().toString(), e.toString()));
								
								channel.register(selector, 0); // unregister the channel
								
								clientCount--;
								try {channel.socket().shutdownInput();} catch(IOException e2) { }
								try {channel.socket().shutdownOutput();} catch(IOException e2) { }
								try {channel.socket().close();} catch(IOException e2) { }
								
								int instruction = handler.clientCloseCallback(clientCount, channel);
								if((instruction & STOP_SERVER) != 0) return;
							}
						} else {
							//
							// What should I do here
							//
							SocketChannel channel = ((SocketChannel)key.channel());
							throw new ShouldntHappenException(String.format("[TcpSelectServer] !!!!!!!!!!!!!!!!!!!!!!!!! The socket was not acceptable or readable? %s", channel.socket().getInetAddress().toString()));
									
						}
					} catch (CancelledKeyException e) {
						int instruction;
						if(key.channel() == serverChannel) {
							instruction = handler.listenSocketClosed(clientCount);				
						} else {
							SocketChannel channel = ((SocketChannel)key.channel());
							clientCount--;
							instruction = handler.clientCloseCallback(clientCount, channel);
						}
						if((instruction & STOP_SERVER) != 0) return;
						key.channel().register(selector, 0); // unregister the channel
					}
					
				}				
			}

		} finally {
			stop();
			handler.serverStopped();
		}
	}
}

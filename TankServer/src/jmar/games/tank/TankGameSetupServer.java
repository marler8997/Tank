package jmar.games.tank;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.RuntimeErrorException;

import jmar.ByteRef;
import jmar.IntegerSet;
import jmar.games.net.BytesChunker;
import jmar.games.net.ChunkByteBuffer;
import jmar.games.net.InputChannelChunker;
import jmar.games.net.OutputChannelChunker;
import jmar.games.net.TcpSelectServer;
import jmar.games.net.TcpSelectServerHandler;
import jmar.games.tank.Constants;
import jmar.games.tank.PuckClientStateBuilder;
import jmar.games.tank.Settings;

public class TankGameSetupServer {
	
	public static TankClientStateForServer[] runSetupServer() throws IOException {		
		final HashMap<SocketChannel,TankClientStateForServer> clients = new HashMap<SocketChannel,TankClientStateForServer>();
		final ArrayList<TankClientStateForServer> clientsOrderedByID = new ArrayList<TankClientStateForServer>();
		
		final ChunkByteBuffer sendChunkBuffer = new ChunkByteBuffer(512);
		
		final byte[] readBytes = new byte[512];
		
		new TcpSelectServer().run(Settings.serverPort, readBytes, new TcpSelectServerHandler() {

			public void serverStopped() {
				// TODO Auto-generated method stub
				
			}		
			
			public int listenSocketClosed(int clientCount) {
				throw new RuntimeException("Listen Socket was closed");
			}	
			
			private void sendClientUpdateToAllClients() throws IOException {
				byte clientLength = (byte) clients.size();
				
				// Create Update Packet
				sendChunkBuffer.startChunk();
				sendChunkBuffer.chunkBuffer.put(Constants.gameSetupClientUpdate);
				sendChunkBuffer.chunkBuffer.put(clientLength);
				for(byte i = 0; i < clientLength; i++) {
					TankClientStateForServer client = clientsOrderedByID.get(i);
					
					byte userNameLength = (byte) client.userName.length();
					sendChunkBuffer.chunkBuffer.put(userNameLength);
					for(byte j = 0; j < userNameLength; j++) {
						sendChunkBuffer.chunkBuffer.put((byte)client.userName.charAt(j));
					}
				}
				sendChunkBuffer.endChunk();
				
				// Send the update to everyone
				for(byte i = 0; i < clientLength; i++) {
					TankClientStateForServer client = clientsOrderedByID.get(i);
					client.builder.tcpOutputChunker.sendChunkAndResetBuffer(sendChunkBuffer);
				}
			}
			
			public int clientNewCallback(int clientCount, SocketChannel channel) throws IOException  {
				Socket clientSocket = channel.socket();
				
				BytesChunker clientInputChunker = new BytesChunker(512);
				OutputChannelChunker clientOutputChunker = new OutputChannelChunker(channel);
				
				//
				// Add potential client
				//
				Inet4Address clientInet4Address = (Inet4Address) clientSocket.getInetAddress();
				int clientTcpPort = clientSocket.getPort();
				
				TankClientStateForServer newClientState = new TankClientStateForServer(clientInputChunker,
						clientOutputChunker,clientInet4Address, clientTcpPort);
				clients.put(channel, newClientState);
				
				return TcpSelectServer.NO_INSTRUCTION;
			}
			public int clientCloseCallback(int clientCount, SocketChannel channel) throws IOException  {
				TankClientStateForServer clientState = clients.get(channel);
				if(clientState == null)
					throw new IllegalStateException("Internal Bug: Received a SocketChanel that was not in the client dictionary");
				
				System.out.println(String.format("Client %s: Has Closed", clientState.tcpKeyString));
				clients.remove(channel);
				clientsOrderedByID.remove(clientState);
				
				sendClientUpdateToAllClients();
				
				return TcpSelectServer.NO_INSTRUCTION;
			}

			public int clientDataCallback(SocketChannel channel, byte[] tcpSelectReadBytes, int tcpSelectBytesRead) throws IOException  {
				TankClientStateForServer clientState = clients.get(channel);
				if(clientState == null)
					throw new IllegalStateException("Internal Bug (Maybe): Received a SocketChanel that was not in the client dictionary");

				PuckClientStateBuilder clientStateBuilder = clientState.builder;
				clientStateBuilder.tcpInputChunker.addBytes(tcpSelectReadBytes, 0, tcpSelectBytesRead);
				int chunkSize;
				while((chunkSize = clientStateBuilder.tcpInputChunker.getChunk()) > 0) {
					byte[] readBytes = clientStateBuilder.tcpInputChunker.bytes;

					System.out.print(String.format("[Server] Read %d byte chunk from %s:", chunkSize, clientState.tcpKeyString));
					for(int i = 0; i < chunkSize; i++) {
						System.out.print(' ');
						System.out.print(readBytes[i]);
					}
					System.out.println();
					
					//
					// Process Bytes
					//
					if(clientState.userName == null) {	
						int userNameLength = readBytes[0];
						clientState.userName = new String(readBytes, 1, userNameLength);
						
						int credentialLength = readBytes[userNameLength + 1];							
						clientState.credential = new byte[credentialLength];
						System.arraycopy(readBytes, userNameLength + 2, clientState.credential, 0, credentialLength);
						
						int udpSourcePortOffset = 2 + userNameLength + credentialLength;
						clientState.setUdpSourcePort(
								(0xFF00 & (readBytes[udpSourcePortOffset    ] << 8)) |
								(0x00FF & (readBytes[udpSourcePortOffset + 1]     )) );
						
	
						System.out.print(String.format("Client %s Username '%s' UdpSourcePort %d Cred: ", clientState.tcpKeyString,
								clientState.userName, clientState.udpSourcePort));
						for(int i = 0; i < clientState.credential.length; i++) {
							System.out.print(' ');
							System.out.print(clientState.credential[i]);
						}
						System.out.println();
						

						// Check if user is already logged in
						for(Iterator<TankClientStateForServer> iterator = clientsOrderedByID.iterator(); iterator.hasNext();) {
							TankClientStateForServer nextClientState = iterator.next();
							if(nextClientState != clientState && nextClientState.userName != null) {
								if(clientState.userName.equals(nextClientState.userName)) {

									System.out.println(String.format("[GameSetup] Telling '%s' that their is a duplicate username", clientState.userName));
									sendChunkBuffer.startChunk();
									sendChunkBuffer.chunkBuffer.put(Constants.joinResponseDuplicateUser);
									sendChunkBuffer.endChunk();
									try {
										clientStateBuilder.tcpOutputChunker.sendChunk(sendChunkBuffer);
									} catch(IOException e) {}
									
									// TODO: should I inform the client that someone tried to login with their userName?
									return TcpSelectServer.CLOSE_CLIENT;
								}
							}
						}	
						
						
						
						// Respond with client id
						clientsOrderedByID.add(clientState);

						
						System.out.println(String.format("[GameSetup] Telling '%s' that their credentials are accepted", clientState.userName));
						sendChunkBuffer.startChunk();
						sendChunkBuffer.chunkBuffer.put((byte)Constants.joinReponseAccept);  // Accept client credentials
						sendChunkBuffer.endChunk();
						try {
							clientStateBuilder.tcpOutputChunker.sendChunk(sendChunkBuffer);
						} catch(IOException e) {
							return TcpSelectServer.CLOSE_CLIENT;
						}
						sendClientUpdateToAllClients();						
						
					} else {
						
						// Get Command ID
						byte command = readBytes[0];
						switch(command) {
						case Constants.readyToStart:
							// TODO: Tell everyone the client is ready
							System.out.println(String.format("[GameSetup] Client '%s' is ready to start", clientState.userName));
							
							break;
						case Constants.startTheFreakinGame:
							
							System.out.println(String.format("[GameSetup] Client '%s' said to start the freakin game", clientState.userName));							
							return TcpSelectServer.STOP_SERVER;
							//break;			
							
						default:
							throw new UnsupportedOperationException(String.format("Client sent unknown commad %d", readBytes[0]));	
							//break;
						}
					}
				}
				
				return TcpSelectServer.NO_INSTRUCTION;
			}
			
		});

		
		//
		// Create array of client states
		//
		
		TankClientStateForServer[] clientStates = (TankClientStateForServer[]) clientsOrderedByID.toArray();
		
		//
		// Make sure the starting positions are set
		//
		IntegerSet randomStartPositionSet = new IntegerSet(clientStates.length);
		byte[] randomStartPositions = new byte[clientStates.length];
		for(int i = 0; i < clientStates.length; i++) {
			TankClientStateForServer clientState = clientStates[i];
			clientState.startPositionIndex = (byte)randomStartPositionSet.pickRandom();
			randomStartPositions[i] = clientState.startPositionIndex;
		}
		if(!randomStartPositionSet.allPicked()) {
			throw new IllegalStateException("There was an error randomizing the client start positions");
		}
		
		
		//
		// Send Start Game to everyone
		//
		sendChunkBuffer.startChunk();
		sendChunkBuffer.chunkBuffer.put(Constants.gameSetupComplete);
		for(int i = 0; i < randomStartPositions.length; i++) {
			sendChunkBuffer.chunkBuffer.put(randomStartPositions[i]);
		}
		sendChunkBuffer.endChunk();
		
		for(int i = 0; i < clientStates.length; i++) {			
			TankClientStateForServer clientState = clientStates[i];

			System.out.println(String.format("[GameSetup] Telling '%s' SetupComplete (StartPositionIndex=%d)", clientState.userName, clientState.startPositionIndex));
			clientState.builder.tcpOutputChunker.sendChunkAndResetBuffer(sendChunkBuffer);			
			
			// TODO: shutdown tcp connection
			clientState.builder = null; // Release State Builder
		}			
		
		
		return clientStates;
	}

}

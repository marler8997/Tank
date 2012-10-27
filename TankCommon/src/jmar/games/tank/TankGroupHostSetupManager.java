package jmar.games.tank;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import jmar.Base64;
import jmar.ByteArray;
import jmar.CodeBugException;
import jmar.DateTime;
import jmar.ShouldntHappenException;
import jmar.games.net.ChunkByteBuffer;
import jmar.games.net.TcpSelectServer;
import jmar.games.net.TcpSelectServerHandler;
import jmar.udpforgames.SimpleUdpServer;

public class TankGroupHostSetupManager implements TcpSelectServerHandler {
	final ChunkByteBuffer outputChunker;

	public String hostUserName;
	TankGroupHostSetupManagerCallback callback;

	HashMap<SocketChannel,RemoteTankClient> clientTcpMap;
	ArrayList<RemoteTankClient> clientsOrdered;		

	
	enum GroupState {
		notHostingGroup,
		settingUpGame,
		startingGame,
		needToDelayGameStartup,
	};
	private GroupState state;

	public TankGroupHostSetupManager() {
		this.outputChunker = new ChunkByteBuffer(512);
		this.state = GroupState.notHostingGroup;
	}	
	public void stateSettingUpGame(String hostUserName, TankGroupHostSetupManagerCallback callback) {
		this.hostUserName = hostUserName;
		this.callback = callback;

		this.clientTcpMap = new HashMap<SocketChannel, RemoteTankClient>();
		this.clientsOrdered = new ArrayList<RemoteTankClient>();

		this.state = GroupState.settingUpGame;
	}	
	public boolean isHostingGroup() {
		return state != GroupState.notHostingGroup;
	}
	public ArrayList<RemoteTankClient> getClientsOrdered() {
		return clientsOrdered;
	}
	public RemoteTankClient tryGetClient(long tcpAddressPortKey) {
		return clientTcpMap.get(tcpAddressPortKey);
	}
	public void ioException(IOException e) {
		callback.exception(e);
	}
	public void serverStopped() {
		state = GroupState.notHostingGroup;
		callback.groupClosed();
	}
	public int listenSocketClosed(int clientCount) {
		return TcpSelectServer.STOP_SERVER;
	}
	public int clientNewCallback(int clientCount, SocketChannel channel) throws IOException {
		synchronized (state) {
			if(state != GroupState.settingUpGame) {
				sendJoinResponse(channel, Constants.joinResponseNotAcceptingClientsRightNow);
				return TcpSelectServer.CLOSE_CLIENT;
			}
			
			// Get Inet Address and Port
			Socket clientSocket = channel.socket();
			Inet4Address clientInet4Address = (Inet4Address) clientSocket.getInetAddress();
			int clientTcpPort = clientSocket.getPort();

			TcpUdpAddressPortPair addressPortPair = new TcpUdpAddressPortPair(clientInet4Address);
			addressPortPair.setTcpSourcePort(clientTcpPort);

			// Make new data for client
			RemoteTankClient client = new RemoteTankClient(channel, addressPortPair);
			clientTcpMap.put(channel, client);
			clientsOrdered.add(client);

			// TODO: Maybe do something if the client count is off
			if(clientCount != clientsOrdered.size()) {
				System.err.println(String.format("[WARNING] the client count from the TcpSelect server is %d, but I have data on %d clients", clientCount, clientsOrdered.size()));
			}

			callback.change(this);

			return TcpSelectServer.NO_INSTRUCTION;			
		}
	}
	public int clientCloseCallback(int clientCount, SocketChannel channel) throws IOException {
		synchronized (state) {
			RemoteTankClient client = clientTcpMap.get(channel);		
			if(client == null) throw new ShouldntHappenException("Missing RemoteTankClient in the SocketChannel Map");

			
			if(state != GroupState.settingUpGame) {
				if(state == GroupState.startingGame) {
					state = GroupState.needToDelayGameStartup;
				} else {
					throw new IllegalStateException(String.format("Right now we cannot handle a client closing their connection while in state: %d", state));					
				}
			}
			
			clientTcpMap.remove(channel);
			clientsOrdered.remove(client);

			callback.change(this);

			return TcpSelectServer.NO_INSTRUCTION;			
		}
	}
	public int clientDataCallback(SocketChannel channel, byte[] readBytes, int bytesRead) throws IOException {
		synchronized (state) {		
			RemoteTankClient client = clientTcpMap.get(channel);
			if(client == null) throw new ShouldntHappenException("Missing RemoteTankClient in the SocketChannel Map");
			
			client.chunker.addBytes(readBytes, 0, bytesRead);
			
			int chunkSize;
			while((chunkSize = client.chunker.getChunk()) > 0) {
				int result = handleChunk(client, client.chunker.bytes, chunkSize);
				if( ((result & TcpSelectServer.CLOSE_CLIENT) != 0) ||
					((result & TcpSelectServer.STOP_SERVER ) != 0) ) {
					return result;
				}
			}
			
			return TcpSelectServer.NO_INSTRUCTION;
		}
	}
	
	int handleChunk(RemoteTankClient client, byte[] chunkBytes, int chunkSize) throws IOException {
		if(state == GroupState.settingUpGame) {

			if(!client.credentials.isAuthenticated()) {	

				if(client.credentials.waitingForInitialChunk()) {
					//
					// Parse initial packet from client
					//
					System.out.println(String.format("[Group] Parsing initial chunk from '%s'", client.getIdString()));
					try {
						client.credentials.parseInitialPacket(chunkBytes, 0);
					} catch(RuntimeException e) {
						System.out.println(String.format("[Group] Could not parse initial chunk fromt '%s': %s", client.getIdString(), e.getMessage()));
						sendJoinResponse(client, Constants.joinResponseCouldNotParseChunk);
						return TcpSelectServer.CLOSE_CLIENT;
					}
					
					//
					// Check if the client's user name is a duplicate
					//
					String clientUserName = client.credentials.getUserName();
					if(clientUserName.equalsIgnoreCase(hostUserName)) {
						System.out.println(String.format("Client user name is same as the hosts '%s'", hostUserName));
						sendJoinResponse(client, Constants.joinResponseDuplicateUser);
						return TcpSelectServer.CLOSE_CLIENT;
					}
					
					for(int i = 0; i < clientsOrdered.size(); i++) {
						RemoteTankClient otherClient = clientsOrdered.get(i);
						if(client == otherClient) continue;
						String otherClientUserName = otherClient.credentials.getUserName();
						if(otherClientUserName == null) continue;
						if(clientUserName.equalsIgnoreCase(otherClientUserName)) {
							System.out.println(String.format("Client user name is same as another clients '%s'", clientUserName));
							sendJoinResponse(client, Constants.joinResponseDuplicateUser);
							return TcpSelectServer.CLOSE_CLIENT;
						}
					}
					
					//
					// Accept the client if they sent an offline key
					//
					if(client.credentials.isOffline) {
						if(!client.credentials.isAuthenticated()) throw new CodeBugException("The client is offline and the initial packet is parsed but it is not authenticated?");
						System.out.println(String.format("Accepted client '%s'", client.getIdString()));
						sendJoinResponse(client, Constants.joinReponseAccept);
						callback.change(this);
						return TcpSelectServer.NO_INSTRUCTION;
					}
					
					
					client.credentials.saveDateString = DateTime.nowStringSlashes();
					
					// Send success response
					outputChunker.startChunk();
					outputChunker.chunkBuffer.put((byte)0); // ok so far
					outputChunker.chunkBuffer.put((byte)hostUserName.length());
					for(int i = 0; i < hostUserName.length(); i++) {
						outputChunker.chunkBuffer.put((byte)hostUserName.charAt(i));
					}
					outputChunker.chunkBuffer.put((byte)client.credentials.saveDateString.length());
					for(int i = 0; i < client.credentials.saveDateString.length(); i++) {
						outputChunker.chunkBuffer.put((byte)client.credentials.saveDateString.charAt(i));
					}
					outputChunker.endChunk();
					client.socketChannel.write(outputChunker.chunkBuffer);
					
					return TcpSelectServer.NO_INSTRUCTION;
				}
				
				//
				// Handle the host key from the client
				//
				byte[] decryptedBytes;
				try {
					decryptedBytes = TankDecryption.decrypt(chunkBytes, 0, chunkSize);
				} catch (IllegalBlockSizeException e) {
					sendJoinResponse(client, Constants.joinResponseDecryptionFailed);
					callback.change(this);
					return TcpSelectServer.CLOSE_CLIENT;
				} catch (BadPaddingException e) {
					sendJoinResponse(client, Constants.joinResponseDecryptionFailed);
					callback.change(this);
					return TcpSelectServer.CLOSE_CLIENT;
				}

				String decryptedString = new String(decryptedBytes);
				System.out.println(String.format("DecryptedString: '%s'", decryptedString));
				// Check key
				String userName = client.credentials.getUserName();
				int indexOf;
				indexOf = decryptedString.indexOf(client.credentials.getUserName());
				if(indexOf < 0) {
					System.out.println(String.format("UserName not found in host key '%s'", client.credentials.getUserName()));
					sendJoinResponse(client, Constants.joinResponseDecryptedDataInvalid);
					callback.change(this);
					return TcpSelectServer.CLOSE_CLIENT;
				}
				indexOf = decryptedString.indexOf(hostUserName, userName.length());
				if(indexOf < 0) {
					System.out.println(String.format("HostUserName not found in host key '%s'", hostUserName));
					sendJoinResponse(client, Constants.joinResponseDecryptedDataInvalid);
					callback.change(this);
					return TcpSelectServer.CLOSE_CLIENT;
				}
				indexOf = decryptedString.indexOf(client.credentials.saveDateString, userName.length() + hostUserName.length());
				if(indexOf < 0) {
					System.out.println(String.format("HostDateTime not found in host key '%s'", client.credentials.saveDateString));
					sendJoinResponse(client, Constants.joinResponseDecryptedDataInvalid);
					callback.change(this);
					return TcpSelectServer.CLOSE_CLIENT;
				}

				client.credentials.gotValidEncryptedHostKey();
				sendJoinResponse(client, Constants.joinReponseAccept);
				callback.change(this);
				
				return TcpSelectServer.NO_INSTRUCTION;
			}
			
			//
			// Handle chunk from authenticated client
			//
			System.out.println(String.format("[Group] Received %d byte chunk from '%s': %s",
					chunkSize, client.getIdString(), ByteArray.toEscapString(chunkBytes, 0, chunkSize)));

			// Parse the chunk make a callback
			return TcpSelectServer.NO_INSTRUCTION;
			
		} else if(state == GroupState.startingGame) {
			System.out.println(String.format("[Group] While starting game got %d byte chunk: %s", chunkSize, ByteArray.toHexString(chunkBytes, 0, chunkSize)));
			
			return TcpSelectServer.NO_INSTRUCTION;
			
		} else {		
			System.out.println(String.format("[Group] Received %d byte chunk from '%s' while in state %d", chunkSize, client.getIdString(), state));
			
			return TcpSelectServer.NO_INSTRUCTION;
		}
		
	}
	

	void sendJoinResponse(RemoteTankClient client, byte code) throws IOException {
		sendJoinResponse(client.socketChannel, code);
	}
	void sendJoinResponse(SocketChannel channel, byte code) throws IOException {
		outputChunker.startChunk();
		outputChunker.chunkBuffer.put(code);
		outputChunker.endChunk();					
		channel.write(outputChunker.chunkBuffer);
	}
	

	// ====================================================
	// Host requesting game to start
	// ====================================================
	//
	// Host-AllClients: byte hostRequestGameStart, byte yourClientIndex,
	// ----------------------------------------------------
	// ------- Case: ClientReady
	// ----------------------------------------------------
	// Client-Host(UDP): byte clientIndex (The host now has the udp source port of the client)
	// Client-Host: byte ready (If the host did not get the udp packet, they send a request for another udp packet, this continues every second until the host gets it or the host times out)
	// ----------------------------------------------------
	// ------- Case: ClientNotReady
	// ----------------------------------------------------
	// Client-Host: byte notReady
	//
	// ...
	// 
	// ----------------------------------------------------
	// ------- Case: All Clients Were Ready And Host Received a UDP packet from every client. (NOTE: the host must also make sure that every UDP IP combo is unique)
	// ----------------------------------------------------
	// Host-AllClients: byte gameStart, (GameInformation like clientNames, startPositions, LevelInformation) (*Now the game starts)
	// ----------------------------------------------------
	// ------- Case: At least one client was not ready or did not respond
	// ----------------------------------------------------
	// Host-AllClients: byte gameStartDelayed
	public void startGame() throws IOException {
		final byte numberOfClientsForGame;
		
		synchronized (state) {
			if(state != GroupState.settingUpGame)
				throw new IllegalStateException(String.format("Expected to be in state %d but was in state %d", GroupState.settingUpGame, state));
			state = GroupState.startingGame;
			
			numberOfClientsForGame = (byte)clientsOrdered.size();
			
			//
			// Check that all clients have been authenticated
			//
			for(byte i = 0; i < clientsOrdered.size(); i++) {
				RemoteTankClient client = clientsOrdered.get(i);
				if(!client.credentials.isAuthenticated())
					throw new IllegalStateException(String.format("Client '%s' has not been authenticated yet", client.getIdString()));				
			}
			
			
		}		
		
		//
		// Start the udp server
		//
		DatagramChannel	channel = DatagramChannel.open();
		DatagramSocket udpSocket = channel.socket();
		udpSocket.bind(new InetSocketAddress(Settings.serverPort));
		channel.configureBlocking(true);		
		
		//
		// Send request to clients to start the game
		//
		
		synchronized (state) {
			verifyNumberOfClients(numberOfClientsForGame);
			for(byte i = 0; i < clientsOrdered.size(); i++) {
				RemoteTankClient client = clientsOrdered.get(i);

				outputChunker.startChunk();
				outputChunker.chunkBuffer.put(Constants.hostRequestGameStart);
				outputChunker.chunkBuffer.put((byte)(i + 1)); // Remote client indices start from 1, the host client index is always 0
				outputChunker.endChunk();
				
				client.socketChannel.write(outputChunker.chunkBuffer);
			}			
		}
		
		DatagramPacket udpPacket = new DatagramPacket(new byte[512], 512);
		boolean allClientsAreReady = false;
		while(state == GroupState.startingGame && !allClientsAreReady) {
			// right now socket should be blocking
			System.out.println("[UdpServer] waiting for packet...");
			udpSocket.receive(udpPacket);
			
			System.out.println(String.format("getAddress()='%s' getPort()='%s' getLength()=%d",
					udpPacket.getAddress(),
					udpPacket.getPort(),
					udpPacket.getLength()));
			
			InetAddress from = udpPacket.getAddress();
			System.out.println(String.format("Got UDP packet from address '%s'", from));
			
			
			System.out.println("Received Udp Packet: " + udpPacket.getLength());
			synchronized (state) {
				verifyNumberOfClients(numberOfClientsForGame);
				
				byte[] data = udpPacket.getData();
				byte clientIndex = data[0];
				
				RemoteTankClient client = clientsOrdered.get(clientIndex - 1);
				
				// Check IP Address
				if(!from.equals(client.network.inet4Address)) {
					System.out.println(String.format("[Group] Got client index %d from inet address '%s' but it does not match the client '%s' inet address '%s'",
							clientIndex, from.getHostAddress(), client.getIdString(), client.network.inet4Address.getHostAddress()));
				}
				int udpSourcePort = udpPacket.getPort();
				System.out.println(String.format("[Group] Got UDP packet from '%s' (UdpSourcePort=%d)", client.getIdString(), udpSourcePort));
				client.network.setUdpSourcePort(udpSourcePort);
				
				
				//
				// Check if all clients are ready
				//
				allClientsAreReady = true;
				for(int i = 0; i < clientsOrdered.size(); i++) {
					RemoteTankClient clientCheck = clientsOrdered.get(i);
					if(!clientCheck.network.udpPortIsSet()) {
						allClientsAreReady = false;
						break;
					}
				}
			}			
		}
		
		synchronized (state) {
			verifyNumberOfClients(numberOfClientsForGame);			
			if(state == GroupState.startingGame && allClientsAreReady) {
				System.out.println("[Group] All clients are ready...need to start");
				
				// Create game information chunk
				outputChunker.startChunk();
				outputChunker.chunkBuffer.put(Constants.hostStartingGame);
				
				// Send the clients ordered
				outputChunker.chunkBuffer.put(numberOfClientsForGame);
				
				for(int i = 0; i < clientsOrdered.size(); i++) {
					RemoteTankClient client = clientsOrdered.get(i);
					String clientUserName = client.credentials.getUserName();
					
					outputChunker.chunkBuffer.put((byte)clientUserName.length());
					for(int j = 0; j < clientUserName.length(); j++) {
						outputChunker.chunkBuffer.put((byte)clientUserName.charAt(j));
					}
				}
				
				// Send start positions
				// Send level information
				
				outputChunker.endChunk();				
				
				for(int i = 0; i < clientsOrdered.size(); i++) {
					RemoteTankClient client = clientsOrdered.get(i);
					client.socketChannel.write(outputChunker.chunkBuffer);
					outputChunker.chunkBuffer.flip();
				}
				
				
			} else {
				System.out.println("[Group] Tell all clients that game is delayed");
				
			}						
		}
		
		
	}
	
	private void verifyNumberOfClients(int clientCount) {
		if(clientCount != clientsOrdered.size())
			throw new IllegalStateException(String.format("Started with %d clients but now we there are %d", clientCount, clientsOrdered.size()));
	}
}

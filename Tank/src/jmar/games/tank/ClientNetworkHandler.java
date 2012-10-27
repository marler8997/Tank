package jmar.games.tank;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.acl.LastOwnerException;
import java.util.Arrays;

import org.lwjgl.Sys;

import jmar.games.Clock;
import jmar.games.net.InputStreamChunker;
import jmar.games.net.OutputStreamChunker;
import jmar.games.tank.Bullet;
import jmar.games.tank.Constants;
import jmar.games.tank.Settings;
import jmar.games.tank.TimeStepLogic;
import jmar.udpforgames.SimpleUdpClient;

public class ClientNetworkHandler {
	public String serverString;
	private InetSocketAddress serverSocketAddress;
	
	byte[] sendPacketBytes, recvBytes1,recvBytes2;
	ByteBuffer sendPacketByteBuffer, recvBuffer1, recvBuffer2;

	Socket tcpSocket;
	InputStreamChunker tcpInputChunker;
	OutputStreamChunker tcpOutputChunker;
	
	SimpleUdpClient udp;
	
	public ClientNetworkHandler() {
		this.serverString = null;
		this.serverSocketAddress = null;
		
		this.sendPacketBytes = new byte[Constants.maxTotalPacketSize];
		this.recvBytes1 = new byte[Constants.maxTotalPacketSize];
		this.recvBytes2 = new byte[Constants.maxTotalPacketSize];
		
		this.sendPacketByteBuffer = ByteBuffer.wrap(this.sendPacketBytes);
		this.recvBuffer1 = ByteBuffer.wrap(this.recvBytes1);
		this.recvBuffer2 = ByteBuffer.wrap(this.recvBytes2);
	}
	
	public void setNewServer(String serverString, Inet4Address serverAddress) {
		if(this.serverString != null) {
			throw new IllegalStateException("You are settings a new server but the old server is still setup");
		}
		
		this.serverString = serverString;
		this.serverSocketAddress = new InetSocketAddress(serverAddress, Settings.serverPort);
	}

	public void leaveGameSetup() {
		this.serverString = null;
		this.serverSocketAddress = null;
		
		if(tcpSocket != null) {
			if(tcpSocket.isConnected()) {
				try { tcpSocket.shutdownInput();} catch(IOException e) {}
				try { tcpSocket.shutdownOutput();} catch(IOException e) {}
			}
			try { tcpSocket.close(); } catch (IOException e) { }
		}
		this.tcpInputChunker = null;
		this.tcpOutputChunker = null;
		
		this.udp = null;		
	}
	
	public void syncGame(TankGame game) throws IOException {
		Puck myPuck = game.myPuck;
		byte[] recvBytes1 = this.recvBytes1;
		byte[] recvBytes2 = this.recvBytes2;
		byte[] sendBytes = this.sendPacketBytes;
		
		//
		// Receive Updates
		//
		
		long timeBeforeReceivingPackets = Clock.getTime();

		int validPacketsReceived = 0;
		byte[] nextBytesToReceive = this.recvBytes1;
		ByteBuffer nextBufferToReceive = this.recvBuffer1;
		byte[] savedBytes = this.recvBytes2;
		ByteBuffer savedBuffer = this.recvBuffer2;
		
		// Keep Receiving Packets
		while(true) {
			// TODO: Check if time limit is up (if you have been spending to much time receiving packets for the current frame)
			nextBufferToReceive.clear();
			SocketAddress from = udp.receiveNonBlocking(nextBufferToReceive);
			
			if(from == null) break;
			
			int bytesReceived = nextBufferToReceive.position();
			
			// Check the size of the buffer
			if(bytesReceived < game.minimumPacketSizeFromServer) {
				System.err.println(String.format("Error: server sent packet of %d bytes, but it should always greater than %d bytes",
						bytesReceived, game.minimumPacketSizeFromServer));
				continue;
			}
			
			validPacketsReceived++;
			
			// Check the frame id
			int packetTimeStep =
					(0xFF00 & (nextBytesToReceive[0] << 8)) |
					(0x00FF & (nextBytesToReceive[1]     )) ;
			
			//System.out.println(String.format("Received Packet TimeStep=%d LastTimeStepFromServer=%d", packetTimeStep, game.lastTimeStepFromServer));
			
			// If this frame
			int timeDiff = TimeStepLogic.timeStepDiff(packetTimeStep, game.lastTimeStepFromServer);
			if(timeDiff > 0) {
				
				game.lastTimeStepFromServer = packetTimeStep;
				game.timeStepWithServerToClientLag = packetTimeStep;
				
				// swap packets
				byte[] tempBytes = nextBytesToReceive;
				ByteBuffer tempBuffer = nextBufferToReceive;
				nextBytesToReceive = savedBytes;
				nextBufferToReceive = savedBuffer;
				savedBytes = tempBytes;
				savedBuffer = tempBuffer;
			} else {
				if(timeDiff == 0) {
					System.out.println(String.format("[ServerError] Received two packets from the server at timestep %d", packetTimeStep));
				} else {
					System.out.println(String.format("[NetworkError of ServerError] Either this packet was received out of order, which is fine, or the server has an error because packet at timestep %d was received before %d",
							game.lastTimeStepFromServer, packetTimeStep));
				}
			}
			
			// TODO: Put Get Bullets From This Packet Here so I don't miss any bullets
			
			
			
		}
		
		long timeAfterReceivingAndBeforeProcessing = Clock.getTime();
		game.currentSecondTimeReceivingPackets += timeAfterReceivingAndBeforeProcessing - timeBeforeReceivingPackets;

		if(validPacketsReceived > 0) {
			if(validPacketsReceived > 1) {
				System.out.println(String.format("Threw away %d packets", validPacketsReceived - 1));
				game.totalPacketsDiscarded += validPacketsReceived - 1;
			}
			
			int packetOffset = 2;					
			for(int clientIndex = 0; clientIndex < game.clients.length; clientIndex++) {
				byte bulletCount = savedBytes[packetOffset + 5];
				
				if(clientIndex == game.myClientIndex) {
					packetOffset += 6 + Constants.bulletNetworkByteLength*bulletCount;
					continue;
				}
				
				PuckClientStateForClient client = game.clients[clientIndex];
				int x        = (0x0000FF00 & (savedBytes[packetOffset    ] <<  8)) |
						       (0x000000FF & (savedBytes[packetOffset + 1]      )) ;
				int y        = (0x0000FF00 & (savedBytes[packetOffset + 2] <<  8)) |
						       (0x000000FF & (savedBytes[packetOffset + 3]      )) ;
				byte controls =               savedBytes[packetOffset + 4];
				
				if(!client.puck.puckArenaObject.bottomLeftEquals(x,y)){
					System.out.println(String.format("[Game] '%s' moved to %dx%d", client.userName, x,y));
					client.puck.puckArenaObject.moveUsingBottomLeft(x, y);
				}

				// TODO: Move this get bullets to the other loop
				
				packetOffset += 6;
				for(int bulletIndex = 0; bulletIndex < bulletCount; bulletIndex++) {
					// Byte packetID,  UInt16 timeStep, Byte bulletCount, {UInt16 initX, UInt16 initY, UInt16 targetX, UInt16 targetY}
					int bulletTimestep      = (0x0000FF00 & (savedBytes[packetOffset    ] <<  8)) |
					          			      (0x000000FF & (savedBytes[packetOffset + 1]      )) ;	
					
					boolean alreadyHaveBullet = false;
					for(int clientBulletIndex = 0; clientBulletIndex < client.puck.bulletCapacity; clientBulletIndex++) {
						Bullet bullet = client.puck.bullets[clientBulletIndex];
						if(bullet.initialTimeStep == bulletTimestep) {
							alreadyHaveBullet = true;
							break;
						}
					}
					
					if(!alreadyHaveBullet) {
						System.out.println(String.format("[Game] Client %s shot a bullet at timestep %d", client.userName, bulletTimestep));
						
						if(!client.addNewBulletFromClient(bulletTimestep, savedBytes, packetOffset + 2)) {
							System.out.println(String.format("[GameBadState] The client %s requested to shoot a bullet but it has none available",client.userName));
						}
					}
					
					packetOffset += Constants.bulletNetworkByteLength;
				}
			
			}

		}		
		
		long timeAfterProcessingAndBeforeReceiving = Clock.getTime();		
		
		
		//
		// Send Updates
		//
		byte updateFlags;		
		int packetDataOffset = 1;
		
		// Player Position
		if(!myPuck.puckArenaObject.bottomLeftEquals(game.lastPuckBottomLeftSent)) {
			updateFlags = Constants.playerPositionFlag;
			
			myPuck.puckArenaObject.copyBottomLeftTo(game.lastPuckBottomLeftSent);
			int left = myPuck.puckArenaObject.getLeft();
			int bottom = myPuck.puckArenaObject.getBottom();

			sendBytes[packetDataOffset++] = (byte)(game.timeStepWithServerToClientLag >> 8);
			sendBytes[packetDataOffset++] = (byte)(game.timeStepWithServerToClientLag     );
			sendBytes[packetDataOffset++] = (byte)(left >> 8);
			sendBytes[packetDataOffset++] = (byte)(left     );
			sendBytes[packetDataOffset++] = (byte)(bottom >> 8);
			sendBytes[packetDataOffset++] = (byte)(bottom     );
			sendBytes[packetDataOffset++] = 0; // Controls
		} else {
			updateFlags = 0;
		}		
		
		byte sendBulletCount = 0;
		
		// Player's Bullets
		for(int i = 0; i < myPuck.bulletCapacity; i++) {
			Bullet bullet = myPuck.bullets[i];
			if(bullet.numberOfPacketsSentIn < Settings.numberOfPacketsToSendABullet) {
				sendBulletCount++;
				bullet.numberOfPacketsSentIn++;
				
				sendBytes[packetDataOffset++] = (byte)(bullet.initialTimeStep >> 8);
				sendBytes[packetDataOffset++] = (byte) bullet.initialTimeStep      ;
				sendBytes[packetDataOffset++] = (byte)(bullet.initialX        >> 8); // Bullet X
				sendBytes[packetDataOffset++] = (byte)(bullet.initialX            );                                                           
				sendBytes[packetDataOffset++] = (byte)(bullet.initialY        >> 8); // Bullet Y
				sendBytes[packetDataOffset++] = (byte)(bullet.initialY            );
				sendBytes[packetDataOffset++] = (byte)(bullet.targetX         >> 8); // Bullet TargetX
				sendBytes[packetDataOffset++] = (byte)(bullet.targetX             );
				sendBytes[packetDataOffset++] = (byte)(bullet.targetY         >> 8); // Bullet TargetY
				sendBytes[packetDataOffset++] = (byte)(bullet.targetY             );
			}
		}
		
		updateFlags |= Constants.playerBulletsCountMask & (sendBulletCount << Constants.playerBulletsCountShift);		
		
		
		if(updateFlags != 0) {
			sendBytes[0] = updateFlags;
			
			/*
			System.out.print(String.format("Sending Packet(%d bytes) Hex:", packetDataOffset));
			for(int i = 0; i < packetDataOffset; i++) {
				System.out.print(' ');
				System.out.print(String.format("%02x",sendBytes[i]));
			}
			System.out.println();
			*/
			
			sendPacketByteBuffer.position(0);
			sendPacketByteBuffer.limit(packetDataOffset);
			udp.send(sendPacketByteBuffer);		
		}
		
		long timeAfterSending = Clock.getTime();
		game.currentSecondTimeSendingPackets += timeAfterSending - timeAfterProcessingAndBeforeReceiving;
	}
	
}

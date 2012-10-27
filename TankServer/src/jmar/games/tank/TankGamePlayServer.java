package jmar.games.tank;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import jmar.ByteRef;
import jmar.games.Clock;
import jmar.games.net.ChunkByteBuffer;
import jmar.games.net.InputChannelChunker;
import jmar.games.net.OutputChannelChunker;
import jmar.games.net.TcpSelectServer;
import jmar.games.net.TcpSelectServerHandler;
import jmar.games.tank.Bullet;
import jmar.games.tank.Constants;
import jmar.games.tank.Level;
import jmar.games.tank.Position;
import jmar.games.tank.Profiler;
import jmar.games.tank.Settings;
import jmar.games.tank.TimeStepLogic;
import jmar.udpforgames.SimpleUdpServer;

public class TankGamePlayServer {	
	
	public static void runGame(TankClientStateForServer[] clients, Level level) throws IOException {
		Profiler profiler = new Profiler(32);
		int timeBucketTotalSendTime   = profiler.createTimeBucket("TotalSendTime");
		int timeBucketCreateSendPacket= profiler.createTimeBucket("CreateSendPacket");
		int timeBucketSendPackets     = profiler.createTimeBucket("SendPackets");
		int timeBucketReceiveLoop     = profiler.createTimeBucket("TotalReceiveLoop");
		int timeBucketReceiveWaitTime = profiler.createTimeBucket("ReceiveWaitTime");
		
		// Setup client start positions
		for(int i = 0; i < clients.length; i++) {
			TankClientStateForServer client = clients[i];

			byte startPositionIndex = client.startPositionIndex;
			Position startPosition = level.middleStartPositions[startPositionIndex];
			byte puckClientOddHalfWidth = Settings.defaultPuckArenaOddHalfWidth;
			byte puckClientOddHalfHeight = Settings.defaultPuckArenaOddHalfHeight;
			
			int puckClientMiddleX = startPosition.x - puckClientOddHalfWidth;
			int puckClientMiddleY = startPosition.y - puckClientOddHalfHeight;
			
			client.updatePlayerState(0, 0, puckClientMiddleX, puckClientMiddleY, (byte)0);
		}		
		
		// Setup Hash for client address/port to client state
		HashMap<Long,TankClientStateForServer> clientUdpMap = new HashMap<Long, TankClientStateForServer>();
		for(int i = 0; i < clients.length; i++) {
			TankClientStateForServer client = clients[i];
			System.out.println(String.format("%s (0x%xld) mapped to '%s'", client.udpKeyString, client.udpAddressPortKey, client.userName));
			clientUdpMap.put(client.udpAddressPortKey, clients[i]);
		}
		
		// Initialize UDP Socket
		DatagramChannel	channel = DatagramChannel.open();
		channel.socket().bind(new InetSocketAddress(Settings.serverPort));
		channel.configureBlocking(false);
		SimpleUdpServer udp = new SimpleUdpServer(channel);
		
		byte[] recvPacketBytes = new byte[Constants.maxTotalPacketSize];
		ByteBuffer recvPacketByteBuffer = ByteBuffer.wrap(recvPacketBytes);
		
		
		
		//
		//
		//
		//		
		// Server Loop Algorithm
		//
		// int lastTimeStepAfterReceiveLoop = 0xFFFF;
		// int lastTimeStepSent = 0xFFFF;
		// boolean needToSendUpdate = false;
		//
		// while(true) {
		//
		//   // This is the receive loop, when you leave this loop, currentTimeStep cannot equal lastTimeStepSent
		//   while(true) {
		//     "Receive available packet"
		//     if(no packets) { "Update currentTimeStep" if(currentTimeStep != lastTimeStepAfterReceiveLoop) break; otherwise, optional sleep? }
		//     if("packet has update") needToSendUpdate = true;
		//     "Update currentTimeStep"
		//     if(currentTimeStep != lastTimeStepAfterReceiveLoop) break;
		//   }
		//   // Note: at this point, currentTimeStep should never equal lastTimeStepAfterReceiveLoop
		//   lastTimeStepAfterReceiveLoop = currentTimeStep;
		//
		//   Update data structures with currentTimeStep
		//   Check physical world, calculate deaths, AI moves
		//   update needToSendUpdate if necessary
		//
		//   if(needToSendUpdate) {
	    //     needToSendUpdate = false;
		//     lastTimeStepSent = lastTimeStepAfterReceiveLoop
		//     send packets
		//   }
		// }
		//
		//
		int lastTimeStepAfterReceiveLoop = Settings.timeStepRolloverValue - 1;
		int lastTimeStepSent = Settings.timeStepRolloverValue - 1;
		boolean needToSendChanges = false;
		
		byte[] sendBytes = new byte[Constants.maxTotalPacketSize];		
		ByteBuffer sendBuffer = ByteBuffer.wrap(sendBytes);
		
		final long initialLoopTime = Clock.getTime();
		
		//
		// TODO: Initial player histories
		//
		int currentTimeStepHistoryIndex = 0;
		
		long now,totalTimeInLoop;
		
		while(true) {
			
			//
			// Print out some info
			//
			/*
			if((timeStep & 0x7F) == 0) {
				//profiler.print();
				System.out.println(String.format("TimeStep %d", timeStep));
				for(int i = 0; i < clients.length; i++) {
					PuckClientStateForServer client = clients[i];
					System.out.print(String.format(" '%s' %dx%d", client.userName,
							client.xHistory[client.latestUpdateHistoryIndex], client.yHistory[client.latestUpdateHistoryIndex]));
				}
				System.out.println();
			}
			*/
			
			

			//
			// Receive Packets
			//
			profiler.startTimeBucket(timeBucketReceiveLoop, Clock.getTime());			
			while(true) {
				recvPacketByteBuffer.clear();
				SocketAddress clientSocketAddress = udp.receiveNonBlocking(recvPacketByteBuffer);
				if(clientSocketAddress == null)  {
					// Just get the current time step for now
					now = Clock.getTime();
					totalTimeInLoop = now - initialLoopTime;			
					int currentTimeStep = (int) ((totalTimeInLoop / Settings.millisPerTimeStep) & 0xFFFF);
					if(currentTimeStep != lastTimeStepAfterReceiveLoop) {
						currentTimeStepHistoryIndex += TimeStepLogic.timeStepDiff(currentTimeStep, lastTimeStepAfterReceiveLoop);
						if(currentTimeStepHistoryIndex >= Settings.serverTimestepHistory) {
							currentTimeStepHistoryIndex -= Settings.serverTimestepHistory;
						}
						lastTimeStepAfterReceiveLoop = currentTimeStep;
						break;
					} else {
						// TODO: Insert optional sleep
						continue;
					}
				}
				
				// Check client IP Address
				InetSocketAddress clientInetSocketAddress;
				Inet4Address clientInet4Address;
				try {
					clientInetSocketAddress = (InetSocketAddress) clientSocketAddress;
				} catch(ClassCastException e) {
					System.out.println(String.format("[Game] Received packet from '%s', which isn't an InetSocketAddress, it is a(n) %s",
							clientSocketAddress.toString(), clientSocketAddress.getClass()));
					continue;					
				}

				try {
					clientInet4Address = (Inet4Address)clientInetSocketAddress.getAddress();
				} catch(ClassCastException e) {
					System.out.println(String.format("[Game] Received packet from '%s', which isn't an Inet4Address, it is a(n) %s",
							clientInetSocketAddress.getAddress().toString(), clientInetSocketAddress.getAddress().getClass()));
					continue;					
				}
				
				int clientSourcePort = clientInetSocketAddress.getPort();
				byte[] clientAddressBytes = clientInet4Address.getAddress();
				long clientUdpPortAddressKey = 
						(0x0000FF0000000000L & (clientAddressBytes[0] << 40)) |
						(0x000000FF00000000L & (clientAddressBytes[1] << 32)) |
						(0x00000000FF000000L & (clientAddressBytes[2] << 24)) |
						(0x0000000000FF0000L & (clientAddressBytes[3] << 16)) |
						(0x000000000000FF00L & (clientSourcePort           )) |
						(0x00000000000000FFL & (clientSourcePort           )) ;

				TankClientStateForServer clientState = clientUdpMap.get(clientUdpPortAddressKey);				
				if(clientState == null) {
					System.err.println(String.format("[Game] Packet from unknown client %s:%d", clientInet4Address.toString(), clientSourcePort));
					continue;
				}				
				
				//
				// Process the Packet
				//
				int packetSize = recvPacketByteBuffer.position();
				int packetOffset = 0;
				try {					
					byte packetUpdateFlags = recvPacketBytes[packetOffset++];
					
					if((packetUpdateFlags & Constants.playerPositionFlag) != 0) {
						packetOffset += 7;
						int positionTimeStep = (0xFF00 & (recvPacketBytes[1] << 8)) |
								 			   (0x00FF & (recvPacketBytes[2]     )) ;
	
						int timeStepDiff = TimeStepLogic.timeStepDiff(lastTimeStepAfterReceiveLoop, positionTimeStep);
						if(timeStepDiff < 0) {
							System.err.println(String.format("[BadDataFromClient] LastTimeStepAfterReceiveLoop=%d, ClientPositionTimestep: %d from client %s is in the future?",
									lastTimeStepAfterReceiveLoop, positionTimeStep, clientState.getLogStringDuringGame()));
						} else if(timeStepDiff >= Settings.serverTimestepHistory) {
							System.err.println(String.format("[Game] LastTimeStepAfterReceiveLoop=%d, ClientPositionTimestep: %d from client %s is too old",
									lastTimeStepAfterReceiveLoop, positionTimeStep, clientState.getLogStringDuringGame()));	
						} else {						
							int clientX         = (0x0000FF00 & (recvPacketBytes[3] <<  8)) |
										          (0x000000FF & (recvPacketBytes[4]      )) ;	
							int clientY         = (0x0000FF00 & (recvPacketBytes[5] <<  8)) |
										          (0x000000FF & (recvPacketBytes[6]      )) ;
							byte clientControls = recvPacketBytes[7];
							clientState.updatePlayerState(currentTimeStepHistoryIndex, timeStepDiff, clientX, clientY, clientControls);
							needToSendChanges = true; // VERY IMPORTANT THAT THIS IS SET						
						}
					}

					int bulletCount = (packetUpdateFlags & Constants.playerBulletsCountMask) >> Constants.playerBulletsCountShift;	
					
					for(int i = 0; i < bulletCount; i++) {
						// UInt16 timeStep, Byte bulletCount, {UInt16 initX, UInt16 initY, UInt16 targetX, UInt16 targetY}
						int bulletTimestep      = (0x0000FF00 & (recvPacketBytes[packetOffset    ] <<  8)) |
						          			      (0x000000FF & (recvPacketBytes[packetOffset + 1]      )) ;	
						
						boolean alreadyHaveBullet = false;
						for(int bulletIndex = 0; bulletIndex < clientState.bulletCapacity; bulletIndex++) {
							Bullet bullet = clientState.bullets[bulletIndex];
							//
							// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
							// WARNING: You cannot have 2 active bullets with the same intialTimestep!!!
							//          This is ok because if a bullet survives long enough to have a time Diff
							//          greater then the rollover value then an exception is thrown by the client
							//
							if(bullet.initialTimeStep == bulletTimestep) {
								alreadyHaveBullet = true;
								break;
							}
						}
						
						if(!alreadyHaveBullet) {
							System.out.println(String.format("[Game] Client %s shot a bullet at timestep %d", clientState.getLogStringDuringGame(), bulletTimestep));
							if(!clientState.addNewBulletFromClient(bulletTimestep, recvPacketBytes, packetOffset + 2)) {
								System.out.println(String.format("[GameBadState] The client %s requested to shoot a bullet but it has none available",
										clientState.getLogStringDuringGame()));
								// TODO: should I send a reject packet for this bullet?
							}
						}
						
						packetOffset += 10;
					}
					
				} catch(ArrayIndexOutOfBoundsException e) {
					System.err.println(String.format("[Game] Error: packet from %s is %d bytes but the data in the packet indicated it should have at least %d bytes",
							clientState.getLogStringDuringGame(), packetSize, packetOffset+1));					
				}

				//
				// Check if we are at the next timeStep yet		
				//
				now = Clock.getTime();
				totalTimeInLoop = now - initialLoopTime;			
				int currentTimeStep = (int) ((totalTimeInLoop / Settings.millisPerTimeStep) & 0xFFFF);
				if(currentTimeStep != lastTimeStepAfterReceiveLoop) {
					currentTimeStepHistoryIndex += TimeStepLogic.timeStepDiff(currentTimeStep, lastTimeStepAfterReceiveLoop);
					if(currentTimeStepHistoryIndex >= Settings.serverTimestepHistory) {
						currentTimeStepHistoryIndex -= Settings.serverTimestepHistory;
					}
					lastTimeStepAfterReceiveLoop = currentTimeStep;
					break;
				}
			}

			profiler.endTimeBucket(timeBucketReceiveLoop, Clock.getTime());
			
			
			//
			// Update current data structures with the current time step
			//
			profiler.atTimeStep(lastTimeStepAfterReceiveLoop);
			for(int i = 0; i < clients.length; i++) {
				TankClientStateForServer client = clients[i];
				client.updateCurrentTimeStep(lastTimeStepAfterReceiveLoop);
				
				for(int bulletIndex = 0; bulletIndex < client.bulletCapacity; bulletIndex++) {
					Bullet clientBullet = client.bullets[bulletIndex];
					if(clientBullet.numberOfPacketsSentIn < Settings.numberOfPacketsToSendABullet) {
						needToSendChanges = true; // THIS IS VERY VERY IMPORTANT
					}
					clientBullet.updateState(lastTimeStepAfterReceiveLoop);
				}
			}
			
			
			
			
			
			
			
			
			//
			// Send The World 
			// (This is the world according to the last time step calculated)
			//
			if(needToSendChanges) {
				System.out.println(String.format("[GameDebug] Sending Updates: LastTimeStepAfterReceiveLoop=%d LastTimeStepSent=%d TimeStepsWithoutSending=%d",
					lastTimeStepAfterReceiveLoop, lastTimeStepSent, TimeStepLogic.timeStepDiff(lastTimeStepAfterReceiveLoop, lastTimeStepSent)));
				
				needToSendChanges = false;
				lastTimeStepSent = lastTimeStepAfterReceiveLoop;
				
				long startOfSend = Clock.getTime();
				profiler.startTimeBucket(timeBucketTotalSendTime, startOfSend);
				profiler.startTimeBucket(timeBucketCreateSendPacket, startOfSend);
				
				
				sendBytes[0] = (byte)(lastTimeStepAfterReceiveLoop >> 8);
				sendBytes[1] = (byte)lastTimeStepAfterReceiveLoop;
				int sendPacketSize = 2;
				
				// Insert client puck states
				for(int clientID = 0; clientID < clients.length; clientID++) {
					TankClientStateForServer client = clients[clientID];
					
					int latestArenaX = client.xHistory[client.latestUpdateHistoryIndex];
					int latestArenaY = client.yHistory[client.latestUpdateHistoryIndex];
					
					//
					// Insert Position and Controls
					//
					sendBytes[sendPacketSize++] = (byte)(latestArenaX >> 8);                                   // X
					sendBytes[sendPacketSize++] = (byte) latestArenaX;
					sendBytes[sendPacketSize++] = (byte)(latestArenaY >> 8);                                   // Y
					sendBytes[sendPacketSize++] = (byte) latestArenaY;
					sendBytes[sendPacketSize++] = client.controlFlagsHistory[client.latestUpdateHistoryIndex]; // Controls
					
					//
					// Insert Bullets
					//
					int bulletCountOffset = sendPacketSize;
					sendPacketSize++;
					
					byte bulletsSent = 0;
					for(int bulletIndex = 0; bulletIndex < client.bulletCapacity; bulletIndex++) {					
						Bullet bullet = client.bullets[bulletIndex];
						if(bullet.numberOfPacketsSentIn < Settings.numberOfPacketsToSendABullet) {
							bullet.numberOfPacketsSentIn++;
							sendBytes[sendPacketSize++] = (byte)(bullet.initialTimeStep >> 8); // Bullet TimeStep
							sendBytes[sendPacketSize++] = (byte)(bullet.initialTimeStep     );
							sendBytes[sendPacketSize++] = (byte)(bullet.initialX        >> 8); // Bullet X
							sendBytes[sendPacketSize++] = (byte)(bullet.initialX            );                                                           
							sendBytes[sendPacketSize++] = (byte)(bullet.initialY        >> 8); // Bullet Y
							sendBytes[sendPacketSize++] = (byte)(bullet.initialY            );
							sendBytes[sendPacketSize++] = (byte)(bullet.targetX         >> 8); // Bullet TargetX
							sendBytes[sendPacketSize++] = (byte)(bullet.targetX             );
							sendBytes[sendPacketSize++] = (byte)(bullet.targetY         >> 8); // Bullet TargetY
							sendBytes[sendPacketSize++] = (byte)(bullet.targetY             );
							bulletsSent++;
						}
					}
					sendBytes[bulletCountOffset] = bulletsSent;
				}
				
				// TODO: Insert AI tank states
				
				profiler.endTimeBucket(timeBucketCreateSendPacket, Clock.getTime());
	
				// Send the packet to everyone
				profiler.startTimeBucket(timeBucketSendPackets, startOfSend);
	
				System.out.println(String.format("[Game] Sending Time Step %d", lastTimeStepAfterReceiveLoop));
				for(int i = 0; i < clients.length; i++) {
					TankClientStateForServer client = clients[i];
					sendBuffer.position(0);
					sendBuffer.limit(sendPacketSize);
					udp.send(sendBuffer, client.udpSocketAddress);
				}			
				profiler.endTimeBucket(timeBucketSendPackets, Clock.getTime());
				
	
				profiler.endTimeBucket(timeBucketTotalSendTime, Clock.getTime());
			}
			
			
		}
	}
}

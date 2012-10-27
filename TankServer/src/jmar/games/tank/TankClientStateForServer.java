package jmar.games.tank;

import java.net.Inet4Address;
import java.net.InetSocketAddress;

import jmar.games.net.BytesChunker;
import jmar.games.net.InputChannelChunker;
import jmar.games.net.OutputChannelChunker;
import jmar.games.tank.Bullet;
import jmar.games.tank.PuckClientStateBuilder;
import jmar.games.tank.Settings;
import jmar.games.tank.TimeStepLogic;

public class TankClientStateForServer {	
	public PuckClientStateBuilder builder;
	
	public String tcpKeyString,udpKeyString;
	
	public final Inet4Address inet4Address;
	public final String inet4AddressString;
	private final long addressPartOfKeys;
	
	public int tcpSourcePort,udpSourcePort;
	public long tcpAddressPortKey,udpAddressPortKey;
	public InetSocketAddress tcpSocketAddress,udpSocketAddress;
	
	
	public String userName;
	public byte[] credential;
	//public byte clientID;
	public byte startPositionIndex;	
	public byte arenaHalfWidth,arenaHalfHeight;
	
	public int[] xHistory;	
	public int[] yHistory;
	public byte[] controlFlagsHistory;
	
	public int[] latestHistoryIndices;     // Rolling array of indices into the history arrays.  the rolling values represent the history indexes that were updated most recently
	public int latestHistoryIndicesIndex;
	
	private int lastTimeStepUpdate;        // The value of timeStep from the last time updateTimeStep was called
	private int latestUpdateTimeStepDiff;  // The timeStep difference between the last updateTimeStep and the last updatePlayerState
	public int latestUpdateHistoryIndex;   // The history index of the latestUpdatePlayerState
	
	public Bullet[] bullets;
	public int bulletCapacity;
	public int bouncesAllowed;
	
	public TankClientStateForServer(BytesChunker tcpInputChunker, OutputChannelChunker tcpOutputChunker,
			Inet4Address inet4Address, int tcpSourcePort) {
		this.builder = new PuckClientStateBuilder(tcpInputChunker, tcpOutputChunker);
		
		this.inet4Address = inet4Address;		
		byte[] addressBytes = inet4Address.getAddress();
		this.inet4AddressString = String.format("%d.%d.%d.%d",
				addressBytes[0], addressBytes[1], addressBytes[2], addressBytes[3]);
		this.addressPartOfKeys = 
				(0x0000FF0000000000L & (addressBytes[0] << 40)) |
				(0x000000FF00000000L & (addressBytes[1] << 32)) |
				(0x00000000FF000000L & (addressBytes[2] << 24)) |
				(0x0000000000FF0000L & (addressBytes[3] << 16)) ;
		
		
		this.tcpSourcePort = tcpSourcePort;		
		this.tcpAddressPortKey = addressPartOfKeys | 
				(0x000000000000FF00L & (tcpSourcePort        )) |
				(0x00000000000000FFL & (tcpSourcePort        )) ;		
		this.tcpSocketAddress = new InetSocketAddress(inet4Address, tcpSourcePort);
		this.tcpKeyString = String.format("%s:%d", this.inet4AddressString, tcpSourcePort);
		
		
		this.userName = null;	
		this.credential = null;
		
		this.xHistory = new int[Settings.serverTimestepHistory];			
		this.yHistory = new int[Settings.serverTimestepHistory];
		this.controlFlagsHistory = new byte[Settings.serverTimestepHistory];
		
		this.latestHistoryIndices = new int [Settings.serverMaxHistoryIndexSaveCount];
		this.latestHistoryIndicesIndex = 0;
		
		this.lastTimeStepUpdate = 0;
		this.latestUpdateTimeStepDiff = 9999; // Big enough that the next update will be less than this
		this.latestUpdateHistoryIndex = 0;

		this.bullets = new Bullet[Settings.maximumPossibleBullets];
		for(int i = 0; i < Settings.maximumPossibleBullets; i++) {
			this.bullets[i] = new Bullet();
		}
		this.bulletCapacity = Settings.defaultBulletCapacity;
		
		reset();
	}
	
	public String getLogStringDuringGame() {
		return String.format("'%s' (%s:%d)", userName, inet4Address.toString(), udpSourcePort);
	}
	
	public void reset() {
		this.userName = null;
		//this.clientID = -1;
		this.startPositionIndex = -1;
		this.arenaHalfWidth = Settings.defaultPuckArenaOddHalfWidth;
		this.arenaHalfHeight = Settings.defaultPuckArenaOddHalfHeight;
		this.udpSourcePort = -1;
		for(int i = 0; i < Settings.maximumPossibleBullets; i++) {
			this.bullets[i].reset();
		}
	}
	
	public void setBulletCapacityAndBouncesAllowed(int bulletCapacity, int bouncesAllowed) {
		this.bulletCapacity = bulletCapacity;
		this.bouncesAllowed = bouncesAllowed;
	}
	
	public void updateCurrentTimeStep(int currentTimeStep) {
		this.latestUpdateTimeStepDiff += TimeStepLogic.timeStepDiff(currentTimeStep, lastTimeStepUpdate);
		this.lastTimeStepUpdate = currentTimeStep;
	}
	
	public void setUdpSourcePort(int udpSourcePort) {
		this.udpSourcePort = udpSourcePort;		
		this.udpAddressPortKey = addressPartOfKeys | 
				(0x000000000000FF00L & (udpSourcePort        )) |
				(0x00000000000000FFL & (udpSourcePort        )) ;
		this.udpSocketAddress = new InetSocketAddress(inet4Address, udpSourcePort);
		this.udpKeyString = String.format("%s:%d", this.inet4AddressString, udpSourcePort);
	}
	
	public void updatePlayerState(int currentHistoryIndex, int timeStepDiff, int x, int y, byte controlFlags) {	
		int historyIndex = currentHistoryIndex - timeStepDiff;
		if(historyIndex < 0) {
			historyIndex += Settings.serverTimestepHistory;
			if(historyIndex < 0) {
				System.out.println(String.format("[Server] Client '%s' updated state lag=%d x=%d y=%d (FrameDiff Too Large)", userName, timeStepDiff, x, y));
				return; // Discard the frame
			}
		}

		System.out.println(String.format("[Server] Client '%s' updated state lag=%d x=%d y=%d", userName, timeStepDiff, x, y));	
		xHistory[historyIndex] = x;
		yHistory[historyIndex] = y;
		controlFlagsHistory[historyIndex] = controlFlags;
		
		if(timeStepDiff < latestUpdateTimeStepDiff) {
			latestUpdateTimeStepDiff = timeStepDiff;
			latestUpdateHistoryIndex = historyIndex;	
			
			// TODO: save the index in the rolling array of history indices
		}
	}
	
	// returns true on success and false if no bullets are availble
	public boolean addNewBulletFromClient(int bulletTimeStep, byte[] bulletData, int dataOffset) {
		for(int i = 0; i < bulletCapacity; i++) {
			Bullet bullet = bullets[i];
			if(!bullet.isActive()) {

				int initialX         = (0x0000FF00 & (bulletData[dataOffset    ] <<  8)) |
							           (0x000000FF & (bulletData[dataOffset + 1]      )) ;	
				int initialY         = (0x0000FF00 & (bulletData[dataOffset + 2] <<  8)) |
				           			   (0x000000FF & (bulletData[dataOffset + 3]      )) ;	
				int targetX          = (0x0000FF00 & (bulletData[dataOffset + 4] <<  8)) |
				           			   (0x000000FF & (bulletData[dataOffset + 5]      )) ;	
				int targetY          = (0x0000FF00 & (bulletData[dataOffset + 6] <<  8)) |
				           			   (0x000000FF & (bulletData[dataOffset + 7]      )) ;
				
				bullet.activate(bulletTimeStep, initialX, initialY, targetX, targetY, bouncesAllowed);
				return true;
			}
		}
		return false;
	}
	
}

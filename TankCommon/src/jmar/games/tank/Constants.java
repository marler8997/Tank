package jmar.games.tank;

import jmar.udpforgames.UdpForGames;

public class Constants {

	//
	// Rules:
	// At any point in time, every puck's location is represented by a non-negative x and y integer pair which are less than the arena width and height respectively.
	// Every puck also has a size which is a non-negative integer representing how many arena units it occupies.
	// The following diagram shows how the size of a puck affects the amount of area units it occupies.
	// 
	//    ---------------------------------------------------------------------
	// 11 |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	// 10 |   |   |   |   |   |   |               |   |   |   |   |   |   |   |
	//    ------------------------|               |----------------------------
	// 9  |   |   |   |   |   |   |               |   |   |   |   |   |   |   |
	//    ------------------------|    size=4     |----------------------------
	// 8  |   |   |       |   |   |    x=6,y=7    |   |   |   |   |   |   |   |
	//    --------| size=2|-------|               |----------------------------
	// 7  |   |   |x=2,y=7|   |   |               |   |   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	// 6  |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	// 5  |   |   |           |   |   |                   |   |   |   |   |   |
	//    --------|           |-------|                   |--------------------
	// 4  |   |   |   size=3  |   |   |                   |   |   |   |   |   |
	//    --------|  x=2,y=3  |-------|                   |--------------------
	// 3  |   |   |           |   |   |      size=5       |   |   |   |   |   |
	//    ----------------------------|      x=7,y=1      |--------------------
	// 2  |   |   |   |   |   |   |   |                   |   |   |   |   |   |
	//    ----------------------------|                   |--------------------
	// 1  |   |   |   |   |   |   |   |                   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	// 0  |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
	//    ---------------------------------------------------------------------
	//    0   1   2   3   4   5   6   7   8   9   10  11  12  13  14  15  16
	//
	//
	// Every puck also has a speed.  This is a non-negitive integer representing the number of time steps to wait per arena unit.
	// Therefore, the lower this number is the faster the puck can travel. The fastest possible speed is 0, which means that the puck can
	// travel 1 arena unit for every time step. If a pucks speed is x, then when it wants to move, it must wait x time steps after registering
	// that it wants to move before it can.  To increase the maximum speed possible in the game, the value Settings.timeStepsPerFrame can be increased.
	//
	//
	//
	// Network Rules:
	//   Positions: A client can send whatever position they want, but the server is the ultimate authority.  If a position is not
	//              accepted then the client will know that their position was not accepted on the next server world update packet.
	//              A client will always update all other clients positions based on the server's world update without any checks.	
	
	public static final float floatPlayerMinStep = .1f;
	public static final int playerPositionToInt = 10; // any player position multiplied by this variable should always be an integer
	
	public static final int maxClients         = 16;
	
	public static final int maxTotalPacketSize = 512;
	public static final int maxUnreliablePacketDataSize = maxTotalPacketSize - UdpForGames.unreliablePacketHeaderLength;
	public static final int maxReliablePacketDataSize = maxTotalPacketSize - UdpForGames.reliablePacketHeaderLength;
	
	
	//
	// Game Setup Protocol (Uses Chunker Protocol, which is built on TCP)
	//
	// We have three entities in a game setup.
	//    1) Clients
	//    2) Host
	//    3) Authority
	//
	// A Client is "Online" if they can contact the authority, otherwise they are "Offline".
	// An Offline Key is a Mac Address/UserName combination that has been encrypted with the Authority's private key.
	// An Offline client must have an offline key to be authenticated by the host.
	//
	// ====================================================
	// Client Joining a Host
	// ====================================================
	//
	// ----------------------------------------------------
	// ------- Case: Offline Client
	// ----------------------------------------------------
	// Client-Host: byte 0(Offline Client), UInt16 offlineKeyLength, byte[] offlineKeyBytes
	//    ----------------------------------------------------
	//    ------- Case: Rejected
	//    ----------------------------------------------------
	//    Host-Client: byte nonZero (DuplicateUser or BadOfflineKey)
	//    ----------------------------------------------------
	//    ------- Case: Accepted
	//    ----------------------------------------------------
	//    Host-Client: byte 0(ClientAccepted)	
	// ----------------------------------------------------
	// ------- Case: Online Client
	// ----------------------------------------------------
	// Client-Host: byte 1(Online Client), byte userNameLength, byte[] userName
	//    ----------------------------------------------------
	//    ------- Case: BadClient
	//    ----------------------------------------------------
	//    Host-Client: byte nonZero (DuplicateUser)
	//    ----------------------------------------------------
	//    ------- Case: Client OK So Far
	//    ----------------------------------------------------	
	//    Host-Client: byte 0(ClientOKSoFar), byte hostUserNameLength, byte[] hostUserName, byte dateTimeStringLength, byte[] dateTimeString
	//    Client-Authority(SSL): UserName, AuthenticationInfo(Password or Session Key), HostUserName, HostDateTimeString
	//    Authority-Client(SSL): RsaPrivateEncrypt(UserName + HostUserName + HostDateTimeString) // Note: The server can authenticate the client, make sure that the host user name is valid, and make sure that the host date time is a valid date tiem
	//    Client-Host: RsaPrivateEncrypt(UserName + HostUserName + PostFix(0))
	//       ----------------------------------------------------
	//       ------- Case: Rejected
	//       ----------------------------------------------------
	//       Host-Client: byte nonZero (DecryptionFailed or DecryptedDataInvalid)
	//       ----------------------------------------------------
	//       ------- Case: Accepted
	//       ----------------------------------------------------
	//       Host-Client: byte 0(ClientAccepted)
	//
	//
	// 
	// ====================================================
	// Game Setup Commands
	// ====================================================
	// The Host has a set of commands they can send to each client that updates the client with what the host is doing.
	// These commands are purely informative and are not crucial for each client to have to start the game.
	//   - LevelSelect (LevelName, LevelBinary (If it's not a core level))
	//   - ClientsUpdate (Send All Client Names) (Sent when a client joins or leaves)
	//
	//
	//
	// ====================================================
	// Host requesting game to start
	// ====================================================
	//
	// Host-AllClients: byte hostRequestGameStart, byte youClientID,
	// ----------------------------------------------------
	// ------- Case: ClientReady
	// ----------------------------------------------------
	// Client-Host(UDP): byte clientID (The host now has the udp source port of the client)
	//                 : (Note) if the host times out and doesn't get the udp packet or the TCP packet saying the client isn't ready, then the host will request another udp packet a couple times before it fails
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
	//
	
	
	public static final byte offlineClient = 0;
	public static final byte onlineClient = 1;

	public static final byte joinReponseAccept                       = 0;
	public static final byte joinResponseDuplicateUser               = 1;
	public static final byte joinResponseDecryptionFailed            = 2;
	public static final byte joinResponseDecryptedDataInvalid        = 3;
	public static final byte joinResponseNotAcceptingClientsRightNow = 4;
	public static final byte joinResponseCouldNotParseChunk          = 5;
	
	
	public static final byte clientReadyForGameStart = 0;
	
	public static final byte hostRequestGameStart = 0;
	public static final byte hostStartingGame     = 1;
	public static final byte hostGameDelayed      = 2;
	
	
	
	
	
	//
	// UDP Protocol
	//
	
	// Client To Server Messages
	public static final int minPacketSize                         = 8;
	
	public static final byte playerPositionFlag                   =  0x01; // Byte packetID, Byte positionTimeStep, UInt16 timeStep, UInt16 arenaX, UInt16 arenaY, Byte controlFlags
	public static final int playerPositionPacketSize              =  8;
	
	public static final byte bulletNetworkByteLength              = 10;
	
	public static final byte playerBulletsCountMask               = 0x1E;
	public static final byte playerBulletsCountShift              =    1;
	
	public static final byte playerBullets                        =  1; // Byte packetID,  Byte bulletCount, {UInt16 bulletTimestep, UInt16 initX, UInt16 initY, UInt16 targetX, UInt16 targetY}
	public static final int playerBulletsMinPacketSize            = 12; // Note: at least one bullet must be included
	
	public static final byte playerPositionAndBullets             =  2; // Byte packetID,  UInt16 positionTimeStep, UInt16 arenaX, UInt16 arenaY, Byte controlFlags, Byte bulletCount, {UInt16 bulletTimestep, UInt16 initX, UInt16 initY, UInt16 targetX, UInt16 targetY}
	public static final int playerPositionAndBulletsMinPacketSize = 19; // Note: at least one bullet must be included
	
	// World State (Server to client)
	// byte frameID, EachClient[float x, float y, byte controlFlags]
	

	//
	// Control Flags
	//
	public static final byte rightFlag = 0x01;
	public static final byte leftFlag  = 0x02;
	public static final byte downFlag  = 0x04;
	public static final byte upFlag    = 0x08;

}

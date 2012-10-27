package jmar.games.tank;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RemoteTankClientCredentials {
	public boolean isOffline;
	private String userName;
	
	enum AuthenticationState {
		waitingForInitialChunk,
		waitingForEncryptedHostKey,
		authenticated		
	};
	private AuthenticationState state;
	
	public String saveDateString;
	
	public RemoteTankClientCredentials() {
		this.userName = null;
		this.state = AuthenticationState.waitingForInitialChunk;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public boolean isAuthenticated() {
		return state == AuthenticationState.authenticated;
	}
	public boolean waitingForInitialChunk() {
		return state == AuthenticationState.waitingForInitialChunk;
	}
	
	public void gotValidEncryptedHostKey() {
		if(state != AuthenticationState.waitingForEncryptedHostKey)
			throw new IllegalStateException("You said you got a valid encrypted host key but you weren't saiting for one?");
		state = AuthenticationState.authenticated;		
	}
	
	//
	// After this, the userName will always be set
	//
	public void parseInitialPacket(byte[] packet, int offset) {
		isOffline = (packet[offset++] == Constants.offlineClient) ? true : false;
		if(isOffline) {
			byte[] offlineKey = new byte[(0xFF00 & (packet[offset    ] << 8)) |
			       				         (0x00FF & (packet[offset + 1]     )) ];
			offset += 2;
			for(int i = 0; i < offlineKey.length; i++) {
				offlineKey[i] = packet[offset++];
			}
			OfflineKeyDecryptor keyDecryptor = new OfflineKeyDecryptor(offlineKey);
			this.userName = keyDecryptor.userName;
			this.state = AuthenticationState.authenticated;
		} else {
			byte[] userName = new byte[packet[offset]];
			offset++;
			for(int i = 0; i < userName.length; i++) {
				userName[i] = packet[offset++];
			}
			this.userName = new String(userName);
			this.state = AuthenticationState.waitingForEncryptedHostKey;
		}
	}
}

package jmar.games.tank;

import java.security.InvalidKeyException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import jmar.Base64;
import jmar.ByteArray;
import jmar.MacAddress;
import jmar.ShouldntHappenException;

public class OfflineKeyDecryptor {
	public final String decryptionFailedMessage;
	
	public final byte[] offlineKey;
	public final MacAddress mac;
	public final String userName;
	
	public OfflineKeyDecryptor(byte[] offlineKey) {
		this.offlineKey = offlineKey;
		
		byte[] decryptedBytes;
		try {
			decryptedBytes = TankDecryption.decrypt(offlineKey, 0, offlineKey.length);
		} catch (IllegalBlockSizeException e) {
			this.decryptionFailedMessage = String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
			this.mac = null;
			this.userName = null;
			return;
		} catch (BadPaddingException e) {
			this.decryptionFailedMessage = String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
			this.mac = null;
			this.userName = null;
			return;
		}
		
		if(decryptedBytes == null || decryptedBytes.length < 7) {
			this.decryptionFailedMessage = String.format("The offline key decrypted to only %d bytes: %s",
					decryptedBytes.length, ByteArray.toHexString(decryptedBytes));
			this.mac = null;
			this.userName = null;
			return;
		}
		
		this.decryptionFailedMessage = null;
		this.mac = new MacAddress(decryptedBytes);
		this.userName =  new String(decryptedBytes, 6, decryptedBytes.length - 6);		
	}
	
	public boolean decrypted() {
		return decryptionFailedMessage == null;
	}
}

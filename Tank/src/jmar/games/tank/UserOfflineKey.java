package jmar.games.tank;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import jmar.Base64;
import jmar.ByteArray;
import jmar.DateTime;
import jmar.MacAddress;

public class UserOfflineKey {
	
	public final MacAddress macAddress;
	public final String userName;
	public final String deviceName;
	public final Date created;
	public final Date deactivated;
	
	public final OfflineKeyDecryptor keyDecryptor;
	
	public final String offlineKeyBase64;
	
	public UserOfflineKey(String expectedUserName, JSONObject json) throws JSONException, ParseException {
		this.userName = expectedUserName;
		this.macAddress = new MacAddress(ByteArray.toByteArray(json.getString("Mac")));
		this.deviceName = json.getString("DeviceName");
		this.created = DateTime.parseDashes(json.getString("Created"));
		Object deactivatedObject = json.get("Deactivated");
		if(deactivatedObject == null || deactivatedObject == JSONObject.NULL) {
			this.deactivated = null;
			this.offlineKeyBase64 = json.getString("OfflineKey");
			this.keyDecryptor = new OfflineKeyDecryptor(Base64.decode(this.offlineKeyBase64));
			
			//
			// Check that the keyDecryptor worked
			//
			if(!keyDecryptor.decrypted())
				throw new RuntimeException(String.format("The offline key from the server '%s' could not be decrypted: %s",
						this.offlineKeyBase64, keyDecryptor.decryptionFailedMessage));
			
			if(!keyDecryptor.mac.equals(this.macAddress))
				throw new RuntimeException(String.format("The offline key from the server could said its mac addres was '%s' but the decrypted mac is '%s'",
						this.macAddress, keyDecryptor.mac));
			if(!keyDecryptor.userName.equals(expectedUserName)) 
				throw new RuntimeException(String.format("Expected the username from the offline key to be '%s' but the decrypted user name is '%s'",
						expectedUserName, keyDecryptor.userName));
			
		} else {
			this.deactivated = DateTime.parseDashes(deactivatedObject.toString());
			this.offlineKeyBase64 = null;
			this.keyDecryptor = null;
		}
	}
	
	public boolean isDeactivated() {
		return deactivated != null;
	}
	
	public void saveOnLocalFileSystem() throws IOException {
		if(isDeactivated()) throw new IllegalStateException(String.format("You cannot save an offline key that is deactivated"));
		LocalOfflineKeyFile.saveNewOfflineKey(this.offlineKeyBase64, this.keyDecryptor);
	}
	
	public String toString() {
		return String.format("Mac: %s, User: %s, DeviceName: %s, Created:%s, Deactivated:%s", macAddress, userName, deviceName, created, deactivated);
	}
}

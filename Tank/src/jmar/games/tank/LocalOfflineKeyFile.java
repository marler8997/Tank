package jmar.games.tank;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;

import jmar.Base64;
import jmar.FileHelper;
import jmar.MacAddress;
import jmar.ShouldntHappenException;
import jmar.platform.Platform;

public class LocalOfflineKeyFile {
	public static final String offlineKeysDir = "OfflineKeys";
	
	private static HashSet<String> validOfflineKeyUsers = null;	
	
	public static File getOfflineKeyDirectory() {
		//
		// Prepare the offline keys directory
		//
		File appDataDir = Platform.iface.getAppDataDir();
		File offlineKeyDirectory = new File(new File(appDataDir, TankGameSettingsManager.tankDirectoryName), offlineKeysDir);
		if(!offlineKeyDirectory.exists()) {
			if(!offlineKeyDirectory.mkdir())
				throw new ShouldntHappenException(String.format("Unable to create the directory '%s'.  It is needed to store offline key files", offlineKeyDirectory));
		}
		return offlineKeyDirectory;
	}

	public static ArrayList<LocalOfflineKeyFile> loadKeyFiles() throws IOException {
		validOfflineKeyUsers = new HashSet<String>();
		
		File offlineKeyDirectory = getOfflineKeyDirectory();
		//
		// Get the offline key files
		//		
		String[] offlineKeyFileNames = offlineKeyDirectory.list();
		if(offlineKeyFileNames == null || offlineKeyFileNames.length <= 0) return null;

		//
		// Read the offline keys
		//
		ArrayList<LocalOfflineKeyFile> offlineKeys = new ArrayList<LocalOfflineKeyFile>();
		
		for(int i = 0; i < offlineKeyFileNames.length; i++) {
			LocalOfflineKeyFile offlineKeyFile = new LocalOfflineKeyFile(new File(offlineKeyDirectory, offlineKeyFileNames[i]));
			offlineKeys.add(offlineKeyFile);
			if(offlineKeyFile.isValidForThisMachine()) {
				validOfflineKeyUsers.add(offlineKeyFile.keyDecryptor.userName);
			}
		}
		
		return offlineKeys;
	}
	
	public static void saveNewOfflineKey(String offlineKeyBase64, OfflineKeyDecryptor keyDecryptor) throws IOException {
		if(!keyDecryptor.decrypted()) throw new InvalidParameterException(String.format("Cannot save an offline key that was not properly decrypted: %s", keyDecryptor.decryptionFailedMessage));
		
		File offlineKeyDirectory = getOfflineKeyDirectory();
		
		File newOfflineKeyFile = new File(offlineKeyDirectory, String.format("%s%s", keyDecryptor.mac.toSmallString(), keyDecryptor.userName));
		FileHelper.writeFile(newOfflineKeyFile, offlineKeyBase64);
		
		loadKeyFiles();
	}
	
	public static boolean userHasValidOfflineKey(String userName) {
		if(validOfflineKeyUsers == null) throw new IllegalStateException("CodeError: You called userHasValidOfflineKey but you have not called loadKeys yet");
		return validOfflineKeyUsers.contains(userName);
	}
	
	
	public final File file;
	public final String offlineKeyBase64;
	private final OfflineKeyDecryptor keyDecryptor;

	public LocalOfflineKeyFile(File offlineKeyFile) throws IOException {
		this.file = offlineKeyFile;
		this.offlineKeyBase64 = FileHelper.readFileToString(offlineKeyFile).trim();
		this.keyDecryptor = new OfflineKeyDecryptor(Base64.decode(offlineKeyBase64));
	}
	
	public boolean isValidForThisMachine() {
		return keyDecryptor.decrypted() && MacAddress.getLocalHostMacAddress().equals(keyDecryptor.mac);
	}
	
	public String getReasonThisKeyIsInvalidForThisMachine() {
		
		if(!keyDecryptor.decrypted()) return keyDecryptor.decryptionFailedMessage;
		
		if(!MacAddress.getLocalHostMacAddress().equals(keyDecryptor.mac))
			return String.format("MAC address '%s' from offline key file '%s' did not match your MAC address '%s'", keyDecryptor.mac, file.getName(), MacAddress.getLocalHostMacAddress());
		
		throw new IllegalStateException(String.format("This local offline key file '%s' is valid", file.getName()));
	}
	
	public String getUserNameIfValid() {
		if(!isValidForThisMachine()) throw new IllegalStateException(getReasonThisKeyIsInvalidForThisMachine());
		return keyDecryptor.userName;
	}
	
	public byte[] getOfflineKey() {
		return keyDecryptor.offlineKey;
	}
	
}

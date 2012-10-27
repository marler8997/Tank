package jmar.games.tank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jmar.Base64;
import jmar.json.JSONToJavaAdapter;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class TankGameSettingsManager {
	public static final String tankDirectoryName = "Tank";
	public static final String settingsFileName = "settings.json";	
	
	public final File puckDirectory;
	public final File puckSettingsFile;
	
	public TankGameSettings settings;
	public JSONToJavaAdapter<TankGameSettings> jsonJavaAdapter;
	
	public TankGameSettingsManager(File appDataDir) throws JSONException, IOException {
		this.puckDirectory = new File(appDataDir, TankGameSettingsManager.tankDirectoryName);
		this.puckSettingsFile = new File(puckDirectory, settingsFileName);
		
		if(!puckDirectory.exists()) {
			puckDirectory.mkdir();
		}
		
		this.settings = new TankGameSettings();
		this.jsonJavaAdapter = new JSONToJavaAdapter<TankGameSettings>(TankGameSettings.class);
		
		if(!puckSettingsFile.exists()) {
			this.settings.setDefaults();
			writeToFile();
		} else {
			JSONTokener jsonTokener = new JSONTokener(new FileInputStream(puckSettingsFile));
			JSONObject settingsAsJson = new JSONObject(jsonTokener);
			this.jsonJavaAdapter.jsonToJava(settingsAsJson, this.settings);
			jsonTokener.end();
		}
	}
	
	public void saveUserAndServer(String user, String server) throws IllegalArgumentException, IOException, JSONException, IllegalAccessException {
		boolean isANewUser = true;
		if(settings.users == null) {
			settings.users = new String[1];
			settings.users[0] = user;
		} else {
			for(int i = 0; i < settings.users.length; i++) {
				if(settings.users[i].equalsIgnoreCase(user)) {
					isANewUser = false;
				}
			}
			if(isANewUser) {
				String [] oldUsers = settings.users;
				settings.users = new String[oldUsers.length + 1];
				settings.users[0] = user;
				for(int i = 1; i < settings.users.length; i++) {
					settings.users[i] = oldUsers[i-1];
				}
			}			
		}
		
		boolean isANewServer = true;
		if(settings.servers == null) {
			settings.servers = new String[1];
			settings.servers[0] = server;
		} else {
			for(int i = 0; i < settings.servers.length; i++) {
				if(settings.servers[i].equalsIgnoreCase(server)) {
					isANewServer = false;
				}
			}
			if(isANewServer) {
				String [] oldServers = settings.servers;
				settings.servers = new String[oldServers.length + 1];
				settings.servers[0] = server;
				for(int i = 1; i < settings.servers.length; i++) {
					settings.servers[i] = oldServers[i-1];
				}
			}			
		}
		
		if(isANewUser || isANewServer) {
			writeToFile();
		}
	}
	
	public void writeToFile() throws IOException, JSONException {
		FileWriter writer = new FileWriter(puckSettingsFile, false);
		writer.write(this.jsonJavaAdapter.makeJsonObject(this.settings).toString());
		writer.close();
	}
}

package jmar.games.tank;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import jmar.MacAddress;
import jmar.awt.SystemExitListener;
import jmar.games.PopupWindow;
import jmar.games.StaticRandom;
import jmar.games.tank.Settings;
import jmar.games.tank.StandardLevels;
import jmar.platform.Platform;


public class TankClientMain {

	public static void main(String[] args) throws Exception {
		try {
			Settings.checkThatSettingsAreValid();
			
			Platform.initPlatformInterface();
			
			File appDataDir = Platform.iface.getAppDataDir();
			if(!appDataDir.exists()) {
				throw new UnsupportedOperationException(String.format("The system's application data directory '%s' does not exist, not sure what to do?",
						appDataDir.getAbsolutePath()));
			}
			
			//
			// Get the MAC address of the computer
			//
			MacAddress localHostMacAddress = MacAddress.getLocalHostMacAddress();
			System.out.println(String.format("[Platform] MAC: %s", localHostMacAddress));
			
			//
			// Make sure we use the system proxy settings
			//
			System.setProperty("java.net.useSystemProxies", "true");
			
			URI internetServerURI = new URI(String.format("http://%s", Settings.internetServerHostName));
			List<Proxy> proxiesForInternetServer = ProxySelector.getDefault().select(internetServerURI);
			if(proxiesForInternetServer == null || proxiesForInternetServer.size() <= 0) {
				System.out.println(String.format("[Proxy Settings] No Proxies for '%s'", internetServerURI.toString()));
			} else {
				
				for(Iterator<Proxy> iterator = proxiesForInternetServer.iterator(); iterator.hasNext(); ) {
					Proxy proxy = iterator.next();
					System.out.println(String.format("[Proxy Settings] URI='%s' %sProxy '%s'", internetServerURI, proxy.type(), proxy.address().toString()));
				}
			}			
			
			//
			// Get Program Settings
			//
			TankGameSettingsManager gameSettingsManager = new TankGameSettingsManager(appDataDir);
			TankGameSettings gameSettings = gameSettingsManager.settings;	
			
			//
			// Create an OpenGL Context so we can check the version and vendor of OpenGL
			//
			Pbuffer pBuffer = new Pbuffer(gameSettings.windowWidth, gameSettings.windowHeight, new PixelFormat(0,0,0), null);
			pBuffer.makeCurrent();
			
			String openGLVersionString = GL11.glGetString(GL11.GL_VERSION);
			String openGLVendorString = GL11.glGetString(GL11.GL_VENDOR);
			
			System.out.println("[Platform] OpenGL version: " + openGLVersionString);
			System.out.println("[Platform] OpenGL vendor : " + openGLVendorString);
			
			//
			// Start the LAN Game Listener
			//
			/*
			LanGameListener lanGameListener = new LanGameListener(new LanGameHandler() {
				public void socketException(SocketException e) {
					System.err.println("Cannot listen for LAN games because there was a SocketException: " + e.getMessage());
				}
				public void ioException(IOException e) {
					System.err.println("While listening for LAN games there was an IOException: " + e.getMessage());					
				}
				public void foundLanHost(String hostUserName) {
					System.out.println(String.format("Found a LAN game from '%s'", hostUserName));
				}
			}, Settings.lanClientPort, Settings.lanServerPort);
			new Thread(lanGameListener).start();
			lanGameListener.sendLanQuery();
			*/
			
			// Send game
			
			
			
			
			//
			// Game Builder
			//
			TankGameBuilder gameBuilder = new TankGameBuilder(gameSettingsManager);
			//gameBuilder.myUserName = getRandomName();
			StandardLevels.buildLevelOne(gameBuilder.levelBuilder);
			
			//
			// Game Menu
			//
			new TankGameFrame(gameBuilder, gameSettings.windowWidth, gameSettings.windowHeight);
		} catch(Exception e) {
			e.printStackTrace();
			PopupWindow.PopupException(e, SystemExitListener.getInstance());
		}
	}
	
	static String[] names = new String[] {
		"Johnny",
		"David",
		"Amy",
		"Josh",
		"Spencer",
		"Christa",
		"Torie",
		"Miranda",
		"Carl",
		"Dylan",
		"Corey",
		"Adrian",
		"Cody"};
	
	public static String getRandomName() {
		return names[StaticRandom.random.nextInt(names.length)];
	}
	
}

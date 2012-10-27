package jmar.platform;

import java.io.File;

public class Platform {
	
	public static String osName = null;
	public static PlatformInterface iface = null;
	
	public static void initPlatformInterface() throws UnknownPlatformException {
		if(iface != null) throw new IllegalStateException("You've already initialized the platform interface");
		osName = System.getProperty("os.name");
		String osNameLowerCase = osName.toLowerCase();
		
		if(osNameLowerCase.indexOf("win") == 0) {
			iface = new WindowsPlatformInterface();
		} else if(osNameLowerCase.indexOf("mac") == 0) {
			iface = new MacPlatformInterface();
		} else if(osNameLowerCase.indexOf("nix") >= 0 || osNameLowerCase.indexOf("nux") >= 0) {
			iface = new LinuxUnixPlatformInterface();
		} else {
			throw new UnknownPlatformException();
		}
	}
	
	public static class WindowsPlatformInterface implements PlatformInterface {
		private File userDir = null;
		private File appDataDir = null;
		
		public File getUserHomeDir() {
			if(userDir == null) {
				String userDirString = System.getProperty("user.home");
				if(userDirString == null) {
					throw new UnsupportedOperationException("Could not get the system's user home directory");
				}
				userDir = new File(userDirString);
			}
			return userDir;
		}
		
		public File getAppDataDir() {
			if(appDataDir == null) {
				String appDataDirString = System.getenv("APPDATA");
				if(appDataDirString == null) {
					throw new UnsupportedOperationException("Could not get the system's application data directory");
				}
				appDataDir = new File(appDataDirString);
			}
			return appDataDir;
		}
		
	}
	
	public static class MacPlatformInterface implements PlatformInterface {
		private File userDir = null;
		private File appDataDir = null;
		
		public File getUserHomeDir() {
			if(userDir == null) {
				String userDirString = System.getProperty("user.home");
				if(userDirString == null) {
					throw new UnsupportedOperationException("Could not get the system's user home directory");
				}
				userDir = new File(userDirString);
			}
			return userDir;
		}

		public File getAppDataDir() {
			if(appDataDir == null) {
				File userDir = getUserHomeDir();
				this.appDataDir = new File(new File(userDir, "Library"), "Application Support" );
			}
			return appDataDir;
		}
	}

	public static class LinuxUnixPlatformInterface implements PlatformInterface {
		private File userDir = null;
		private File appDataDir = null;
		
		public File getUserHomeDir() {
			if(userDir == null) {
				String userDirString = System.getProperty("user.home");
				if(userDirString == null) {
					throw new UnsupportedOperationException("Could not get the system's user home directory");
				}
				userDir = new File(userDirString);
			}
			return userDir;
		}

		public File getAppDataDir() {
			throw new UnsupportedOperationException("Could not get the system's application data directory");
		}
	}
}

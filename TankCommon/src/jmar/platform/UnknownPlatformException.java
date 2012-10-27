package jmar.platform;

public class UnknownPlatformException extends Exception {
	public UnknownPlatformException() {
		super(String.format("The current platform '%s' is unknown", Platform.osName));
	}
}

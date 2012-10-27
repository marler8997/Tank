package jmar;

public class ShouldntHappenException extends RuntimeException {
	public ShouldntHappenException(String message) {
		super(message);
	}
	public ShouldntHappenException(Exception e) {
		super(String.format("Got a '%s' exception which should never happend: %s", e.getClass().getSimpleName(), e.getMessage()));
	}
}

package jmar.games;

public class Clock {
	public static long getTime() {
		return System.nanoTime() / 1000000;
	}
}

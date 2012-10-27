package jmar.games.tank;

public interface IHostThread extends Runnable {
	public void prepareToRun();
	public void stop();
}

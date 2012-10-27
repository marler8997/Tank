package jmar;

public class CallbackThread implements Runnable {	
	public static void Go(Runnable runnable, Runnable doneCallback) {
		new Thread(new CallbackThread(runnable, doneCallback)).start();
	}	
	
	private final Runnable runnable;
	private final Runnable doneCallback;
	
	private CallbackThread(Runnable runnable, Runnable doneCallback) {
		this.runnable = runnable;
		this.doneCallback = doneCallback;
	}	
	
	public void run() {
		try {
			runnable.run();
		} finally {
			doneCallback.run();
		}
	}
}

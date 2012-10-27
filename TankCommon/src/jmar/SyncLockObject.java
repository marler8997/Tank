package jmar;

public class SyncLockObject {	
	private boolean locked;	
	private final Object syncObject;
	
	public SyncLockObject() {
		this.locked = false;
		this.syncObject = new Object();
	}
	
	public boolean requestlock() {		
		synchronized(syncObject) {
			if(locked) return false;
			locked = true;
			return true;
		}
	}
	
	public void unlock() {
		synchronized(syncObject) {
			locked = false;
		}
	}
}

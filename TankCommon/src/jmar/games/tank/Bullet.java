package jmar.games.tank;

public class Bullet {
	private boolean active;
	int numberOfPacketsSentIn;
	
	// Initial state when the bullet is activated
	public int initialTimeStep;
    public int initialX,initialY;
    public int targetX,targetY;
    
    // Current state data for the current shot
    public int currentArenaX,currentArenaY;   
    public float currentUnitVectorX,currentUnitVectorY; 
    public int currentBouncesLeft;
    
    public Bullet() {
    	this.reset();
    }    
    
    public void reset() {
    	this.active = false;
    	this.numberOfPacketsSentIn = Settings.numberOfPacketsToSendABullet;
    }
    
    public boolean isActive() {
    	return active;
    }
    
    public void updateState(int timeStep) {
    	// TODO: Make sure that the timeStep doesn't roll more than once here
    	int timeDiff = TimeStepLogic.timeStepDiff(timeStep, initialTimeStep);
    	
    	if(timeDiff <= 0) {return;}
    	if(timeDiff >= Settings.timeStepRolloverValue) {
    		throw new IllegalStateException(String.format("It seems that a bullet has been active for waaaay to long, it has survived 65535 time steps, which is %f seconds",
    				((float)(Settings.millisPerTimeStep * Settings.timeStepRolloverValue) / 1000f)));
    	}
    	
    	int newArenaX = initialX + (int)(currentUnitVectorX * timeDiff / ((float)(Settings.bulletInverseVelocityTimePerArenaUnit + 1)));
    	int newArenaY = initialY + (int)(currentUnitVectorY * timeDiff / ((float)(Settings.bulletInverseVelocityTimePerArenaUnit + 1)));
    	
    	// Check for collisions
    	if(newArenaX < 0 || newArenaX + Settings.defaultBulletArenaSize >= ArenaUtils.arenaWidth) {
    		active = false;
    		return;
    	}
    	if(newArenaY < 0 || newArenaY + Settings.defaultBulletArenaSize >= ArenaUtils.arenaHeight) {
    		active = false;
    		return;
    	}
    	
    	currentArenaX = newArenaX;    	
    	currentArenaY = newArenaY;
    }
    
    
    public void activate(int initialTimeStep, int initialX, int initialY, int targetX, int targetY, int bouncesAllowed) {
    	//
    	// Warning: because of the network protocol, 2 active bullets from the same client cannot have the same initialTimeStep
    	// This means that if a bullet stays active for enough time for the timeStep to rolloever (132 seconds), then this could be a problem
    	// if the client happens to fire another bullet at the same time.
    	// UPDATE: Actually this is ok because if a timestep survives long enough for it's timeDiff to rollover than an exception is thrown
    	//
    	System.out.println(String.format("[DEBUG] Activated Bullet InitialTimeStep=%d, Initial=(%d,%d), Target=(%d,%d)",
    			initialTimeStep, initialX, initialY, targetX, targetY));
		active = true;
    	numberOfPacketsSentIn = 0;
    	
    	// Initial State
    	this.initialTimeStep = initialTimeStep;
    	this.initialX = initialX;
    	this.initialY = initialY;
    	this.targetX = targetX;
    	this.targetY = targetY;
    	
    	// Current Shot State    	
    	this.currentArenaX = initialX;
    	this.currentArenaY = initialY;
    	
    	int xDirection = targetX - initialX;
    	int yDirection = targetY - initialY;
    	double vectorLength = Math.sqrt(xDirection * xDirection + yDirection * yDirection);
    	
    	this.currentUnitVectorX = (float)((double)xDirection / vectorLength);
    	this.currentUnitVectorY = (float)((double)yDirection / vectorLength);
    	
    	this.currentBouncesLeft = bouncesAllowed;
    }
    
    public String toString() {
    	return String.format("Bullet Initial=(%d,%d) Target=(%d,%d) ShootTime=%d CurrentPos=(%d,%d) Dir=(%f,%f)", initialX, initialY,
    			targetX, targetY, initialTimeStep, currentArenaX, currentArenaY, currentUnitVectorX, currentUnitVectorY);
    }
}

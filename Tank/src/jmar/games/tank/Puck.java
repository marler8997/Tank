package jmar.games.tank;

import static org.lwjgl.opengl.GL11.*;
import jmar.games.tank.ArenaUtils;
import jmar.games.tank.Bullet;
import jmar.games.tank.MovingArenaObject;
import jmar.games.tank.Settings;

import org.lwjgl.util.vector.Vector3f;

public class Puck {
	public static final int defaultPlayerSizeFromCenter = 15;
	public static final int defaultBullets = 5;	
	
	public MovingArenaObject puckArenaObject;

    public final BulletForClient[] bullets;
    public int bulletCapacity;
    public int bulletBouncesAllowed;
    
    public Vector3f color;
    private Vector3f sideColor;

    
    public Puck(int middleX, int middleY, int oddHalfWidth, int oddHalfHeight, int inverseVelocityTimePerArenaUnit, int bulletCapacity, int bulletBouncesAllowed, Vector3f color){
    	this.puckArenaObject = new MovingArenaObject(middleX, middleY, oddHalfWidth, oddHalfHeight, inverseVelocityTimePerArenaUnit);
    	
    	this.bullets = new BulletForClient[Settings.maximumPossibleBullets];
    	for(int i = 0; i < bullets.length; i++) {
    		bullets[i] = new BulletForClient();
    	}
    	this.bulletBouncesAllowed = bulletBouncesAllowed;
    	this.bulletCapacity = bulletCapacity;
    	
    	this.color = new Vector3f(color);
    	this.sideColor = new Vector3f(color.x * .8f, color.y * .8f, color.z * .8f);	  
    }   
    
    // returns -1 for no available bullets
    public int availableBulletIndex() {
    	for(int i = 0; i < bulletCapacity; i++) {
    		if(!bullets[i].isActive()) return i;
    	}
    	return -1;
    }
    
    public void userRequestShoot(int timeStep, int bulletIndex, int targetX, int targetY) {
    	// Activate the bullet
    	bullets[bulletIndex].activate(timeStep, puckArenaObject.getMiddleX(), puckArenaObject.getMiddleY(), targetX, targetY, bulletBouncesAllowed);
    	System.out.println(String.format("Shoot [%d] %s", bulletIndex, bullets[bulletIndex].toString()));
    }
    
    public void moveBullets(int timeStep) {
    	for(int i = 0; i < bulletCapacity; i++) {
    		Bullet bullet = bullets[i];
    		if(bullet.isActive()) {
    			bullet.updateState(timeStep);
    		}
    	}
    }

	public void glDraw() {	
		glBegin(GL_QUADS);	

	    
	    float glLeft   = ArenaUtils.arenaToGLX(puckArenaObject.getLeft()  );
	    float glRight  = ArenaUtils.arenaToGLX(puckArenaObject.getRight() );
	    float glBottom = ArenaUtils.arenaToGLY(puckArenaObject.getBottom());
	    float glTop    = ArenaUtils.arenaToGLY(puckArenaObject.getTop()   );

	    // Draw Shadow
	    glColor4f(0,0,0,.2f);
		glVertex3f(glLeft  - Settings.puckShadowGlOffset, glBottom - Settings.puckShadowGlOffset, 0.1f);
		glVertex3f(glLeft  - Settings.puckShadowGlOffset, glTop    + Settings.puckShadowGlOffset, 0.1f);
		glVertex3f(glRight + Settings.puckShadowGlOffset, glTop    + Settings.puckShadowGlOffset, 0.1f);
		glVertex3f(glRight + Settings.puckShadowGlOffset, glBottom - Settings.puckShadowGlOffset, 0.1f);    
	    
	    
	    glColor3f(sideColor.x, sideColor.y, sideColor.z);
	    
		//draw left
		glVertex3f(glLeft, glBottom, 0);
		glVertex3f(glLeft, glBottom, Settings.puckGLHeight);
		glVertex3f(glLeft, glTop, Settings.puckGLHeight);
		glVertex3f(glLeft, glTop, 0);
	    
		//draw right
		glVertex3f(glRight, glBottom, 0);
		glVertex3f(glRight, glBottom, Settings.puckGLHeight);
		glVertex3f(glRight, glTop, Settings.puckGLHeight);
		glVertex3f(glRight, glTop, 0);		

		//draw front
		glVertex3f(glLeft, glBottom, 0);
		glVertex3f(glLeft, glBottom, Settings.puckGLHeight);
		glVertex3f(glRight, glBottom, Settings.puckGLHeight);
		glVertex3f(glRight, glBottom, 0);		
		
		// draw top
	    glColor3f(color.x, color.y, color.z);
	    
		glVertex3f(glLeft, glTop, Settings.puckGLHeight);
		glVertex3f(glRight, glTop, Settings.puckGLHeight);
	    glColor3f(color.x * .9f, color.y* .9f, color.z* .9f);
		glVertex3f(glRight, glBottom, Settings.puckGLHeight);
	    glColor3f(color.x, color.y, color.z);
		glVertex3f(glLeft, glBottom, Settings.puckGLHeight);
		
		glEnd();
		
		for(int i = 0; i < bulletCapacity; i++) {
			BulletForClient bullet = bullets[i];
			if(bullet.isActive()) {
				bullet.glDraw();
			}
		}
		
	}
}

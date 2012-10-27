package jmar.games.tank;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex3f;
import jmar.games.tank.ArenaUtils;
import jmar.games.tank.Bullet;
import jmar.games.tank.Settings;

public class BulletForClient extends Bullet {

    public BulletForClient() {
    	super();
    }    
    
    public void glDraw() {
		glBegin(GL_QUADS);

	    float glLeft   = ArenaUtils.arenaToGLX(this.currentArenaX);
	    float glRight  = ArenaUtils.arenaToGLX(this.currentArenaX + Settings.defaultBulletArenaSize);
	    float glBottom = ArenaUtils.arenaToGLY(this.currentArenaY);
	    float glTop    = ArenaUtils.arenaToGLY(this.currentArenaY + Settings.defaultBulletArenaSize);
	    
	    float glHeightDiff = ((float)((ArenaUtils.arenaToGLWidth(Settings.defaultBulletArenaSize)) +
	    		(ArenaUtils.arenaToGLHeight(Settings.defaultBulletArenaSize)))) / 4f;
		glColor3f(.8f, .8f, .8f);
	    
		//draw left
		glVertex3f(glLeft, glBottom, Settings.bulletHeight - glHeightDiff);
		glVertex3f(glLeft, glBottom, Settings.bulletHeight + glHeightDiff);
		glVertex3f(glLeft, glTop   , Settings.bulletHeight + glHeightDiff);
		glVertex3f(glLeft, glTop   , Settings.bulletHeight - glHeightDiff);
	    
		//draw right
		glVertex3f(glRight, glBottom, Settings.bulletHeight - glHeightDiff);
		glVertex3f(glRight, glBottom, Settings.bulletHeight + glHeightDiff);
		glVertex3f(glRight, glTop, Settings.bulletHeight + glHeightDiff);
		glVertex3f(glRight, glTop, Settings.bulletHeight - glHeightDiff);		

		//draw front
		glVertex3f(glLeft, glBottom, Settings.bulletHeight - glHeightDiff);
		glVertex3f(glLeft, glBottom, Settings.bulletHeight + glHeightDiff);
		glVertex3f(glRight, glBottom, Settings.bulletHeight + glHeightDiff);
		glVertex3f(glRight, glBottom, Settings.bulletHeight - glHeightDiff);
		
		// draw top
		glColor3f(1f, 1f, 1f);	    
		glVertex3f(glLeft, glTop, Settings.bulletHeight + glHeightDiff);
		glVertex3f(glRight, glTop, Settings.bulletHeight + glHeightDiff);
	    glColor3f(.9f, .9f, .9f);
		glVertex3f(glRight, glBottom, Settings.bulletHeight + glHeightDiff);
		glColor3f(1f, 1f, 1f);	  
		glVertex3f(glLeft, glBottom, Settings.bulletHeight + glHeightDiff);
		
		glEnd();
    	
    }

}

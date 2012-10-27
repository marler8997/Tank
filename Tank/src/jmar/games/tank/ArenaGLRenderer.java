package jmar.games.tank;

import static org.lwjgl.opengl.GL11.*;
import jmar.games.tank.ArenaUtils;
import jmar.games.tank.Settings;
import jmar.games.tank.Wall;

import org.lwjgl.util.vector.Vector3f;

public class ArenaGLRenderer {
	private TankGame game;
	
	public float gridHalfGLThickness;
	public Vector3f gridColor;
	
	public ArenaGLRenderer(TankGame game) {
		this.game = game;
		this.gridHalfGLThickness = 0;
		this.gridColor = new Vector3f(0,1,0);
	}
	
	public void glDraw() {
		float glWidthHalf = ArenaUtils.glArenaHalfWidth;
		float glHeighHalf = ArenaUtils.glArenaHalfHeight;

		glBegin(GL_QUADS);

		// Arena Floor Grid
		if(gridHalfGLThickness > 0) {
			glColor3f(gridColor.x, gridColor.y, gridColor.z);
			
			float columnGLYBottom = ArenaUtils.arenaToGLY(0);
			float columnGLYTop = ArenaUtils.arenaToGLY(ArenaUtils.arenaHeight);
			for(int i = 0; i <= ArenaUtils.arenaWidth; i+=15) {
				
				float columnGlX = ArenaUtils.arenaToGLX(i);
				float columnGLXLeft = columnGlX - gridHalfGLThickness;
				float columnGLXRight = columnGlX + gridHalfGLThickness;
				
				for(int j = 0; j < ArenaUtils.arenaHeight; j+=10) {
					glVertex3f(columnGLXLeft , columnGLYTop, 0);
					glVertex3f(columnGLXRight, columnGLYTop, 0);
					glVertex3f(columnGLXRight, columnGLYBottom, 0);
					glVertex3f(columnGLXLeft , columnGLYBottom, 0);					
				}
			}
			
			float rowGLXLeft = ArenaUtils.arenaToGLX(0);
			float rowGLXRight = ArenaUtils.arenaToGLX(ArenaUtils.arenaWidth);			
			for(int i = 0; i <= ArenaUtils.arenaHeight; i+=10) {
				float rowGlY = ArenaUtils.arenaToGLY(i);
				float rowGLXTop = rowGlY + gridHalfGLThickness;
				float rowGLXBottom= rowGlY - gridHalfGLThickness;
				
				for(int j = 0; j < ArenaUtils.arenaHeight; j+=10) {
					glVertex3f(rowGLXLeft , rowGLXTop, 0);
					glVertex3f(rowGLXRight, rowGLXTop, 0);
					glVertex3f(rowGLXRight, rowGLXBottom, 0);
					glVertex3f(rowGLXLeft , rowGLXBottom, 0);					
				}
				
			}
		}
		
		// Walls			
	    glColor3f(.3f, .3f, .3f);
	    
		// Top Wall
		glVertex3f(-glWidthHalf, glHeighHalf, 0);
		glVertex3f(-glWidthHalf, glHeighHalf, Settings.wallHeight);
		glVertex3f( glWidthHalf, glHeighHalf, Settings.wallHeight);
		glVertex3f( glWidthHalf, glHeighHalf, 0);
		
		// Left Wall
		glVertex3f(-glWidthHalf, -glHeighHalf, 0);
		glVertex3f(-glWidthHalf, -glHeighHalf, Settings.wallHeight);
		glVertex3f(-glWidthHalf,  glHeighHalf, Settings.wallHeight);
		glVertex3f(-glWidthHalf,  glHeighHalf, 0);			

		// Right Wall
		glVertex3f(glWidthHalf, -glHeighHalf, 0);
		glVertex3f(glWidthHalf, -glHeighHalf, Settings.wallHeight);
		glVertex3f(glWidthHalf,  glHeighHalf, Settings.wallHeight);
		glVertex3f(glWidthHalf,  glHeighHalf, 0);	
		
		// Wall Caps
	    glColor3f(.4f, .4f, .4f);
	    
	    // Top Cap
		glVertex3f(-glWidthHalf, glHeighHalf                             , Settings.wallHeight);
		glVertex3f(-glWidthHalf, glHeighHalf + Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f( glWidthHalf, glHeighHalf + Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f( glWidthHalf, glHeighHalf                             , Settings.wallHeight);
		
		
		// Bottom Inner Wall
	    glColor3f(.3f, .3f, .3f);
		glVertex3f(-glWidthHalf, -glHeighHalf, 0);
		glVertex3f(-glWidthHalf, -glHeighHalf, Settings.wallHeight);
		glVertex3f( glWidthHalf, -glHeighHalf, Settings.wallHeight);
		glVertex3f( glWidthHalf, -glHeighHalf, 0);
		
		// Left Outer Wall
		glVertex3f(-glWidthHalf - Settings.boundaryWallWidth, -glHeighHalf - Settings.boundaryWallWidth, 0);
		glVertex3f(-glWidthHalf - Settings.boundaryWallWidth, -glHeighHalf - Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f(-glWidthHalf - Settings.boundaryWallWidth,  glHeighHalf + Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f(-glWidthHalf - Settings.boundaryWallWidth,  glHeighHalf + Settings.boundaryWallWidth, 0);

		// Right Outer Wall
		glVertex3f(glWidthHalf + Settings.boundaryWallWidth, -glHeighHalf - Settings.boundaryWallWidth, 0);
		glVertex3f(glWidthHalf + Settings.boundaryWallWidth, -glHeighHalf - Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f(glWidthHalf + Settings.boundaryWallWidth,  glHeighHalf + Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f(glWidthHalf + Settings.boundaryWallWidth,  glHeighHalf + Settings.boundaryWallWidth, 0);

		// Front Cover Wall
		glVertex3f(-glWidthHalf - Settings.boundaryWallWidth, -glHeighHalf - Settings.boundaryWallWidth, 0);
		glVertex3f(-glWidthHalf - Settings.boundaryWallWidth, -glHeighHalf - Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f( glWidthHalf + Settings.boundaryWallWidth, -glHeighHalf - Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f( glWidthHalf + Settings.boundaryWallWidth, -glHeighHalf - Settings.boundaryWallWidth, 0);


	    glColor3f(.4f, .4f, .4f);
		// Left Cap
		glVertex3f(-glWidthHalf                             , -glHeighHalf - Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f(-glWidthHalf - Settings.boundaryWallWidth, -glHeighHalf - Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f(-glWidthHalf - Settings.boundaryWallWidth,  glHeighHalf + Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f(-glWidthHalf                             ,  glHeighHalf + Settings.boundaryWallWidth, Settings.wallHeight);	
		
		// Right Cap
		glVertex3f(glWidthHalf                             , -glHeighHalf - Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f(glWidthHalf + Settings.boundaryWallWidth, -glHeighHalf - Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f(glWidthHalf + Settings.boundaryWallWidth,  glHeighHalf + Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f(glWidthHalf                             ,  glHeighHalf + Settings.boundaryWallWidth, Settings.wallHeight);	
		
		// Bottom Cap
		glVertex3f(-glWidthHalf, -glHeighHalf                             , Settings.wallHeight);
		glVertex3f(-glWidthHalf, -glHeighHalf - Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f( glWidthHalf, -glHeighHalf - Settings.boundaryWallWidth, Settings.wallHeight);
		glVertex3f( glWidthHalf, -glHeighHalf                             , Settings.wallHeight);
		

		
		// Draw the non-boundary walls
		for(int i = 0; i < game.level.walls.length; i++) {
			Wall wall = game.level.walls[i];
			
			// Walls			

		    float glLeft   = ArenaUtils.arenaToGLX(wall.x);
		    float glRight  = ArenaUtils.arenaToGLX(wall.rightLimit);
		    float glBottom = ArenaUtils.arenaToGLY(wall.y);
		    float glTop    = ArenaUtils.arenaToGLY(wall.topLimit);

		    glColor3f(.3f, .3f, .3f);
		    // Back
			glVertex3f(glLeft , glTop   , Settings.wallHeight);
			glVertex3f(glRight, glTop   , Settings.wallHeight);
			glVertex3f(glRight, glTop   , 0);
			glVertex3f(glLeft , glTop   , 0);
		    // Front
			glVertex3f(glLeft , glBottom, Settings.wallHeight);
			glVertex3f(glRight, glBottom, Settings.wallHeight);
			glVertex3f(glRight, glBottom, 0);
			glVertex3f(glLeft , glBottom, 0);
			// Left
			glVertex3f(glLeft , glTop   , Settings.wallHeight);
			glVertex3f(glLeft , glBottom, Settings.wallHeight);
			glVertex3f(glLeft , glBottom, 0);
			glVertex3f(glLeft , glTop   , 0);
			// Right
			glVertex3f(glRight, glTop   , Settings.wallHeight);
			glVertex3f(glRight, glBottom, Settings.wallHeight);
			glVertex3f(glRight, glBottom, 0);
			glVertex3f(glRight, glTop   , 0);
		    glColor3f(.4f, .4f, .4f);
			// Top
			glVertex3f(glLeft , glTop   , Settings.wallHeight);
			glVertex3f(glRight, glTop   , Settings.wallHeight);
			glVertex3f(glRight, glBottom, Settings.wallHeight);
			glVertex3f(glLeft , glBottom, Settings.wallHeight);	
		}		
		
		
		
		
		glEnd();
	}
	
	
}

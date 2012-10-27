package jmar.games.menus;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector4f;

public class Panel extends Container {
	public Panel() {
		super(0,0,0,0,null);
	}
	public Panel(float x, float y, float xLength, float yLength, Vector4f backColor) {
		super(x,y,xLength,yLength,backColor);
	}
		
	public void glDraw(GlobalState globalState) {
		
		if(backColor != null) {
			// draw background
			glBegin(GL_QUADS);	

		    glColor4f(backColor.x, backColor.y, backColor.z, backColor.w);
		    
			//draw left
			glVertex2f(x          ,y          );
			glVertex2f(x          ,y + height);
			glVertex2f(x + width,y + height);
			glVertex2f(x + width,y          );
			
			glEnd();
		}
		
		if(children != null) {
			for(int i =0; i < children.size(); i++) {
				children.get(i).glDraw(globalState);
			}
		}
		
	}	

	public String toString() {
		return "Panel" + super.toString();
	}
}

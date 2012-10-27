package jmar.games.menus;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;
import jmar.games.Color;
import jmar.games.menus.text.Font;
import jmar.games.menus.text.TextRenderer;

import org.lwjgl.util.vector.Vector4f;

public class Button extends Control {

	public float backColorGradient;
	
	public int borderWidth;
	public Vector4f borderColor;
	
	public String text;	
	public Font font;
	
	public Button(float x, float y, float xLength, float yLength, Vector4f backColor,
			String text, Font font){
		super(x,y,xLength,yLength,backColor);
		
		this.backColorGradient = .1f;
		
		this.borderWidth = 1;
		this.borderColor = Color.Black4f;
		
		this.text = text;
		this.font = font;
	}
	
	

	public void glDraw(GlobalState globalState) {
		glBegin(GL_QUADS);
		
		boolean mouseIsOver = (globalState != null && globalState.mouseOverControl == this);
		
		float gradientMultipler;
		// draw Background
		gradientMultipler = 1 + .1f + backColorGradient + ((mouseIsOver) ? .1f : 0);
	    glColor4f(backColor.x * gradientMultipler, backColor.y * gradientMultipler,
	    		backColor.z * gradientMultipler, backColor.w);
	    
		glVertex2f(x          ,y + height);
		glVertex2f(x + width,y + height);		
	
		gradientMultipler = 1 - backColorGradient + ((mouseIsOver) ? .1f : 0);	
	    glColor4f(backColor.x * gradientMultipler, backColor.y * gradientMultipler,
	    		backColor.z * gradientMultipler, backColor.w);
		
		glVertex2f(x + width,y          );
		glVertex2f(x          ,y          );
		
		glEnd();
		
		// Draw Text
		if(this.text != null) {
			float textWidth = font.getFontWidth(this.text.length());
			//if(textWidth > xLength) throw new IllegalStateException(String.format("Button text has width %f but total width is only %f", textWidth, xLength));
			TextRenderer.drawText(text, x + width / 2f - textWidth / 2f, y + height / 2f - this.font.charHeight / 2f, this.font); 
		}
		
		

		glBegin(GL_QUADS);
		
		// Draw Border
	    glColor4f(borderColor.x, borderColor.y, borderColor.z, borderColor.w);
		glVertex2f(x          ,y          );
		glVertex2f(x + width,y          );
		glVertex2f(x + width,y + borderWidth);
		glVertex2f(x          ,y + borderWidth);

		glVertex2f(x          ,y + height         );
		glVertex2f(x + width,y + height          );
		glVertex2f(x + width,y + height - borderWidth);
		glVertex2f(x          ,y + height - borderWidth);

		glVertex2f(x              ,y          );
		glVertex2f(x              ,y + height);
		glVertex2f(x + borderWidth,y + height);
		glVertex2f(x + borderWidth,y          );
		
		glVertex2f(x + width              ,y          );
		glVertex2f(x + width              ,y + height);
		glVertex2f(x + width - borderWidth,y + height);
		glVertex2f(x + width - borderWidth,y          );
		
		glEnd();
	}
	
	public String toString() {
		return "Button" + super.toString();
	}
}

package jmar.games.menus;

import static org.lwjgl.opengl.GL11.*;

import jmar.games.Color;
import jmar.games.menus.text.Font;
import jmar.games.menus.text.TextRenderer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector4f;

public class InputTextBox extends Control {
	public int borderWidth;
	public Vector4f borderColor;
	
	int textCursorPosition;
	int textScrollPosition;
	public String text;
	public Font font;
	
	public InputTextBox(float x, float y, float xLength, float yLength, Font font){
		super(x,y,xLength,yLength,Color.White4f);
		
		this.borderWidth = 1;
		this.borderColor = Color.Black4f;
		
		this.textCursorPosition = 0;
		this.textScrollPosition = -1;
		this.text = text;
		this.font = font;
	}	

	public void glDraw(GlobalState globalState) {
		glBegin(GL_QUADS);
		
		// draw Background
	    glColor4f(backColor.x , backColor.y, backColor.z, backColor.w);
	    
		glVertex2f(x          ,y + height);
		glVertex2f(x + width,y + height);
		glVertex2f(x + width,y          );
		glVertex2f(x          ,y          );		

		glEnd();
		
		
		// Draw Text
		if(this.text != null) {
			float textWidth = font.getFontWidth(this.text.length());
			if(textWidth + 2 * font.charSpacing <= width) {
				TextRenderer.drawText(text, x + font.charSpacing, y + height / 2f - this.font.charHeight / 2f, this.font);		
			} else {
				if(textScrollPosition != -1) {
					throw new UnsupportedOperationException("Text Scroll Posiiton no yet supported");
				} else {
					TextRenderer.drawText(text, x - textWidth + width, y + height / 2f - this.font.charHeight / 2f, this.font); 							
				}
			}
		}

		glBegin(GL_QUADS);
		
		// Draw Cursor
		if(globalState != null && globalState.focusedControl == this) {
			float textWidth = 0;
			if(this.text != null) {
				textWidth = font.getFontWidth(this.text.length());
			}
		    glColor4f(0,0,0,1);
			glVertex2f(x + textWidth + 3,  y + height/2 + this.font.charHeight + 2);
			glVertex2f(x + textWidth + 5, y + height/2 + this.font.charHeight + 2);
			glVertex2f(x + textWidth + 5, y + height/2 - this.font.charHeight + 2);
			glVertex2f(x + textWidth + 3, y + height/2 - this.font.charHeight + 2);
		}		
		
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

	public void receiveKeyDown(int key, char keyChar) {
		//System.out.println(String.format("Key=%d, Char=%c", key, keyChar));
		if(key == Keyboard.KEY_BACK) {
			if(text != null && text.length() > 0) text = text.substring(0, text.length()-1);
		} else if(key == Keyboard.KEY_RETURN) {
			if(enterKeyWhileFocusedListener != null) enterKeyWhileFocusedListener.enterKeyWhileFocused(this);
		} else {
			if(text == null) text = "";
			text += keyChar;
		}
	}
	public void receiveKeyUp(int key, char keyChar) { }
	
	public String toString() {
		return "InputTextBox" + super.toString();
	}
}
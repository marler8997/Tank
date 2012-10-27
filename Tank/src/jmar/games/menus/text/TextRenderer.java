package jmar.games.menus.text;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

import org.lwjgl.util.vector.Vector4f;

public class TextRenderer {
	public static void drawText(String text, float x, float y, Font font) {
		// Set Color
		glColor4f(font.color.x, font.color.y, font.color.z, font.color.w);
		
		// Setup Position
		Vector4f position = new Vector4f(x, y, font.charWidth, font.charHeight);
		
		// Render each character
		for(int i = 0; i < text.length(); i++) {
			font.renderers[text.charAt(i)].drawChar(position);
			position.x += font.charWidth + font.charSpacing;
		}		
	}	
}

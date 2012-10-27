package jmar.games.menus.text.character;

import org.lwjgl.util.vector.Vector4f;
import static org.lwjgl.opengl.GL11.*;

import jmar.games.menus.text.CharacterRenderer;
import jmar.games.menus.text.Font;
import jmar.games.menus.text.FullCharacterRendererSet;
import jmar.games.menus.text.NoCharacter;

public class EightSegRenderer implements CharacterRenderer {
	public static final byte Top             = 0x01;
	public static final byte TopRight        = 0x02;
	public static final byte BottomRight     = 0x04;
	public static final byte Bottom          = 0x08;
	public static final byte BottomLeft      = 0x10;
	public static final byte TopLeft         = 0x20;
	public static final byte HorzCenter      = 0x40;
	public static final byte VertCenter      = (byte)0x80;
	
	
	public final byte segmentFlags;
	public final float segmentWidth;
	
	public EightSegRenderer(byte segmentFlags, float segmentWidth) {
		this.segmentFlags = segmentFlags;
		this.segmentWidth = segmentWidth;
	}

	@Override
	public void drawChar(Vector4f dim) {		
		glBegin(GL_QUADS);
	    
		float x = dim.x;
		float y = dim.y;
		float width = dim.z;
		float height = dim.w;
		float halfHeight = height / 2f;
		
		if((segmentFlags & Top) != 0) {
			glVertex2f(x        ,y + height               );
			glVertex2f(x + width,y + height               );
			glVertex2f(x + width,y + height - segmentWidth);	
			glVertex2f(x        ,y + height - segmentWidth);		
		}
		if((segmentFlags & TopRight) != 0) {
			glVertex2f(x + width - segmentWidth,y + height);
			glVertex2f(x + width               ,y + height);
			glVertex2f(x + width               ,y + halfHeight);
			glVertex2f(x + width - segmentWidth,y + halfHeight);
		}
		if((segmentFlags & BottomRight) != 0) {
			glVertex2f(x + width - segmentWidth,y + halfHeight);
			glVertex2f(x + width               ,y + halfHeight);
			glVertex2f(x + width               ,y             );
			glVertex2f(x + width - segmentWidth,y             );
		}
		if((segmentFlags & Bottom) != 0) {
			glVertex2f(x        ,y               );
			glVertex2f(x + width,y               );
			glVertex2f(x + width,y + segmentWidth);	
			glVertex2f(x        ,y + segmentWidth);		
		}
		if((segmentFlags & BottomLeft) != 0) {
			glVertex2f(x + segmentWidth,y + halfHeight);
			glVertex2f(x               ,y + halfHeight);
			glVertex2f(x               ,y             );
			glVertex2f(x + segmentWidth,y             );
		}
		if((segmentFlags & TopLeft) != 0) {
			glVertex2f(x + segmentWidth,y + height);
			glVertex2f(x               ,y + height);
			glVertex2f(x               ,y + halfHeight);
			glVertex2f(x + segmentWidth,y + halfHeight);
		}
		
		float segmentHalfWidth;
		if((segmentFlags & HorzCenter) != 0) {
			segmentHalfWidth = segmentWidth / 2f;
			glVertex2f(x        ,y + halfHeight + segmentHalfWidth);
			glVertex2f(x + width,y + halfHeight + segmentHalfWidth);
			glVertex2f(x + width,y + halfHeight - segmentHalfWidth);	
			glVertex2f(x        ,y + halfHeight - segmentHalfWidth);		
		}
		if((segmentFlags & VertCenter) != 0) {
			segmentHalfWidth = segmentWidth / 2f;
			float widthHalf = width/2f;
			glVertex2f(x + widthHalf - segmentHalfWidth,y + height);
			glVertex2f(x + widthHalf + segmentHalfWidth,y + height);
			glVertex2f(x + widthHalf + segmentHalfWidth,y         );	
			glVertex2f(x + widthHalf - segmentHalfWidth,y         );		
		}
		
		glEnd();
	}	
}

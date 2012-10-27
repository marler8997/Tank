package jmar.games.menus.text.character;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.util.vector.Vector4f;

import jmar.games.menus.text.CharacterRenderer;

public class SpecialRenderer {
	public static class YRenderer implements CharacterRenderer {
		
		public float segmentWidth;
		private float segmentHalfWidth;
		
		public YRenderer(float segmentWidth) {
			this.segmentWidth = segmentWidth;
			this.segmentHalfWidth = segmentWidth / 2;
		}
		
		@Override
		public void drawChar(Vector4f dim) {
			float x = dim.x;
			float y = dim.y;
			float width = dim.z;
			float height = dim.w;
			
			float xMiddle = x + width / 2f;
			float yMiddle = y + height / 2f;
			
			glBegin(GL_QUADS);
			
			glVertex2f(xMiddle - segmentHalfWidth,yMiddle);
			glVertex2f(xMiddle + segmentHalfWidth,yMiddle);
			glVertex2f(xMiddle + segmentHalfWidth,y      );
			glVertex2f(xMiddle - segmentHalfWidth,y      );

			glVertex2f(x                         , y + height);
			glVertex2f(x + segmentWidth          ,y + height);
			glVertex2f(xMiddle + segmentHalfWidth,yMiddle );
			glVertex2f(xMiddle - segmentHalfWidth,yMiddle);

			glVertex2f(x + width - segmentWidth  , y + height);
			glVertex2f(x + width                 ,y + height);
			glVertex2f(xMiddle + segmentHalfWidth,yMiddle );
			glVertex2f(xMiddle - segmentHalfWidth,yMiddle);		
			
			glEnd();
		}		
	}

	public static class RRenderer extends EightSegRenderer {
		
		public RRenderer(float segmentWidth) {
			super((byte)(EightSegRenderer.Top | EightSegRenderer.TopRight | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft | EightSegRenderer.HorzCenter), segmentWidth);
		}
		
		@Override
		public void drawChar(Vector4f dim) {
			super.drawChar(dim);
			
			float x = dim.x;
			float y = dim.y;
			float width = dim.z;
			float height = dim.w;
			
			float xMiddle = x + width/2;
			float yMiddle = y + height / 2f;
			
			// Draw leg of R
			glBegin(GL_QUADS);
			
			glVertex2f(xMiddle                 ,yMiddle);
			glVertex2f(xMiddle   + segmentWidth,yMiddle);
			glVertex2f(x + width               ,y      );
			glVertex2f(x + width - segmentWidth,y      );
			
			glEnd();
		}		
	}

	public static class NRenderer extends EightSegRenderer {
		
		public NRenderer(float segmentWidth) {
			super((byte)(EightSegRenderer.TopRight | EightSegRenderer.BottomRight | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft), segmentWidth);
		}
		
		@Override
		public void drawChar(Vector4f dim) {
			super.drawChar(dim);
			
			float x = dim.x;
			float y = dim.y;
			float width = dim.z;
			float height = dim.w;
			
			// Draw leg of R
			glBegin(GL_QUADS);
			
			glVertex2f(x                       ,y + height);
			glVertex2f(x   + segmentWidth      ,y + height);
			glVertex2f(x + width               ,y      );
			glVertex2f(x + width - segmentWidth,y      );
			
			glEnd();
		}		
	}

	public static class VRenderer implements CharacterRenderer {
		public float segmentWidth;
		public float segmentHalfWidth;
		
		public VRenderer(float segmentWidth) {
			this.segmentWidth = segmentWidth;
			this.segmentHalfWidth = segmentWidth / 2f;
		}
		
		@Override
		public void drawChar(Vector4f dim) {
			float x = dim.x;
			float y = dim.y;
			float width = dim.z;
			float height = dim.w;
			
			float xMiddle = x + width / 2f;
			
			// Draw leg of R
			glBegin(GL_QUADS);
			
			glVertex2f(x                         ,y + height);
			glVertex2f(x       + segmentWidth    ,y + height);
			glVertex2f(xMiddle + segmentHalfWidth,y      );
			glVertex2f(xMiddle - segmentHalfWidth,y      );

			glVertex2f(x + width - segmentWidth,y + height);
			glVertex2f(x + width               ,y + height);
			glVertex2f(xMiddle + segmentHalfWidth,y      );
			glVertex2f(xMiddle - segmentHalfWidth,y      );
			
			glEnd();
		}		
	}
	
	public static class PeriodRenderer implements CharacterRenderer {
		
		public float segmentWidth;
		private float segmentHalfWidth;
		
		public PeriodRenderer(float segmentWidth) {
			this.segmentWidth = segmentWidth;
			this.segmentHalfWidth = segmentWidth / 2;
		}
		
		@Override
		public void drawChar(Vector4f dim) {
			float x = dim.x;
			float y = dim.y;
			float width = dim.z;
			
			float xMiddle = x + width / 2f;
			
			glBegin(GL_QUADS);
			glVertex2f(xMiddle - segmentHalfWidth,y + segmentWidth);
			glVertex2f(xMiddle + segmentHalfWidth,y + segmentWidth);
			glVertex2f(xMiddle + segmentHalfWidth,y);
			glVertex2f(xMiddle - segmentHalfWidth,y);
			glEnd();
		}
		
	}
}

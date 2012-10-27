package jmar.games.menus;

import jmar.games.menus.text.Font;
import jmar.games.menus.text.TextRenderer;

public class Text extends Control {	
	public String text;	
	public Font font;
	
	public AlignX alignX;
	public AlignY alignY;
	
	public Text(float x, float y, String text, Font font, AlignX alignX, AlignY alignY){
		super(x,y,0,0,null);		
		this.text = text;
		this.font = font;
		
		this.alignX = alignX;
		this.alignY = alignY;
	}	

	public void glDraw(GlobalState globalState) {		
		// Draw Text
		if(this.text != null) {
			float textWidth;
			
			float x,y;
			
			switch(alignX) {
			case Left:
				x = this.x;
				break;
			case Center:
				textWidth = font.getFontWidth(this.text.length());
				x = this.x - textWidth / 2;
				break;
			case Right:
				textWidth = font.getFontWidth(this.text.length());
				x = this.x - textWidth;	
				break;
			default:
				throw new IllegalStateException("Unknown AlignX");
			}
			switch(alignY) {
			case Top:
				y = this.y + font.charHeight;
				break;
			case Center:
				y = this.y + font.charHeight / 2;
				break;
			case Bottom:
				y = this.y;
				break;	
			default:
				throw new IllegalStateException("Unknown AlignY");
			}
			

			TextRenderer.drawText(this.text, x, y, this.font); 
			
		}
	}
	
	public String toString() {
		return "Text" + super.toString();
	}
}

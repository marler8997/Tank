package jmar.games.menus.text;

import org.lwjgl.util.vector.Vector4f;

public class Font {
	public final CharacterRenderer[] renderers;
	public float charWidth,charHeight,charSpacing;
	public Vector4f color;
	
	public Font(FullCharacterRendererSet renderSet, float charWidth, float charHeight, float charSpacing, Vector4f color) {
		this.renderers = renderSet.renderers;
		this.charWidth = charWidth;
		this.charHeight = charHeight;
		this.charSpacing = charSpacing;
		this.color = color;
	}
	
	public float getFontWidth(int charLength) {
		return (charWidth+charSpacing) * charLength - charSpacing;		
	}	
}

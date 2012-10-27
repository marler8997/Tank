package jmar.games.menus;

import jmar.games.menus.events.*;

import org.lwjgl.util.vector.Vector4f;

public abstract class Control {
	public float x,y;
	public float width,height;
	
	public Vector4f backColor;
	
	public GainFocusListener gainFocusListener;
	public MouseDownListener mouseDownListener;
	public EnterKeyWhileFocusedListener enterKeyWhileFocusedListener;
	
	public Control(float x, float y, float xLength, float yLength, Vector4f backColor) {
		this.x = x;
		this.y = y;
		this.width = xLength;
		this.height = yLength;		
		this.backColor = backColor;
	}

	public void glDraw() { glDraw(null); }
	public abstract void glDraw(GlobalState globalState);
	
	public void receiveKeyDown(int key, char keyChar) { }
	public void receiveKeyUp(int key, char keyChar) { }

	public Control getControlAt(int x, int y) {
		if(x >= this.x && x <= this.x + width &&
				 y >= this.y &&  y <= this.y + height) {
			return this;
		}
		return null;
	}
	
	public String toString() {
		return String.format("Control (%f,%f) %fx%f", x,y, width, height);
	}
}

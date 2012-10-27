package jmar.games.menus;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector4f;
public abstract class Container extends Control {

	protected ArrayList<Control> children;
	
	public Container(float x, float y, float xLength, float yLength, Vector4f backColor) {
		super(x, y, xLength, yLength, backColor);
	}
	
	public void addControl(Control control) {
		if(children == null) children = new ArrayList<Control>();
		children.add(control);
	}
	public void removeControl(Control control) {
		if(children != null) {
			this.children.remove(control);
		}
	}
	
	public void removeAllControls() {
		this.children = null;
	}

	public Control getControlAt(int x, int y) {
		if(children != null) {
			// Go backwards because the last children is always on top
			int i;
			for(i = children.size()-1; i >= 0; i--) {
				Control control = children.get(i).getControlAt(x, y);
				if(control != null) return control;
			}
		}

		return super.getControlAt(x,y);		
	}
}

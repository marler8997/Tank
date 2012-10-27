package jmar.games.menus;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;

import jmar.games.GLException;
import jmar.games.tank.Settings;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector4f;

public class WindowPanel extends Container {
	private GlobalState globalState;
	private ArrayList<UILoopCallback> loopCallbacks;
	public boolean continueGuiLoop;
	
	public WindowPanel(int windowWidth, int windowHeight, Vector4f backColor) {
		super(0, 0, windowWidth, windowHeight, backColor);
		this.globalState = new GlobalState();
		this.loopCallbacks = null;
		this.continueGuiLoop = true;
	}
	
	public void addUILoopCallback(UILoopCallback callback) {
		if(this.loopCallbacks == null) this.loopCallbacks = new ArrayList<UILoopCallback>();
		this.loopCallbacks.add(callback);
	}
	
	public void removeAllUILoopCallbacks() {
		this.loopCallbacks = null;
	}
	public void removeUILoopCallback(UILoopCallback callback) {
		this.loopCallbacks.remove(callback);
	}
	
	// Returns true if display requested close, false if gui loop was terminated
	public boolean run() throws GLException {
		GLException.throwOnError();
		
		// Initialize OpenGL for 2D
		glMatrixMode(GL11.GL_PROJECTION);       // Switches to the camera perspective
		glLoadIdentity();                       // Reset Camera
		glOrtho(0, width, 0, height, 1, -1);
		glMatrixMode(GL11.GL_MODELVIEW);
		
		while(continueGuiLoop) {
			GLException.throwOnError();
			if(Display.isCloseRequested()) return true;
			
			if(this.loopCallbacks != null) {
				for(int i = 0; i < loopCallbacks.size(); i++) {
					boolean keepCallback = this.loopCallbacks.get(i).loopCallback(this);
					if(this.loopCallbacks == null) break;
					if(!keepCallback) {
						this.loopCallbacks.remove(i);
						i--;
					}
				}
			}
			
			updateControlState();
			
			if(!continueGuiLoop) break;

			glDraw(globalState);

			Display.update(); // Swaps Screens and updates user inputs
			Display.sync(Settings.menuFps);	
		}
		
		GLException.throwOnError();
		
		return false;
	}
	
	public void redrawFromWithinAnEventListener() {
		if(!continueGuiLoop) return;		
		glDraw(globalState);		
		Display.update();
	}
	
	public void glDraw(GlobalState globalState) {
		glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		glClearColor(backColor.x, backColor.y, backColor.z, 1);
		
		if(children != null) {
			for(int i =0; i < children.size(); i++) {
				children.get(i).glDraw(globalState);
			}
		}		
	}
	
	private void updateControlState() {
		int mouseX = Mouse.getX();
		int mouseY = Mouse.getY();

		Control newMouseOverControl = getControlAt(mouseX, mouseY);
		if(newMouseOverControl != globalState.mouseOverControl) {
			//System.out.println("MouseOver " + newMouseOverControl.toString());
			globalState.mouseOverControl = newMouseOverControl;
		}
		
		//
		// Mouse Events
		//
		while(Mouse.next()) {
			int button = Mouse.getEventButton();
			if(button >= 0) {
				int mouseEventX = Mouse.getEventX();
				int mouseEventY = Mouse.getEventY();
				Control control = getControlAt(mouseEventX, mouseEventY);
				/*
				System.out.println(String.format("Button=%d %s (%d,%d) %s",
						Mouse.getEventButton(), Mouse.getEventButtonState() ? "Down" : "Up",
								mouseEventX, mouseEventY, control.toString()));
				*/
				boolean buttonIsDown = Mouse.getEventButtonState();
				if(buttonIsDown) {
					globalState.lastMouseDownControl = control;
					if(control.mouseDownListener != null) {
						control.mouseDownListener.mouseDown(control, mouseEventX, mouseEventY, button);
						if(!continueGuiLoop) return; // Important if this event stopped the gui loop
					}

					globalState.focusedControl = control;
					if(control.gainFocusListener != null) {
						control.gainFocusListener.gainFocus(control);
						if(!continueGuiLoop) return; // Important if this event stopped the gui loop
					}
				} else {
					// call mouseUpListener
				}
			}
		}
		
		//
		// Keyboard Events
		//
		while(Keyboard.next()) {
			int key = Keyboard.getEventKey();
			char keyChar = Keyboard.getEventCharacter();
			boolean keyDown = Keyboard.getEventKeyState();
			
			if(globalState.focusedControl != null) {
				if(keyDown) globalState.focusedControl.receiveKeyDown(key, keyChar);
				else globalState.focusedControl.receiveKeyUp(key, keyChar);
				if(!continueGuiLoop) return; // Important if this event stopped the gui loop
			}
		}
	}

	
	public void manuallySetFocus(Control control) {
		globalState.focusedControl = control;
		if(control.gainFocusListener != null) {
			control.gainFocusListener.gainFocus(control);
		}		
	}
	
	// Will never return null
	public Control getControlAt(int x, int y) {		
		Control control = super.getControlAt(x, y);
		if(control != null) return control;
		return this;
	}
	
	public String toString() {
		return String.format("WindowPanel %fx%f", width, height);
	}	
}

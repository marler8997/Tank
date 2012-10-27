package jmar.games;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class GLException extends Exception {
	public final int glError;
	public GLException(int glError, String message) {
		super(message);
		this.glError = glError;
	}
	public static void throwOnError() throws GLException {
		int errorValue = GL11.glGetError();		
		if (errorValue != GL11.GL_NO_ERROR) {
			throw new GLException(errorValue, GLU.gluErrorString(errorValue));
		}
	}
}

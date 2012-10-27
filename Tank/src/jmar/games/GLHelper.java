package jmar.games;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class GLHelper {
	public static Vector2f screenToWorld(float x, float y, float zDepth) {	
	      FloatBuffer modelViewBuffer = BufferUtils.createFloatBuffer(16);
	      FloatBuffer projectionViewBuffer = BufferUtils.createFloatBuffer(16);
	      IntBuffer viewPortTransformBuffer = BufferUtils.createIntBuffer(16);
	      
	      GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewBuffer);
	      GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionViewBuffer);
	      GL11.glGetInteger(GL11.GL_VIEWPORT, viewPortTransformBuffer);
	      
	      modelViewBuffer.rewind();
	      projectionViewBuffer.rewind();
	      viewPortTransformBuffer.rewind();
	      
	      /*
	      System.out.println("ModelView Matrix");
	      for(int i = 0; i < 4; i++) {
	    	  System.out.print("|");
	    	  for(int j = 0; j < 4; j++) {
	    		  float m = modelViewBuffer.get(i+j*4);
	    		  System.out.print(String.format(" %3.1f", m));
	    	  }
	    	  System.out.println("|");
	      }

	      System.out.println("ProjectionView Matrix");
	      for(int i = 0; i < 4; i++) {
	    	  System.out.print("|");
	    	  for(int j = 0; j < 4; j++) {
	    		  float m = projectionViewBuffer.get(i+j*4);
	    		  System.out.print(String.format(" %3.1f", m));
	    	  }
	    	  System.out.println("|");
	      }
	      System.out.println(String.format("ViewPort Transform: %d %d %d %d %d %d", 
	    		  viewPortTransformBuffer.get(0),
	    		  viewPortTransformBuffer.get(1),
	    		  viewPortTransformBuffer.get(2),
	    		  viewPortTransformBuffer.get(3),
	    		  viewPortTransformBuffer.get(4),
	    		  viewPortTransformBuffer.get(5)
	    		  ));
	      */

		  FloatBuffer posBuffer = BufferUtils.createFloatBuffer(4);	
		  
	      GLU.gluUnProject(
	         x,
	         y,
	         zDepth,
	         modelViewBuffer,
	         projectionViewBuffer,
	         viewPortTransformBuffer,
	         posBuffer
	      );
	      
	      return new Vector2f(posBuffer.get(0), posBuffer.get(1));
	   }
	
		public static void dlDrawBox(Vector3f position, float size) {
			
		}
}

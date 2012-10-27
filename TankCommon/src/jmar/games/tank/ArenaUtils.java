package jmar.games.tank;

import jmar.test.Test;
import jmar.test.TestFailure;

public class ArenaUtils {
	
	public static int windowWidth,windowHeight;
	
	public static int arenaWidth,arenaHeight;
	
	public static float glOrthoWidth,glOrthoHeight;
	public static float glOrthoHalfWidth,glOrthoHalfHeight;
	
	public static float glArenaWidth,glArenaHeight;
	public static float glArenaHalfWidth,glArenaHalfHeight;
	
	// TODO: [Performance] [Cache] faster by caching arenaToGLX(0) and arenaToGLY(0)
	public static float arenaToGLWidth(int width) {
		return ArenaUtils.arenaToGLX(width) - ArenaUtils.arenaToGLX(0);
	}
	public static float arenaToGLHeight(int height) {
		return ArenaUtils.arenaToGLY(height) - ArenaUtils.arenaToGLY(0);
	}
	
	
	
	public static float arenaToGLX(int arenaX) {
		return ((float)(2*arenaX) - (float)arenaWidth) * glArenaHalfWidth / arenaWidth;
	}
	public static float arenaToGLY(int arenaY) {
		return ((float)(2*arenaY) - (float)arenaHeight) * glArenaHalfHeight / arenaHeight;
	}
	
	public static int glToArenaX(float glX) {
		return (int)(((float)arenaWidth) * (.5f + glX / glArenaWidth));
	}
	public static int glToArenaY(float glY) {
		return (int)(((float)arenaHeight) * (.5f + glY / glArenaHeight));
	}
	
	
	public static void main(String[] args) throws TestFailure {
		ArenaUtils.arenaWidth = 800;
		ArenaUtils.arenaHeight = 600;
		ArenaUtils.glArenaHalfWidth = 200;
		ArenaUtils.glArenaHalfHeight = 100;
		ArenaUtils.glArenaWidth = glArenaHalfWidth * 2;
		ArenaUtils.glArenaHeight = glArenaHalfHeight * 2;
		
		System.out.println(String.format("0 == glToArenaX(arenaToGLX(0)) == %d ", glToArenaX(arenaToGLX(0))));
		Test.assertTrue(0 == glToArenaX(arenaToGLX(0)));

		System.out.println(String.format("0 == glToArenaY(arenaToGLY(0)) == %d ", glToArenaY(arenaToGLY(0))));
		Test.assertTrue(0 == glToArenaY(arenaToGLY(0)));

		System.out.println(String.format("200 == glToArenaY(arenaToGLY(200)) == %d ", glToArenaY(arenaToGLY(200))));
		Test.assertTrue(200 == glToArenaY(arenaToGLY(200)));
	}
}

package jmar.games.menus;

public interface UILoopCallback {
	// return false to remove this callback
	boolean loopCallback(WindowPanel windowPanel);
}

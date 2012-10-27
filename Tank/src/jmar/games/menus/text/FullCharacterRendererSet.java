package jmar.games.menus.text;

public class FullCharacterRendererSet {
	public final CharacterRenderer[] renderers;
	public FullCharacterRendererSet() {
		this.renderers = new CharacterRenderer[256];
		for(int i = 0; i < renderers.length; i++) {
			this.renderers[i] = NoCharacter.instance;
		}
	}

}

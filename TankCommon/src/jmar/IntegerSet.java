package jmar;

import jmar.games.StaticRandom;

public class IntegerSet {	
	private final boolean [] picked;
	private int pickCount;
	
	public IntegerSet(int size) {
		this.picked = new boolean[size];
		this.pickCount = 0;
	}
	
	public boolean allPicked() {
		return pickCount >= picked.length;
	}
	
	public int pickRandom() {
		if(pickCount >= picked.length) throw new IllegalStateException("You've already picked all the integers from this set");
		
		int randomIndex = StaticRandom.random.nextInt(picked.length);
		for(int i = 0; i < picked.length; i++) {
			if(!picked[randomIndex]) {
				picked[randomIndex] = true;
				pickCount++;
				return randomIndex;
			}
			randomIndex++;
			if(randomIndex >= picked.length) {
				randomIndex = 0;
			}
		}
		throw new IllegalStateException("You've already picked all the integers from this set");
	}
}

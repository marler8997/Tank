package jmar.games.tank;

import jmar.games.tank.Bullet;

public class PuckClientStateForClient {

	public String userName;
	public byte clientIndex;	
	public Puck puck;
	
	public PuckClientStateForClient(String userName, byte clientIndex, Puck puck) {
		this.userName = userName;
		this.clientIndex = clientIndex;
		this.puck = puck;
	}
	
	// returns true on success and false if no bullets are availble
	public boolean addNewBulletFromClient(int bulletTimeStep, byte[] bulletData, int dataOffset) {
		for(int i = 0; i < puck.bulletCapacity; i++) {
			Bullet bullet = puck.bullets[i];
			if(!bullet.isActive()) {

				int initialX         = (0x0000FF00 & (bulletData[dataOffset    ] <<  8)) |
							           (0x000000FF & (bulletData[dataOffset + 1]      )) ;	
				int initialY         = (0x0000FF00 & (bulletData[dataOffset + 2] <<  8)) |
				           			   (0x000000FF & (bulletData[dataOffset + 3]      )) ;	
				int targetX          = (0x0000FF00 & (bulletData[dataOffset + 4] <<  8)) |
				           			   (0x000000FF & (bulletData[dataOffset + 5]      )) ;	
				int targetY          = (0x0000FF00 & (bulletData[dataOffset + 6] <<  8)) |
				           			   (0x000000FF & (bulletData[dataOffset + 7]      )) ;
				
				bullet.activate(bulletTimeStep, initialX, initialY, targetX, targetY, puck.bulletBouncesAllowed);
				return true;
			}
		}
		return false;
	}
	
}

package jmar.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SystemExitListener implements ActionListener {
	
	private static SystemExitListener instance = null;
	
	public static SystemExitListener getInstance() {
		if(instance == null) {
			instance = new SystemExitListener();
		}
		return instance;
	}	
	
	private SystemExitListener() {}
	
	public void actionPerformed(ActionEvent arg0) {
		System.exit(0);
	}
}

package jmar.games.tank.menus;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import jmar.awt.BorderLayoutHelper;
import jmar.games.tank.TankGameFrame;

public class PanelContainer extends JPanel {
	public PanelContainer() {
		setLayout(new BorderLayout());
	}	
	public void setPanel(JPanel panel) {
		BorderLayoutHelper.remove(this, BorderLayout.CENTER);
		BorderLayoutHelper.set(this, panel, BorderLayout.CENTER);
	}
}

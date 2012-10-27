package jmar.games.tank.menus;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import jmar.games.tank.TankGameFrame;

public class InitialGroupPanel extends JPanel {
	final TankGameFrame tankGameFrame;
	
	public InitialGroupPanel(final TankGameFrame tankGameFrame) {
		this.tankGameFrame = tankGameFrame;
		
		setBackground(Color.white);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(Box.createVerticalStrut(10));
		
		JButton hostGroupButton = new JButton("Host Group");
		hostGroupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tankGameFrame.setHostGroupPanel();
			}
		});
		hostGroupButton.setAlignmentX(CENTER_ALIGNMENT);
		add(hostGroupButton);

		add(Box.createVerticalStrut(10));
		
		JButton joinGroupButton = new JButton("Join Group");
		joinGroupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tankGameFrame.setJoinGroupPanel();
			}
		});
		joinGroupButton.setAlignmentX(CENTER_ALIGNMENT);
		add(joinGroupButton);
	}
}

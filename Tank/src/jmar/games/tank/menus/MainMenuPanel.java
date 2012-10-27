package jmar.games.tank.menus;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmar.awt.JPanelX;
import jmar.games.tank.LocalOfflineKeyFile;
import jmar.games.tank.TankGameFrame;

public class MainMenuPanel extends JPanel {
	private final TankGameFrame tankGameFrame;
	
	
	JPanelX onlineStatusPanel;
	JPanelX userNamePanel;
	JPanelX getOfflineKeyPanel;
	
	public MainMenuPanel(final TankGameFrame tankGameFrame) {
		this.tankGameFrame = tankGameFrame;
		
		setBackground(Color.white);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JButton backToLoginButton = new JButton("Back to Login");
		backToLoginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tankGameFrame.callbackMainMenuLogout();
			}
		});
		add(backToLoginButton);
		
		onlineStatusPanel = new JPanelX();
		add(onlineStatusPanel);
		
		userNamePanel = new JPanelX();
		add(userNamePanel);		
		
		getOfflineKeyPanel = new JPanelX();
		add(getOfflineKeyPanel);
		
		JButton startGameButton = new JButton("Start Game");
		startGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tankGameFrame.callbackStartGame();
			}
		});
		add(startGameButton);
	}
	
	public void setup(final String userName, final String password, final boolean offlineMode) {
		onlineStatusPanel.removeAll();
		userNamePanel.removeAll();
		if(offlineMode) {
			JLabel offlineLabel = new JLabel("Offline");
			offlineLabel.setForeground(Color.RED);
			onlineStatusPanel.add(offlineLabel);			

			userNamePanel.add(new JLabel(String.format("Playing offline as: %s", userName)));			
			
		} else {
			JLabel onlineLabel = new JLabel("Online");
			onlineLabel.setForeground(Color.GREEN);
			onlineStatusPanel.add(onlineLabel);
			
			userNamePanel.add(new JLabel(String.format("Logged in as: %s", userName)));
		}

		getOfflineKeyPanel.removeAll();
		if(!LocalOfflineKeyFile.userHasValidOfflineKey(userName)) {
			JLabel message = new JLabel("You do not have an offline key for this user");
			JButton getOfflineKeyButton = new JButton("Get Offline Key");
			getOfflineKeyButton.addActionListener(new ActionListener() {
				public synchronized void actionPerformed(ActionEvent e) {					
					tankGameFrame.callbackMainMenuMakeOfflineKey();
				}
			});
			
			getOfflineKeyPanel.add(message);
			getOfflineKeyPanel.add(getOfflineKeyButton);
		} else {
			getOfflineKeyPanel.add(new JLabel("You have an offline key for this user"));
		}
	}
}

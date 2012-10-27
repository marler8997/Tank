package jmar.games.tank.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import jmar.games.PopupWindow;
import jmar.games.net.TcpSelectServer;
import jmar.games.tank.Settings;
import jmar.games.tank.TankGameFrame;
import jmar.games.tank.TankGroupHostSetupManager;

public class HostGroupPanel extends JPanel {
	private final TankGameFrame tankGameFrame;
	
	final TcpSelectServer tcpSelectServer;
	
	final JRadioButton lanRadioButton,internetRadioButton;
	final JButton startOrStopButton;
	
	public HostGroupPanel(final TankGameFrame tankGameFrame) {
		this.tankGameFrame = tankGameFrame;
		this.tcpSelectServer = new TcpSelectServer();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JButton backToMainMenu = new JButton("Back to Main Menu");
		backToMainMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tankGameFrame.callbackHostGroupCancel();
			}
		});
		add(backToMainMenu);
		
		add(Box.createVerticalStrut(3));
		
		JLabel title = new JLabel("Host Group");
		title.setAlignmentX(CENTER_ALIGNMENT);
		add(title);

		add(Box.createVerticalStrut(3));
		
		lanRadioButton = new JRadioButton("LAN");
		internetRadioButton = new JRadioButton("Internet");
		internetRadioButton.setSelected(true);
		ButtonGroup radioButtonGroup = new ButtonGroup();
		radioButtonGroup.add(lanRadioButton);
		radioButtonGroup.add(internetRadioButton);		
		
		add(lanRadioButton);
		add(internetRadioButton);
		
		
		add(Box.createVerticalStrut(6));
		
		startOrStopButton = new JButton("Start Server");
		startOrStopButton.setAlignmentX(CENTER_ALIGNMENT);
		startOrStopButton.addActionListener(new ActionListener() {			
			public synchronized void actionPerformed(ActionEvent e) {
				toggleHostServer();
			}
		});
		add(startOrStopButton);
	}
	public void setup() {
		if(tankGameFrame.gameBuilder.iAmInOfflineMode()) {
			lanRadioButton.setSelected(true);
		}
	}
	private synchronized void toggleHostServer() {
		if(tankGameFrame.groupHostSetupManager.isHostingGroup()) {
			stopHostServer();
		} else {
			startHostServer();
		}		
	}
	public void stopHostServerIfRunning() {
		stopHostServer();
	}
	private void startHostServer() {
		if(tankGameFrame.groupHostSetupManager.isHostingGroup()) {
			PopupWindow.PopupSmallMessage("Server already running", "It appears that a host server is already running");
			return;
		}		
		
		boolean isLanGroup = lanRadioButton.isSelected();
		if(!isLanGroup) {
			PopupWindow.PopupSmallMessage("Not Implemented Yet", "Internet Games are not implemented yet");
			System.err.println("Internet games not yet supported");
			return;
		}

		startOrStopButton.setText("Stop Server");
		lanRadioButton.setEnabled(false);
		internetRadioButton.setEnabled(false);
		
		tankGameFrame.callbackHostGroupStartedHosting(isLanGroup);
		tankGameFrame.groupHostSetupManager.stateSettingUpGame(tankGameFrame.gameBuilder.myUserName, tankGameFrame.hostingGroupStatusPanel.groupManagerCallback);
		
		tcpSelectServer.prepareToRun();
		new Thread(new Runnable() {
			public void run() {				
				try {
					tcpSelectServer.run(Settings.serverPort, new byte[512], tankGameFrame.groupHostSetupManager);
				} catch (IOException e) {
					PopupWindow.PopupException(e);
				}
			}
		}).start();
	}
	private void stopHostServer() {
		startOrStopButton.setText("Start Server");
		lanRadioButton.setEnabled(true);
		if(tankGameFrame.gameBuilder.iAmInOfflineMode() == false) internetRadioButton.setEnabled(true);
		
		if(tankGameFrame.groupHostSetupManager.isHostingGroup()) {
			tcpSelectServer.stop();
		}
	}
}
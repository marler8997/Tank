package jmar.games.tank.menus;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmar.awt.JPanelX;
import jmar.awt.JPanelY;
import jmar.games.net.TcpSelectServer;
import jmar.games.tank.HostThreadCallback;
import jmar.games.tank.RemoteTankClient;
import jmar.games.tank.TankGameFrame;
import jmar.games.tank.TankGroupHostSetupManager;
import jmar.games.tank.TankGroupHostSetupManagerCallback;

public class HostingGroupStatusPanel extends JPanel {
	final TankGameFrame tankGameFrame;
	
	final JPanel memberPanel;	
	
	public final TankGroupHostSetupManagerCallback groupManagerCallback;
	
	public HostingGroupStatusPanel(final TankGameFrame tankGameFrame) {
		this.tankGameFrame = tankGameFrame;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.white);
		
		this.memberPanel = new JPanelY();
		groupManagerCallback = new TankGroupHostSetupManagerCallback() {
			public void change(TankGroupHostSetupManager groupManager) {
				refreshClientsGui(groupManager.getClientsOrdered());
			}
			public void groupClosed() {
				removeAll();
				memberPanel.removeAll();
				tankGameFrame.callbackHostGroupClosed();
			}
			@Override
			public void exception(Exception e) {
				removeAll();
				memberPanel.removeAll();
				e.printStackTrace();
			}
		};
	}
	public void setup(boolean isLanGroup) {
		this.removeAll();
		this.memberPanel.removeAll();

		add(Box.createVerticalStrut(3));
		
		JLabel panelTitle = new JLabel(String.format("Hosting %s Group", isLanGroup ? "LAN" : "Internet"));
		panelTitle.setFont(new Font(MenuFont.defaultFontName, Font.BOLD, 24));
		panelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(panelTitle);

		add(Box.createVerticalStrut(8));
		
		JButton stopHostingButton = new JButton("Stop Hosting");
		stopHostingButton.setAlignmentX(CENTER_ALIGNMENT);
		stopHostingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tankGameFrame.callbackHostingGroupStatusPanelStopHosting();
			}
		});
		add(stopHostingButton);

		add(Box.createVerticalStrut(8));
		
		JLabel noMembersLabel = new JLabel("No members in your group");
		noMembersLabel.setAlignmentX(LEFT_ALIGNMENT);
		memberPanel.add(noMembersLabel);
		
		add(memberPanel);
	}
	public void refreshClientsGui(List<RemoteTankClient> clients) {
		memberPanel.removeAll();
		memberPanel.add(new JLabel(String.format("%d clients", clients.size())));
		for(int i = 0; i < clients.size(); i++) {
			RemoteTankClient client = clients.get(i);
			JPanel memberSubPanel = new JPanelX();
			
			JLabel memberNameLabel = new JLabel(client.getIdString());
			memberSubPanel.add(memberNameLabel);
			memberPanel.add(memberSubPanel);
		}
		validate();
		repaint();		
	}
}

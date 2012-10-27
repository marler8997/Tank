package jmar.games.tank.menus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmar.games.PopupWindow;
import jmar.games.net.InputStreamChunker;
import jmar.games.net.OutputStreamChunker;
import jmar.games.tank.Constants;
import jmar.games.tank.Settings;
import jmar.games.tank.TankGameFrame;

public class InGroupAsClientPanel extends JPanel {
	final TankGameFrame tankGameFrame;	
	final JPanel membersPanel;	
	public InGroupAsClientPanel(TankGameFrame tankGameFrame) {
		this.tankGameFrame = tankGameFrame;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.membersPanel = new JPanel();
		this.membersPanel.setLayout(new BoxLayout(this.membersPanel, BoxLayout.Y_AXIS));
	}
	public void setupAsLanClient(String lanServerHost) {
		removeAll();
		membersPanel.removeAll();
		
		JLabel title = new JLabel(String.format("LAN Host: %s", lanServerHost));
		title.setFont(MenuFont.menuSectionTitleFont);
		add(title);
		
		add(Box.createVerticalStrut(20));
		
		add(membersPanel);
	}
	public void updateMembers(String host, List<String> clients) {
		membersPanel.removeAll();
		
		membersPanel.add(new JLabel(String.format("(Host) %s", host)));
		
		for(int i = 0; i < clients.size(); i++) {
			membersPanel.add(Box.createVerticalStrut(20));
			membersPanel.add(new JLabel(String.format("[%d] %s", clients.get(i))));
		}
	}	
	public void run(Socket socket, InputStreamChunker inputChunker, OutputStreamChunker outputChunker) {
		try {
			DatagramSocket udpSocket;
			byte myClientIndex;
			boolean gameRequestReceived = false;
			
			while(true) {
				int chunkSize = inputChunker.readChunkFullBlocking();
				
				switch(inputChunker.bytes[0]) {
					case Constants.hostRequestGameStart:
						gameRequestReceived = true;
						myClientIndex = inputChunker.bytes[1];
						tankGameFrame.gameBuilder.setMyClientIndex(myClientIndex);
						
						byte[] message = new byte[]{myClientIndex};
						DatagramPacket packet = new DatagramPacket(message, message.length);
						udpSocket = new DatagramSocket();
						udpSocket.connect(new InetSocketAddress(socket.getInetAddress(), Settings.serverPort));
						
						udpSocket.send(packet);
						
						outputChunker.sendChunk(new byte[]{Constants.clientReadyForGameStart});
						break;
					case Constants.hostStartingGame:
						
						// Read Game Settings
						throw new UnsupportedOperationException("Have not implemented reading game settings yet");
						
						
						
						
						/*
						
						tankGameFrame.callbackStartGame();
						
						break;
						*/
					case Constants.hostGameDelayed:
						gameRequestReceived = false;
						break;
					default:
						System.err.println(String.format("Unknown first byte from host: %d", inputChunker.bytes[0]));
						break;
				}
			}
		} catch(IOException e) {
			PopupWindow.PopupException(e);
			tankGameFrame.callbackInGroupClosed();
		} catch(RuntimeException e) {
			PopupWindow.PopupException(e);
			tankGameFrame.callbackInGroupClosed();			
		}
	}
}

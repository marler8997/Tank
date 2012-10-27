package jmar.games.tank.menus;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.json.JSONException;
import org.json.JSONObject;

import jmar.Base64;
import jmar.ByteArray;
import jmar.MacAddress;
import jmar.UrlEncoder;
import jmar.awt.JPanelX;
import jmar.awt.JTextFieldOneLine;
import jmar.games.PopupWindow;
import jmar.games.net.BytesChunker;
import jmar.games.net.HttpRequest;
import jmar.games.net.InputStreamChunker;
import jmar.games.net.OutputStreamChunker;
import jmar.games.tank.Constants;
import jmar.games.tank.Settings;
import jmar.games.tank.TankGameFrame;

public class JoinGroupPanel extends JPanel {
	final TankGameFrame tankGameFrame;
	
	final JTextField lanServerInput;
	final JLabel statusLabel; 
	
	private Thread joinGroupThread;
	
	public JoinGroupPanel(final TankGameFrame tankGameFrame) {
		this.tankGameFrame = tankGameFrame;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				
		//
		// LAN Group Section
		//
		JLabel joinLanGroupTitle = new JLabel("Join LAN Group");
		joinLanGroupTitle.setFont(MenuFont.menuSectionTitleFont);
		add(joinLanGroupTitle);
		
		JPanelX lanServerInputPanel = new JPanelX();
			lanServerInputPanel.add(new JLabel("LAN Host (IPAddress or Host Name): "));
			lanServerInput = new JTextFieldOneLine();
			lanServerInputPanel.add(lanServerInput);
		add(lanServerInputPanel);
		
		JButton joinLanServerButton = new JButton("Join LAN Server");
		ActionListener joinLanServerActionListener = new ActionListener() {
			public synchronized void actionPerformed(ActionEvent e) {
				final String lanServer = lanServerInput.getText();
				if(lanServer == null || lanServer.length() <= 0) {
					PopupWindow.PopupSmallMessage("Missing Server Host", "Please supply a Server Host", null);
					return;
				}
				
				if(joinGroupThread != null && joinGroupThread.isAlive()) return;
				
				joinGroupThread = new Thread(new Runnable() {
					public void run() {
						setStatus(String.format("Connecting to '%s'", lanServer));
						
						Socket socket;
						InputStream input;
						OutputStream output;
						try {
							socket = new Socket(lanServer, Settings.serverPort);
							input = socket.getInputStream();
							output = socket.getOutputStream();
						} catch (UnknownHostException e) {
							setErrorStatus(String.format("Unknown Host: '%s'", lanServer));
							return;
						} catch (IOException e) {
							setErrorStatus(String.format("Could not connect to '%s'", lanServer));
							return;							
						}
						
						OutputStreamChunker outputChunker = new OutputStreamChunker(output);
						
						// Login to the host server						
						final byte[] initialPacket;
						if(tankGameFrame.gameBuilder.iAmInOfflineMode()) {
							byte[] offlineKey = tankGameFrame.gameBuilder.offlineKey;
							
							initialPacket = new byte[3 + tankGameFrame.gameBuilder.offlineKey.length];
							initialPacket[0] = Constants.offlineClient;
							initialPacket[1] = (byte)(offlineKey.length >> 8);
							initialPacket[2] = (byte)(offlineKey.length     );
							for(int i = 0; i < offlineKey.length; i++) {
								initialPacket[3 + i] = offlineKey[i];
							}
						} else {
							String userName = tankGameFrame.gameBuilder.myUserName;
							initialPacket = new byte[2 + userName.length()];
							initialPacket[0] = Constants.onlineClient;
							initialPacket[1] = (byte)userName.length();
							for(int i = 0; i < userName.length(); i++) {
								initialPacket[2 + i] = (byte) userName.charAt(i);
							}							
						}
						
						try {
							setStatus("Sending initial packet to host...");
							outputChunker.sendChunk(initialPacket);
						} catch (IOException e) {
							setErrorStatus("Failed to send data to the host");
							return;
						}							

						InputStreamChunker inputChunker = new InputStreamChunker(input);
							
						int chunkSize;
						try {
							setStatus("Reading host response...");
							chunkSize = inputChunker.readChunkFullBlocking();
						} catch (IOException e) {
							setErrorStatus("Failed to read data from host");
							return;
						}

						byte joinResponseCode = inputChunker.bytes[0];
						if(joinResponseCode!= 0) {
							setErrorStatus(String.format("Host responded with join error code: %d", joinResponseCode));
							return;
						}						

						if(tankGameFrame.gameBuilder.iAmInOfflineMode()) {
							setStatus("Host accepted offline key");
							tankGameFrame.callbackJoinGroupPanelJoinedLanGroup(lanServer);
							return;							
						}
						
						//
						// Parse join response
						//
						int offset = 1;
						char[] hostUserNameChars = new char[inputChunker.bytes[offset++]];
						for(int i = 0; i < hostUserNameChars.length; i++) {
							hostUserNameChars[i] = (char) inputChunker.bytes[offset++];
						}
						String hostUserName = new String(hostUserNameChars);
						char[] dateTimeStringChars = new char[inputChunker.bytes[offset++]];
						for(int i = 0; i < dateTimeStringChars.length; i++) {
							dateTimeStringChars[i] = (char) inputChunker.bytes[offset++];
						}
						String dateTimeString = new String(dateTimeStringChars);
						
						System.out.println(String.format("HostUserName: '%s' DateTime: '%s'", hostUserName, dateTimeString));
						
						//
						// Get credentials for the host
						//
						String postData = String.format("UserName=%s&Password=%s&Mac=%s&HostUserName=%s&HostDateTime=%s",
								tankGameFrame.gameBuilder.myUserName, tankGameFrame.gameBuilder.myPassword, MacAddress.getLocalHostMacAddress().toSmallString(),
								hostUserName, UrlEncoder.encode(dateTimeString));
						String hostKeyBase64;
						try {
							setStatus("Requesting a host key from the tank server...");
							JSONObject json = HttpRequest.HttpPostJson(Settings.makeTankInternetHttp("GetCredentialsForHost"), postData.getBytes());
							hostKeyBase64 = json.getString("HostCredentials");							
						} catch (IOException e) {
							setErrorStatus(String.format("IOException contacting tank server: %s", e.getMessage()));
							return;
						} catch (JSONException e) {
							setErrorStatus(String.format("Bad Data from tank server: %s", e.getMessage()));
							return;
						}
						
						//
						// Send credentials to host
						//
						System.out.println(String.format("[Debug] hostkeyBase64='%s'", hostKeyBase64));
						byte[] hostKey = Base64.decode(hostKeyBase64);

						System.out.println(String.format("[Debug] hostkey='%s'", ByteArray.toHexString(hostKey, 0, hostKey.length)));
						try {
							setStatus("Sending host key...");
							outputChunker.sendChunk(hostKey);
						} catch (IOException e) {
							setErrorStatus("Failed to send host key to host");
							return;
						}
						
						//
						// Get response
						//
						try {
							setStatus("Waiting for result from host...");
							chunkSize = inputChunker.readChunkFullBlocking();
						} catch (IOException e) {
							setErrorStatus("Failed to read input from host");
							return;
						}

						joinResponseCode = inputChunker.bytes[0];
						if(joinResponseCode!= 0) {
							setErrorStatus(String.format("Host responded with join error code: %d", joinResponseCode));
							return;
						}					

						setStatus("Host accepted credentials");
						tankGameFrame.setInGroupAsClientPanelForLan(lanServer);
						tankGameFrame.inGroupAsClientPanel.run(socket, inputChunker, outputChunker);
					}					
				});
				joinGroupThread.start();				
			}
		};
		joinLanServerButton.addActionListener(joinLanServerActionListener);
		lanServerInput.addActionListener(joinLanServerActionListener);
		add(joinLanServerButton);		
		
		//
		// Internet Group Section
		//
		JLabel joinInternetGroupTitle = new JLabel("Join Internet Group");
		joinInternetGroupTitle.setFont(MenuFont.menuSectionTitleFont);
		add(joinInternetGroupTitle);
		
		add(new JLabel("Not yet implemented"));
		
		add(Box.createVerticalStrut(20));
		
		statusLabel = new JLabel();
		statusLabel.setAlignmentX(CENTER_ALIGNMENT);
		add(statusLabel);
	}	
	void setStatus(String status) {
		statusLabel.setText(status);
		statusLabel.setForeground(Color.black);
		validate();
		repaint();
	}
	void setErrorStatus(String errorStatus) {
		statusLabel.setText(errorStatus);
		statusLabel.setForeground(Color.red);
		validate();
		repaint();
	}	
}
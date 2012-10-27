package jmar.games.tank.menus;

import java.awt.Color;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.json.JSONException;
import org.json.JSONObject;

import jmar.MacAddress;
import jmar.ShouldntHappenException;
import jmar.ThreadHelper;
import jmar.awt.JPanelX;
import jmar.awt.JPanelY;
import jmar.awt.JTextFieldOneLine;
import jmar.games.net.HttpRequest;
import jmar.games.tank.LocalOfflineKeyFile;
import jmar.games.tank.Settings;
import jmar.games.tank.TankGameFrame;
import jmar.games.tank.TankGameSettings;


public class InternetLoginPanel extends JPanel {

	private final TankGameFrame tankGameFrame;
	
	final JTextFieldOneLine userNameInput;
	final JTextFieldOneLine passwordInput;	
	final JLabel loginStatusLabel;
	
	final JPanelY offlineKeyListPanel;
	
	
	Thread currentLoginThread;
	
	public InternetLoginPanel(final TankGameFrame tankGameFrame) throws IOException {
		this.tankGameFrame = tankGameFrame;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(Box.createVerticalStrut(5));
		
		JLabel loginLabel = new JLabel("Login");
		loginLabel.setAlignmentX(CENTER_ALIGNMENT);
		loginLabel.setFont(MenuFont.menuSectionTitleFont);
		add(loginLabel);

		add(Box.createVerticalStrut(20));
		
		JPanelX credentialsPanel = new JPanelX();
		credentialsPanel.setAlignmentX(CENTER_ALIGNMENT);
			JLabel userNameLabel = new JLabel("User Name: ");
			credentialsPanel.add(userNameLabel);			
			userNameInput = new JTextFieldOneLine();
			credentialsPanel.add(userNameInput);
			
			credentialsPanel.add(Box.createHorizontalStrut(5));
			
			JLabel passwordLabel = new JLabel("Password: ");
			credentialsPanel.add(passwordLabel);
			passwordInput = new JTextFieldOneLine();
			credentialsPanel.add(passwordInput);
		add(credentialsPanel);
		credentialsPanel.setMaximumSize(new Dimension(400,50));

		add(Box.createVerticalStrut(10));
		
		JButton loginButton = new JButton("Login");
		loginButton.setAlignmentX(CENTER_ALIGNMENT);
		currentLoginThread = null;
		ActionListener loginListener = new ActionListener() {			
			public synchronized void actionPerformed(ActionEvent arg0) {
				
				if(currentLoginThread != null && currentLoginThread.isAlive()) {
					System.err.println("You've already got a thread trying to login...");
					return;
				}
				
				final String userNameString = userNameInput.getText();
				if(userNameString == null || userNameString.length() <= 0) {
					setLoginErrorStatus("Please supply a user name");
					return;
				}
				
				final String passwordString = passwordInput.getText();
				if(passwordString == null || passwordString.length() <= 0) {
					setLoginErrorStatus("Please supply a password");
					return;
				}
								
				currentLoginThread = new Thread(new Runnable() {
					public void run() {
						String postData = String.format("UserName=%s&Password=%s&Mac=%s", userNameString, passwordString, MacAddress.getLocalHostMacAddress().toSmallString());
						try {
							setLoginStatus("Logging in...");
							JSONObject json = HttpRequest.HttpPostJson(Settings.makeTankInternetHttp("Login"), postData.getBytes());

							String errorMessage = json.optString("error");
							if(errorMessage != null && errorMessage != "") {
								setLoginErrorStatus(errorMessage);
								return;
							}
							
							setLoginStatus(String.format("You are logged in as '%s'", userNameString));
							tankGameFrame.callbackLoginPanelDone(userNameString, passwordString, null);
						
						
						} catch (IOException e) {
							e.printStackTrace();
							setLoginErrorStatus(String.format("IOException: %s", e.getMessage()));
						} catch (JSONException e) {
							setLoginErrorStatus(String.format("Could not parse response from server: %s", e.getMessage()));
							return;
						} catch(Exception e) {
							e.printStackTrace();
							setLoginErrorStatus(String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()));
						}
					}
				});
				currentLoginThread.start();
			}
		};
		add(loginButton);
		
		loginButton.addActionListener(loginListener);
		userNameInput.addActionListener(loginListener);
		passwordInput.addActionListener(loginListener);
		
		add(Box.createVerticalStrut(10));
		
		loginStatusLabel = new JLabel();
		loginStatusLabel.setAlignmentX(CENTER_ALIGNMENT);
		add(loginStatusLabel);
		

		add(Box.createVerticalStrut(30));
		
		JPanelY playOfflinePanel = new JPanelY();		
			JLabel offlineModeLabel = new JLabel("Play Offline");
			offlineModeLabel.setAlignmentX(CENTER_ALIGNMENT);
			offlineModeLabel.setFont(MenuFont.menuSectionTitleFont);
			playOfflinePanel.add(offlineModeLabel);
			
			playOfflinePanel.add(Box.createVerticalStrut(5));
		
			offlineKeyListPanel = new JPanelY();
			playOfflinePanel.add(offlineKeyListPanel);
			//refreshOfflineKeyPanel();
			
		add(playOfflinePanel);
		
		add(Box.createVerticalGlue());
	}
	
	public void setup() {
		offlineKeyListPanel.removeAll();
		
		List<LocalOfflineKeyFile> offlineKeyFiles;
		try {
			offlineKeyFiles = LocalOfflineKeyFile.loadKeyFiles();
		} catch (IOException e) {
			throw new ShouldntHappenException(e);
		}
		
		if(offlineKeyFiles == null || offlineKeyFiles.size() <= 0) {				
			JLabel noOfflineUsers = new JLabel("You currently do not have any offline keys saved on this system");
			offlineKeyListPanel.add(noOfflineUsers);
		} else {
			for(int i = 0; i < offlineKeyFiles.size(); i++) {
				final LocalOfflineKeyFile offlineKeyFile = offlineKeyFiles.get(i);
				JPanel keyPanel = new JPanel();
				keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.X_AXIS));
					if(offlineKeyFile.isValidForThisMachine()) {
						JLabel offlineKeyStatus = new JLabel(String.format("File: %s, User: %s", offlineKeyFile.file.getName(), offlineKeyFile.getUserNameIfValid()));
						JButton playOfflineButton = new JButton("Play");
						playOfflineButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								tankGameFrame.callbackLoginPanelDone(offlineKeyFile.getUserNameIfValid(), null, offlineKeyFile.getOfflineKey());						
							}
						});
						keyPanel.add(offlineKeyStatus);
						keyPanel.add(playOfflineButton);
					} else {
						JLabel offlineKeyStatus = new JLabel(String.format("File: '%s' Key: '%s' Error: %s",
								offlineKeyFile.file.getName(), offlineKeyFile.offlineKeyBase64, offlineKeyFile.getReasonThisKeyIsInvalidForThisMachine()));
						offlineKeyStatus.setForeground(Color.red);
						JButton deleteKeyFileButton = new JButton("Delete");
						deleteKeyFileButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								System.out.println(String.format("Deleting offline key file '%s'", offlineKeyFile.file.getName()));
								if(!offlineKeyFile.file.delete()) {
									System.err.println(String.format("Failed to delete offline key file '%s'", offlineKeyFile.file.getName()));	
								} else {
									EventQueue.invokeLater(new Runnable() {
										public void run() {
											setup();
											validate();
											repaint();
										}
									});
								}
							}
						});
						
						keyPanel.add(offlineKeyStatus);
						keyPanel.add(deleteKeyFileButton);
					}
					offlineKeyListPanel.add(keyPanel);
			}
		}
	}
	
	public void reset() {
		setLoginStatus("");
		passwordInput.setText("");
	}
	
	public void setLoginErrorStatus(String error) {
		System.err.println("[Gui Error Status] " + error);
		this.loginStatusLabel.setText(error);
		this.loginStatusLabel.setForeground(Color.RED);
		validate();
		repaint();		
	}
	public void setLoginStatus(String status) {
		System.out.println("[Gui Status] " + status);
		this.loginStatusLabel.setText(status);
		this.loginStatusLabel.setForeground(Color.BLACK);
		validate();
		repaint();		
	}
	
}

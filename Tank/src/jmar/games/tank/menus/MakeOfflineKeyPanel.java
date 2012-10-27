package jmar.games.tank.menus;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.crypto.Mac;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jmar.Base64;
import jmar.MacAddress;
import jmar.SyncLockObject;
import jmar.awt.JPanelX;
import jmar.awt.JPanelY;
import jmar.awt.JTextFieldOneLine;
import jmar.games.net.HttpJsonCallback;
import jmar.games.net.HttpRequest;
import jmar.games.tank.LocalOfflineKeyFile;
import jmar.games.tank.OfflineKeyDecryptor;
import jmar.games.tank.Settings;
import jmar.games.tank.TankGameFrame;
import jmar.games.tank.UserOfflineKey;

public class MakeOfflineKeyPanel extends JPanel {
	private final TankGameFrame tankGameFrame;
	private final SyncLockObject httpRequestLock;
	
	public MakeOfflineKeyPanel(TankGameFrame tankGameFrame) {
		this.tankGameFrame = tankGameFrame;
		this.httpRequestLock = new SyncLockObject();
		
		setBackground(Color.white);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
	}
	
	public void setup(final String userName, final String password) {
		removeAll();
		add(new JLabel(String.format("Requesting Current Offline Keys for %s", userName)));		
		
		final String userNamePasswordPostData = String.format("UserName=%s&Password=%s", userName, password);
		final String userNamePasswordMacPostData = String.format("UserName=%s&Password=%s&Mac=%s", userName, password, MacAddress.getLocalHostMacAddress().toSmallString());
		
		HttpRequest.HttpPostJson(Settings.makeTankInternetHttp("GetAllMyOfflineKeys"), userNamePasswordPostData.getBytes(), httpRequestLock, new HttpJsonCallback() {
			public void Error(String error) {
				removeAll();
				add(new JLabel(String.format("Error requesting offline keys for %s", userName)));		
			}
			public void Success(JSONObject json) throws JSONException {
				try {
					removeAll();
					
					//
					// Get Offline Keys from JSON
					//
					Object offlineKeysObject = json.get("OfflineKeys");
					JSONArray offlineKeysJsonArray;
					if(offlineKeysObject == JSONObject.NULL) {
						offlineKeysJsonArray = new JSONArray();
					} else {
						offlineKeysJsonArray = (JSONArray) offlineKeysObject;
					}
					int offlineKeyLimit = json.getInt("OfflineKeyLimit");
					int deactivatedOfflineKeyLimit = json.getInt("DeactivatedOfflineKeyLimit");
	
					//
					// Process the Offline Keys
					//
					ArrayList<UserOfflineKey> userOfflineKeys = new ArrayList<UserOfflineKey>();
					UserOfflineKey offlineKeyForThisMachine = null;
					
					int offlineKeyCount = 0;
					int deactivatedOfflineKeyCount = 0;
					for(int i = 0; i < offlineKeysJsonArray.length(); i++) {
						UserOfflineKey userOfflineKey = null;
						try {
							userOfflineKey = new UserOfflineKey(userName, offlineKeysJsonArray.getJSONObject(i));
						} catch (ParseException e) {
							removeAll();
							add(new JLabel(String.format("Failed to parse an offline key from the server: %s", offlineKeysJsonArray.getJSONObject(i).toString())));
							return;
						}
						
						if(userOfflineKey.isDeactivated()) {
							deactivatedOfflineKeyCount++;
						} else {
							offlineKeyCount++;
						}
						if(MacAddress.getLocalHostMacAddress().equals(userOfflineKey.macAddress)) {
							offlineKeyForThisMachine = userOfflineKey;
						}
						userOfflineKeys.add(userOfflineKey);
						System.out.println("[Debug] OfflineKey: " + userOfflineKey.toString());
					}
					
					//
					// If the offline key for this machine has already been registered
					//
					if(offlineKeyForThisMachine != null) {
						final UserOfflineKey finalOfflineKeyForThisMachine = offlineKeyForThisMachine;
						if(offlineKeyForThisMachine.isDeactivated()) {
							JLabel deactivationMessage = new JLabel(String.format("The offline key for this machine was deactivated on %s", offlineKeyForThisMachine.deactivated));
							deactivationMessage.setForeground(Color.red);
							add(deactivationMessage);
						} else {	
							add(new JLabel("This machine already has a valid offline key registered."));
							JButton saveMyOfflineKey = new JButton("Save Key");
							saveMyOfflineKey.addActionListener(new ActionListener() {						
								public void actionPerformed(ActionEvent e) {
									try {
										finalOfflineKeyForThisMachine.saveOnLocalFileSystem();
										tankGameFrame.callbackMakeOfflineKeyDone();
									} catch (IOException e1) {
										// TODO: make popup here
										e1.printStackTrace();
									}
								}
							});
							add(saveMyOfflineKey);
						}
						return;
					}				
					
					
					boolean canRegisterNewKey = offlineKeyCount < offlineKeyLimit;
					boolean canDeactivateKeys = deactivatedOfflineKeyCount < deactivatedOfflineKeyLimit;				
	
					//
					// Add RegisterNewKey panel
					//
					JPanel registerNewKeyPanel = new JPanelY();
					if(canRegisterNewKey) {
						
						JLabel registerNewKeyTitle = new JLabel("Register New Offline Key");
						registerNewKeyTitle.setFont(MenuFont.menuSectionTitleFont);
						registerNewKeyPanel.add(registerNewKeyTitle);
						
						JPanel deviceNamePanel = new JPanelX();
						deviceNamePanel.add(new JLabel("Device Name: "));
						final JTextField deviceNameInput = new JTextFieldOneLine();
						deviceNamePanel.add(deviceNameInput);
						registerNewKeyPanel.add(deviceNamePanel);
						
						registerNewKeyPanel.add(new JLabel(String.format("Mac Address: %s", MacAddress.getLocalHostMacAddress())));
						registerNewKeyPanel.add(new JLabel(String.format("User Name: %s", userName)));
						
						JButton registerNewDeviceButton = new JButton("Register");						
						registerNewDeviceButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								String deviceName = deviceNameInput.getText();
								if(deviceName == null || deviceName.length() <= 0) {
									// TODO: need to popup an error
									System.err.println("[FUTURE GUI POPUP] Please supply a device name");
									return;
								}
								
								HttpRequest.HttpPostJson(Settings.makeTankInternetHttp("NewOfflineKey"),
										String.format("%s&DeviceName=%s", userNamePasswordMacPostData, deviceName).getBytes(), httpRequestLock, new HttpJsonCallback() {
									public void Error(String error) {
										// TODO: need to popup an error
										System.err.println("[FUTURE GUI POPUP] " + error);
									}
									public void Success(JSONObject json) throws JSONException {
										String newOfflineKeyBase64 = json.getString("OfflineKey");
										OfflineKeyDecryptor keyDecryptor = new OfflineKeyDecryptor(Base64.decode(newOfflineKeyBase64));
										if(!keyDecryptor.decrypted()) {
											// TODO: need to popup an error
											System.err.println("[FUTURE GUI POPUP] " + keyDecryptor.decryptionFailedMessage);
											return;
										}
										// TODO: check mac address and user name
										try {
											LocalOfflineKeyFile.saveNewOfflineKey(newOfflineKeyBase64, keyDecryptor);
										} catch (IOException e) {
											e.printStackTrace();
											System.err.println("[FUTURE GUI POPUP] " + e.getMessage());
										}
										
										// TODO: popup success
										tankGameFrame.callbackMakeOfflineKeyDone();		
									}
								});								
							}
						});
						
						registerNewKeyPanel.add(registerNewDeviceButton);
					} else {
						JLabel message;
						if(canDeactivateKeys) {
							message = new JLabel("You must deactivate an offline key in order to register a new one");						
						} else {
							message = new JLabel("You have reached your offline key limit.  You may not deactivate or register any new offline keys");							
						}
						message.setForeground(Color.red);					
						registerNewKeyPanel.add(message);
					}
					add(registerNewKeyPanel);
					add(Box.createVerticalStrut(5));
					add(new JSeparator());
					add(Box.createVerticalStrut(5));
					
					
					// update gui				
					if(offlineKeyCount <= 0 && deactivatedOfflineKeyCount <= 0) {
						add(new JLabel("You have no offline keys"));					
					} else {
						if(deactivatedOfflineKeyCount <= 0) {
							add(new JLabel(String.format("You have %d offline key(s)", offlineKeyCount)));	
							if(offlineKeyCount >= offlineKeyLimit) {
								JLabel limitWarningLabel = new JLabel(String.format("You must deactivate an offline key to create a new one."));
								add(limitWarningLabel);							
							}
						} else {
							add(new JLabel(String.format("You have %d offline key(s) (The max is %s)", offlineKeyCount, offlineKeyLimit)));
							add(new JLabel(String.format("You have %d deactivated offline key(s) (The max is %s)", offlineKeyCount, deactivatedOfflineKeyLimit)));						
						}					
					}		
					
					
					
					for(int i = 0; i < userOfflineKeys.size(); i++) {
						add(Box.createVerticalStrut(5));
						add(new JSeparator());
						add(Box.createVerticalStrut(5));
						
						UserOfflineKey userOfflineKey = userOfflineKeys.get(i);
						JPanel offlineKeyPanel = new JPanelY();
						offlineKeyPanel.add(new JLabel(String.format("Device Name: %s", userOfflineKey.deviceName)));
						offlineKeyPanel.add(new JLabel(String.format("Mac Address: %s", userOfflineKey.macAddress)));
						offlineKeyPanel.add(new JLabel(String.format("Created On: %s", userOfflineKey.created)));
						if(userOfflineKey.isDeactivated()) {
							JLabel deactivationDateLabel = new JLabel(String.format("Deactivated On: %s", userOfflineKey.deactivated));
							deactivationDateLabel.setForeground(Color.red);
							offlineKeyPanel.add(deactivationDateLabel);						
						}
						add(offlineKeyPanel);
					}
					
					add(Box.createVerticalGlue());
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					validate();
					repaint();
				}
			}
		});
	}
}

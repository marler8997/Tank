package jmar.games.menus;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import jmar.games.menus.text.Font;
import jmar.games.menus.text.FullCharacterRendererSet;
import jmar.games.menus.text.character.FullRenderSetFactory;
import jmar.games.menus.events.*;
import jmar.games.tank.Constants;
import jmar.games.tank.GameSetupEventListener;
import jmar.games.tank.JoinServerEventListener;
import jmar.games.tank.TankGameBuilder;

import org.lwjgl.util.vector.Vector4f;

/*
 * Puck Client Menu
 * =======================================================================
 * 
 * 
 * 
 */
/*
public class Example implements GameSetupEventListener {
	public TankGameBuilder gameBuilder;
	public int windowWidth,windowHeight;
	
	// UI Controls
	public final WindowPanel windowPanel;
	
	private Font blackFont,redFont,blueFont;
	
	//
	// Choose Server Menu
	//
	private Panel chooseServer_Panel;
	
	private Text chooseServer_UserNameLabel;
	private InputTextBox chooseServer_UserNameInput;
	
	private InputTextBox chooseServer_ServerInput;
	private Button chooseServer_ConnectButton;
	private Text chooseServer_Status;
	
	//
	// Game Setup Menu
	//
	private Panel gameSetup_Panel;
	
	private Text gameSetup_ServerTitleText;
	private Button gameSetup_LeaveButton;
	private Button gameSetup_StartGameButton;
	private Text gameSetup_Status;
	
	private Panel gameSetup_ClientsPanel;
	private Text[] gameSetup_ClientControlsByID;
	
	public Example(TankGameBuilder gameBuilder, int windowWidth, int windowHeight) {
		this.gameBuilder = gameBuilder;
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		
		FullCharacterRendererSet eightSegRenderSet = FullRenderSetFactory.makeSimpleRenderSet(2);
		this.blackFont = new Font(eightSegRenderSet, 10, 15, 2, new Vector4f(0,0,0,1));
		this.redFont = new Font(eightSegRenderSet, 10, 15, 2, new Vector4f(1,0,0,1));
		this.blueFont = new Font(eightSegRenderSet, 10, 15, 2, new Vector4f(0,.8f,1,1));
		
		// Initialize Menu
		
		int lineHeight = 25;
		int buttonWidth = 100;
		Vector4f buttonColor = new Vector4f(.6f,.9f,.9f,1);
		
		this.windowPanel = new WindowPanel(windowWidth, windowHeight, new Vector4f(.95f,.95f,.95f,1));
		
		// Setup Choose Server Panel
		this.chooseServer_Panel = new Panel(0, 0, 0, 0, null);
		
		this.chooseServer_UserNameLabel = new Text(        290, windowHeight - 102, "User Name:", blackFont, AlignX.Right, AlignY.Center);
		this.chooseServer_UserNameInput = new InputTextBox(300, windowHeight - 100, 300, lineHeight, blackFont);
		if(gameBuilder.myUserName != null) this.chooseServer_UserNameInput.text = gameBuilder.myUserName;
		
		this.chooseServer_ServerInput = new InputTextBox(210, this.windowHeight - 200, 200, lineHeight, blackFont);
		this.chooseServer_ConnectButton = new Button(420, this.windowHeight - 200, buttonWidth, lineHeight, buttonColor, "Connect", blackFont);
		this.chooseServer_Status = new Text(this.windowWidth / 2, this.windowHeight / 2, "Choose a server", blackFont, AlignX.Center, AlignY.Center);

		this.chooseServer_Panel.addControl(chooseServer_UserNameLabel);
		this.chooseServer_Panel.addControl(chooseServer_UserNameInput);
		this.chooseServer_Panel.addControl(chooseServer_ServerInput);
		this.chooseServer_Panel.addControl(chooseServer_ConnectButton);
		this.chooseServer_Panel.addControl(chooseServer_Status);


		chooseServer_ServerInput.enterKeyWhileFocusedListener = new EnterKeyWhileFocusedListener() {
			public void enterKeyWhileFocused(Control control) {
				uiConnectCallback();
			}
		};
		chooseServer_UserNameInput.enterKeyWhileFocusedListener = chooseServer_ServerInput.enterKeyWhileFocusedListener;
		chooseServer_ConnectButton.mouseDownListener = new MouseDownListener() {	
			public void mouseDown(Control control, int x, int y, int button) {
				uiConnectCallback();
			}
		};		
		
		this.windowPanel.addControl(chooseServer_Panel);
		this.windowPanel.manuallySetFocus(this.chooseServer_ServerInput);
		
		// Setup Game Setup Panel
		this.gameSetup_Panel = new Panel();
		this.gameSetup_ServerTitleText = new Text(this.windowWidth / 2, this.windowHeight - 50, null, blackFont, AlignX.Center, AlignY.Center);
		this.gameSetup_LeaveButton = new Button(this.windowWidth / 2 - buttonWidth / 2, this.windowHeight - 100, buttonWidth, lineHeight, buttonColor, "Leave", blackFont);
		this.gameSetup_Status = new Text(this.windowWidth / 2, 200, null, blackFont, AlignX.Center, AlignY.Center);
		this.gameSetup_StartGameButton = new Button(this.windowWidth - 100 - (buttonWidth + 50), 100, buttonWidth + 50, lineHeight*2, buttonColor, "Start Game", blackFont);
		
		this.gameSetup_ClientsPanel = new Panel();
		this.gameSetup_ClientControlsByID = new Text[Constants.maxClients];
		
		this.gameSetup_Panel.addControl(this.gameSetup_ServerTitleText);
		this.gameSetup_Panel.addControl(this.gameSetup_LeaveButton);
		this.gameSetup_Panel.addControl(this.gameSetup_ClientsPanel);
		this.gameSetup_Panel.addControl(this.gameSetup_Status);
		this.gameSetup_Panel.addControl(this.gameSetup_StartGameButton);
		
		gameSetup_LeaveButton.mouseDownListener = new MouseDownListener() {
			public void mouseDown(Control control, int x, int y, int button) {
				leaveServer();
			}
		};
		gameSetup_StartGameButton.mouseDownListener = new MouseDownListener() {
			public void mouseDown(Control control, int x, int y, int button) {
				gameSetup_uiStartGameCallback();
			}
		};
	}
	
	private void gotoGameSetupMenu() {
		this.gameSetup_ServerTitleText.text = "Server: " + gameBuilder.networkHandler.serverString;
		
		this.windowPanel.removeAllControls();
		this.windowPanel.addControl(this.gameSetup_Panel);		

		gameSetup_ClientsPanel.removeAllControls();
		gameSetup_AddClientControl(gameBuilder.getMyClientID());
	}

	private void gotoChooseServerMenu() {
		this.windowPanel.removeAllControls();
		this.windowPanel.addControl(this.chooseServer_Panel);
		
		this.chooseServer_Status.text = "Choose a server";
		this.chooseServer_Status.font = blackFont;
		this.windowPanel.manuallySetFocus(this.chooseServer_ServerInput);		
	}
	
	public void leaveServer() {
		this.gameBuilder.networkHandler.leaveGameSetup();	
		windowPanel.removeAllUILoopCallbacks();
		gotoChooseServerMenu();		
	}
	
	
	public void uiConnectCallback() {
		final String userName = chooseServer_UserNameInput.text;
		if(userName == null || userName.length() <= 0) {
			this.chooseServer_Status.text = "Please supply a user name";
			this.chooseServer_Status.font = redFont;
			return;
		}
		gameBuilder.setMyUserName(userName);
		
		
		final String serverString = chooseServer_ServerInput.text;
		if(serverString == null || serverString.length() <= 0) {
			this.chooseServer_Status.text = "Please supply a server";
			this.chooseServer_Status.font = redFont;
			return;
		}
		
		InetAddress inetAddress = null;
		try {
			this.chooseServer_Status.text = String.format("Getting ip address for '%s'...", serverString);
			this.chooseServer_Status.font = blackFont;
			this.windowPanel.redrawFromWithinAnEventListener();
			
			inetAddress = InetAddress.getByName(serverString);
		} catch(UnknownHostException e) {
			this.chooseServer_Status.text = String.format("Unknown host '%s': %s", serverString, e.getMessage());
			this.chooseServer_Status.font = redFont;
			return;
		}
		
		// Check that it is an Inet4Address
		gameBuilder.networkHandler.setNewServer(serverString, (Inet4Address)inetAddress);
		
		// Change the menu
		
		gameBuilder.networkHandler.joinServer(gameBuilder, new JoinServerEventListener() {
			public void waitingForCredentialResponse() {
				chooseServer_Status.text = "Waiting for response...";
				chooseServer_Status.font = blackFont;
				windowPanel.redrawFromWithinAnEventListener();
			}				
			public void sendingCredentials() {
				chooseServer_Status.text = "Sending credentials...";
				chooseServer_Status.font = blackFont;
				windowPanel.redrawFromWithinAnEventListener();
			}
			public void ioException(IOException e) {
				chooseServer_Status.text = String.format("IOException: %s", e.getMessage());
				chooseServer_Status.font = blackFont;
				windowPanel.redrawFromWithinAnEventListener();
			}
			public void done(boolean success) {
				if(success) {
					//
					// Save User and Server
					//
					try {
						gameBuilder.gameSettingsManager.saveUserAndServer(userName, serverString);
					} catch (Exception e) {
						System.err.println("Error: could not save game settings: " + e.toString());
					}
					
					
					chooseServer_Status.text = "Successfully Joined Server :)";
					chooseServer_Status.font = blackFont;
					windowPanel.redrawFromWithinAnEventListener();

					gotoGameSetupMenu();
					
					windowPanel.addUILoopCallback(new UILoopCallback() {						
						@Override
						public boolean loopCallback(WindowPanel windowPanel) {
							gameBuilder.networkHandler.handlePacketsDuringSetup(new GameSetupEventListener() {
								
								@Override
								public void newClient(byte clientID, String userName) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void levelDownload(byte[] level) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void ioexception(IOException e) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void gameSetupComplete(byte[] clientStartPositionIndices) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void clientReady(byte clientID) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void clientLeft(byte clientID) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void badDataFromServer(String message) {
									// TODO Auto-generated method stub
									
								}
							});
							return true;
						}
					});
				} else {
					// re-enable ability to click again
					gameBuilder.networkHandler.leaveGameSetup();
				}
			}
			public void credentialsRejected() {
				chooseServer_Status.text = "Credentials Rejected :(";
				chooseServer_Status.font = redFont;
				windowPanel.redrawFromWithinAnEventListener();
			}
			@Override
			public void duplicateUser() {
				chooseServer_Status.text = "That user has already joined this server";
				chooseServer_Status.font = redFont;
				windowPanel.redrawFromWithinAnEventListener();
			}
			public void connectingOverTcp() {
				chooseServer_Status.text = String.format("Connecting to %s...", gameBuilder.networkHandler.serverString);
				chooseServer_Status.font = blackFont;
				windowPanel.redrawFromWithinAnEventListener();
			}
		});	
		
	}
	
	private void gameSetup_uiStartGameCallback() {
		try {
			gameBuilder.networkHandler.requestStartFreakinGame();
		} catch (IOException e) {
			chooseServer_Status.text = "IOException while requesting to start game";
			leaveServer();			
		}
	}
	
	
	private void gameSetup_AddClientControl(byte clientID) {
		System.out.println(String.format("Adding client %d: %s", clientID, gameBuilder.clientUserNames[clientID]));
		String controlText = clientID + ": " + gameBuilder.clientUserNames[clientID];
		gameSetup_ClientControlsByID[clientID] = new Text(25, this.windowHeight - 200 - clientID*35, controlText, blackFont, AlignX.Left, AlignY.Top);
		if(clientID == gameBuilder.getMyClientID()) {
			gameSetup_ClientControlsByID[clientID].font = blueFont;
		}
		gameSetup_ClientsPanel.addControl(gameSetup_ClientControlsByID[clientID]);		
	}

	@Override
	public void newClient(byte clientID, String userName) {
		System.out.println(String.format("[GameSetup] New Player '%s'", userName));
		gameBuilder.addClient(clientID, userName);
		gameSetup_AddClientControl(clientID);
	}
	@Override
	public void clientLeft(byte clientID) {
		gameBuilder.removeClient(clientID);
		gameSetup_ClientsPanel.removeAllControls();
		for(byte i = 0; i <= gameBuilder.maxClientID; i++) {
			gameSetup_AddClientControl(i);		
		}
	}


	@Override
	public void levelDownload(byte[] level) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void clientReady(byte clientID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void gameSetupComplete(byte[] clientStartPositionIndices) {
		System.out.println("Game Setup Complete");
		
		System.out.print(String.format("ClientStartPositionIndices(%d):", clientStartPositionIndices.length));
		for(int i = 0; i < clientStartPositionIndices.length; i++) {
			System.out.print(String.format(" '%s'=%d", gameBuilder.clientUserNames[i],  clientStartPositionIndices[i]));
		}
		System.out.println();
		
		gameBuilder.clientStartPositionIndices = clientStartPositionIndices;
		windowPanel.continueGuiLoop = false;
	}

	@Override
	public void badDataFromServer(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ioexception(IOException e) {
		// TODO Auto-generated method stub
		
	}
}
*/
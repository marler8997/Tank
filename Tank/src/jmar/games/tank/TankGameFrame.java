package jmar.games.tank;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.DatagramSocket;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import jmar.awt.BorderLayoutHelper;
import jmar.games.GLException;
import jmar.games.PopupWindow;
import jmar.games.net.InputStreamChunker;
import jmar.games.net.OutputStreamChunker;
import jmar.games.tank.menus.HostGroupPanel;
import jmar.games.tank.menus.HostingGroupStatusPanel;
import jmar.games.tank.menus.InGroupAsClientPanel;
import jmar.games.tank.menus.InitialGroupPanel;
import jmar.games.tank.menus.InternetLoginPanel;
import jmar.games.tank.menus.JoinGroupPanel;
import jmar.games.tank.menus.MainMenuPanel;
import jmar.games.tank.menus.MakeOfflineKeyPanel;
import jmar.games.tank.menus.PanelContainer;

public class TankGameFrame extends JFrame {
	
	public TankGameBuilder gameBuilder;
	public int windowContentWidth,windowContentHeight;
	
	public final TankGroupHostSetupManager groupHostSetupManager;
	
	// Panels	
	final InternetLoginPanel internetLoginPanel;
	final MainMenuPanel mainMenuPanel;
	final MakeOfflineKeyPanel makeOfflineKeyPanel;
	
	final HostGroupPanel hostGroupPanel;

	final PanelContainer groupPanelContainer;
	final InitialGroupPanel initialGroupPanel;
	public final HostingGroupStatusPanel hostingGroupStatusPanel;
	
	final JoinGroupPanel joinGroupPanel;
	public final InGroupAsClientPanel inGroupAsClientPanel;
	
	
	final Canvas gameDisplayCanvas;
	Thread currentGameThread;
	
	public TankGameFrame(TankGameBuilder gameBuilder, int windowContentWidth, int windowContentHeight) throws IOException {
		super("Tank");
		
		this.gameBuilder = gameBuilder;
		this.windowContentWidth = windowContentWidth;
		this.windowContentHeight = windowContentHeight;
		
		//
		// Set Close Operations
		//
		addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(-1);
			}
			public void windowClosed(WindowEvent e) {
			}
			public void windowActivated(WindowEvent e) {}
		});
		
		//
		// Set Layout and size of frame
		//
		setLayout(null);
		
		Container contentPane = getContentPane();
		
		contentPane.setLayout(new BorderLayout());
		contentPane.setPreferredSize(new Dimension(windowContentWidth, windowContentHeight));
		
		pack();
		
		//
		// Center the frame
		//
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();		
		setLocation(dim.width / 2 - getWidth() / 2 , dim.height / 2 - getHeight() / 2);
		
		//
		// Set Group Host Manager
		//
		groupHostSetupManager = new TankGroupHostSetupManager();
		
		//
		// Style
		//
		
		//
		// Initialize Panels
		//
		internetLoginPanel = new InternetLoginPanel(this);
		
		mainMenuPanel = new MainMenuPanel(this);
		
		makeOfflineKeyPanel = new MakeOfflineKeyPanel(this);

		initialGroupPanel = new InitialGroupPanel(this);

		groupPanelContainer = new PanelContainer();
		groupPanelContainer.setPreferredSize(new Dimension(200,0));
		
		groupPanelContainer.setPanel(initialGroupPanel);
		
		hostGroupPanel = new HostGroupPanel(this);
		hostingGroupStatusPanel = new HostingGroupStatusPanel(this);
		
		joinGroupPanel = new JoinGroupPanel(this);
		inGroupAsClientPanel = new InGroupAsClientPanel(this);
		
		
		gameDisplayCanvas = new Canvas();
		gameDisplayCanvas.setFocusable(true);
		gameDisplayCanvas.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				System.err.println("Need to handle resize");
			}
		});
		
		//
		// Setup Initial State
		//
		setInternetLoginPanel();
		
		setVisible(true);
	}	
	
	private void undoStateChangesToReturnToLoginPanel() {
		hostGroupPanel.stopHostServerIfRunning();	
	}	
	
	private void setInternetLoginPanel() {
		System.out.println("[GUI] Set InternetLogin Panel");
		
		undoStateChangesToReturnToLoginPanel();
		
		internetLoginPanel.reset();
		internetLoginPanel.setup();

		BorderLayoutHelper.set(getContentPane(), internetLoginPanel, BorderLayout.CENTER);
		BorderLayoutHelper.remove(getContentPane(), BorderLayout.LINE_END);
		
		validate();
		repaint();
	}
	
	// Note: password is null if and only if offline mode is true
	public void callbackLoginPanelDone(String userName, String password, byte[] offlineKey) {
		gameBuilder.myUserName = userName;
		gameBuilder.myPassword = password;
		gameBuilder.offlineKey = offlineKey;
		setMainMenuPanel();
	}


	public synchronized void callbackStartGame() {
		if(currentGameThread != null && currentGameThread.isAlive()) {
			PopupWindow.PopupSmallMessage("Game Thread Still Running", "You already have a game thread running");
			return;
		}
		
		System.out.println("[Event] Start game");

		currentGameThread = new Thread(new Runnable() {
			public void run() {
				try {
					//
					// Check if you are the host
					//
					if(groupHostSetupManager.isHostingGroup()) {
						try {
							groupHostSetupManager.startGame();
						} catch(RuntimeException e) {
							e.printStackTrace();
							PopupWindow.PopupException(e);
							return;
						}
						throw new UnsupportedOperationException("not yet implemented");
						
					}
					
					
					setGameDisplay();
					
					Display.setParent(gameDisplayCanvas);
					Display.setVSyncEnabled(false);
					
					System.out.println("Setting display mode...");
					Display.setDisplayMode(new DisplayMode(windowContentWidth, windowContentHeight));
			
					//frame.setPreferredSize(new Dimension(1024, 786));
					//frame.setMinimumSize(new Dimension(800, 600));
					//frame.pack();
					//frame.setVisible(true);
					
					System.out.println("Creating display...");
					Display.create();
					
					new TankGame(gameBuilder, windowContentWidth, windowContentHeight).run();
				} catch (GLException e) {
					setMainMenuPanel();
					PopupWindow.PopupException(e);
				} catch (IOException e) {
					setMainMenuPanel();
					PopupWindow.PopupException(e);
				} catch (LWJGLException e) {
					setMainMenuPanel();
					PopupWindow.PopupException(e);
				} catch (InterruptedException e) {
					setMainMenuPanel();
					PopupWindow.PopupException(e);
				}
			}				
		});
		currentGameThread.start();
	}
	void setGameDisplay() {
		System.out.println("[GUI] Set GameDisplay Canvas");
		
		getContentPane().removeAll();
		BorderLayoutHelper.set(getContentPane(), gameDisplayCanvas, BorderLayout.CENTER);
		gameDisplayCanvas.requestFocus();
		
		
		validate();
		repaint();		
	}	
	
	private void setMainMenuPanel() {
		System.out.println("[GUI] Set MainMenu Panel");

		mainMenuPanel.setup(gameBuilder.myUserName, gameBuilder.myPassword, gameBuilder.iAmInOfflineMode());
		
		BorderLayoutHelper.set(getContentPane(), mainMenuPanel, BorderLayout.CENTER);
		BorderLayoutHelper.set(getContentPane(), groupPanelContainer, BorderLayout.LINE_END);
		
		validate();
		repaint();
	}
	public void callbackMainMenuLogout() {		
		gameBuilder.myUserName = null;
		gameBuilder.myPassword = null;
		setInternetLoginPanel();
	}
	public void callbackMainMenuMakeOfflineKey() {
		setMakeOfflineKeyPanel();
	}	
	private void setMakeOfflineKeyPanel() {
		System.out.println("[GUI] Set MakeOfflineKey Panel");

		makeOfflineKeyPanel.setup(gameBuilder.myUserName, gameBuilder.myPassword);
		
		BorderLayoutHelper.set(getContentPane(), makeOfflineKeyPanel, BorderLayout.CENTER);
		BorderLayoutHelper.set(getContentPane(), groupPanelContainer, BorderLayout.LINE_END);

		validate();
		repaint();
	}	
	public void callbackMakeOfflineKeyDone() {
		setMainMenuPanel();
	}	
	public void setJoinGroupPanel() {
		System.out.println("[GUI] Set JoinGroup Panel");
		
		BorderLayoutHelper.set(getContentPane(), joinGroupPanel, BorderLayout.CENTER);
		BorderLayoutHelper.remove(getContentPane(), BorderLayout.LINE_END);

		validate();
		repaint();		
	}
	
	public void callbackJoinGroupPanelCancel() {
		throw new UnsupportedOperationException();
	}
	public void callbackJoinGroupPanelJoinedLanGroup(String lanServer) {
		setInGroupAsClientPanelForLan(lanServer);
	}
	public void callbackJoinGroupPanelJoinedInternetGroup(String hostUserName) {
		throw new UnsupportedOperationException();		
	}
	
	public void setInGroupAsClientPanelForLan(String lanServer) {
		System.out.println("[GUI] Set InGroupAsClient Panel");

		inGroupAsClientPanel.setupAsLanClient(lanServer);

		BorderLayoutHelper.set(getContentPane(), inGroupAsClientPanel, BorderLayout.CENTER);
		BorderLayoutHelper.remove(getContentPane(), BorderLayout.LINE_END);

		validate();
		repaint();
	}	
	public void callbackInGroupClosed() {
		setMainMenuPanel();
	}
	public void setHostGroupPanel() {
		System.out.println("[GUI] Set HostGroup panel");
		
		hostGroupPanel.setup();
		
		BorderLayoutHelper.set(getContentPane(), hostGroupPanel, BorderLayout.CENTER);
		BorderLayoutHelper.remove(getContentPane(), BorderLayout.LINE_END);

		validate();
		repaint();
	}
	public void callbackHostGroupCancel() {
		hostGroupPanel.stopHostServerIfRunning();
		setMainMenuPanel();
	}
	public void callbackHostGroupClosed() {
		groupPanelContainer.setPanel(initialGroupPanel);
		hostGroupPanel.stopHostServerIfRunning();
	}	
	public void callbackHostGroupStartedHosting(boolean isLanGroup) {
		hostingGroupStatusPanel.setup(isLanGroup);
		groupPanelContainer.setPanel(hostingGroupStatusPanel);	
		setMainMenuPanel();
	}
	
	public void callbackHostingGroupStatusPanelStopHosting() {
		hostGroupPanel.stopHostServerIfRunning();
		groupPanelContainer.setPanel(initialGroupPanel);
		setMainMenuPanel();
	}
}

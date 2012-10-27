package jmar.games.tank;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import jmar.games.Clock;
import jmar.games.GLException;
import jmar.games.GLHelper;
import jmar.games.GLTexture;
import jmar.games.menus.AlignX;
import jmar.games.menus.AlignY;
import jmar.games.menus.GlobalState;
import jmar.games.menus.Text;
import jmar.games.menus.text.Font;
import jmar.games.menus.text.FullCharacterRendererSet;
import jmar.games.menus.text.character.FullRenderSetFactory;
import jmar.games.tank.ArenaPoint;
import jmar.games.tank.ArenaUtils;
import jmar.games.tank.Level;
import jmar.games.tank.Position;
import jmar.games.tank.Settings;

import org.lwjgl.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import static org.lwjgl.opengl.GL11.*;

public class TankGame {
	public final static int maxCameraSpinRotation = 45;
	public final static int maxCameraOverheadRotation = 0;
	public final static int minCameraOverheadRotation = -80;
	public final static float squareRootOf2 = 1.4142f;	
	
	public final String myUserName;
	public final byte myClientIndex;
	
	private final ClientNetworkHandler networkHandler;
	public final int minimumPacketSizeFromServer;

	public Level level;
	
	public GLTexture floorTexture;	
	private final ArenaGLRenderer arenaRenderer;
	public final PuckClientStateForClient[] clients;
	public final Puck myPuck;
	public ArenaPoint lastPuckBottomLeftSent;
	
	
	private float cameraOverheadRotation, cameraSpinRotation;
	private int lastClickArenaX,lastClickArenaY;
	

	public int lastTimeStepFromServer;
	int timeStepWithServerToClientLag; // This should always be equal to lastTimeStepFromServer + timeStepsSinceLastPacketFromServer
	
	//
	// Profile Data
	//
	private long totalSecondsCounter;
	int currentSecondFrameCount;
	private long currentSecondStartTime;
	
	//long currentSecondTimeProcessingUserInput;
	//float averageTimeProcessingUserInput;
	
	long currentSecondTimeProcessingBulletMoves;
	float averageTimeProcessingBulletMoves;
	
	long currentSecondTimeReceivingPackets;
	float averageTimeReceivingPackets;
	
	long currentSecondTimeProcessingPackets;
	float averageTimeProcessingPackets;
	
	long currentSecondTimeSendingPackets;
	float averageTimeSendingPackets;
	
	long currentSecondTimeInGLDraw;
	float averageTimeInGLDraw;
	
	long currentSecondTimeInIdle;
	float averageTimeInIdle;
	
	float averageTimeInOther;
	
	long totalPacketsDiscarded;
	
	//
	// Debug Controls
	//
	public Text debugText;
	public Text debugText2;
	public GlobalState controlGlobalState;
	
	public TankGame(TankGameBuilder gameBuilder, int windowWidth, int windowHeight) throws IOException, InterruptedException, GLException {
		this.myUserName = gameBuilder.myUserName;
		this.myClientIndex = gameBuilder.getMyClientIndex();

		ArenaUtils.windowWidth = windowWidth;
		ArenaUtils.windowHeight = windowHeight;		
		ArenaUtils.arenaWidth = gameBuilder.levelBuilder.arenaWidth;
		ArenaUtils.arenaHeight = gameBuilder.levelBuilder.arenaHeight;

		this.level = gameBuilder.levelBuilder.makeLevel();
		this.level.print();

		this.arenaRenderer = new ArenaGLRenderer(this);
		
		
		

		Vector3f red = new Vector3f(1,.2f,0);
		Vector3f blue = new Vector3f(0,.4f,1);
		
		this.networkHandler = gameBuilder.networkHandler;		
		
		if(networkHandler == null) {
			this.minimumPacketSizeFromServer = -1;
			Position startPosition = this.level.middleStartPositions[0];

			byte puckClientOddHalfWidth = Settings.defaultPuckArenaOddHalfWidth;
			byte puckClientOddHalfHeight = Settings.defaultPuckArenaOddHalfHeight;
			
			this.clients = new PuckClientStateForClient[] {
					new PuckClientStateForClient(gameBuilder.myUserName, (byte)0, 
					new Puck(startPosition.x, startPosition.y, puckClientOddHalfWidth, puckClientOddHalfHeight,
							Settings.defaultPuckInverseVelocity, Settings.maximumPossibleBullets, 1, red))
			};
		} else {
			this.minimumPacketSizeFromServer =
					2 +                                // Frame ID
					6 * (gameBuilder.clients.length); // Minimum 6 bytes per client
			this.clients = new PuckClientStateForClient[gameBuilder.clients.length];
	
			for(byte i = 0; i < clients.length; i++) {
				Vector3f puckColor = (i == myClientIndex) ? red : blue;
				
				byte startPositionIndex = (byte) gameBuilder.clients[i].startPositionIndex;
				Position startPosition = this.level.middleStartPositions[startPositionIndex];
				byte puckClientOddHalfWidth = Settings.defaultPuckArenaOddHalfWidth;
				byte puckClientOddHalfHeight = Settings.defaultPuckArenaOddHalfHeight;
				
				this.clients[i] = new PuckClientStateForClient(gameBuilder.clients[i].userName, i,
						new Puck(startPosition.x, startPosition.y, puckClientOddHalfWidth, puckClientOddHalfHeight,
								Settings.defaultPuckInverseVelocity, Settings.maximumPossibleBullets, 1, puckColor));
			}
		}
		this.myPuck = this.clients[myClientIndex].puck;
		this.lastPuckBottomLeftSent = new ArenaPoint();
		this.myPuck.puckArenaObject.copyBottomLeftTo(this.lastPuckBottomLeftSent);
		
		//
		// Debug
		//
		FullCharacterRendererSet eightSegRenderSet = FullRenderSetFactory.makeSimpleRenderSet(1);
		Font whiteFont  = new Font(eightSegRenderSet, 6, 8, 1, new Vector4f(1,1,1,1));		
		this.debugText  = new Text(7, 14, null,whiteFont, AlignX.Left, AlignY.Center);
		this.debugText2 = new Text(7, 3, null,whiteFont, AlignX.Left, AlignY.Center);
		this.controlGlobalState = new GlobalState();
	}
	
	private void initializeGL() throws GLException, FileNotFoundException, IOException {
		ArenaUtils.glOrthoHalfWidth = (float)ArenaUtils.windowWidth / 2f;
		ArenaUtils.glOrthoHalfHeight = (float)ArenaUtils.windowHeight /2f;
		ArenaUtils.glOrthoWidth = ArenaUtils.windowWidth;
		ArenaUtils.glOrthoHeight = ArenaUtils.windowHeight;	
		ArenaUtils.glArenaHalfWidth  = ArenaUtils.glOrthoWidth* .45f;
		ArenaUtils.glArenaHalfHeight = ArenaUtils.glOrthoHeight * .45f;
		ArenaUtils.glArenaWidth      = ArenaUtils.glOrthoWidth* .9f;
		ArenaUtils.glArenaHeight     = ArenaUtils.glOrthoHeight * .9f;

		// Load Textures
		this.floorTexture = new GLTexture(new FileInputStream("res/Wood.png"), GL13.GL_TEXTURE0);
		
		// Setup Camera and View
		glMatrixMode(GL11.GL_PROJECTION);       // Switches to the camera perspective
		glLoadIdentity();                       // Reset Camera
		glOrtho(-ArenaUtils.glOrthoHalfWidth , ArenaUtils.glOrthoHalfWidth ,
				-ArenaUtils.glOrthoHalfHeight, ArenaUtils.glOrthoHalfHeight,
				-ArenaUtils.glOrthoHalfWidth , ArenaUtils.glOrthoHalfWidth );	
		GLException.throwOnError();	
		glMatrixMode(GL11.GL_MODELVIEW);       // Switches to the camera perspective
		glClearColor(.7f, .9f, 1f, 1);	
		GLException.throwOnError();
		
		// Enable transparency
		glEnable (GL_BLEND);
		glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GLException.throwOnError();
		
		// Enable depth
		glEnable (GL_DEPTH_TEST);
		GLException.throwOnError();		
	}
	
	
	public void run() throws GLException, IOException, LWJGLException {	
		initializeGL();
		
		//
		// Initialize
		//
		this.lastTimeStepFromServer = -1;
		
		//
		// Initialize Camera
		//
		this.cameraOverheadRotation = -20;
		
		//
		// Initialize Profile Data
		//
		totalSecondsCounter = 0;
		currentSecondFrameCount = 0;
		
		currentSecondTimeProcessingBulletMoves = 0;
		averageTimeProcessingBulletMoves = 0;
		
		currentSecondTimeReceivingPackets = 0;
		averageTimeReceivingPackets = 0;
		
		currentSecondTimeProcessingPackets = 0;
		averageTimeProcessingPackets = 0;
		
		currentSecondTimeSendingPackets = 0;;
		averageTimeSendingPackets = 0;
		
		currentSecondTimeInGLDraw = 0;
		averageTimeInGLDraw = 0;
		
		currentSecondTimeInIdle = 0;
		averageTimeInIdle = 0;
		
		averageTimeInOther = 0;
		
		totalPacketsDiscarded = 0;
		
		
		//
		// Frame Loop (execute once per frame)
		//
		currentSecondStartTime = Clock.getTime();
		
		while (!Display.isCloseRequested()) {			
			/*
			if((frame & 0x7F) == 0) {
				//profiler.print();
				System.out.println(String.format("Frame %d", frame));
				for(int i = 0; i < clients.length; i++) {
					PuckClientStateForClient client = clients[i];
					System.out.print(String.format(" '%s' %fx%f", client.userName,
							client.puck.x, client.puck.y));
				}
				System.out.println();
			}
			*/
			
			//
			// Handle Window Resize
			//
			if(Display.wasResized()) {
				ArenaUtils.windowWidth = Display.getWidth();
				ArenaUtils.windowHeight = Display.getHeight();
				Display.destroy();
				Display.setDisplayMode(new DisplayMode(ArenaUtils.windowWidth, ArenaUtils.windowHeight));
				Display.create();
				initializeGL();
			}
			
			glMatrixMode(GL11.GL_PROJECTION);       // Switches to the camera perspective
			glLoadIdentity();                       // Reset Camera
			glOrtho(-ArenaUtils.glOrthoHalfWidth , ArenaUtils.glOrthoHalfWidth ,
					-ArenaUtils.glOrthoHalfHeight, ArenaUtils.glOrthoHalfHeight,
					-ArenaUtils.glOrthoHalfWidth , ArenaUtils.glOrthoHalfWidth );
			GLException.throwOnError();	
			glMatrixMode(GL11.GL_MODELVIEW);       // Switches to the camera perspective	
			
			
			//
			// Process the time steps for the current frame
			//
			for(int i = 0; i < Settings.timeStepsPerFrame; i++) {
				//
				// Process User Input
				//
				processUserInput();
				
				//
				// Move Bullets
				//
				long timeBeforeBulletMoves = Clock.getTime();
				for(int clientIndex = 0; clientIndex < clients.length; clientIndex++) {
					PuckClientStateForClient client = clients[clientIndex];
					client.puck.moveBullets(timeStepWithServerToClientLag);
				}
				long timeAfterBulletMoves = Clock.getTime();
				this.currentSecondTimeProcessingBulletMoves += timeAfterBulletMoves - timeBeforeBulletMoves;
				
				//
				// Sync with the network
				//
				if(networkHandler != null) {
					networkHandler.syncGame(this);
				}
					
				timeStepWithServerToClientLag++;
				if(timeStepWithServerToClientLag >= Settings.timeStepRolloverValue) timeStepWithServerToClientLag = 0;
			}
						
			//
			// Draw the game
			//
			long timeBeforeGLDraw = Clock.getTime();			
			glDraw();
			long timeAfterGLDraw = Clock.getTime();
			this.currentSecondTimeInGLDraw += timeAfterGLDraw - timeBeforeGLDraw;			
			
			//
			// Update the screen
			//
			Display.update();
			
			//
			// Wait for the end of frame
			//
			long timeBeforeIdle = Clock.getTime();			
			Display.sync(Settings.fps);
			long timeAfterIdle = Clock.getTime();
			this.currentSecondTimeInIdle += timeAfterIdle - timeBeforeIdle;
			
			
			//
			// Increment frame variables and update debug data
			//			
			currentSecondFrameCount++;
			if(currentSecondFrameCount >= Settings.fps) {
				totalSecondsCounter++;
				
				//
				// Save Profile Data
				//
				long now = Clock.getTime();
				long actualTimeElapsedForOneSecond = now - currentSecondStartTime;

				long currentSecondTimeInOther = actualTimeElapsedForOneSecond - currentSecondTimeProcessingBulletMoves - currentSecondTimeReceivingPackets -
						currentSecondTimeProcessingPackets - currentSecondTimeSendingPackets - currentSecondTimeInGLDraw - currentSecondTimeInIdle;
				averageTimeInOther += ((float)currentSecondTimeInOther - averageTimeInOther) / (float)totalSecondsCounter;	
				
				averageTimeProcessingBulletMoves += ((float)currentSecondTimeProcessingBulletMoves - averageTimeProcessingBulletMoves) / (float)totalSecondsCounter;
				currentSecondTimeProcessingBulletMoves = 0;
				
				averageTimeReceivingPackets += ((float)currentSecondTimeReceivingPackets - averageTimeReceivingPackets) / (float)totalSecondsCounter;
				currentSecondTimeReceivingPackets = 0;
				
				averageTimeProcessingPackets += ((float)currentSecondTimeProcessingPackets - averageTimeProcessingPackets) / (float)totalSecondsCounter;
				currentSecondTimeProcessingPackets = 0;

				averageTimeSendingPackets += ((float)currentSecondTimeSendingPackets - averageTimeSendingPackets) / (float)totalSecondsCounter;
				currentSecondTimeSendingPackets = 0;

				averageTimeInGLDraw += ((float)currentSecondTimeInGLDraw - averageTimeInGLDraw) / (float)totalSecondsCounter;
				currentSecondTimeInGLDraw = 0;

				averageTimeInIdle += ((float)currentSecondTimeInIdle  - averageTimeInIdle ) / (float)totalSecondsCounter;
				currentSecondTimeInIdle  = 0;
				
				
				this.debugText.text = String.format("BULLETS %.1f RECV %.1f PROC %.1f SEND %.1f DRAW %.1f IDLE %.1f OTHER %.1f", averageTimeProcessingBulletMoves,
						averageTimeReceivingPackets, averageTimeProcessingPackets, averageTimeSendingPackets, averageTimeInGLDraw, averageTimeInIdle, averageTimeInOther);
				this.debugText2.text = String.format("PacketsDiscarded %d", totalPacketsDiscarded);
				
				currentSecondFrameCount = 0;
				currentSecondStartTime = Clock.getTime();
			}
		}
	}	
	
	public Vector2f mouseToGLXY(int mouseX, int mouseY) {
		final float totalOrthDepth = ArenaUtils.glOrthoWidth;
		
		float mouseViewVerticalPlaneY = -1f * (mouseY-ArenaUtils.windowHeight/2);
		float floorTiltZDepth = mouseViewVerticalPlaneY * (float)Math.tan(cameraOverheadRotation * Math.PI / 180);
		float distanceToMiddleFloor = ArenaUtils.glOrthoHalfWidth - Settings.puckGLHeight;		
		
		return GLHelper.screenToWorld(Mouse.getX(),Mouse.getY(), (distanceToMiddleFloor + floorTiltZDepth) / totalOrthDepth);
	}
	
	private void processUserInput() {
		//
		// Camera Controls
		//
		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			// limit camera spin rotation
			cameraSpinRotation-=.1f;
			if(cameraSpinRotation < -maxCameraSpinRotation) cameraSpinRotation = -maxCameraSpinRotation;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			cameraSpinRotation+=.1f;
			if(cameraSpinRotation > maxCameraSpinRotation) cameraSpinRotation = maxCameraSpinRotation;
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			cameraOverheadRotation-=.1f;
			if(cameraOverheadRotation < minCameraOverheadRotation) cameraOverheadRotation = minCameraOverheadRotation;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			cameraOverheadRotation+=.1f;
			if(cameraOverheadRotation > maxCameraOverheadRotation) cameraOverheadRotation = maxCameraOverheadRotation;
		}
		
		boolean orthoChanged = false;
		if(Keyboard.isKeyDown(Keyboard.KEY_G)) {
			if(ArenaUtils.glOrthoHalfHeight > 1.0f) {
				ArenaUtils.glOrthoHalfHeight -= 1.0f;
				ArenaUtils.glOrthoHeight = 2 * ArenaUtils.glOrthoHalfHeight;
				ArenaUtils.glArenaHalfHeight = ArenaUtils.glOrthoHeight * .45f;
				ArenaUtils.glArenaHeight     = ArenaUtils.glOrthoHeight * .9f;
				orthoChanged = true;
			}
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_T)) {
			ArenaUtils.glOrthoHalfHeight += 1.0f;
			ArenaUtils.glOrthoHeight = 2 * ArenaUtils.glOrthoHalfHeight;
			ArenaUtils.glArenaHalfHeight = ArenaUtils.glOrthoHeight * .45f;
			ArenaUtils.glArenaHeight = ArenaUtils.glOrthoHeight * .9f;
			orthoChanged = true;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_H)) {
			if(ArenaUtils.glOrthoHalfWidth > 1.0f) {
				ArenaUtils.glOrthoHalfWidth -= 1.0f;
				ArenaUtils.glOrthoWidth = 2 * ArenaUtils.glOrthoHalfWidth;
				ArenaUtils.glArenaHalfWidth = ArenaUtils.glOrthoWidth* .45f;
				ArenaUtils.glArenaWidth = ArenaUtils.glOrthoWidth* .9f;
				orthoChanged = true;
			}
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_F)) {
			ArenaUtils.glOrthoHalfWidth += 1.0f;
			ArenaUtils.glOrthoWidth = 2 * ArenaUtils.glOrthoHalfWidth;
			ArenaUtils.glArenaHalfWidth = ArenaUtils.glOrthoWidth* .45f;
			ArenaUtils.glArenaWidth = ArenaUtils.glOrthoWidth* .9f;
			orthoChanged = true;
		}
		if(orthoChanged) {
			System.out.println(String.format("Ortho %f %f", ArenaUtils.glOrthoWidth, ArenaUtils.glOrthoHeight));
		}
		

		
		//
		// Tank Controls
		//
		boolean moveUpKeyDown    = Keyboard.isKeyDown(Keyboard.KEY_W);
		boolean moveDownKeyDown  = Keyboard.isKeyDown(Keyboard.KEY_S);
		boolean moveLeftKeyDown  = Keyboard.isKeyDown(Keyboard.KEY_A);
		boolean moveRightKeyDown = Keyboard.isKeyDown(Keyboard.KEY_D);
		
		if(moveLeftKeyDown && !moveRightKeyDown && myPuck.puckArenaObject.getLeft() > 0) {
			//System.out.println(String.format("[Control] Timestep %5d: Left", timeStepWithServerToClientLag));
			myPuck.puckArenaObject.oneTimestepMoveLeft();
		}
		else if(moveRightKeyDown && !moveLeftKeyDown && myPuck.puckArenaObject.getRight() < ArenaUtils.arenaWidth) {
			//System.out.println(String.format("[Control] Timestep %5d: Right", timeStepWithServerToClientLag));
			myPuck.puckArenaObject.oneTimestepMoveRight();
		}
		
		if(moveDownKeyDown && !moveUpKeyDown && myPuck.puckArenaObject.getBottom() > 0) {
			//System.out.println(String.format("[Control] Timestep %5d: Down", timeStepWithServerToClientLag));
			myPuck.puckArenaObject.oneTimestepMoveDown();
		}
		else if(moveUpKeyDown && !moveDownKeyDown && myPuck.puckArenaObject.getTop() < ArenaUtils.arenaHeight) {
			//System.out.println(String.format("[Control] Timestep %5d: Up", timeStepWithServerToClientLag));
			myPuck.puckArenaObject.oneTimestepMoveUp();
		}	
		
		//
		// Mouse Events
		//
		while(Mouse.next()) {
			// Left Click
			if(Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
				int clickX = Mouse.getEventX();
				int clickY = Mouse.getEventY();
				System.out.println(String.format("TimeStep %d LeftDown (%d,%d)", timeStepWithServerToClientLag, clickX, clickY));
				
				
				//
				// Get Arena Coordinates
				//
				
				System.out.println(String.format("X-Axis rotate: %f Y-Axis rotate: %f", cameraOverheadRotation, cameraSpinRotation));
				glPushMatrix();
				glRotatef(cameraSpinRotation, 0, 1, 0);
				glRotatef(cameraOverheadRotation, 1, 0, 0);
				Vector2f clickXY = mouseToGLXY(clickX,clickY);
				glPopMatrix();
				this.lastClickArenaX = ArenaUtils.glToArenaX(clickXY.x);
				this.lastClickArenaY = ArenaUtils.glToArenaY(clickXY.y);
				System.out.println(String.format("(%d,%d) -> (%f,%f) (%d,%d)", clickX, clickY, clickXY.x, clickXY.y,
						lastClickArenaX, lastClickArenaY));				
				
				//
				// Shoot
				//
				int bulletIndex = myPuck.availableBulletIndex();
				if(bulletIndex >= 0) {
					myPuck.userRequestShoot(timeStepWithServerToClientLag, bulletIndex,
							this.lastClickArenaX - Settings.defaultBulletArenaSize / 2, this.lastClickArenaY - Settings.defaultBulletArenaSize / 2);
				}
			}
		}		
	}
	
	
	public void glDraw() {
		
		// Clear the screen and the depth buffer
		glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		
		glPushMatrix();
		
		// Translate origin to center of arena for rotation
		glRotatef(cameraSpinRotation, 0, 1, 0);
		glRotatef(cameraOverheadRotation, 1, 0, 0);
		
		// Draw the floor
		glEnable(GL_TEXTURE_2D);
			glBindTexture(GL_TEXTURE_2D, floorTexture.textureID);
			glBegin(GL_QUADS);
				glColor3f(1, 1, 1);
				glTexCoord2f(0, 0); glVertex3f(-ArenaUtils.windowWidth / 2 - 100, -ArenaUtils.windowHeight / 2 - 100, 0);
				glTexCoord2f(0, 1); glVertex3f(-ArenaUtils.windowWidth / 2 - 100,  ArenaUtils.windowHeight / 2 + 100, 0);
				glTexCoord2f(1, 1); glVertex3f( ArenaUtils.windowWidth / 2 + 100,  ArenaUtils.windowHeight / 2 + 100, 0);glColor3f(.9f, .9f, .9f);
				glTexCoord2f(1, 0); glVertex3f( ArenaUtils.windowWidth / 2 + 100, -ArenaUtils.windowHeight / 2 - 100, 0);
			glEnd();			
		glDisable(GL_TEXTURE_2D);
		
		// Draw the bottom/back of the arena
		arenaRenderer.glDraw();	
		
		// Draw the puck(s)
		for(int i = 0; i < clients.length; i++) {
			clients[i].puck.glDraw();
		}
		
		//
		// Draw Last Click
		//
		glBegin(GL_QUADS);
		
	    glColor4f(.8f, 0, 0, .4f);
	    float glX = ArenaUtils.arenaToGLX(this.lastClickArenaX);
	    float glY = ArenaUtils.arenaToGLY(this.lastClickArenaY);
		glVertex3f(glX - 5, glY + 5, Settings.puckGLHeight);
		glVertex3f(glX + 5, glY + 5, Settings.puckGLHeight);
		glVertex3f(glX + 5, glY - 5, Settings.puckGLHeight);
		glVertex3f(glX - 5, glY - 5, Settings.puckGLHeight);
		glEnd();
				
		
		//
		// Draw mouse position
		//		
		Vector2f mouseGLXY = mouseToGLXY(Mouse.getX(), Mouse.getY());
		glBegin(GL_QUADS);
	    glColor4f(.8f, 0, 0, .4f);
		glVertex3f(mouseGLXY.x - 11, mouseGLXY.y +  7, Settings.puckGLHeight);
		glVertex3f(mouseGLXY.x -  7, mouseGLXY.y + 11, Settings.puckGLHeight);    
		glVertex3f(mouseGLXY.x + 11, mouseGLXY.y -  7, Settings.puckGLHeight);
		glVertex3f(mouseGLXY.x +  7, mouseGLXY.y - 11, Settings.puckGLHeight);

		glVertex3f(mouseGLXY.x +  7, mouseGLXY.y + 11, Settings.puckGLHeight);
		glVertex3f(mouseGLXY.x + 11, mouseGLXY.y +  7, Settings.puckGLHeight);    
		glVertex3f(mouseGLXY.x -  7, mouseGLXY.y - 11, Settings.puckGLHeight);
		glVertex3f(mouseGLXY.x - 11, mouseGLXY.y -  7, Settings.puckGLHeight);
		
		float myPuckGLX = ArenaUtils.arenaToGLX(myPuck.puckArenaObject.getMiddleX());
		float myPuckGLY = ArenaUtils.arenaToGLY(myPuck.puckArenaObject.getMiddleY());

		float changeInX =  myPuckGLX - mouseGLXY.x;
		float changeInY =  myPuckGLY - mouseGLXY.y;
		float vectorLength = (float) Math.sqrt(changeInX*changeInX + changeInY*changeInY);

		changeInX /= vectorLength;
		changeInY /= vectorLength;
		
		float side1AtTankX = myPuckGLX - changeInY * Settings.tankToCursorLineWidth;
		float side1AtTankY = myPuckGLY + changeInX * Settings.tankToCursorLineWidth;
		float side2AtTankX = myPuckGLX + changeInY * Settings.tankToCursorLineWidth;
		float side2AtTankY = myPuckGLY - changeInX * Settings.tankToCursorLineWidth;
		
		float side1AtMouseX = mouseGLXY.x - changeInY * Settings.tankToCursorLineWidth;
		float side1AtMouseY = mouseGLXY.y + changeInX * Settings.tankToCursorLineWidth;
		float side2AtMouseX = mouseGLXY.x + changeInY * Settings.tankToCursorLineWidth;
		float side2AtMouseY = mouseGLXY.y - changeInX * Settings.tankToCursorLineWidth;
		
		glVertex3f(side1AtTankX , side1AtTankY , Settings.puckGLHeight);
		glVertex3f(side2AtTankX , side2AtTankY , Settings.puckGLHeight);
		glVertex3f(side2AtMouseX, side2AtMouseY, Settings.puckGLHeight);
		glVertex3f(side1AtMouseX, side1AtMouseY, Settings.puckGLHeight);
		
		glEnd();
		
		
		glPopMatrix();
		
		//
		// Draw Debug Information
		//
		glPushMatrix();
		glTranslatef(-ArenaUtils.glOrthoHalfWidth, -ArenaUtils.glOrthoHalfHeight, 0);
		this.debugText.glDraw(controlGlobalState);
		this.debugText2.glDraw(controlGlobalState);		
		glPopMatrix();
	}
}

package jmar.games.tank;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import jmar.ShouldntHappenException;

public class Settings {	
	public static final String internetServerHostName = "tank.marler.info";
	public static final HttpURLConnection makeTankInternetHttp(String resource) {
		try {
			URL url = new URL(String.format("http://%s/%s", internetServerHostName, resource));
			return (HttpURLConnection)url.openConnection();
		} catch (MalformedURLException e) {
			throw new ShouldntHappenException(e);
		} catch (IOException e) {
			throw new ShouldntHappenException(e);
		}
	}
	
	//
	// Network
	//
	public static final int lanServerPort           = 40567;
	public static final int lanClientPort           = 40568;
	
	public static final int serverPort              = 40569;

    public static final int clientJoinAttempts          = 6;
    public static final int clientJoinRecvTimeoutMillis = 500;
	
	
    //
    // Constants
    //   
    public static final float wallHeight            = 20f;
    public static final float boundaryWallWidth     = 20f;
    /*
    public static final float arenaXLength          = 200f;
    public static final float arenaXLengthHalf      = arenaXLength / 2f;
    public static final float arenaZLength          = 150f;
    public static final float arenaZLengthHalf      = arenaZLength / 2f;
  
    public static final float floorHeight           = 5f;
    public static final float floorHeightHalf       = floorHeight / 2f;
    public static final float boundaryWallWidthHalf = boundaryWallWidth / 2f;
    
    public static final float wallHeightHalf        = wallHeight / 2f;
    public static final float southWallHeight       = 6f;
    public static final float southWallHeightHalf   = wallHeight / 2f;
    
    */
    
    public static final int defaultArenaWidth       = 800;
    public static final int defaultArenaHeight      = 600;
    
    public static final byte defaultPuckArenaOddHalfWidth  = 15;
    public static final byte defaultPuckArenaOddHalfHeight = 15;
    public static final int defaultPuckInverseVelocity      = 3;
    
    
    public static final int bulletInverseVelocityTimePerArenaUnit = 1;
    
    //
    // Client Graphical Settings
    //
    public static final float tankToCursorLineWidth = 2f;
    public static final float puckShadowGlOffset    = 3f;
    
    public static final float puckGLHeight            = 15f;
    public static final float bulletHeight          = 10f;
    public static final int defaultBulletArenaSize  = 8;
    
    public static final int maximumPossibleBullets  = 8;

    public static final int puckMaxHalfArenaSize    = 20;
    
    public static final int numberOfPacketsToSendABullet = 32;
    
    //
    // Timing and Sync Variables
    //
    
    

    public static final int menuFps                 = 20;

    public static final int defaultBulletCapacity   = 5;
    
    
    //
    // Your game fps is determined by 2 variables
    // 1. millisPerTimeStep
    // 2. timeStepsPerFrame
    // Restrictions:
    //   1. millisPerTimeStep * timeStepsPerFrame = millisPerFrame, and millisPerFrame must always be an Integer, therefore, this value must divide 1000
    //
    //
    
    
    public static final int millisPerTimeStep        =    2;
    public static final int timeStepsPerFrame        =    10;    
    public static final int serverMillisecondHistory = 1000; // The server will save the history from the last 1000 milliseconds
    public static final int serverTimestepHistory    = serverMillisecondHistory / millisPerTimeStep; // Doesn't have to be exact
    
    public static final int serverMaxHistoryIndexSaveCount = 64;  // Save indices for the last 64 updates from the client
    
    public static final int fps                     = 1000 / millisPerTimeStep / timeStepsPerFrame;
    public static final int timeStepRolloverValue   = 0x10000; // UInt16.MAX
    
    
    public static void checkThatSettingsAreValid() {
    	System.out.println(String.format("[Game Settings] Checking game settings(millisPerTimeStep=%d, timeStepsPerFrame=%d)...",
    			millisPerTimeStep, timeStepsPerFrame));
        ValidTimeSettings.throwIfSettingsAreNotValid(millisPerTimeStep, timeStepsPerFrame);
    }
    
    
    private static class ValidTimeSettings {        
        //
        // Your game fps is determined by 2 variables
    	//
        // 1. millisPerTimeStep: How many milliseconds equals 1 time step (A time step is one step in all physical calculations)
        // 2. timeStepsPerFrame (How many times you will update your physical world every frame)
        //
    	// fps = 1000 / timeStepsPerFrame / millisPerTimeStep
    	//    	
    	// Restrictions:
        //   millisPerTimeStep * timeStepsPerFrame = millisPerFrame, and millisPerFrame must always be an Integer, therefore, this value must divide 1000
        //   The prime factors of 1000 are 2, 2, 2, 5, 5 and 5.  (3 2's and 3 5's)
    	//   They can be combined in 15 ways to make the following numbers
    	//   2, 4, 5, 8, 10, 20, 25, 40, 50, 100, 125, 200, 250, 500 and 1000
    	//   Therefore, millisPerTimeStep and timeStepsPerFrame are restricted to these numbers (except that they could also be 1)
    	//   Since millisPerTimeStep will likely never be too large, the only values supported are:
    	//   1, 2, 4 and 5 (more could be added if it is desired)
    	//   This class provides a list of valid values for timeStepsPerFrame for each of these millisPerTimestep
        //
        private static ValidTimeSettings[] validGameSettingCombinations = null;
        private static ValidTimeSettings[] getValidTimeSettings() {
        	if(validGameSettingCombinations == null) {
        		validGameSettingCombinations = new ValidTimeSettings[] {
        				new ValidTimeSettings( 1, new int[] {1, 2, 4, 5, 8, 10, 20, 25, 40, 50, 100, 125, 200, 250, 500}),
        				new ValidTimeSettings( 2, new int[] {1, 2, 4, 5,    10, 20, 25,     50, 100, 125,      250, 500}),
        				new ValidTimeSettings( 4, new int[] {1, 2,    5,            25,     50,      125               }),
        				new ValidTimeSettings( 5, new int[] {1, 2, 4, 5, 8, 10, 20, 25, 40, 50, 100,      200          })
        		};
        	}
        	return validGameSettingCombinations;
        };
        
        
    	public int millisPerTimeStep;
    	public int [] validTimeStepsPerFrame;
    	public ValidTimeSettings(int millisPerTimeStep, int[] validTimeStepsPerFrame) {
    		this.millisPerTimeStep = millisPerTimeStep;
    		this.validTimeStepsPerFrame = validTimeStepsPerFrame;
    	}
    	
    	public static void throwIfSettingsAreNotValid(int millisPerTimeStep, int timeStepsPerFrame) {
    		ValidTimeSettings [] validTimeSettingsArray = getValidTimeSettings();
    		for(int i = 0; i < validTimeSettingsArray.length; i++) {
    			ValidTimeSettings validTimeSettings = validTimeSettingsArray[i];
    			if(validTimeSettings.millisPerTimeStep == millisPerTimeStep) {
    				for(int j = 0; j < validTimeSettings.validTimeStepsPerFrame.length; j++) {
    					if(validTimeSettings.validTimeStepsPerFrame[j] == timeStepsPerFrame) return;
    				}
    			}
    		}
    		throw new IllegalStateException(String.format("The time settings you have (millistPerTimeStep=%d, timeStepsPerFrame=%d) are invalid because if you multiply these 2 values together (%d) they must divide 1000",
    				millisPerTimeStep, timeStepsPerFrame, millisPerTimeStep * timeStepsPerFrame));
    	}
    }
    
    
}

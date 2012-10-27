package jmar.games.tank;

public class Profiler {
	
	private int lastTimeStep;
	private long totalTimeSteps;
	
	public String [] timeBucketNames;
	public long [] timeBucketMillis;
	public long [] timeBucketStarts;
	private int timeBucketCount;
	
	//private int readAttemptsInCurrentFrame;
	//private float readAttemptsPerFrame;
	
	
	public Profiler(int timeBucketCapacity) {
		this.totalTimeSteps = 0;
		
		this.timeBucketNames = new String[timeBucketCapacity];
		this.timeBucketMillis = new long[timeBucketCapacity];
		this.timeBucketStarts = new long[timeBucketCapacity];
		this.timeBucketCount = 0;
		
		//this.readAttemptsInCurrentFrame = 0;
		//this.readAttemptsPerFrame = 0;
	}
	
	public int createTimeBucket(String bucketName) {
		timeBucketNames[timeBucketCount] = bucketName;
		timeBucketMillis[timeBucketCount] = 0;
		timeBucketCount++;
		return timeBucketCount-1;
	}
	
	public void startTimeBucket(int timeBucket, long startTime) {
		this.timeBucketStarts[timeBucket] = startTime;
	}
	
	public void endTimeBucket(int timeBucket, long endTime) {
		this.timeBucketMillis[timeBucket] += endTime - timeBucketStarts[timeBucket];
	}

	/*
	public void saveReadAttempt() {
		readAttemptsInCurrentFrame++;
	}
	*/
	
	public void atTimeStep(int timeStep) {
		if(timeStep != lastTimeStep) {
			totalTimeSteps += TimeStepLogic.timeStepDiff(timeStep, lastTimeStep);
			
			//readAttemptsPerFrame += ((float)readAttemptsInCurrentFrame - readAttemptsPerFrame) / (float)totalFrames;
			
			// Reset Variables
			//readAttemptsInCurrentFrame = 0;
		}
	}
	
	public void print() {
		System.out.println("---------------------------------------");
		System.out.println(String.format("TimeSteps %d", totalTimeSteps));
		//System.out.println(String.format("Reads Per Frame: %f",readAttemptsPerFrame));
		for(int i = 0; i < timeBucketCount; i++) {
			System.out.println(String.format("%s: %d millis", timeBucketNames[i], timeBucketMillis[i]));
		}
	}
}

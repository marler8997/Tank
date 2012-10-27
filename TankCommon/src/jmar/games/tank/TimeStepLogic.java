package jmar.games.tank;

import jmar.test.Test;
import jmar.test.TestFailure;

public class TimeStepLogic {
	public static final int timestepCompareLimit = 100;

	public static boolean thisTimestepIsBefore(int thisTimestep, int otherTimestep) {
		int timeDiff = otherTimestep - thisTimestep;
		return (timeDiff > 0 && timeDiff < timestepCompareLimit) || (timeDiff < timestepCompareLimit - (Settings.timeStepRolloverValue));
	}
	public static boolean timeStepDiffIsPositive(int timeDiff) {
		return (timeDiff > 0 && timeDiff < timestepCompareLimit) || (timeDiff < timestepCompareLimit - (Settings.timeStepRolloverValue));
	}
	public static int timeStepDiff(int newerTimeStep, int olderTimeStep) {
		return (newerTimeStep >= olderTimeStep) ? (newerTimeStep - olderTimeStep) :
			(newerTimeStep - olderTimeStep + (Settings.timeStepRolloverValue));
	}
	
	public static void main(String[] args) throws TestFailure {
		//
		// Test thisTimestepIsBefore
		//
		Test.assertTrue(thisTimestepIsBefore(0, 1));
		Test.assertFalse(thisTimestepIsBefore(1, 0));		

		Test.assertTrue(thisTimestepIsBefore(Settings.timeStepRolloverValue-1, 0));
		Test.assertFalse(thisTimestepIsBefore(0, Settings.timeStepRolloverValue-1));

		Test.assertTrue(thisTimestepIsBefore(Settings.timeStepRolloverValue-1 - (timestepCompareLimit - 2), 0));
		Test.assertFalse(thisTimestepIsBefore(0, Settings.timeStepRolloverValue-1 - (timestepCompareLimit - 1)));

		Test.assertTrue(thisTimestepIsBefore(Settings.timeStepRolloverValue-1, timestepCompareLimit - 2));
		Test.assertFalse(thisTimestepIsBefore(timestepCompareLimit - 1, Settings.timeStepRolloverValue-1));

		try {
			Test.assertTrue(thisTimestepIsBefore(Settings.timeStepRolloverValue-1, timestepCompareLimit));
			Test.fail();
		} catch(TestFailure e) {}
		try {
			Test.assertTrue(thisTimestepIsBefore(timestepCompareLimit, Settings.timeStepRolloverValue-1));
			Test.fail();
		} catch(TestFailure e) {}
		
		//
		// Test timeStepDiff
		//
		System.out.println(String.format("diff 0 0 = %d", timeStepDiff(0,0)));
		System.out.println(String.format("diff 1 0 = %d", timeStepDiff(1,0)));
		System.out.println(String.format("diff 65535 0 = %d", timeStepDiff(65535,0)));
		System.out.println(String.format("diff 0 65535 = %d", timeStepDiff(0,65535)));
		System.out.println(String.format("diff 1 65534 = %d", timeStepDiff(1,65534)));
		
		
		
	}
	
}

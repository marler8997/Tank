package jmar.test;

public class Test {
	public static void fail() throws TestFailure {
		throw new TestFailure("Manually failed the test");
	}
	
	public static void assertTrue(boolean bool) throws TestFailure {
		if(!bool) throw new TestFailure("Assertion failed");
	}
	public static void assertFalse(boolean bool) throws TestFailure {
		if(bool) throw new TestFailure("Assertion failed");
	}
	
	public static void assertEqual(int a, int b) throws TestFailure {
		if(a != b) throw new TestFailure(String.format("%d does not equal %d", a, b));
	}
}
